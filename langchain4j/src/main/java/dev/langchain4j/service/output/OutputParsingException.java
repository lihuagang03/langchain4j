package dev.langchain4j.service.output;

import dev.langchain4j.exception.LangChain4jException;

/**
 * 输出解析异常
 */
public class OutputParsingException extends LangChain4jException {

    public OutputParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
