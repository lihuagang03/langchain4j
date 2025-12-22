package dev.langchain4j.data.document.loader;

import static dev.langchain4j.spi.ServiceHelper.loadFactories;

import dev.langchain4j.Internal;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.exception.LangChain4jException;
import dev.langchain4j.spi.data.document.parser.DocumentParserFactory;

/**
 * 文档解析器的加载器
 */
@Internal
class DocumentParserLoader {
    /**
     * 加载文档解析器
     * @return 文档解析器
     */
    static DocumentParser loadDocumentParser() {
        // 加载 文档解析器工厂
        var factories = loadFactories(DocumentParserFactory.class);

        if (factories.size() > 1) {
            throw new LangChain4jException("Conflict: multiple document parsers have been found in the classpath. "
                    + "Please explicitly specify the one you wish to use.");
        }

        for (DocumentParserFactory factory : factories) {
            // 创建 文档解析器
            return factory.create();
        }

        return null;
    }
}
