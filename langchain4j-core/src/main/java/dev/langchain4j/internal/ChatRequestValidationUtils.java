package dev.langchain4j.internal;

import dev.langchain4j.Internal;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.exception.UnsupportedFeatureException;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.ToolChoice;

import java.util.List;
import java.util.Locale;

import static dev.langchain4j.data.message.ContentType.TEXT;
import static dev.langchain4j.internal.Utils.isNullOrEmpty;
import static dev.langchain4j.model.chat.request.ToolChoice.AUTO;

/**
 * 聊天请求验证工具
 */
@Internal
public class ChatRequestValidationUtils {

    /**
     * 验证聊天消息列表
     * @param messages 聊天消息列表
     */
    public static void validateMessages(List<ChatMessage> messages) {
        for (ChatMessage message : messages) {
            if (message instanceof UserMessage userMessage) {
                // 用户消息
                for (Content content : userMessage.contents()) {
                    // 文本内容
                    if (content.type() != TEXT) {
                        throw new UnsupportedFeatureException(String.format(
                                "Content of type %s is not supported yet by this model provider",
                                content.type().toString().toLowerCase(Locale.ROOT)));
                    }
                }
            }
        }
    }

    /**
     * 验证聊天请求参数
     * @param parameters 聊天请求参数
     */
    public static void validateParameters(ChatRequestParameters parameters) {
        String errorTemplate = "%s is not supported yet by this model provider";

        if (parameters.modelName() != null) {
            throw new UnsupportedFeatureException(String.format(errorTemplate, "'modelName' parameter"));
        }
        if (parameters.temperature() != null) {
            throw new UnsupportedFeatureException(String.format(errorTemplate, "'temperature' parameter"));
        }
        if (parameters.topP() != null) {
            throw new UnsupportedFeatureException(String.format(errorTemplate, "'topP' parameter"));
        }
        if (parameters.topK() != null) {
            throw new UnsupportedFeatureException(String.format(errorTemplate, "'topK' parameter"));
        }
        if (parameters.frequencyPenalty() != null) {
            throw new UnsupportedFeatureException(String.format(errorTemplate, "'frequencyPenalty' parameter"));
        }
        if (parameters.presencePenalty() != null) {
            throw new UnsupportedFeatureException(String.format(errorTemplate, "'presencePenalty' parameter"));
        }
        if (parameters.maxOutputTokens() != null) {
            throw new UnsupportedFeatureException(String.format(errorTemplate, "'maxOutputTokens' parameter"));
        }
        if (!parameters.stopSequences().isEmpty()) {
            throw new UnsupportedFeatureException(String.format(errorTemplate, "'stopSequences' parameter"));
        }
    }

    /**
     * 验证工具规格列表
     * @param toolSpecifications 工具规格列表
     */
    public static void validate(List<ToolSpecification> toolSpecifications) {
        if (!isNullOrEmpty(toolSpecifications)) {
            throw new UnsupportedFeatureException("tools are not supported yet by this model provider");
        }
    }

    /**
     * 验证工具选择机制
     * @param toolChoice 工具选择机制
     */
    public static void validate(ToolChoice toolChoice) {
        if (toolChoice != null && toolChoice != AUTO) {
            throw new UnsupportedFeatureException(String.format("%s.%s is not supported yet by this model provider",
                    ToolChoice.class.getSimpleName(), toolChoice));
        }
    }

    /**
     * 验证响应格式
     * @param responseFormat 响应格式
     */
    public static void validate(ResponseFormat responseFormat) {
        String errorTemplate = "%s is not supported yet by this model provider";
        if (responseFormat != null && responseFormat.type() == ResponseFormatType.JSON) {
            throw new UnsupportedFeatureException(String.format(errorTemplate, "JSON response format"));
        }
    }
}
