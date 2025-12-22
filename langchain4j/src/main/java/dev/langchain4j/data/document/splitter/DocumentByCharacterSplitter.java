package dev.langchain4j.data.document.splitter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.TokenCountEstimator;

/**
 * 按字符拆分文档
 * 将提供的文档拆分为字符，并尝试将尽可能多的字符放入单个文本段中，同时遵守 maxSegmentSize 设置的限制。
 * Splits the provided {@link Document} into characters and attempts to fit as many characters as possible
 * into a single {@link TextSegment}, adhering to the limit set by {@code maxSegmentSize}.
 * <p>
 * maxSegmentSize 可以以字符（默认）或词元为单位定义。
 * 对于基于词元的限制，必须提供 TokenCountEstimator。
 * The {@code maxSegmentSize} can be defined in terms of characters (default) or tokens.
 * For token-based limit, a {@link TokenCountEstimator} must be provided.
 * <p>
 * 如果多个字符可以在最大分段大小内容纳，它们会被连接在一起而不使用分隔符。
 * If multiple characters fit within {@code maxSegmentSize}, they are joined together without delimiters.
 * <p>
 * 每个文本段落都继承文档的所有元数据，并包含一个“index”元数据键，表示其在文档中的位置（从0开始）。
 * Each {@link TextSegment} inherits all metadata from the {@link Document} and includes an "index" metadata key
 * representing its position within the document (starting from 0).
 */
public class DocumentByCharacterSplitter extends HierarchicalDocumentSplitter {

    public DocumentByCharacterSplitter(int maxSegmentSizeInChars,
                                       int maxOverlapSizeInChars) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, null);
    }

    public DocumentByCharacterSplitter(int maxSegmentSizeInChars,
                                       int maxOverlapSizeInChars,
                                       DocumentSplitter subSplitter) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, subSplitter);
    }

    public DocumentByCharacterSplitter(int maxSegmentSizeInTokens,
                                       int maxOverlapSizeInTokens,
                                       TokenCountEstimator tokenCountEstimator) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator, null);
    }

    public DocumentByCharacterSplitter(int maxSegmentSizeInTokens,
                                       int maxOverlapSizeInTokens,
                                       TokenCountEstimator tokenCountEstimator,
                                       DocumentSplitter subSplitter) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator, subSplitter);
    }

    @Override
    public String[] split(String text) {
        // 字符边界由空字符串检测到
        return text.split("");
    }

    @Override
    public String joinDelimiter() {
        // 多个字符使用空字符串连接在一起
        return "";
    }

    @Override
    protected DocumentSplitter defaultSubSplitter() {
        return null;
    }
}
