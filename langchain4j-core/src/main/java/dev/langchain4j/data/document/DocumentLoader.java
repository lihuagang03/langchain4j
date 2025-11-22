package dev.langchain4j.data.document;

import java.io.InputStream;

/**
 * 文档加载器
 * Utility class for loading documents.
 */
public class DocumentLoader {

    private DocumentLoader() {
    }

    /**
     * 使用指定的文档解析器从给定的源加载文档。
     * Loads a document from the given source using the given parser.
     *
     * <p>Forwards the source Metadata to the parsed Document.
     *
     * @param source The source from which the document will be loaded.
     * @param parser The parser that will be used to parse the document.
     * @return The loaded document.
     * @throws BlankDocumentException when the parsed {@link Document} is blank/empty.
     */
    public static Document load(DocumentSource source, DocumentParser parser) {
        try (InputStream inputStream = source.inputStream()) {
            Document document = parser.parse(inputStream);
            document.metadata().putAll(source.metadata().toMap());
            return document;
        } catch (BlankDocumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load document", e);
        }
    }
}
