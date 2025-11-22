package dev.langchain4j.data.document;

import dev.langchain4j.data.segment.TextSegment;

/**
 * 文档，表示未结构化的文本片段，通常对应单个文件的内容。
 * 这段文字可能来源于各种来源，例如文本文件、PDF、DOCX或网页（HTML）。
 * 每个文档可能都有相关的元数据，包括其来源、所有者、创建日期等。
 * Represents an unstructured piece of text that usually corresponds to a content of a single file.
 * This text could originate from various sources such as a text file, PDF, DOCX, or a web page (HTML).
 * Each document may have associated {@link Metadata} including its source, owner, creation date, etc.
 */
public interface Document {

    /**
     * 文件的名称
     * Common metadata key for the name of the file from which the document was loaded.
     */
    String FILE_NAME = "file_name";
    /**
     * 目录的绝对路径
     * Common metadata key for the absolute path of the directory from which the document was loaded.
     */
    String ABSOLUTE_DIRECTORY_PATH = "absolute_directory_path";
    /**
     * 网址
     * Common metadata key for the URL from which the document was loaded.
     */
    String URL = "url";

    /**
     * 返回此文档的文本。
     * Returns the text of this document.
     *
     * @return the text.
     */
    String text();

    /**
     * Returns the metadata associated with this document.
     *
     * @return the metadata.
     */
    Metadata metadata();

    /**
     * Builds a {@link TextSegment} from this document.
     *
     * @return a {@link TextSegment}
     */
    default TextSegment toTextSegment() {
        if (metadata().containsKey("index")) {
            return TextSegment.from(text(), metadata().copy());
        } else {
            return TextSegment.from(text(), metadata().copy().put("index", "0"));
        }
    }

    /**
     * Creates a new Document from the given text.
     *
     * <p>The created document will have empty metadata.</p>
     *
     * @param text the text of the document.
     * @return a new Document.
     */
    static Document from(String text) {
        return new DefaultDocument(text);
    }

    /**
     * Creates a new Document from the given text.
     *
     * @param text     the text of the document.
     * @param metadata the metadata of the document.
     * @return a new Document.
     */
    static Document from(String text, Metadata metadata) {
        return new DefaultDocument(text, metadata);
    }

    /**
     * Creates a new Document from the given text.
     *
     * <p>The created document will have empty metadata.</p>
     *
     * @param text the text of the document.
     * @return a new Document.
     */
    static Document document(String text) {
        return from(text);
    }

    /**
     * Creates a new Document from the given text.
     *
     * @param text     the text of the document.
     * @param metadata the metadata of the document.
     * @return a new Document.
     */
    static Document document(String text, Metadata metadata) {
        return from(text, metadata);
    }
}
