package dev.langchain4j.data.document;

import dev.langchain4j.exception.LangChain4jException;

/**
 * 空白文档异常
 */
public class BlankDocumentException extends LangChain4jException {

    public BlankDocumentException() {
        // 文档是空白的
        super("The document is blank");
    }
}
