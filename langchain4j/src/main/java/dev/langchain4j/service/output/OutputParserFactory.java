package dev.langchain4j.service.output;

import dev.langchain4j.Internal;

/**
 * 输出解析器工厂
 */
@Internal
interface OutputParserFactory {

    OutputParser<?> get(Class<?> rawClass, Class<?> typeArgumentClass);
}
