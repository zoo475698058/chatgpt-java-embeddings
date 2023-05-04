package com.chatgpt.embeddings.DocParser;

import java.io.InputStream;
import java.util.List;

public abstract class AbstractParser {
    public abstract List<String> parse(InputStream inputStream) throws Exception;
}
