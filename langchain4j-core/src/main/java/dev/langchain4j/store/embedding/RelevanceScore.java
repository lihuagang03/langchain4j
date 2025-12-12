package dev.langchain4j.store.embedding;

/**
 * 相关度评分/相关性得分
 * 用于在余弦相似度和相关性得分之间转换的工具类。
 * Utility class for converting between cosine similarity and relevance score.
 */
public class RelevanceScore {
    private RelevanceScore() {}

    /**
     * 将余弦相似度转换为相关度评分。
     * Converts cosine similarity into relevance score.
     *
     * @param cosineSimilarity Cosine similarity in the range [-1..1] where -1 is not relevant and 1 is relevant.
     * @return Relevance score in the range [0..1] where 0 is not relevant and 1 is relevant.
     */
    public static double fromCosineSimilarity(double cosineSimilarity) {
        return (cosineSimilarity + 1) / 2;
    }
}
