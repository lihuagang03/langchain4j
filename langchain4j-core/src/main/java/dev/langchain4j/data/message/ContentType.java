package dev.langchain4j.data.message;

/**
 * 内容类型
 * The type of content, e.g. text or image.
 * Maps to implementations of {@link Content}.
 */
public enum ContentType {

    /**
     * 文本内容
     * Text content.
     */
    TEXT(TextContent.class),

    /**
     * 图像内容
     * Image content.
     */
    IMAGE(ImageContent.class),

    /**
     * 音频内容
     * Audio content.
     */
    AUDIO(AudioContent.class),

    /**
     * 视频内容
     * Video content.
     */
    VIDEO(VideoContent.class),

    /**
     * PDF文件内容
     * PDF file content.
     */
    PDF(PdfFileContent.class);

    /**
     * 内容类
     */
    private final Class<? extends Content> contentClass;

    ContentType(Class<? extends Content> contentClass) {
        this.contentClass = contentClass;
    }

    /**
     * Returns the class of the content type.
     *
     * @return The class of the content type.
     */
    public Class<? extends Content> getContentClass() {
        return contentClass;
    }
}
