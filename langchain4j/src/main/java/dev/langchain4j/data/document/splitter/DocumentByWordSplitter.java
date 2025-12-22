package dev.langchain4j.data.document.splitter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.TokenCountEstimator;

/**
 * 按单词拆分文档
 * 将提供的文档分割成单词，并尝试将尽可能多的单词放入一个文本片段中，同时遵守 maxSegmentSize 设置的限制。
 * Splits the provided {@link Document} into words and attempts to fit as many words as possible
 * into a single {@link TextSegment}, adhering to the limit set by {@code maxSegmentSize}.
 * <p>
 * maxSegmentSize 可以以字符（默认）或词元为单位定义。
 * 对于基于词元的限制，必须提供 TokenCountEstimator。
 * The {@code maxSegmentSize} can be defined in terms of characters (default) or tokens.
 * For token-based limit, a {@link TokenCountEstimator} must be provided.
 * <p>
 * 单词边界由至少一个空格（" "）检测到。
 * 前后任何额外的空白都将被忽略。
 * 因此，以下示例都是有效的单词分隔符：" "、"  "、"\n" 等等。
 * Word boundaries are detected by a minimum of one space (" ").
 * Any additional whitespaces before or after are ignored.
 * So, the following examples are all valid word separators: " ", "  ", "\n", and so on.
 * <p>
 * 如果多个单词可以在最大分段大小内容纳，它们将用空格（" "）连接在一起。
 * If multiple words fit within {@code maxSegmentSize}, they are joined together using a space (" ").
 * <p>
 * 尽管这种情况不应该发生，但如果单个单词过长并超过最大分段大小（maxSegmentSize），
 * 将使用子拆分器（默认情况下为 DocumentByCharacterSplitter）将其拆分为更小的部分并放入多个分段中。
 * 这些分段仅包含被拆分的长单词的部分内容。
 * Although this should not happen, if a single word is too long and exceeds {@code maxSegmentSize},
 * the {@code subSplitter} ({@link DocumentByCharacterSplitter} by default) is used to split it into smaller parts and
 * place them into multiple segments.
 * Such segments contain only the parts of the split long word.
 * <p>
 * 每个文本段落都继承文档的所有元数据，并包含一个表示其在文档中位置的“index”元数据键（从0开始）。
 * Each {@link TextSegment} inherits all metadata from the {@link Document} and includes an "index" metadata key
 * representing its position within the document (starting from 0).
 */
public class DocumentByWordSplitter extends HierarchicalDocumentSplitter {

    public DocumentByWordSplitter(int maxSegmentSizeInChars,
                                  int maxOverlapSizeInChars) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, null);
    }

    public DocumentByWordSplitter(int maxSegmentSizeInChars,
                                  int maxOverlapSizeInChars,
                                  DocumentSplitter subSplitter) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, subSplitter);
    }

    public DocumentByWordSplitter(int maxSegmentSizeInTokens,
                                  int maxOverlapSizeInTokens,
                                  TokenCountEstimator tokenCountEstimator) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator, null);
    }

    public DocumentByWordSplitter(int maxSegmentSizeInTokens,
                                  int maxOverlapSizeInTokens,
                                  TokenCountEstimator tokenCountEstimator,
                                  DocumentSplitter subSplitter) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator, subSplitter);
    }

    @Override
    public String[] split(String text) {
        // 单词边界由至少一个空格（" "）检测到
        return text.split("\\s+");
    }

    @Override
    public String joinDelimiter() {
        // 多个单词使用一个空格（" "）连接在一起
        return " ";
    }

    @Override
    protected DocumentSplitter defaultSubSplitter() {
        return new DocumentByCharacterSplitter(maxSegmentSize, maxOverlapSize, tokenCountEstimator);
    }
}
