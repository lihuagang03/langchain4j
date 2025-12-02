package dev.langchain4j.exception;

/**
 * 内容过滤异常
 * 当大型语言模型提供商因内容过滤或违规使用政策而拒绝处理请求时，会抛出异常。
 * Exception thrown when the LLM provider refuses to process a request due to content filtering
 * or violation of usage policies.
 * <p>
 * 这通常表示输入内容被标记为不适当、不安全或违反提供方的内容指南。
 * This typically indicates that the input was flagged as inappropriate, unsafe, or against
 * the provider’s content guidelines.
 *
 * @since 1.2.0
 */
public class ContentFilteredException extends InvalidRequestException {

    public ContentFilteredException(String message) {
        super(message);
    }

    public ContentFilteredException(Throwable cause) {
        super(cause);
    }

    public ContentFilteredException(String message, Throwable cause) {
        super(message, cause);
    }
}
