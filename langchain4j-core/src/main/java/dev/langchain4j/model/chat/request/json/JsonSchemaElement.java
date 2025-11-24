package dev.langchain4j.model.chat.request.json;

/**
 * JSON模式元素
 * JSON架构元素的基本接口。
 * A base interface for a JSON schema element.
 *
 * @see JsonAnyOfSchema
 * @see JsonArraySchema
 * @see JsonBooleanSchema
 * @see JsonEnumSchema
 * @see JsonIntegerSchema
 * @see JsonNullSchema
 * @see JsonNumberSchema
 * @see JsonObjectSchema
 * @see JsonRawSchema
 * @see JsonReferenceSchema
 * @see JsonStringSchema
 */
public interface JsonSchemaElement {

    String description();
}
