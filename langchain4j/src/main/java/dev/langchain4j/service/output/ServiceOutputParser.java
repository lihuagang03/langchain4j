package dev.langchain4j.service.output;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static dev.langchain4j.service.TypeUtils.getRawClass;
import static dev.langchain4j.service.TypeUtils.resolveFirstGenericParameterClass;
import static dev.langchain4j.service.TypeUtils.resolveFirstGenericParameterType;
import static dev.langchain4j.service.TypeUtils.typeHasRawClass;

import dev.langchain4j.Internal;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.TokenStream;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

/**
 * 服务输出解析器
 */
@Internal
public class ServiceOutputParser {

    /**
     * 输出解析器工厂
     */
    private final OutputParserFactory outputParserFactory;

    public ServiceOutputParser() {
        this(new DefaultOutputParserFactory());
    }

    ServiceOutputParser(OutputParserFactory outputParserFactory) {
        this.outputParserFactory = ensureNotNull(outputParserFactory, "outputParserFactory");
    }

    /**
     * 解析聊天响应
     * @param chatResponse 聊天响应
     * @param returnType 返回类型
     * @return 响应对象
     */
    public Object parse(ChatResponse chatResponse, Type returnType) {

        // AI服务调用的结果
        if (typeHasRawClass(returnType, Result.class)) {
            // In the case of returnType = Result<List<String>>, returnType will be set to List<String>
            returnType = resolveFirstGenericParameterType(returnType);
        }

        // 返回类型
        // In the case of returnType = List<String> these two would be set like:
        // rawClass = List.class
        // typeArgumentClass = String.class
        Class<?> rawClass = getRawClass(returnType);
        Class<?> typeArgumentClass = resolveFirstGenericParameterClass(returnType);

        // 模型响应
        if (rawClass == Response.class) {
            // AI消息
            // legacy
            return Response.from(chatResponse.aiMessage(), chatResponse.tokenUsage(), chatResponse.finishReason());
        }

        if (rawClass == void.class || rawClass == Void.class) {
            return null;
        }

        // AI消息
        AiMessage aiMessage = chatResponse.aiMessage();
        if (rawClass == AiMessage.class) {
            return aiMessage;
        }

        // AI消息的文本内容
        String text = aiMessage.text();
        if (rawClass == String.class) {
            return text;
        }

        // 输出解析器
        OutputParser<?> outputParser = outputParserFactory.get(rawClass, typeArgumentClass);
        return outputParser.parse(text);
    }

    public Optional<JsonSchema> jsonSchema(Type returnType) {

        // AI服务调用的结果
        if (typeHasRawClass(returnType, Result.class)) {
            // In the case of returnType = Result<List<String>>, returnType will be set to List<String>
            returnType = resolveFirstGenericParameterType(returnType);
        }

        // 返回类型
        // In the case of returnType = List<String> these two would be set like:
        // rawClass = List.class
        // typeArgumentClass = String.class
        Class<?> rawClass = getRawClass(returnType);
        Class<?> typeArgumentClass = resolveFirstGenericParameterClass(returnType);

        if (schemaNotRequired(rawClass)) {
            return Optional.empty();
        }

        // 输出解析器
        OutputParser<?> outputParser = outputParserFactory.get(rawClass, typeArgumentClass);
        return outputParser.jsonSchema();
    }

    /**
     * 输出格式说明
     * @param returnType 返回类型
     * @return 输出格式说明
     */
    public String outputFormatInstructions(Type returnType) {

        // AI服务调用的结果
        if (typeHasRawClass(returnType, Result.class)) {
            // In the case of returnType = Result<List<String>>, returnType will be set to List<String>
            returnType = resolveFirstGenericParameterType(returnType);
        }

        // 返回类型
        // In the case of returnType = List<String> these two would be set like:
        // rawClass = List.class
        // typeArgumentClass = String.class
        Class<?> rawClass = getRawClass(returnType);
        Class<?> typeArgumentClass = resolveFirstGenericParameterClass(returnType);

        if (schemaNotRequired(rawClass)) {
            return "";
        }

        // 输出解析器
        OutputParser<?> outputParser = outputParserFactory.get(rawClass, typeArgumentClass);
        String formatInstructions = outputParser.formatInstructions();
        if (!formatInstructions.startsWith("\nYou must")) {
            // 您必须严格按照以下格式回答:
            formatInstructions = "\nYou must answer strictly in the following format: " + formatInstructions;
        }
        return formatInstructions;
    }

    private static boolean schemaNotRequired(Class<?> type) {
        // 字符串、AI消息、词元流、模型响应、映射表、void
        return type == String.class
                || type == AiMessage.class
                || type == TokenStream.class
                || type == Response.class
                || type == Map.class
                || type == void.class
                || type == Void.class;
    }
}
