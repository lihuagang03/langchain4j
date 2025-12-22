package dev.langchain4j.data.document.splitter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.TokenCountEstimator;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * 按正则拆分文档
 * 使用提供的正则表达式将提供的文档拆分为多个部分，并尝试尽可能多地将这些部分放入单个文本段落中，同时遵守 maxSegmentSize 设置的限制。
 * Splits the provided {@link Document} into parts using the provided {@code regex} and attempts to fit as many parts
 * as possible into a single {@link TextSegment}, adhering to the limit set by {@code maxSegmentSize}.
 * <p>
 * maxSegmentSize 可以用字符（默认）或词元来定义。
 * 对于基于词元的限制，必须提供 TokenCountEstimator。
 * The {@code maxSegmentSize} can be defined in terms of characters (default) or tokens.
 * For token-based limit, a {@link TokenCountEstimator} must be provided.
 * <p>
 * 如果多个部分适合最大分段大小，它们将使用提供的连接分隔符连接在一起。
 * If multiple parts fit within {@code maxSegmentSize}, they are joined together using the provided {@code joinDelimiter}.
 * <p>
 * 如果某个部分过长并超过 maxSegmentSize，则会使用子拆分器（应提供）将其拆分为子部分，并将这些子部分放入多个段中。
 * 这样的段只包含被拆分的长部分的子部分。
 * If a single part is too long and exceeds {@code maxSegmentSize}, the {@code subSplitter} (which should be provided)
 * is used to split it into sub-parts and place them into multiple segments.
 * Such segments contain only the sub-parts of the split long part.
 * <p>
 * 每个文本段落都继承文档的所有元数据，并包含一个“index”元数据键，用于表示其在文档中的位置（从0开始）。
 * Each {@link TextSegment} inherits all metadata from the {@link Document} and includes an "index" metadata key
 * representing its position within the document (starting from 0).
 */
public class DocumentByRegexSplitter extends HierarchicalDocumentSplitter {

    /**
     * 正则表达式
     */
    private final String regex;
    /**
     * 连接分隔符
     */
    private final String joinDelimiter;

    public DocumentByRegexSplitter(String regex,
                                   String joinDelimiter,
                                   int maxSegmentSizeInChars,
                                   int maxOverlapSizeInChars) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, null);
        this.regex = ensureNotNull(regex, "regex");
        this.joinDelimiter = ensureNotNull(joinDelimiter, "joinDelimiter");
    }

    public DocumentByRegexSplitter(String regex,
                                   String joinDelimiter,
                                   int maxSegmentSizeInChars,
                                   int maxOverlapSizeInChars,
                                   DocumentSplitter subSplitter) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, subSplitter);
        this.regex = ensureNotNull(regex, "regex");
        this.joinDelimiter = ensureNotNull(joinDelimiter, "joinDelimiter");
    }

    public DocumentByRegexSplitter(String regex,
                                   String joinDelimiter,
                                   int maxSegmentSizeInTokens,
                                   int maxOverlapSizeInTokens,
                                   TokenCountEstimator tokenCountEstimator) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator, null);
        this.regex = ensureNotNull(regex, "regex");
        this.joinDelimiter = ensureNotNull(joinDelimiter, "joinDelimiter");
    }

    public DocumentByRegexSplitter(String regex,
                                   String joinDelimiter,
                                   int maxSegmentSizeInTokens,
                                   int maxOverlapSizeInTokens,
                                   TokenCountEstimator tokenCountEstimator,
                                   DocumentSplitter subSplitter) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator, subSplitter);
        this.regex = ensureNotNull(regex, "regex");
        this.joinDelimiter = ensureNotNull(joinDelimiter, "joinDelimiter");
    }

    @Override
    public String[] split(String text) {
        // 边界通过正则表达式来检测到
        return text.split(regex);
    }

    @Override
    public String joinDelimiter() {
        // 多个使用连接分隔符连接在一起
        return joinDelimiter;
    }

    @Override
    protected DocumentSplitter defaultSubSplitter() {
        return null;
    }
}
