package dev.langchain4j.agentic.internal;

import dev.langchain4j.internal.DefaultExecutorProvider;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 异步响应
 */
public class AsyncResponse<T> {

    /**
     * 异步的响应
     */
    private final CompletableFuture<T> futureResponse;

    public AsyncResponse(Supplier<T> responseSupplier) {
        // 异步执行响应
        this.futureResponse = CompletableFuture.supplyAsync(responseSupplier, DefaultExecutorProvider.getDefaultExecutorService());
    }

    public T blockingGet() {
        // 阻塞地获取
        return futureResponse.join();
    }

    @Override
    public String toString() {
        return result().toString();
    }

    public Object result() {
        // 响应结果
        return futureResponse.isDone() ? futureResponse.join() : "<pending>";
    }
}
