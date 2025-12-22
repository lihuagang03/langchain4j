package dev.langchain4j.data.document.source;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSource;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.exception.LangChain4jException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * 网址文档来源
 */
public class UrlSource implements DocumentSource {

    /**
     * 网址
     */
    private final URL url;

    public UrlSource(URL url) {
        this.url = ensureNotNull(url, "url");
    }

    @Override
    public InputStream inputStream() throws IOException {
        // 网址连接
        URLConnection connection = url.openConnection();
        return connection.getInputStream();
    }

    @Override
    public Metadata metadata() {
        return Metadata.from(Document.URL, url.toString());
    }

    public static UrlSource from(String url) {
        try {
            return new UrlSource(new URL(url));
        } catch (MalformedURLException e) {
            throw new LangChain4jException(e);
        }
    }

    public static UrlSource from(URL url) {
        return new UrlSource(url);
    }

    public static UrlSource from(URI uri) {
        try {
            return new UrlSource(uri.toURL());
        } catch (MalformedURLException e) {
            throw new LangChain4jException(e);
        }
    }
}
