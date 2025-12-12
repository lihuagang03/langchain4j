package dev.langchain4j.spi.services;

import dev.langchain4j.Internal;
import dev.langchain4j.service.TokenStream;

import java.lang.reflect.Type;

/**
 * 词元流适配器
 */
@Internal
public interface TokenStreamAdapter {

    boolean canAdaptTokenStreamTo(Type type);

    /**
     * 适配词元流
     * @param tokenStream 词元流
     */
    Object adapt(TokenStream tokenStream);
}
