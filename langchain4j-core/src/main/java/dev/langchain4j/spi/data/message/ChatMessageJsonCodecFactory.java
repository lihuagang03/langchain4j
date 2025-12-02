package dev.langchain4j.spi.data.message;

import dev.langchain4j.Internal;
import dev.langchain4j.data.message.ChatMessageJsonCodec;

/**
 * 聊天消息JSON编解码器工厂
 * A factory for creating {@link ChatMessageJsonCodec} objects.
 * Used for SPI.
 */
@Internal
public interface ChatMessageJsonCodecFactory {

    /**
     * Creates a new {@link ChatMessageJsonCodec} object.
     * @return the new {@link ChatMessageJsonCodec} object.
     */
    ChatMessageJsonCodec create();
}
