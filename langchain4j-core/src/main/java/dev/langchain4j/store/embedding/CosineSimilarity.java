package dev.langchain4j.store.embedding;

import dev.langchain4j.data.embedding.Embedding;

import static dev.langchain4j.internal.Exceptions.illegalArgument;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * 余弦相似度
 * 用于计算两个向量之间余弦相似度的工具类。
 * Utility class for calculating cosine similarity between two vectors.
 */
public class CosineSimilarity {
    private CosineSimilarity() {}

    /**
     * 一个用于避免除以零的小值。
     * A small value to avoid division by zero.
     */
    public static final float EPSILON = 1e-8f;

    /**
     * 计算两个向量之间的余弦相似度。
     * Calculates cosine similarity between two vectors.
     * <p>
     * 余弦相似度衡量两个向量之间夹角的余弦值，用以表示它们的方向相似性。
     * 它产生的值的范围为：
     * Cosine similarity measures the cosine of the angle between two vectors, indicating their directional similarity.
     * It produces a value in the range:
     * <p>
     * -1 表示向量完全相反（方向相反）。
     * -1 indicates vectors are diametrically opposed (opposite directions).
     * <p>
     * 0 表示向量正交（没有方向相似性）。
     * 0 indicates vectors are orthogonal (no directional similarity).
     * <p>
     * 1 表示向量指向相同方向（但不一定大小相同）。
     * 1 indicates vectors are pointing in the same direction (but not necessarily of the same magnitude).
     * <p>
     * 不要将其与余弦距离（[0..2]）混淆，余弦距离用来量化两个向量的差异。
     * Not to be confused with cosine distance ([0..2]), which quantifies how different two vectors are.
     * <p>
     * 全零向量的嵌入被认为与所有其他向量正交；包括其他全零向量。
     * Embeddings of all-zeros vectors are considered orthogonal to all other vectors;
     * including other all-zeros vectors.
     *
     * @param embeddingA first embedding vector
     * @param embeddingB second embedding vector
     * @return cosine similarity in the range [-1..1]
     */
    public static double between(Embedding embeddingA, Embedding embeddingB) {
        ensureNotNull(embeddingA, "embeddingA");
        ensureNotNull(embeddingB, "embeddingB");

        float[] vectorA = embeddingA.vector();
        float[] vectorB = embeddingB.vector();

        if (vectorA.length != vectorB.length) {
            throw illegalArgument("Length of vector a (%s) must be equal to the length of vector b (%s)",
                    vectorA.length, vectorB.length);
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        // Avoid division by zero.
        return dotProduct / Math.max(Math.sqrt(normA) * Math.sqrt(normB), EPSILON);
    }

    /**
     * 将相关性分数转换为余弦相似度。
     * Converts relevance score into cosine similarity.
     *
     * @param relevanceScore Relevance score in the range [0..1] where 0 is not relevant and 1 is relevant.
     * @return Cosine similarity in the range [-1..1] where -1 is not relevant and 1 is relevant.
     */
    public static double fromRelevanceScore(double relevanceScore) {
        return relevanceScore * 2 - 1;
    }
}
