package dev.langchain4j.rag.content.injector;

import static dev.langchain4j.internal.Utils.copy;
import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotEmpty;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static java.util.stream.Collectors.joining;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.Content;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内容注入器的默认实现
 * Default implementation of {@link ContentInjector} intended to be suitable for the majority of use cases.
 * <br>
 * <br>
 * It's important to note that while efforts will be made to avoid breaking changes,
 * the default behavior of this class may be updated in the future if it's found
 * that the current behavior does not adequately serve the majority of use cases.
 * Such changes would be made to benefit both current and future users.
 * <br>
 * <br>
 * This implementation appends all given {@link Content}s to the end of the given {@link UserMessage}
 * in their order of iteration.
 * Refer to {@link #DEFAULT_PROMPT_TEMPLATE} and implementation for more details.
 * <br>
 * <br>
 * Configurable parameters (optional):
 * <br>
 * - {@link #promptTemplate}: The prompt template that defines how the original {@code userMessage}
 * and {@code contents} are combined into the resulting {@link UserMessage}.
 * The text of the template should contain the {@code {{userMessage}}} and {@code {{contents}}} variables.
 * <br>
 * - {@link #metadataKeysToInclude}: A list of {@link Metadata} keys that should be included
 * with each {@link Content#textSegment()}.
 */
public class DefaultContentInjector implements ContentInjector {

    /**
     * 默认的提示模版
     */
    public static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = PromptTemplate.from(
            """
                    {{userMessage}}

                    Answer using the following information:
                    {{contents}}""");

    /**
     * 提示模版
     */
    private final PromptTemplate promptTemplate;
    private final List<String> metadataKeysToInclude;

    public DefaultContentInjector() {
        this(DEFAULT_PROMPT_TEMPLATE, null);
    }

    public DefaultContentInjector(List<String> metadataKeysToInclude) {
        this(DEFAULT_PROMPT_TEMPLATE, ensureNotEmpty(metadataKeysToInclude, "metadataKeysToInclude"));
    }

    public DefaultContentInjector(PromptTemplate promptTemplate) {
        this(ensureNotNull(promptTemplate, "promptTemplate"), null);
    }

    public DefaultContentInjector(PromptTemplate promptTemplate, List<String> metadataKeysToInclude) {
        this.promptTemplate = getOrDefault(promptTemplate, DEFAULT_PROMPT_TEMPLATE);
        this.metadataKeysToInclude = copy(metadataKeysToInclude);
    }

    public static DefaultContentInjectorBuilder builder() {
        return new DefaultContentInjectorBuilder();
    }

    @Override
    public ChatMessage inject(List<Content> contents, ChatMessage chatMessage) {
        if (contents.isEmpty()) {
            return chatMessage;
        }

        // 提示
        Prompt prompt = createPrompt(chatMessage, contents);
        if (chatMessage instanceof UserMessage userMessage) {
            // 用户消息
            return userMessage.toBuilder()
                    .contents(List.of(TextContent.from(prompt.text())))
                    .build();
        } else {
            // 转换为用户消息
            return prompt.toUserMessage();
        }
    }

    protected Prompt createPrompt(ChatMessage chatMessage, List<Content> contents) {
        Map<String, Object> variables = new HashMap<>();
        // 用户消息
        variables.put("userMessage", ((UserMessage) chatMessage).singleText());
        // 内容列表
        variables.put("contents", format(contents));
        return promptTemplate.apply(variables);
    }

    protected String format(List<Content> contents) {
        // 内容格式化，两个换行符间隔
        return contents.stream().map(this::format).collect(joining("\n\n"));
    }

    protected String format(Content content) {

        // 文本片段
        TextSegment segment = content.textSegment();

        if (metadataKeysToInclude.isEmpty()) {
            return segment.text();
        }

        String segmentContent = segment.text();
        String segmentMetadata = format(segment.metadata());

        return format(segmentContent, segmentMetadata);
    }

    protected String format(Metadata metadata) {
        StringBuilder formattedMetadata = new StringBuilder();
        for (String metadataKey : metadataKeysToInclude) {
            String metadataValue = metadata.getString(metadataKey);
            if (metadataValue != null) {
                if (!formattedMetadata.isEmpty()) {
                    formattedMetadata.append("\n");
                }
                // key: value，每个一行
                formattedMetadata.append(metadataKey).append(": ").append(metadataValue);
            }
        }
        return formattedMetadata.toString();
    }

    protected String format(String segmentContent, String segmentMetadata) {
        // 内容，换行，元数据
        return segmentMetadata.isEmpty()
                ? segmentContent
                : String.format("content: %s\n%s", segmentContent, segmentMetadata);
    }

    public static class DefaultContentInjectorBuilder {

        private PromptTemplate promptTemplate;
        private List<String> metadataKeysToInclude;

        DefaultContentInjectorBuilder() {}

        public DefaultContentInjectorBuilder promptTemplate(PromptTemplate promptTemplate) {
            this.promptTemplate = promptTemplate;
            return this;
        }

        public DefaultContentInjectorBuilder metadataKeysToInclude(List<String> metadataKeysToInclude) {
            this.metadataKeysToInclude = metadataKeysToInclude;
            return this;
        }

        public DefaultContentInjector build() {
            return new DefaultContentInjector(this.promptTemplate, this.metadataKeysToInclude);
        }
    }
}
