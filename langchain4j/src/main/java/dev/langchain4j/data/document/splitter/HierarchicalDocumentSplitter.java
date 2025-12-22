package dev.langchain4j.data.document.splitter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.exception.LangChain4jException;
import dev.langchain4j.model.TokenCountEstimator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.langchain4j.internal.Utils.firstChars;
import static dev.langchain4j.internal.ValidationUtils.ensureBetween;
import static dev.langchain4j.internal.ValidationUtils.ensureGreaterThanZero;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * 分层文档拆分器
 * 用于分层文档拆分器的基类。
 * Base class for hierarchical document splitters.
 *
 * <p>
 * 扩展了 DocumentSplitter，并提供了在单个段落过长时进行子拆分文档的机制。
 * Extends {@link DocumentSplitter} and provides machinery for sub-splitting documents
 * when a single segment is too long.
 */
public abstract class HierarchicalDocumentSplitter implements DocumentSplitter {
    /**
     * 分层文档拆分器
     * 重叠句子分割器
     */
    private HierarchicalDocumentSplitter overlapSentenceSplitter;

    private HierarchicalDocumentSplitter getOverlapSentenceSplitter() {
        // 重叠句子分割器
        if (overlapSentenceSplitter == null) {
            // 按句拆分文档
            overlapSentenceSplitter = new DocumentBySentenceSplitter(1, 0, null, null);
        }
        return overlapSentenceSplitter;
    }

    /**
     * 片段索引
     */
    private static final String INDEX = "index";

    /**
     * 最大分段大小
     */
    protected final int maxSegmentSize;
    /**
     * 最大重叠大小
     */
    protected final int maxOverlapSize;
    /**
     * 词元计数估算器
     */
    protected final TokenCountEstimator tokenCountEstimator;
    /**
     * 子文档拆分器
     */
    protected final DocumentSplitter subSplitter;

    /**
     * Creates a new instance of {@link HierarchicalDocumentSplitter}.
     *
     * @param maxSegmentSizeInChars The maximum size of a segment in characters.
     * @param maxOverlapSizeInChars The maximum size of the overlap between segments in characters.
     */
    protected HierarchicalDocumentSplitter(int maxSegmentSizeInChars, int maxOverlapSizeInChars) {
        this(maxSegmentSizeInChars, maxOverlapSizeInChars, null, null);
    }

    /**
     * Creates a new instance of {@link HierarchicalDocumentSplitter}.
     *
     * @param maxSegmentSizeInChars The maximum size of a segment in characters.
     * @param maxOverlapSizeInChars The maximum size of the overlap between segments in characters.
     * @param subSplitter           The sub-splitter to use when a single segment is too long.
     */
    protected HierarchicalDocumentSplitter(int maxSegmentSizeInChars,
                                           int maxOverlapSizeInChars,
                                           HierarchicalDocumentSplitter subSplitter) {
        this(maxSegmentSizeInChars, maxOverlapSizeInChars, null, subSplitter);
    }

    /**
     * Creates a new instance of {@link HierarchicalDocumentSplitter}.
     *
     * @param maxSegmentSizeInTokens The maximum size of a segment in tokens.
     * @param maxOverlapSizeInTokens The maximum size of the overlap between segments in tokens.
     * @param tokenCountEstimator    The {@code TokenCountEstimator} to use to estimate the number of tokens in a text.
     */
    protected HierarchicalDocumentSplitter(int maxSegmentSizeInTokens,
                                           int maxOverlapSizeInTokens,
                                           TokenCountEstimator tokenCountEstimator) {
        this(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator, null);
    }

    /**
     * Creates a new instance of {@link HierarchicalDocumentSplitter}.
     *
     * @param maxSegmentSizeInTokens The maximum size of a segment in tokens.
     * @param maxOverlapSizeInTokens The maximum size of the overlap between segments in tokens.
     * @param tokenCountEstimator    The {@code TokenCountEstimator} to use to estimate the number of tokens in a text.
     * @param subSplitter            The sub-splitter to use when a single segment is too long.
     */
    protected HierarchicalDocumentSplitter(int maxSegmentSizeInTokens,
                                           int maxOverlapSizeInTokens,
                                           TokenCountEstimator tokenCountEstimator,
                                           DocumentSplitter subSplitter) {
        this.maxSegmentSize = ensureGreaterThanZero(maxSegmentSizeInTokens, "maxSegmentSize");
        this.maxOverlapSize = ensureBetween(maxOverlapSizeInTokens, 0, maxSegmentSize, "maxOverlapSize");
        this.tokenCountEstimator = tokenCountEstimator;
        this.subSplitter = subSplitter == null ? defaultSubSplitter() : subSplitter;
    }

    /**
     * 将提供的文本分割成若干部分。
     * Splits the provided text into parts.
     * Implementation API.
     *
     * @param text The text to be split.
     * @return An array of parts.
     */
    protected abstract String[] split(String text);

    /**
     * 用于重新连接各部分的分隔符字符串。
     * Delimiter string to use to re-join the parts.
     *
     * @return The delimiter.
     */
    protected abstract String joinDelimiter();

    /**
     * 当单个段落过长时使用的默认子文档拆分器。
     * The default sub-splitter to use when a single segment is too long.
     *
     * @return The default sub-splitter.
     */
    protected abstract DocumentSplitter defaultSubSplitter();

    /**
     * 将单个文档拆分为文本片段对象列表。
     */
    @Override
    public List<TextSegment> split(Document document) {
        ensureNotNull(document, "document");

        // 文本片段列表
        List<TextSegment> segments = new ArrayList<>();
        // 片段构建者
        SegmentBuilder segmentBuilder = new SegmentBuilder(maxSegmentSize, this::estimateSize, joinDelimiter());
        // 索引
        AtomicInteger index = new AtomicInteger(0);

        // 将提供的文本分割成若干部分
        String[] parts = split(document.text());
        String overlap = null;
        for (String part : parts) {
            // 文本部分的大小
            int partSize = segmentBuilder.sizeOf(part);

            // 如果提供的大小可以添加到当前段，则返回 true。
            if (segmentBuilder.hasSpaceFor(partSize)) {
                // The part fits in the current segment, so we append it.
                segmentBuilder.append(part);
                continue;
            }

            if (segmentBuilder.isNotEmpty()) {
                // 片段文本
                // The part won't fit in the current segment, so we flush the current segment.
                String segmentText = segmentBuilder.toString();
                if (!segmentText.equals(overlap)) {
                    // 从提供的文本和文档创建一个新的文本片段
                    segments.add(createSegment(segmentText, document, index.getAndIncrement()));

                    // 返回所提供段落文本末尾的重叠区域
                    overlap = overlapFrom(segmentText);

                    // 重置当前段落
                    segmentBuilder.reset();
                    segmentBuilder.append(overlap);

                    if (segmentBuilder.hasSpaceFor(partSize)) {
                        // The part fits in the current segment, so we append it.
                        segmentBuilder.append(part);
                        continue;
                    }
                }
            }

            // Enforce that we have a sub-splitter defined.
            if (subSplitter == null) {
                throw new LangChain4jException(String.format(
                        "The text \"%s...\" (%s %s long) doesn't fit into the maximum segment size (%s %s), " +
                                "and there is no subSplitter defined to split it further.",
                        firstChars(part, 30),
                        estimateSize(part), tokenCountEstimator == null ? "characters" : "tokens",
                        maxSegmentSize, tokenCountEstimator == null ? "characters" : "tokens"

                ));
            }

            // 将部分文本的拆分委托给子文档拆分器
            // Delegate the splitting of the part to the sub-splitter.
            segmentBuilder.append(part);
            for (TextSegment segment : subSplitter.split(Document.from(segmentBuilder.toString()))) {
                // 从提供的文本和文档创建一个新的文本片段
                segments.add(createSegment(segment.text(), document, index.getAndIncrement()));
            }

            // 最后的文本片段
            TextSegment lastSegment = segments.get(segments.size() - 1);
            overlap = overlapFrom(lastSegment.text());

            segmentBuilder.reset();
            segmentBuilder.append(overlap);
        }

        if (segmentBuilder.isNotEmpty() && !segmentBuilder.toString().equals(overlap)) {
            // 从提供的文本和文档创建一个新的文本片段
            segments.add(createSegment(segmentBuilder.toString(), document, index.getAndIncrement()));
        }

        return segments;
    }

    /**
     * 返回所提供段落文本末尾的重叠区域。
     * Returns the overlap region at the end of the provided segment text.
     *
     * @param segmentText The segment text.
     * @return The overlap region, or an empty string if there is no overlap.
     */
    String overlapFrom(String segmentText) {
        if (maxOverlapSize == 0) {
            return "";
        }

        // 重叠句子分割器
        // 总是按句子拆分，因为它是文本中最小的有意义单位
        // always split by sentence, as it is the smallest meaningful unit of text
        List<String> sentences = Arrays.asList(getOverlapSentenceSplitter().split(segmentText));
        Collections.reverse(sentences);

        // 重叠句子片段
        SegmentBuilder overlapBuilder = new SegmentBuilder(maxOverlapSize, this::estimateSize, joinDelimiter());
        for (String sentence : sentences) {
            if (overlapBuilder.hasSpaceFor(sentence)) {
                overlapBuilder.prepend(sentence);
            } else {
                break;
            }
        }
        return overlapBuilder.toString();
    }

    /**
     * 估算提供文本中的大小。
     * Estimates the size in the provided text.
     *
     * <p>If a {@link TokenCountEstimator} is provided, the number of tokens is estimated.
     * Otherwise, the number of characters is estimated.
     *
     * @param text The text.
     * @return The estimated number of tokens.
     */
    int estimateSize(String text) {
        if (tokenCountEstimator != null) {
            return tokenCountEstimator.estimateTokenCountInText(text);
        } else {
            return text.length();
        }
    }

    /**
     * 从提供的文本和文档创建一个新的文本片段。
     * Creates a new {@link TextSegment} from the provided text and document.
     *
     * <p>
     * 该段落继承文档的所有元数据。该段落还包含一个“index”元数据键，用于表示段落在文档中的位置。
     * The segment inherits all metadata from the document. The segment also includes
     * an "index" metadata key representing the segment position within the document.
     *
     * @param text     The text of the segment.
     * @param document The document to which the segment belongs.
     * @param index    The index of the segment within the document.
     */
    static TextSegment createSegment(String text, Document document, int index) {
        // 片段索引
        Metadata metadata = document.metadata().copy().put(INDEX, String.valueOf(index));
        return TextSegment.from(text, metadata);
    }
}
