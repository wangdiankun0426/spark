package com.spark.test.llm;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.spark.llm.model.ModelFactory;
import com.spark.llm.store.ESVectorStore;
import com.spark.llm.utils.ChunkUtil;
import com.spark.prompt.PromptTemplateLoader;
import com.spark.llm.IAgent;
import com.spark.utils.JsonUtil;
import com.spark.utils.TextUtil;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.*;

import java.io.IOException;
import java.util.*;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/9/11 10:34
 */
@SpringBootTest
public class LlmTest {
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Test
    public void test() {
        String input = "李威怎么都没想到，自己上任红山县委书记的第一天就被人算计了。";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("model", "text-embedding-v4");
        paramMap.put("input", input);
        paramMap.put("dimension", "1024");
        paramMap.put("encoding_format", "float");
        List<Float> embedding = null;
        try {
            String body = HttpRequest.post("https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings")
                    .header("Authorization", "Bearer sk-c735eaab50af4802a6a9c45d4e7d6ed4")
                    .header("Content-Type", "application/json")
                    .body(JsonUtil.toString(paramMap))
                    .execute()
                    .body();
            JSONObject jsonObject = JSONUtil.parseObj(body);
            JSONArray data = jsonObject.getJSONArray("data");
            JSONObject result = data.get(0, JSONObject.class, Boolean.TRUE);
            embedding = result.getBeanList("embedding", Float.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(embedding);

        Map data = new HashMap();
        data.put("content", input);
        data.put("embedding", embedding);
        // 使用 IndexQuery 设置 ID 和文档内容
        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(UUID.randomUUID().toString())
                .withSource(JsonUtil.toString(data))
                .build();
        String index = elasticsearchOperations.index(indexQuery, IndexCoordinates.of("spark-store"));
    }

    @Autowired
    private ElasticsearchClient client;

    @Test
    public void test1() throws IOException {
        String input = "红山县委书记是谁？";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("model", "text-embedding-v4");
        paramMap.put("input", input);
        paramMap.put("dimension", "1024");
        paramMap.put("encoding_format", "float");
        List<Float> embedding = null;
        try {
            String body = HttpRequest.post("https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings")
                    .header("Authorization", "Bearer sk-c735eaab50af4802a6a9c45d4e7d6ed4")
                    .header("Content-Type", "application/json")
                    .body(JsonUtil.toString(paramMap))
                    .execute()
                    .body();
            JSONObject jsonObject = JSONUtil.parseObj(body);
            JSONArray data = jsonObject.getJSONArray("data");
            JSONObject result = data.get(0, JSONObject.class, Boolean.TRUE);
            embedding = result.getBeanList("embedding", Float.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(embedding);

        List<Float> finalEmbedding = embedding;
        SearchResponse<Map> response = client.search(s -> s
                        .index("spark-store")
                        .knn(k -> k
                                .field("embedding")
                                .queryVector(finalEmbedding)
                                .k(10)
                                .numCandidates(200)
                        ),
                Map.class
        );
        List<String> list = response.hits().hits().stream()
                .map(hit -> {
                    Map map = hit.source();
                    return (String) map.get("content");
                }).toList();
        System.out.println(list);

    }

    @Autowired
    private ModelFactory modelFactory;
    @Autowired
    private ESVectorStore vectorStore;

    @Test
    public void test2() {
        String content = "李威怎么都没想到，自己上任红山县委书记的第一天，就被人算计了。" +
                "清晨的阳光洒在县委大院斑驳的梧桐树影上，空气中还带着秋日特有的凉意。李威一身素色夹克，肩背一个旧帆布包，步行穿过大院门口那两排迎宾的彩旗。没有警车开道，没有锣鼓喧天，他刻意低调而来，只想用最朴素的方式开启人生新的一页。" +
                "三十八岁，省委最年轻的地厅级干部，从省发改委副主任空降为红山县委书记，这本是一次被寄予厚望的“淬炼”。红山，资源丰富却发展滞后，民怨积深，是全省出了名的“硬骨头”。省委派他来，既是信任，也是考验。" +
                "可就在他踏进县委办公楼一楼大厅的那一刻，一张照片如利刃般刺入他的眼帘——" +
                "大厅电子屏正循环播放“欢迎新书记莅临指导”的PPT，背景是红山万亩茶园的航拍图，配乐激昂。可就在第三页，一张合影赫然在列：前县委书记周德海与县政协主席、常务副县长等人在某山庄举杯畅饮，而照片角落，竟清晰地映出一名身着暴露的年轻女子，手中酒杯正与周德海相碰。" +
                "更令人窒息的是，这张照片下方，赫然写着一行小字：“2023年乡村振兴茶产业调研座谈会”。" +
                "李威脚步一顿，脸色骤沉。" +
                "这不是欢迎，是陷阱。" +
                "他知道周德海虽已调离，但在红山经营十二年，门生故吏遍布要害部门。这张照片，绝非偶然上传。它是警告，是羞辱，更是赤裸裸的挑衅——你李威不是清廉自持吗？那你敢不敢动这张照片背后的人？" +
                "“小张！”李威转身叫住随行的县委办秘书，“谁负责今天的大屏内容？”" +
                "“是……是宣传部王部长安排的。”小张低声回答，额角渗出冷汗。" +
                "李威没再说话。他径直走向电梯，按下九楼。他知道，真正的战斗，从这一刻就已经打响。";
//        ResultData<String> fromText = TextUtil.getFromText("C:\\Users\\w1561\\Desktop\\demo.txt", true);
//        String content = fromText.getData();
        List<String> chunkList = ChunkUtil.handleChunk(content, null, null);
        System.out.println(chunkList.size());
        for (String chunk : chunkList) {
            System.out.println(chunk);
            Map<String, Object> variables = Map.of("document", chunk);
            Prompt prompt = PromptTemplateLoader.create("prompts/qa-prompt.txt", variables);
            String chat = modelFactory.getDefaultChatModel().chat(prompt.text());
            System.out.println(chat);
        }
//        String question = "李威上任的时候穿的什么衣服？";
//        Response<Embedding> response = modelFactory.getDefaultEmbeddingModel().embed(question);
//        List<String> relevantDocs = vectorStore.search(response.content(), 2);
//        String context = String.join("\n", relevantDocs);
//        Map<String, Object> variables = Map.of("document", context);
//        Prompt prompt = PromptTemplateLoader.create("prompts/qa-prompt.txt", variables);
////        System.out.println(prompt);
//        String chat = modelFactory.getDefaultChatModel().chat(prompt.text());
//        System.out.println(chat);
    }

    @Test
    public void testAgent() {
        String question = "今天济南的天气怎么样？";
        IAgent agent = AiServices.builder(IAgent.class)
                .chatModel(modelFactory.getDefaultChatModel())
                .tools(new MultiplyTool())
                .build();
        String reply = agent.chat("test-llm-agent", question);
        System.out.println(reply);
    }

    public class MultiplyTool {
        @Tool(name = "getWeather", value = "查询某个城市的天气情况")
        public String getWeather(String city) {
            return city+"今天天气多云转晴，温度-20到-10摄氏度之间";
        }
    }
}
