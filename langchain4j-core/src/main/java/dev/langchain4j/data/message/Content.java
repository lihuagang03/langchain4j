package dev.langchain4j.data.message;

/**
 * 消息内容
 * 消息内容的抽象基接口。
 * Abstract base interface for message content.
 *
 * @see TextContent
 * @see ImageContent
 * @see AudioContent
 * @see VideoContent
 * @see PdfFileContent
 */
public interface Content {
    /**
     * 内容的类型
     * Returns the type of content.
     *
     * <p>Can be used to cast the content to the correct type.</p>
     *
     * @return The type of content.
     */
    ContentType type();
}
