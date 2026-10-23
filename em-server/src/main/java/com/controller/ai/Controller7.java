package com.controller.ai;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/rag")
public class Controller7 {
    @Resource
    private VectorStore vectorStore;

    @Resource(name = "dashScopeChatClient")
    private ChatClient chatClient;
    /**
     * RAG 顾问：检索 + 上下文增强。
     * Spring AI 1.1.x 已移除 QuestionAnswerAdvisor，改用 RetrievalAugmentationAdvisor 组合。
     */
    private RetrievalAugmentationAdvisor ragAdvisor;

    @PostConstruct
    public void init() {
        // 1. 检索器：把用户问题转向量，从 Redis 向量库取 topK=4、相似度>=0.4 的文档
        VectorStoreDocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(4)
                .similarityThreshold(0.4)
                .build();

        // 2. 上下文增强器：用中文模板把检索到的文档拼进用户问题
        PromptTemplate promptTemplate = new PromptTemplate("""
							你是一个严谨的知识库问答助手，请只根据下面提供的【知识库内容】回答用户问题。
							要求：
							1. 答案必须完全基于知识库内容，不要编造知识库中不存在的信息。
							2. 不要说“根据上下文”“根据提供的资料”这类话，直接自然地回答。
							3. 如果知识库内容不足以回答问题，请直接回答“抱歉，知识库中没有找到相关信息”。
			
							知识库内容：
							-----
							{context}
							-----
			
							用户问题：{query}
							""");

        PromptTemplate emptyContextTemplate = new PromptTemplate(
                "用户的问题是：{query}。知识库中没有检索到任何相关内容，请直接回答“抱歉，知识库中没有找到相关信息”。");

        ContextualQueryAugmenter augmenter = ContextualQueryAugmenter.builder()
                .promptTemplate(promptTemplate)
                .emptyContextPromptTemplate(emptyContextTemplate)
                .allowEmptyContext(false)
                .build();

        // 3. 组装 RAG Advisor
        this.ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .queryAugmenter(augmenter)
                .build();

    }

    /**
     * 导入内置示例知识库：长文本先切片再 embedding 入库 没做冥等处理
     */
    @GetMapping("/rag/load")
    public Map<String, Object> loadKnowledge() {
        List<Document> documents = List.of(
                Document.builder()
                        .text("""
											招商服务平台是面向地方招商引资工作的一体化数字化系统，主要功能包括：外资企业信息管理、
											本地项目信息管理、中介机构管理、招商审批流程管理、数据字典与权限管理。
											平台采用 Spring Boot 3 + Vue 3 前后端分离架构，后端使用 MySQL 存储业务数据。
											""")
                        .metadata("source", "平台介绍")
                        .build(),
                Document.builder()
                        .text("""
											外资企业信息管理模块用于登记和维护在本地区投资的外资企业档案，包含企业名称、所属国家地区、
											投资行业、注册资本、联系人等字段；本地项目信息管理模块用于跟踪本地招商项目从对接到落地的全过程。
											""")
                        .metadata("source", "业务模块")
                        .build(),
                Document.builder()
                        .text("""
											中介机构管理模块维护为招商工作提供服务的中介代理机构信息，并支持对机构进行评级。
											审批流程模块支持自定义审批节点，一条招商项目可依次经过经办人、部门负责人、分管领导等多级审批，
											审批记录全程留痕，可随时查看审批进度。
											""")
                        .metadata("source", "业务模块")
                        .build(),
                Document.builder()
                        .text("""
											平台的技术支持热线是 400-888-6666，服务时间为工作日 9:00-18:00。
											系统管理员账号由各单位信息化部门统一开通，初始密码首次登录后必须修改，
											忘记密码请联系管理员重置，不支持自助找回。
											""")
                        .metadata("source", "使用帮助")
                        .build());


        // 长文档按 Token 切片，避免单段过长影响检索精度
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(400)
                .withMinChunkSizeChars(100)
                .build();

        List<Document> chunks = splitter.apply(documents);
        vectorStore.add(chunks);

        return Map.of("sourceDocs", documents.size(), "chunksAdded", chunks.size());

    }


    /**
     * 导入单条自定义知识文本
     */
    @GetMapping("/rag/add")
    public Map<String, Object> addKnowledge(@RequestParam("text") String text) {
        Document document = Document.builder()
                .text(text)
                .metadata("source", "manual")
                .build();
        List<Document> chunks = new TokenTextSplitter().apply(List.of(document));
        vectorStore.add(chunks);
        return Map.of("chunksAdded", chunks.size());
    }

    @GetMapping("/rag/search")
    public List<Map<String, Object>> search(@RequestParam("msg") String msg) {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder()
                .query(msg)
                .topK(4)
                .similarityThreshold(0.4)
                .build());

        return results.stream()
                .map(doc -> Map.<String, Object>of(
                        "id", doc.getId(),
                        "text", doc.getText() == null ? "" : doc.getText(),
                        "score", doc.getScore() == null ? 0.0 : doc.getScore(),
                        "source", doc.getMetadata().getOrDefault("source", "")))
                .toList();
    }

    /**
     * RAG 问答：检索知识库片段作为上下文，再让大模型基于知识库回答
     */
    @GetMapping("/rag/ask")
    public Map<String, Object> ask(@RequestParam("msg") String msg) {
        String answer = chatClient.prompt()
                .user(msg)
                .advisors(ragAdvisor)
                .call()
                .content();

        return Map.of(
                "question", msg,
                "mode", "RAG",
                "answer", answer == null ? "" : answer
        );
    }

    @GetMapping("/rag/ask-direct")
    public Map<String, Object> askDirect(@RequestParam("msg") String msg) {
        String answer = chatClient.prompt()
                .user(msg)
                .call()
                .content();
        return Map.of("question", msg, "mode", "DIRECT", "answer", answer == null ? "" : answer);
    }

    @GetMapping("/rag/init-file")
    public Map<String, Object> initKnowledgeFromFile() throws Exception {
        String classpathLocation = "rag-storage/zhaoshang-knowledge-base.txt";
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        if (!resource.exists()) {
            throw new IllegalStateException("知识库文件不存在: classpath:" + classpathLocation);
        }

        String content = resource.getContentAsString(StandardCharsets.UTF_8);
        Document document = Document.builder()
                .text(content)
                .metadata("source", "zhaoshang-knowledge-base.txt")
                .build();

        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(400)
                .withMinChunkSizeChars(100)
                .build();
        List<Document> chunks = splitter.apply(List.of(document));

        // 固定 ID：文件内容不变时切片结果稳定，重复初始化覆盖旧数据而不是产生重复文档
        List<Document> chunksWithFixedId = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            chunksWithFixedId.add(chunks.get(i).mutate().id("zhaoshang-kb-" + i).build());
        }

        vectorStore.add(chunksWithFixedId);
        return Map.of(
                "file", classpathLocation,
                "chunksAdded", chunksWithFixedId.size(),
                "chars", content.length());
    }




}


