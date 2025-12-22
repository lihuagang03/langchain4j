package dev.langchain4j.data.document.splitter;

import dev.langchain4j.Internal;

import java.util.function.Function;

import static dev.langchain4j.internal.ValidationUtils.ensureGreaterThanZero;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * 片段构建者
 * 用于分层文档拆分器的片段构建者。
 * Segment builder utility class for HierarchicalDocumentSplitter.
 */
@Internal
class SegmentBuilder {

    /**
     * 最大片段数量
     */
    private final int maxSegmentSize;
    /**
     * 文本到大小的转换函数
     */
    private final Function<String, Integer> sizeFunction;
    /**
     * 连接分隔符
     */
    private final String joinSeparator;
    /**
     * 连接分隔符的大小
     */
    private final int joinSeparatorSize;
    /**
     * 片段
     */
    private String segment = "";
    /**
     * 片段大小
     */
    private int segmentSize = 0;

    /**
     * Creates a new instance of {@link SegmentBuilder}.
     *
     * @param maxSegmentSize The maximum size of a segment.
     * @param sizeFunction   The function to use to estimate the size of a text.
     * @param joinSeparator  The separator to use when joining multiple texts into a single segment.
     */
    public SegmentBuilder(int maxSegmentSize, Function<String, Integer> sizeFunction, String joinSeparator) {
        this.maxSegmentSize = ensureGreaterThanZero(maxSegmentSize, "maxSegmentSize");
        this.sizeFunction = ensureNotNull(sizeFunction, "sizeFunction");
        this.joinSeparator = ensureNotNull(joinSeparator, "joinSeparator");
        this.joinSeparatorSize = sizeOf(joinSeparator);
    }

    /**
     * Returns the current size of the segment (as returned by the {@code sizeFunction}).
     *
     * @return The current size of the segment.
     */
    public int getSize() {
        return segmentSize;
    }

    /**
     * Returns {@code true} if the provided text can be added to the current segment.
     *
     * @param text The text to check.
     * @return {@code true} if the provided text can be added to the current segment.
     */
    public boolean hasSpaceFor(String text) {
        int totalSize = sizeOf(text);
        if (isNotEmpty()) {
            totalSize += segmentSize + joinSeparatorSize;
        }
        return totalSize <= maxSegmentSize;
    }

    /**
     * 如果提供的大小可以添加到当前段，则返回 true。
     * Returns {@code true} if the provided size can be added to the current segment.
     *
     * @param size The size to check.
     * @return {@code true} if the provided size can be added to the current segment.
     */
    public boolean hasSpaceFor(int size) {
        int totalSize = size;
        if (isNotEmpty()) {
            totalSize += segmentSize + joinSeparatorSize;
        }
        return totalSize <= maxSegmentSize;
    }

    /**
     * 返回提供的文本的大小（由 sizeFunction 返回）。
     * Returns the size of the provided text (as returned by the {@code sizeFunction}).
     *
     * @param text The text to check.
     * @return The size of the provided text.
     */
    public int sizeOf(String text) {
        return sizeFunction.apply(text);
    }

    /**
     * 将提供的文本附加到当前段落。
     * Appends the provided text to the current segment.
     *
     * @param text The text to append.
     */
    public void append(String text) {
        if (isNotEmpty()) {
            segment += joinSeparator;
        }
        segment += text;
        segmentSize = sizeOf(segment);
    }

    /**
     * 将提供的文本添加到当前段的开头。
     * Prepends the provided text to the current segment.
     *
     * @param text The text to prepend.
     */
    public void prepend(String text) {
        if (isNotEmpty()) {
            segment = text + joinSeparator + segment;
        } else {
            segment = text;
        }
        segmentSize = sizeOf(segment);
    }

    /**
     * Returns {@code true} if the current segment is not empty.
     *
     * @return {@code true} if the current segment is not empty.
     */
    public boolean isNotEmpty() {
        return !segment.isEmpty();
    }

    @Override
    public String toString() {
        return segment.trim();
    }

    /**
     * 重置当前段落。
     * Resets the current segment.
     */
    public void reset() {
        segment = "";
        segmentSize = 0;
    }
}
