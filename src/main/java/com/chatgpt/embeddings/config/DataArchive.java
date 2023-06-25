package com.chatgpt.embeddings.config;

/**
 * @author Zero
 * @date 2023/4/28
 */
public class DataArchive {

    /**
     * 数据集名称
     */
    public static final String COLLECTION_NAME = "embeddings_data";
    /**
     * 分片数量
     */
    public static final Integer SHARDS_NUM = 8;
    /**
     * 特征值长度
     */
    public static final Integer DATA_DIMENSION = 1536;

    /**
     * 字段
     */
    public static class Field {
        public static final String ID = "id";
        public static final String CONTENT = "content";
        public static final String CONTENT_COUNT = "content_count";
        public static final String CONTENT_VECTOR = "content_vector";
    }
}
