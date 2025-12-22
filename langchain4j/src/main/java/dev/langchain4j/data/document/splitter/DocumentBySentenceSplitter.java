package dev.langchain4j.data.document.splitter;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.exception.LangChain4jException;
import dev.langchain4j.model.TokenCountEstimator;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.InputStream;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * 按句拆分文档
 * 将提供的文档分割成句子，并尝试在单个文本片段中容纳尽可能多的句子，同时遵守 maxSegmentSize 设置的限制。
 * Splits the provided {@link Document} into sentences and attempts to fit as many sentences as possible
 * into a single {@link TextSegment}, adhering to the limit set by {@code maxSegmentSize}.
 * <p>
 * maxSegmentSize 可以以字符（默认）或词元为单位定义。
 * 对于基于词元的限制，必须提供 TokenCountEstimator。
 * The {@code maxSegmentSize} can be defined in terms of characters (default) or tokens.
 * For token-based limit, a {@link TokenCountEstimator} must be provided.
 * <p>
 * 句子边界是使用 Apache OpenNLP 库和英文句子模型检测的。
 * Sentence boundaries are detected using the Apache OpenNLP library with the English sentence model.
 * <p>
 * 如果多个句话可以包含在最大段落大小内，它们会用空格（" "）连接在一起。
 * If multiple sentences fit within {@code maxSegmentSize}, they are joined together using a space (" ").
 * <p>
 * 如果一个句子过长并且超过了最大分段大小（maxSegmentSize），
 * 则使用子分割器（默认是 DocumentByWordSplitter）将其拆分成更小的部分，并将这些部分放入多个段落中。
 * 这些段落只包含被拆分的长句的部分内容。
 * If a single sentence is too long and exceeds {@code maxSegmentSize},
 * the {@code subSplitter} ({@link DocumentByWordSplitter} by default) is used to split it into smaller parts and
 * place them into multiple segments.
 * Such segments contain only the parts of the split long sentence.
 * <p>
 * 每个文本段落都继承文档的所有元数据，并包含一个表示其在文档中位置的“index”元数据键（从0开始）。
 * Each {@link TextSegment} inherits all metadata from the {@link Document} and includes an "index" metadata key
 * representing its position within the document (starting from 0).
 */
public class DocumentBySentenceSplitter extends HierarchicalDocumentSplitter {

    /**
     * 句子模型
     */
    private final SentenceModel sentenceModel;

    public DocumentBySentenceSplitter(int maxSegmentSizeInChars,
                                      int maxOverlapSizeInChars) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, null);
        this.sentenceModel = createSentenceModel();
    }

    public DocumentBySentenceSplitter(int maxSegmentSizeInChars,
                                      int maxOverlapSizeInChars,
                                      DocumentSplitter subSplitter) {
        super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, subSplitter);
        this.sentenceModel = createSentenceModel();
    }

    public DocumentBySentenceSplitter(int maxSegmentSizeInTokens,
                                      int maxOverlapSizeInTokens,
                                      TokenCountEstimator tokenCountEstimator) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator, null);
        this.sentenceModel = createSentenceModel();
    }

    public DocumentBySentenceSplitter(int maxSegmentSizeInTokens,
                                      int maxOverlapSizeInTokens,
                                      TokenCountEstimator tokenCountEstimator,
                                      DocumentSplitter subSplitter) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator, subSplitter);
        this.sentenceModel = createSentenceModel();
    }

    /**
     * @param sentenceModel The {@link SentenceModel} to be used for splitting text into sentences.
     *                      Pretrained models for various languages can be found
     *                      <a href="https://opennlp.apache.org/models.html#sentence_detection">here</a>.
     */
    public DocumentBySentenceSplitter(int maxSegmentSizeInTokens,
                                      int maxOverlapSizeInTokens,
                                      TokenCountEstimator tokenCountEstimator,
                                      DocumentSplitter subSplitter,
                                      SentenceModel sentenceModel) {
        super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator, subSplitter);
        this.sentenceModel = ensureNotNull(sentenceModel, "sentenceModel");
    }

    /**
     * 创建句子模型
     */
    private SentenceModel createSentenceModel() {
        String sentenceModelFilePath = "/opennlp/opennlp-en-ud-ewt-sentence-1.2-2.5.0.bin";
        try (InputStream is = getClass().getResourceAsStream(sentenceModelFilePath)) {
            return new SentenceModel(is);
        } catch (Exception e) {
            throw new LangChain4jException(e);
        }
    }

    @Override
    public String[] split(String text) {
        // 句子边界由至少一个空格（" "）检测到
        // 句子检测器ME
        SentenceDetectorME sentenceDetector = new SentenceDetectorME(sentenceModel);
        return sentenceDetector.sentDetect(text);
    }

    @Override
    public String joinDelimiter() {
        // 多个句子使用一个空格（" "）连接在一起
        return " ";
    }

    @Override
    protected DocumentSplitter defaultSubSplitter() {
        return new DocumentByWordSplitter(maxSegmentSize, maxOverlapSize, tokenCountEstimator);
    }
}
