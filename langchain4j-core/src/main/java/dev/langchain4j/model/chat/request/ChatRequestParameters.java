package dev.langchain4j.model.chat.request;

import dev.langchain4j.agent.tool.ToolSpecification;

import java.util.List;

/**
 * 聊天请求参数
 * 表示大多数大型语言模型提供商支持的常见聊天请求参数。
 * 特定的 LLM 提供商集成可以扩展此接口，以添加提供商特定的参数。
 * Represents common chat request parameters supported by most LLM providers.
 * Specific LLM provider integrations can extend this interface to add provider-specific parameters.
 *
 * @see DefaultChatRequestParameters
 */
public interface ChatRequestParameters {

    /**
     * 模型名称
     */
    String modelName();

    /**
     * 温度
     * 调整生成的随机性，控制生成风格的严谨或随意。
     */
    Double temperature();

    /**
     * 核采样
     * 用于控制生成文本的多样性和创造性。
     * 它定义了一个累积概率阈值，模型在生成下一个词时，只考虑概率累积和达到该阈值的一组最小候选词。
     * 调整 Top-P 参数可以让您根据具体任务需求（例如，是需要事实准确的总结还是需要有创意的故事）来平衡模型的生成质量和创造力。
     */
    Double topP();

    /**
     * 限制候选词数量，使生成内容集中。
     */
    Integer topK();

    /**
     * 频率惩罚
     * 通过对已经生成的词元施加惩罚，来减少重复词语的出现。
     */
    Double frequencyPenalty();

    /**
     * 存在惩罚
     * 通过对已经生成的词元施加惩罚，来鼓励模型生成更多新内容。
     */
    Double presencePenalty();

    /**
     * 最大输出词元数量
     * 控制生成的回复中最多可以包含多少个词元（tokens），直接影响生成文本的长度。
     */
    Integer maxOutputTokens();

    /**
     * 停止词序列
     */
    List<String> stopSequences();

    /**
     * 工具规格列表
     */
    List<ToolSpecification> toolSpecifications();

    /**
     * 工具选择机制
     */
    ToolChoice toolChoice();

    /**
     * 响应格式
     */
    ResponseFormat responseFormat();

    /**
     * Creates a new {@link ChatRequestParameters} by combining the current parameters with the specified ones.
     * Values from the specified parameters override values from the current parameters when there is overlap.
     * Neither the current nor the specified {@link ChatRequestParameters} objects are modified.
     *
     * <p>Example:
     * <pre>
     * Current parameters:
     *   temperature = 1.0
     *   maxOutputTokens = 100
     *
     * Specified parameters:
     *   temperature = 0.5
     *   modelName = my-model
     *
     * Result:
     *   temperature = 0.5        // Overridden from specified
     *   maxOutputTokens = 100    // Preserved from current
     *   modelName = my-model     // Added from specified
     * </pre>
     *
     * @param parameters the parameters whose values will override the current ones
     * @return a new {@link ChatRequestParameters} instance combining both sets of parameters
     */
    ChatRequestParameters overrideWith(ChatRequestParameters parameters);

    static DefaultChatRequestParameters.Builder<?> builder() {
        return new DefaultChatRequestParameters.Builder<>();
    }
}
