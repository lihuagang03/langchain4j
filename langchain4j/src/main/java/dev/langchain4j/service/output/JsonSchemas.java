package dev.langchain4j.service.output;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.TokenStream;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Optional;

import static dev.langchain4j.internal.JsonSchemaElementUtils.jsonObjectOrReferenceSchemaFrom;
import static dev.langchain4j.service.TypeUtils.getRawClass;
import static dev.langchain4j.service.TypeUtils.resolveFirstGenericParameterClass;

/**
 * JSON模式的工具类
 */
public class JsonSchemas {

    public static Optional<JsonSchema> jsonSchemaFrom(Type returnType) {

        if (!isPojo(returnType) || returnType == void.class) {
            return Optional.empty();
        }

        Class<?> rawClass = getRawClass(returnType);

        // JSON模式
        JsonSchema jsonSchema = JsonSchema.builder()
                .name(rawClass.getSimpleName())
                .rootElement(jsonObjectOrReferenceSchemaFrom(rawClass, null, false, new LinkedHashMap<>(), true))
                .build();

        return Optional.of(jsonSchema);
    }

    private static boolean isPojo(Type returnType) {

        // 字符串、AI消息、词元流、模型响应
        if (returnType == String.class
                || returnType == AiMessage.class
                || returnType == TokenStream.class
                || returnType == Response.class) {
            return false;
        }

        // Explanation (which will make this a lot easier to understand):
        // In the case of List<String> these two would be set like:
        // rawClass: List.class
        // typeArgumentClass: String.class
        Class<?> rawClass = getRawClass(returnType);
        Class<?> typeArgumentClass = resolveFirstGenericParameterClass(returnType);

        // 输出解析器
        OutputParser<?> outputParser = new DefaultOutputParserFactory().get(rawClass, typeArgumentClass);
        return outputParser instanceof PojoOutputParser;
    }
}
