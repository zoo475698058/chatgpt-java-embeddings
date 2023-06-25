package com.chatgpt.embeddings;

import com.chatgpt.embeddings.config.DataArchive;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.DropCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.index.CreateIndexParam;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ChatgptJavaEmbeddingsApplicationTests {
    @Autowired
    MilvusServiceClient milvusClient;

    @Test
    void prepare() {
        dropCollection(milvusClient);
        createCollection(milvusClient);
        buildIndex(milvusClient);
    }

    void dropCollection(MilvusServiceClient client){
        client.dropCollection(
                DropCollectionParam.newBuilder()
                        .withDatabaseName(DataArchive.DATABASE_NAME)
                        .withCollectionName(DataArchive.COLLECTION_NAME)
                        .build()
        );
    }

    void createCollection(MilvusServiceClient client){
        FieldType fieldType1 = FieldType.newBuilder()
                .withName(DataArchive.Field.ID)
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(true)
                .build();
        FieldType fieldType2 = FieldType.newBuilder()
                .withName(DataArchive.Field.CONTENT_COUNT)
                .withDataType(DataType.Int32)
                .build();
        FieldType fieldType3 = FieldType.newBuilder()
                .withName(DataArchive.Field.CONTENT)
                .withDataType(DataType.VarChar)
                .withMaxLength(1024)
                .build();
        FieldType fieldType4 = FieldType.newBuilder()
                .withName(DataArchive.Field.CONTENT_VECTOR)
                .withDataType(DataType.FloatVector)
                .withDimension(DataArchive.DATA_DIMENSION)
                .build();
        CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                .withDatabaseName(DataArchive.DATABASE_NAME)
                .withCollectionName(DataArchive.COLLECTION_NAME)
                .withShardsNum(DataArchive.SHARDS_NUM)
                .addFieldType(fieldType1)
                .addFieldType(fieldType2)
                .addFieldType(fieldType3)
                .addFieldType(fieldType4)
                .build();
        client.createCollection(createCollectionReq);
    }

    void buildIndex(MilvusServiceClient client){
        final String INDEX_PARAM = "{\"nlist\":1024}";
        client.createIndex(
                CreateIndexParam.newBuilder()
                        .withDatabaseName(DataArchive.DATABASE_NAME)
                        .withCollectionName(DataArchive.COLLECTION_NAME)
                        .withFieldName(DataArchive.Field.CONTENT_VECTOR)
                        .withIndexType(IndexType.IVF_FLAT)
                        .withMetricType(MetricType.L2)
                        .withExtraParam(INDEX_PARAM)
                        .withSyncMode(Boolean.FALSE)
                        .build()
        );
    }

}
