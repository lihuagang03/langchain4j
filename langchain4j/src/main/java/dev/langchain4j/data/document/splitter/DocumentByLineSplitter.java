package dev.langchain4j.data.document.splitter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.TokenCountEstimator;

/**
 * 按行拆分文档
 * 将提供的文档分割成多行，并尝试将尽可能多的行放入单个文本片段中，同时遵守 maxSegmentSize 设置的限制。
 * Splits the provided {@link Document} into lines and attempts to fit as many lines as possible
 * into a single {@link TextSegment}, adhering to the limit set by {@code maxSegmentSize}.
 * <p>
 * maxSegmentSize 可以以字符（默认）或词元来定义。
 * 对于基于词元的限制，必须提供 TokenCountEstimator。
 * The {@code maxSegmentSize} can be defined in terms of characters (default) or tokens.
 * For token-based limit, a {@link TokenCountEstimator} must be provided.
 * <p>
 * 行边界由至少一个换行符（"\n"）检测到。
 * 前后任何额外的空白都会被忽略。
 * 因此，以下示例都是有效的行分隔符："\n"、"\n\n"、" \n"、"\n " 等等。
 * Line boundaries are detected by a minimum of one newline character ("\n").
 * Any additional whitespaces before or after are ignored.
 * So, the following examples are all valid line separators: "\n", "\n\n", " \n", "\n " and so on.
 * <p>
 * 如果多行内容可以符合最大分段大小，它们会使用换行符（"\n"）合并在一起。
 * If multiple lines fit within {@code maxSegmentSize}, they are joined together using a newline ("\n").
 * <p>
 * 如果单行过长并超过了最大分段大小（maxSegmentSize），
 * 将使用子拆分器（默认情况下为 DocumentBySentenceSplitter）将其拆分成更小的部分，并放入多个分段中。
 * 这些分段只包含拆分后的长行的部分内容。
 * If a single line is too long and exceeds {@code maxSegmentSize},
 * the {@code subSplitter} ({@link DocumentBySentenceSplitter} by default) is used to split it into smaller parts and
 * place them into multiple segments.
 * Such segments contain only the parts of the split long line.
 * <p>
 * 每个文本片段都继承文档的所有元数据，并包含一个“index”元数据键，表示其在文档中的位置（从0开始）。
 * Each {@link TextSegment} inherits all metadata from the {@link Document} and includes an "index" metadata key
 * representing its position within the document (starting from 0).
 */
public class DocumentByLineSplitter extends HierarchicalDocumentSplitter {

    public DocumentByLineSplitter(int maxSegmentSizeInChars,
                                  int maxOverlapSizeInChars) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, null);
    }

    public DocumentByLineSplitter(int maxSegmentSizeInChars,
                                  int maxOverlapSizeInChars,
                                  DocumentSplitter subSplitter) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, subSplitter);
    }

    public DocumentByLineSplitter(int maxSegmentSizeInTokens,
                                  int maxOverlapSizeInTokens,
                                  TokenCountEstimator tokenCountEstimator) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator, null);
    }

    public DocumentByLineSplitter(int maxSegmentSizeInTokens,
                                  int maxOverlapSizeInTokens,
                                  TokenCountEstimator tokenCountEstimator,
                                  DocumentSplitter subSplitter) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator, subSplitter);
    }

    @Override
    public String[] split(String text) {
        // 行边界由至少一个换行符（"\n"）检测到
        return text.split("\\s*\\R\\s*");
    }

    @Override
    public String joinDelimiter() {
        // 多行使用一个换行符（"\n"）连接在一起
        return "\n";
    }

    @Override
    protected DocumentSplitter defaultSubSplitter() {
        return new DocumentBySentenceSplitter(maxSegmentSize, maxOverlapSize, tokenCountEstimator);
    }
}
