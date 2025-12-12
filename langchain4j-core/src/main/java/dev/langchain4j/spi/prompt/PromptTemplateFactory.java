package dev.langchain4j.spi.prompt;

import dev.langchain4j.Internal;

import java.util.Map;

/**
 * 提示模版工厂
 * 一个用于创建提示模板的工厂。
 * A factory for creating prompt templates.
 */
@Internal
public interface PromptTemplateFactory {

    /**
     * 工厂的输入接口。
     * Interface for input for the factory.
     */
    @Internal
    interface Input {

        /**
         * 获取模板字符串。
         * Get the template string.
         * @return the template string.
         */
        String getTemplate();

        /**
         * 获取模板的名称。
         * Get the name of the template.
         * @return the name of the template.
         */
        default String getName() { return "template"; }
    }

    /**
     * 提示模板的接口。
     * Interface for a prompt template.
     */
    @Internal
    interface Template {
        /**
         * 渲染模板。
         * Render the template.
         * @param variables the variables to use.
         * @return the rendered template.
         */
        String render(Map<String, Object> variables);
    }

    /**
     * 创建一个新的提示模板。
     * Create a new prompt template.
     * @param input the input to the factory.
     * @return the prompt template.
     */
    Template create(Input input);
}
