package dev.langchain4j.data.message;

/**
 * 内容类型
 * The type of content, e.g. text or image.
 * Maps to implementations of {@link Content}.
 */
public enum ContentType {

    /**
     * Text content.
     * 文本内容
     */
    TEXT(TextContent.class),

    /**
     * Image content.
     * 图像内容
     */
    IMAGE(ImageContent.class),

    /**
     * Audio content.
     * 音频内容
     */
    AUDIO(AudioContent.class),

    /**
     * Video content.
     * 视频内容
     */
    VIDEO(VideoContent.class),

    /**
     * PDF file content.
     * PDF 文件内容
     */
    PDF(PdfFileContent.class);

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
