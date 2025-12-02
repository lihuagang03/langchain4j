package dev.langchain4j.data.document.splitter;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.model.TokenCountEstimator;

/**
 * 文档拆分器的工具类
 */
public class DocumentSplitters {

    /**
     * 这是一个推荐用于通用文本的文档拆分器。
     * 它会先尝试将文档拆分为段落，并尽可能将多个段落放入单个文本片段中。
     * This is a recommended {@link DocumentSplitter} for generic text.
     * It tries to split the document into paragraphs first and fits
     * as many paragraphs into a single {@link dev.langchain4j.data.segment.TextSegment} as possible.
     * If some paragraphs are too long, they are recursively split into lines, then sentences,
     * then words, and then characters until they fit into a segment.
     *
     * @param maxSegmentSizeInTokens The maximum size of the segment, defined in tokens.
     * @param maxOverlapSizeInTokens The maximum size of the overlap, defined in tokens.
     *                               Only full sentences are considered for the overlap.
     * @param tokenCountEstimator    The {@code TokenCountEstimator} that is used to count tokens in the text.
     * @return recursive document splitter
     */
    public static DocumentSplitter recursive(int maxSegmentSizeInTokens,
                                             int maxOverlapSizeInTokens,
                                             TokenCountEstimator tokenCountEstimator) {
        // 段落
        return new DocumentByParagraphSplitter(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator,
                // 行
                new DocumentByLineSplitter(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator,
                        // 句
                        new DocumentBySentenceSplitter(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator,
                                // 单词
                                new DocumentByWordSplitter(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenCountEstimator)
                        )
                )
        );
    }

    /**
     * This is a recommended {@link DocumentSplitter} for generic text.
     * It tries to split the document into paragraphs first and fits
     * as many paragraphs into a single {@link dev.langchain4j.data.segment.TextSegment} as possible.
     * If some paragraphs are too long, they are recursively split into lines, then sentences,
     * then words, and then characters until they fit into a segment.
     *
     * @param maxSegmentSizeInChars The maximum size of the segment, defined in characters.
     * @param maxOverlapSizeInChars The maximum size of the overlap, defined in characters.
     *                              Only full sentences are considered for the overlap.
     * @return recursive document splitter
     */
    public static DocumentSplitter recursive(int maxSegmentSizeInChars, int maxOverlapSizeInChars) {
        return recursive(maxSegmentSizeInChars, maxOverlapSizeInChars, null);
    }
}
