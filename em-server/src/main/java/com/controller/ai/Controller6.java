package com.controller.ai;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/demo")
public class Controller6 {
    @Resource(name="dashscopeEmbeddingModel")  //注意 sparingai alibaba 在这里留了个深坑,这个名字不要写成 dashScopeEmbeddingModel
    private EmbeddingModel embedModel;

    @Resource
    private VectorStore vectorStore;

    @GetMapping("/text2embed")
    public EmbeddingResponse text2Embed(String msg) {
        EmbeddingResponse resp = embedModel.call(new EmbeddingRequest(List.of(msg), DashScopeEmbeddingOptions.builder().build()));
        return resp;
    }

    @GetMapping("/embed2vector/add")
    public Map<String, Object> add() {
        List<Document> documents=List.of(
                Document.builder()
                        .text("大语言模型（LLM）是基于 Transformer 架构的深度学习模型，能够理解和生成自然语言。")
                        .metadata("category", "ai")
                        .build(),

                Document.builder()
                        .text("向量数据库是专门用于存储和检索高维向量的数据库，常用于语义搜索和 RAG 检索增强生成。")
                        .metadata("category", "database")
                        .build(),

                Document.builder()
                        .text("文本嵌入（Embedding）可以把文本转换成固定维度的浮点向量，语义相近的文本在向量空间中距离更近。")
                        .metadata("category", "embedding")
                        .build(),

                Document.builder()
                        .text("Redis Stack 内置 RediSearch 模块，支持使用 HNSW 算法对向量做 KNN 相似度检索。")
                        .metadata("category", "redis")
                        .build());


        vectorStore.add(documents);

        List<String> idList = documents.stream().map(Document::getId).toList();

        return Map.of("添加了",idList.size() ,"idList",idList);

    }



    /**
     * 将任意文本向量化后写入 Redis 向量数据库。
     * 示例：http://localhost:8080/ai/demo/embed2vector/add-text?msg=这是一条自定义的知识库内容
     */
    @GetMapping("/embed2vector/add-text")
    public Map<String, Object> addText(@RequestParam("msg") String msg) {
        Document document = Document.builder()
                .text(msg)
                .metadata("source", "manual")
                .build();
        vectorStore.add(List.of(document));
        return Map.of("id", document.getId(), "text", document.getText());
    }

    /**
     * 从 Redis 向量数据库做相似度检索：把查询文本转向量，再 KNN(近邻查询)查询最相近的文档。
     */
    @GetMapping("/embed2vector/get")
    public List<Map<String, Object>> getAll(@RequestParam(name = "msg") String msg) {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder()
                .query(msg)
                .topK(3)
                .similarityThreshold(0.4)
                .build()
        );


        return results.stream().map(
                        doc ->
                                Map.<String, Object>of(
                                        "id", doc.getId(),
                                        "text", doc.getText() == null ? "" : doc.getText(),
                                        "score", doc.getScore() == null ? 0.0 : doc.getScore(),
                                        "metadata", doc.getMetadata())
                )

                .toList();
    }


}
