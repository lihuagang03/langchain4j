package dev.langchain4j.data.document;

import dev.langchain4j.exception.LangChain4jException;
import java.io.InputStream;

/**
 * 文档加载器
 * 用于加载文档。
 * Utility class for loading documents.
 */
public class DocumentLoader {

    private DocumentLoader() {
    }

    /**
     * 使用指定的文档解析器从给定的源加载文档。
     * Loads a document from the given source using the given parser.
     *
     * <p>
     * 将源元数据转发到解析后的文档。
     * Forwards the source Metadata to the parsed Document.
     *
     * @param source The source from which the document will be loaded.
     * @param parser The parser that will be used to parse the document.
     * @return The loaded document.
     * @throws BlankDocumentException when the parsed {@link Document} is blank/empty.
     */
    public static Document load(DocumentSource source, DocumentParser parser) {
        try (InputStream inputStream = source.inputStream()) {
            // 将给定的输入流解析为文档
            Document document = parser.parse(inputStream);
            // 与此文档关联的元数据
            document.metadata().putAll(source.metadata().toMap());
            return document;
        } catch (BlankDocumentException e) {
            // 空白文档异常
            throw e;
        } catch (Exception e) {
            // 加载文档失败
            throw new LangChain4jException("Failed to load document", e);
        }
    }
}
