package dev.langchain4j.model.chat.request;

import static dev.langchain4j.internal.Utils.copy;
import static dev.langchain4j.internal.Utils.isNullOrEmpty;
import static dev.langchain4j.internal.ValidationUtils.ensureNotEmpty;
import static java.util.Arrays.asList;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ChatMessage;
import java.util.List;
import java.util.Objects;

/**
 * 聊天请求
 */
public class ChatRequest {

    /**
     * 聊天消息列表
     */
    private final List<ChatMessage> messages;
    /**
     * 聊天请求参数
     */
    private final ChatRequestParameters parameters;

    protected ChatRequest(Builder builder) {
        this.messages = copy(ensureNotEmpty(builder.messages, "messages"));

        // 聊天请求参数
        DefaultChatRequestParameters.Builder<?> parametersBuilder = ChatRequestParameters.builder();

        if (builder.modelName != null) {
            validate(builder, "modelName");
            parametersBuilder.modelName(builder.modelName);
        }
        if (builder.temperature != null) {
            validate(builder, "temperature");
            parametersBuilder.temperature(builder.temperature);
        }
        if (builder.topP != null) {
            validate(builder, "topP");
            parametersBuilder.topP(builder.topP);
        }
        if (builder.topK != null) {
            validate(builder, "topK");
            parametersBuilder.topK(builder.topK);
        }
        if (builder.frequencyPenalty != null) {
            validate(builder, "frequencyPenalty");
            parametersBuilder.frequencyPenalty(builder.frequencyPenalty);
        }
        if (builder.presencePenalty != null) {
            validate(builder, "presencePenalty");
            parametersBuilder.presencePenalty(builder.presencePenalty);
        }
        if (builder.maxOutputTokens != null) {
            validate(builder, "maxOutputTokens");
            parametersBuilder.maxOutputTokens(builder.maxOutputTokens);
        }
        if (!isNullOrEmpty(builder.stopSequences)) {
            validate(builder, "stopSequences");
            parametersBuilder.stopSequences(builder.stopSequences);
        }
        if (!isNullOrEmpty(builder.toolSpecifications)) {
            validate(builder, "toolSpecifications");
            parametersBuilder.toolSpecifications(builder.toolSpecifications);
        }
        if (builder.toolChoice != null) {
            validate(builder, "toolChoice");
            parametersBuilder.toolChoice(builder.toolChoice);
        }
        if (builder.responseFormat != null) {
            validate(builder, "responseFormat");
            parametersBuilder.responseFormat(builder.responseFormat);
        }

        if (builder.parameters != null) {
            this.parameters = builder.parameters;
        } else {
            this.parameters = parametersBuilder.build();
        }
    }

    public List<ChatMessage> messages() {
        return messages;
    }

    public ChatRequestParameters parameters() {
        return parameters;
    }

    public String modelName() {
        return parameters.modelName();
    }

    public Double temperature() {
        return parameters.temperature();
    }

    public Double topP() {
        return parameters.topP();
    }

    public Integer topK() {
        return parameters.topK();
    }

    public Double frequencyPenalty() {
        return parameters.frequencyPenalty();
    }

    public Double presencePenalty() {
        return parameters.presencePenalty();
    }

    public Integer maxOutputTokens() {
        return parameters.maxOutputTokens();
    }

    public List<String> stopSequences() {
        return parameters.stopSequences();
    }

    public List<ToolSpecification> toolSpecifications() {
        return parameters.toolSpecifications();
    }

    public ToolChoice toolChoice() {
        return parameters.toolChoice();
    }

    public ResponseFormat responseFormat() {
        return parameters.responseFormat();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRequest that = (ChatRequest) o;
        return Objects.equals(this.messages, that.messages) && Objects.equals(this.parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messages, parameters);
    }

    @Override
    public String toString() {
        return "ChatRequest {" + " messages = " + messages + ", parameters = " + parameters + " }";
    }

    /**
     * Transforms this instance to a {@link Builder} with all of the same field values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        /**
         * 聊天消息列表
         */
        private List<ChatMessage> messages;
        /**
         * 聊天请求参数
         */
        private ChatRequestParameters parameters;

        /**
         * 模型名称
         */
        private String modelName;
        /**
         * 温度
         * 调整生成的随机性，控制生成风格的严谨或随意。
         */
        private Double temperature;
        /**
         * 核采样
         * 用于控制生成文本的多样性和创造性。
         * 它定义了一个累积概率阈值，模型在生成下一个词时，只考虑概率累积和达到该阈值的一组最小候选词。
         * 调整 Top-P 参数可以让您根据具体任务需求（例如，是需要事实准确的总结还是需要有创意的故事）来平衡模型的生成质量和创造力。
         */
        private Double topP;
        /**
         * 限制候选词数量，使生成内容集中。
         */
        private Integer topK;
        /**
         * 频率惩罚
         * 通过对已经生成的词元施加惩罚，来减少重复词语的出现。
         */
        private Double frequencyPenalty;
        /**
         * 存在惩罚
         * 通过对已经生成的词元施加惩罚，来鼓励模型生成更多新内容。
         */
        private Double presencePenalty;
        /**
         * 最大输出词元数量
         * 控制生成的回复中最多可以包含多少个词元（tokens），直接影响生成文本的长度。
         */
        private Integer maxOutputTokens;
        /**
         * 停止词序列
         */
        private List<String> stopSequences;
        /**
         * 工具规格列表
         */
        private List<ToolSpecification> toolSpecifications;
        /**
         * 工具选择机制
         */
        private ToolChoice toolChoice;
        /**
         * 响应格式
         */
        private ResponseFormat responseFormat;

        public Builder() {}

        public Builder(ChatRequest chatRequest) {
            this.messages = chatRequest.messages;
            this.parameters = chatRequest.parameters;
        }

        public Builder messages(List<ChatMessage> messages) {
            this.messages = messages;
            return this;
        }

        public Builder messages(ChatMessage... messages) {
            return messages(asList(messages));
        }

        public Builder parameters(ChatRequestParameters parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder topP(Double topP) {
            this.topP = topP;
            return this;
        }

        public Builder topK(Integer topK) {
            this.topK = topK;
            return this;
        }

        public Builder frequencyPenalty(Double frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public Builder presencePenalty(Double presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }

        public Builder maxOutputTokens(Integer maxOutputTokens) {
            this.maxOutputTokens = maxOutputTokens;
            return this;
        }

        public Builder stopSequences(List<String> stopSequences) {
            this.stopSequences = stopSequences;
            return this;
        }

        public Builder toolSpecifications(List<ToolSpecification> toolSpecifications) {
            this.toolSpecifications = toolSpecifications;
            return this;
        }

        public Builder toolSpecifications(ToolSpecification... toolSpecifications) {
            return toolSpecifications(asList(toolSpecifications));
        }

        public Builder toolChoice(ToolChoice toolChoice) {
            this.toolChoice = toolChoice;
            return this;
        }

        public Builder responseFormat(ResponseFormat responseFormat) {
            this.responseFormat = responseFormat;
            return this;
        }

        public ChatRequest build() {
            return new ChatRequest(this);
        }
    }

    private static void validate(Builder builder, String name) {
        if (builder.parameters != null) {
            throw new IllegalArgumentException("Cannot set both 'parameters' and '%s' on ChatRequest".formatted(name));
        }
    }
}
