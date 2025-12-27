package dev.langchain4j.guardrail;

import static dev.langchain4j.internal.JsonParsingUtils.extractAndParseJson;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.internal.JsonParsingUtils;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON提取器输出防护
 * 一个输出防护机制，用于检查响应是否可以成功反序列化为类型为 T 的对象（从 JSON）。
 * An {@link OutputGuardrail} that will check whether or not a response can be successfully deserialized to an object
 * of type {@code T} from JSON.
 * <p>
 *     如果反序列化失败，LLM 将使用 getInvalidJsonReprompt(AiMessage, String) 重新提示，
 *     默认情况下为 DEFAULT_REPROMPT_PROMPT。
 *     If deserialization fails, the LLM will be reprompted with {@link #getInvalidJsonReprompt(AiMessage, String)}, which
 *     defaults to {@link #DEFAULT_REPROMPT_PROMPT}.
 * </p>
 *
 * @param <T> The type of object that the class should deserialize from JSON
 */
public class JsonExtractorOutputGuardrail<T> implements OutputGuardrail {
    /**
     * 重新提示时使用的默认消息
     * The default message to use when reprompting
     */
    public static final String DEFAULT_REPROMPT_MESSAGE = "Invalid JSON";

    /**
     * 在重新提示期间附加到大型语言模型的默认提示
     * The default prompt to append to the LLM during a reprompt
     *
     * 确保你返回一个符合指定格式的有效 JSON 对象
     */
    public static final String DEFAULT_REPROMPT_PROMPT =
            "Make sure you return a valid JSON object following the specified format";

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonExtractorOutputGuardrail.class);
    /**
     * 对象映射器
     */
    private final ObjectMapper objectMapper;
    /**
     * 输出类
     */
    private Class<T> outputClass;
    /**
     * 输出类型
     */
    private TypeReference<T> outputType;

    public JsonExtractorOutputGuardrail(ObjectMapper objectMapper, Class<T> outputClass) {
        this.objectMapper = ensureNotNull(objectMapper, "objectMapper");
        this.outputClass = ensureNotNull(outputClass, "outputClass");
    }

    public JsonExtractorOutputGuardrail(ObjectMapper objectMapper, TypeReference<T> outputType) {
        this.objectMapper = ensureNotNull(objectMapper, "objectMapper");
        this.outputType = ensureNotNull(outputType, "outputType");
    }

    public JsonExtractorOutputGuardrail(Class<T> outputClass) {
        this(new ObjectMapper(), outputClass);
    }

    public JsonExtractorOutputGuardrail(TypeReference<T> outputType) {
        this(new ObjectMapper(), outputType);
    }

    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        var llmResponse = ensureNotNull(responseFromLLM, "responseFromLLM").text();
        LOGGER.debug("LLM output: {}", llmResponse);

        return deserialize(llmResponse)
                .map(r -> successWith(r.json(), r.value()))
                .orElseGet(() -> invokeInvalidJson(responseFromLLM, llmResponse));
    }

    protected OutputGuardrailResult invokeInvalidJson(AiMessage aiMessage, String json) {
        LOGGER.debug("Found invalid JSON for aiMessage = {} and json = {}", aiMessage, json);
        return reprompt(getInvalidJsonMessage(aiMessage, json), getInvalidJsonReprompt(aiMessage, json));
    }

    /**
     * 生成一条消息，指示提供的 JSON 无效。
     * Generates a message indicating that the provided JSON is invalid.
     *
     * @param aiMessage the AI message associated with the invalid JSON. This parameter is not used.
     * @param json the JSON that failed validation. This parameter is not used.
     * @return a default message indicating that the JSON is invalid.
     */
    protected String getInvalidJsonMessage(
            @SuppressWarnings("unused") AiMessage aiMessage, @SuppressWarnings("unused") String json) {
        return DEFAULT_REPROMPT_MESSAGE;
    }

    /**
     * 生成一个重试提示消息，指出提供的 JSON 无效。
     * Generates a reprompt message indicating that the provided JSON is invalid.
     * <p>
     *     此消息附加在上一次请求中的用户消息后。
     *     This message is appended to the user message from the previous request.
     * </p>
     *
     * @param aiMessage the AI message associated with the invalid JSON. This parameter is not used.
     * @param json the JSON input that failed validation. This parameter is not used.
     * @return a reprompt message indicating that the JSON is invalid.
     */
    protected String getInvalidJsonReprompt(
            @SuppressWarnings("unused") AiMessage aiMessage, @SuppressWarnings("unused") String json) {
        return DEFAULT_REPROMPT_PROMPT;
    }

    /**
     * 尝试使用配置的 ObjectMapper 将提供的 LLM 响应字符串反序列化为类型 T 的对象。
     * 如果反序列化失败，则返回一个空的 Optional。
     * Tries to deserialize the provided LLM response string into an object of type T using the configured {@link ObjectMapper}.
     * If deserialization fails, an empty Optional is returned.
     *
     * @param llmResponse the JSON-formatted response string to be deserialized
     * @return an Optional containing the deserialized object if successful, or an empty Optional if deserialization fails
     */
    protected Optional<JsonParsingUtils.ParsedJson<T>> deserialize(String llmResponse) {
        try {
            return this.outputClass != null
                    ? extractAndParseJson(llmResponse, text -> this.objectMapper.readValue(text, this.outputClass))
                    : extractAndParseJson(llmResponse, text -> this.objectMapper.readValue(text, this.outputType));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
