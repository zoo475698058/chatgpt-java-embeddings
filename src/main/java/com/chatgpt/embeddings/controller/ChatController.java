package com.chatgpt.embeddings.controller;

import com.chatgpt.embeddings.util.DocParse;
import com.chatgpt.embeddings.config.DataArchive;
import com.chatgpt.embeddings.config.MilvusConfig;
import com.chatgpt.embeddings.vo.PDFData;
import com.chatgpt.embeddings.vo.ReplyMsg;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.entity.embeddings.EmbeddingResponse;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.R;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.collection.ReleaseCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.response.SearchResultsWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Zero
 * @date 2023/4/28
 */
@RestController
@CrossOrigin
public class ChatController {
    private final static String KEY = "";
    private final static String HOST = "";
    @Autowired
    private MilvusConfig milvusConfig;

    @RequestMapping("/chat")
    public ReplyMsg chat(HttpServletRequest request, @RequestBody Map<String, Object> map) {
        try {
            String question = (String) map.get("question");

            OpenAiClient v2 = OpenAiClient.builder()
                    .apiKey(Arrays.asList(KEY))
                    .apiHost(HOST)
                    .build();
            EmbeddingResponse embeddings = v2.embeddings(question);
            if (embeddings == null) {
                return ReplyMsg.retErrorMsg("openai embeddings error");
            }
            List<BigDecimal> vector = embeddings.getData().get(0).getEmbedding();
            List<Float> vectors = vector.stream().map(BigDecimal::floatValue).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

            List<PDFData> searchResult = search(Arrays.asList(vectors));
            List<Message> messageList = new ArrayList<>();
            for (PDFData data : searchResult) {
                messageList.add(Message.builder().role(Message.Role.ASSISTANT).content(data.getContent()).build());
            }
            messageList.add(Message.builder().role(Message.Role.USER).content(question).build());

            ChatCompletion chatCompletion = ChatCompletion.builder().model(ChatCompletion.Model.GPT_3_5_TURBO.getName()).messages(messageList).build();
            ChatCompletionResponse completions = v2.chatCompletion(chatCompletion);
            Message result = completions.getChoices().get(0).getMessage();
            return ReplyMsg.retSuccessMsg(result.getContent());
        } catch (Exception e) {
            return ReplyMsg.retErrorMsg(e.getMessage());
        }
    }

    @PostMapping("/uploadPdf")
    public ReplyMsg uploadPdf(MultipartFile file) {
        try {
            DocParse parse = new DocParse();
            List<String> sentenceList = parse.pdfParse(file.getInputStream());
            return insertVector(sentenceList);
        } catch (Exception e) {
            return ReplyMsg.retErrorMsg(e.getMessage());
        }
    }

    @PostMapping("/uploadTxt")
    public ReplyMsg uploadTxt(MultipartFile file) {
        try {
            DocParse parse = new DocParse();
            List<String> sentenceList = parse.txtParse(file.getInputStream());
            return insertVector(sentenceList);
        } catch (Exception e) {
            return ReplyMsg.retErrorMsg(e.getMessage());
        }
    }

    private ReplyMsg insertVector(List<String> sentenceList){
        OpenAiClient v2 = OpenAiClient.builder()
                .apiKey(Arrays.asList(KEY))
                .apiHost(HOST)
                .build();

        List<Integer> contentWordCount = new ArrayList<>();
        List<List<Float>> contentVector = new ArrayList<>();
        for (String str : sentenceList) {
            contentWordCount.add(str.length());
            EmbeddingResponse embeddings = v2.embeddings(str);
            if (embeddings == null) {
                return ReplyMsg.retErrorMsg("openai embeddings error");
            }
            List<BigDecimal> vector = embeddings.getData().get(0).getEmbedding();
            List<Float> vectors = vector.stream().map(BigDecimal::floatValue).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            contentVector.add(vectors);
        }

        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field(DataArchive.Field.CONTENT, sentenceList));
        fields.add(new InsertParam.Field(DataArchive.Field.CONTENT_COUNT, contentWordCount));
        fields.add(new InsertParam.Field(DataArchive.Field.CONTENT_VECTOR, contentVector));

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(DataArchive.COLLECTION_NAME)
                .withFields(fields)
                .build();
        R<MutationResult> resultR = milvusConfig.milvusClient().insert(insertParam);
        return ReplyMsg.retSuccess(resultR.getStatus());
    }

    private List<PDFData> search(List<List<Float>> search_vectors) {
        MilvusServiceClient milvusClient = milvusConfig.milvusClient();
        milvusClient.loadCollection(
                LoadCollectionParam.newBuilder()
                        .withCollectionName(DataArchive.COLLECTION_NAME)
                        .build()
        );

        int SEARCH_K = 4;
        String SEARCH_PARAM = "{\"nprobe\":10}";
        List<String> ids = Arrays.asList(DataArchive.Field.ID);
        List<String> contents = Arrays.asList(DataArchive.Field.CONTENT);
        List<String> contentWordCounts = Arrays.asList(DataArchive.Field.CONTENT_COUNT);
        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(DataArchive.COLLECTION_NAME)
                .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                .withOutFields(ids)
                .withOutFields(contents)
                .withOutFields(contentWordCounts)
                .withTopK(SEARCH_K)
                .withVectors(search_vectors)
                .withVectorFieldName(DataArchive.Field.CONTENT_VECTOR)
                .withParams(SEARCH_PARAM)
                .build();
        R<SearchResults> respSearch = milvusClient.search(searchParam);
        List<PDFData> pdfDataList = new ArrayList<>();
        if (R.Status.Success.getCode() == respSearch.getStatus()) {
            SearchResults resp = respSearch.getData();
            if (!resp.hasResults()) {
                return new ArrayList<>();
            }
            for (int i = 0; i < search_vectors.size(); ++i) {
                SearchResultsWrapper wrapperSearch = new SearchResultsWrapper(resp.getResults());
                List<Long> id = (List<Long>) wrapperSearch.getFieldData(DataArchive.Field.ID, 0);
                List<String> content = (List<String>) wrapperSearch.getFieldData(DataArchive.Field.CONTENT, 0);
                List<Integer> contentWordCount = (List<Integer>) wrapperSearch.getFieldData(DataArchive.Field.CONTENT_COUNT, 0);
                PDFData pdfData = new PDFData(id.get(0), content.get(0), contentWordCount.get(0));
                pdfDataList.add(pdfData);
            }

        }
        milvusClient.releaseCollection(
                ReleaseCollectionParam.newBuilder()
                        .withCollectionName(DataArchive.COLLECTION_NAME)
                        .build());
        return pdfDataList;
    }

}
