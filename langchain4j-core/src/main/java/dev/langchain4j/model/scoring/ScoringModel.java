package dev.langchain4j.model.scoring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;

import java.util.List;

import static dev.langchain4j.internal.ValidationUtils.ensureEq;
import static java.util.Collections.singletonList;

/**
 * 评分模型，一个能够根据查询对文本进行评分的模型。
 * Represents a model capable of scoring a text against a query.
 * <br>
 * 在针对同一查询对多个文本进行评分时，有助于识别最相关的文本。
 * Useful for identifying the most relevant texts when scoring multiple texts against the same query.
 * <br>
 * 评分模型可以用于重新排序。
 * The scoring model can be employed for re-ranking purposes.
 */
public interface ScoringModel {

    /**
     * 根据给定的查询对指定文本进行评分。
     * Scores a given text against a given query.
     *
     * @param text  The text to be scored.
     * @param query The query against which to score the text.
     * @return the score.
     */
    default Response<Double> score(String text, String query) {
        return score(TextSegment.from(text), query);
    }

    /**
     * 根据给定的查询对指定文本段进行评分。
     * Scores a given {@link TextSegment} against a given query.
     *
     * @param segment The {@link TextSegment} to be scored.
     * @param query   The query against which to score the segment.
     * @return the score.
     */
    default Response<Double> score(TextSegment segment, String query) {
        Response<List<Double>> response = scoreAll(singletonList(segment), query);
        ensureEq(response.content().size(), 1,
                "Expected a single score, but received %d", response.content().size());
        return Response.from(response.content().get(0), response.tokenUsage(), response.finishReason());
    }

    /**
     * Scores all provided {@link TextSegment}s against a given query.
     *
     * @param segments The list of {@link TextSegment}s to score.
     * @param query    The query against which to score the segments.
     * @return the list of scores. The order of scores corresponds to the order of {@link TextSegment}s.
     */
    Response<List<Double>> scoreAll(List<TextSegment> segments, String query);
}
