package dev.langchain4j.classification;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 文本分类器
 * 根据一组标签对给定文本进行分类。
 * 它可以为每个分类返回零个、一个或多个标签。
 * Classifies a given text based on a set of labels.
 * It can return zero, one, or multiple labels for each classification.
 *
 * @param <L> The type of the label  (e.g., String, Enum, etc.)
 */
public interface TextClassifier<L> {

    /**
     * 对给定的文本进行分类。
     * Classifies the given text.
     *
     * @param text Text to classify.
     * @return A list of labels. Can contain zero, one, or multiple labels.
     */
    default List<L> classify(String text) {
        return classifyWithScores(text).scoredLabels().stream()
                .map(ScoredLabel::label)
                .collect(toList());
    }

    /**
     * 对给定的文本片段进行分类。
     * Classifies the given {@link TextSegment}.
     *
     * @param textSegment {@link TextSegment} to classify.
     * @return A list of labels. Can contain zero, one, or multiple labels.
     */
    default List<L> classify(TextSegment textSegment) {
        return classify(textSegment.text());
    }

    /**
     * 对给定的文档进行分类。
     * Classifies the given {@link Document}.
     *
     * @param document {@link Document} to classify.
     * @return A list of labels. Can contain zero, one, or multiple labels.
     */
    default List<L> classify(Document document) {
        return classify(document.text());
    }

    /**
     * 对给定文本进行分类，并返回带有分数的标签。
     * Classifies the given text and returns labels with scores.
     *
     * @param text Text to classify.
     * @return a result object containing a list of labels with corresponding scores.
     * Can contain zero, one, or multiple labels.
     */
    ClassificationResult<L> classifyWithScores(String text);

    /**
     * 对给定的文本片段进行分类，并返回带有分数的标签。
     * Classifies the given {@link TextSegment} and returns labels with scores.
     *
     * @param textSegment {@link TextSegment} to classify.
     * @return a result object containing a list of labels with corresponding scores.
     * Can contain zero, one, or multiple labels.
     */
    default ClassificationResult<L> classifyWithScores(TextSegment textSegment) {
        return classifyWithScores(textSegment.text());
    }

    /**
     * 对给定的文档进行分类，并返回带有分数的标签。
     * Classifies the given {@link Document} and returns labels with scores.
     *
     * @param document {@link Document} to classify.
     * @return a result object containing a list of labels with corresponding scores.
     * Can contain zero, one, or multiple labels.
     */
    default ClassificationResult<L> classifyWithScores(Document document) {
        return classifyWithScores(document.text());
    }
}
