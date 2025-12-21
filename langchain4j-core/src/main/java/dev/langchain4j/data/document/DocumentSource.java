package dev.langchain4j.data.document;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文档来源
 * 定义文档源的接口。
 * 文档可以从各种来源加载，例如文件系统、HTTP、FTP等。
 * Defines the interface for a Document source.
 * Documents can be loaded from various sources such as the file system, HTTP, FTP, etc.
 */
public interface DocumentSource {

    /**
     * 提供一个输入流来读取文档的内容。
     * 这个方法可以用于从各种来源读取数据，比如本地文件或网络连接。
     * Provides an {@link InputStream} to read the content of the document.
     * This method can be implemented to read from various sources like a local file or a network connection.
     *
     * @return An InputStream from which the document content can be read.
     * @throws IOException If an I/O error occurs while creating the InputStream.
     */
    InputStream inputStream() throws IOException;

    /**
     * 返回与文档来源相关的元数据。
     * 这可能包括诸如来源位置、创建日期、所有者等详细信息。
     * Returns the metadata associated with the source of the document.
     * This could include details such as the source location, date of creation, owner, etc.
     *
     * @return A {@link Metadata} object containing information about the document
     *         source, such as {@link Document#FILE_NAME} and
     *         {@link Document#ABSOLUTE_DIRECTORY_PATH}.
     */
    Metadata metadata();
}
