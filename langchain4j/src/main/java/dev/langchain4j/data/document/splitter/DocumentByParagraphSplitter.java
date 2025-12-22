package dev.langchain4j.data.document.splitter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.TokenCountEstimator;

/**
 * 按段落拆分文档
 * 将提供的文档分成段落，并尝试将尽可能多的段落放入单个文本片段中，同时遵守 maxSegmentSize 设置的限制。
 * Splits the provided {@link Document} into paragraphs and attempts to fit as many paragraphs as possible
 * into a single {@link TextSegment}, adhering to the limit set by {@code maxSegmentSize}.
 * <p>
 * maxSegmentSize 可以用字符（默认）或词元来定义。
 * 对于基于词元的限制，必须提供 TokenCountEstimator。
 * The {@code maxSegmentSize} can be defined in terms of characters (default) or tokens.
 * For token-based limit, a {@link TokenCountEstimator} must be provided.
 * <p>
 * 段落边界通过至少两个换行符("\n\n")来检测。
 * 在这些换行符之前、之间或之后的额外空格都会被忽略。
 * 因此，下面的例子都是有效的段落分隔符："\n\n", "\n\n\n", "\n \n", " \n \n " 等等。
 * Paragraph boundaries are detected by a minimum of two newline characters ("\n\n").
 * Any additional whitespaces before, between, or after are ignored.
 * So, the following examples are all valid paragraph separators: "\n\n", "\n\n\n", "\n \n", " \n \n ", and so on.
 * <p>
 * 如果多个段落可以在最大片段大小内容纳，它们将使用两个换行符("\n\n")连接在一起。
 * If multiple paragraphs fit within {@code maxSegmentSize}, they are joined together using a double newline ("\n\n").
 * <p>
 * 如果单个段落过长并超过 maxSegmentSize，则会使用子文档拆分器
 * （默认是 DocumentBySentenceSplitter）将其拆分为更小的部分，并将它们放入多个段落中。
 * 这些段落仅包含被拆分的长段落的部分内容。
 * If a single paragraph is too long and exceeds {@code maxSegmentSize},
 * the {@code subSplitter} ({@link DocumentBySentenceSplitter} by default) is used to split it into smaller parts and
 * place them into multiple segments.
 * Such segments contain only the parts of the split long paragraph.
 * <p>
 * 每个文本片段都继承文档的所有元数据，并包含一个“index”元数据键，表示其在文档中的位置（从0开始）。
 * Each {@link TextSegment} inherits all metadata from the {@link Document} and includes an "index" metadata key
 * representing its position within the document (starting from 0).
 */
public class DocumentByParagraphSplitter extends HierarchicalDocumentSplitter {

    public DocumentByParagraphSplitter(int maxSegmentSizeInChars,
                                       int maxOverlapSizeInChars) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, null);
    }

    public DocumentByParagraphSplitter(int maxSegmentSizeInChars,
                                       int maxOverlapSizeInChars,
                                       DocumentSplitter subSplitter) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, subSplitter);
    }

    public DocumentByParagraphSplitter(int maxSegmentSizeInTokens,
                                       int maxOverlapSizeInTokens,
                                       TokenCountEstimator tokenCountEstimator) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator, null);
    }

    public DocumentByParagraphSplitter(int maxSegmentSizeInTokens,
                                       int maxOverlapSizeInTokens,
                                       TokenCountEstimator tokenCountEstimator,
                                       DocumentSplitter subSplitter) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator, subSplitter);
    }

    @Override
    public String[] split(String text) {
        // 段落边界通过至少两个换行符("\n\n")来检测到
        return text.split("\\s*(?>\\R)\\s*(?>\\R)\\s*");
    }

    @Override
    public String joinDelimiter() {
        // 多个段落使用两个个换行符（"\n\n"）连接在一起
        return "\n\n";
    }

    @Override
    protected DocumentSplitter defaultSubSplitter() {
        return new DocumentBySentenceSplitter(maxSegmentSize, maxOverlapSize, tokenCountEstimator);
    }
}
