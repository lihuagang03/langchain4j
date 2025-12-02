package dev.langchain4j.exception;

/**
 * 不支持的功能异常
 */
public class UnsupportedFeatureException extends LangChain4jException {

    public UnsupportedFeatureException(String message) {
        super(message);
    }
}
