package dev.langchain4j.service.output;

import dev.langchain4j.Internal;
import dev.langchain4j.model.chat.request.json.JsonSchema;

import java.util.Optional;

/**
 * 输出解析器
 * 表示一个输出解析器。
 * Represents an output parser.
 *
 * @param <T> the type of the output.
 */
@Internal
interface OutputParser<T> {

    /**
     * 解析给定的文本。
     * Parse the given text.
     *
     * @param text the text to parse.
     * @return the parsed output.
     */
    T parse(String text);

    /**
     * 该类型的 JSON 模式。
     * JSON schema of the type.
     *
     * @return the JSON schema, if supported.
     */
    default Optional<JsonSchema> jsonSchema() {
        return Optional.empty();
    }

    /**
     * 文本格式的描述。
     * Description of the text format.
     *
     * @return the description of the text format.
     */
    String formatInstructions();
}
