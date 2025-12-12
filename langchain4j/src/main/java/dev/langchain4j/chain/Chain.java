package dev.langchain4j.chain;

import dev.langchain4j.service.AiServices;

/**
 * 链步骤
 * 表示一个接收输入并产生输出的链步骤。
 * Represents a chain step that takes an input and produces an output.
 * <br>
 * 链条将不会进一步开发，建议改用人工智能服务。
 * Chains are not going to be developed further, it is recommended to use {@link AiServices} instead.
 *
 * @param <Input>  the input type
 * @param <Output> the output type
 */
@FunctionalInterface
public interface Chain<Input, Output> {

    /**
     * 执行链步骤。
     * Execute the chain step.
     *
     * @param input the input
     * @return the output
     */
    Output execute(Input input);
}
