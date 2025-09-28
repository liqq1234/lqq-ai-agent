package com.lqq.lqqaiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 向量存储配置类
 * 配置RAG功能所需的向量数据库
 */
@Configuration
@Slf4j
public class CodingAppVectorStoreConfig {

    @Resource
    private CodingAppMarkdownReader codingAppMarkdownReader;

    /**
     * 创建向量存储Bean
     * 使用SimpleVectorStore作为内存向量数据库
     *
     * @param dashscopeEmbeddingModel DashScope嵌入模型
     * @return 配置好的向量存储
     */
    @Bean("codingAppVectorStore")  // 指定Bean名称，与Service中的@Resource名称对应
    public VectorStore codingAppVectorStore(@Qualifier("dashscopeEmbeddingModel") EmbeddingModel dashscopeEmbeddingModel) {
        log.info("开始创建向量存储...");

        try {
            // 创建SimpleVectorStore
            SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                    .build();

            // 加载文档
            log.info("加载Markdown文档到向量存储...");
            List<Document> documents = codingAppMarkdownReader.loadMarkdown();

            if (documents != null && !documents.isEmpty()) {
                simpleVectorStore.add(documents);
                log.info("成功加载 {} 个文档到向量存储", documents.size());
            } else {
                log.warn("没有找到文档，向量存储为空");
            }

            return simpleVectorStore;

        } catch (Exception e) {
            log.error("创建向量存储时发生错误: {}", e.getMessage(), e);
            // 返回一个空的向量存储，避免启动失败
            return SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        }
    }

}
