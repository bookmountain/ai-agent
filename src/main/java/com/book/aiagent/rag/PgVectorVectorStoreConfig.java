package com.book.aiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
@ConditionalOnProperty(name = "app.rag.pgvector.enabled", havingValue = "true")
public class PgVectorVectorStoreConfig {
    private static final String DEFAULT_SCHEMA = "public";
    private static final String DEFAULT_TABLE = "vector_store";

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Value("${spring.ai.vectorstore.pgvector.max-document-batch-size}")
    private int maxDocumentBatchSize;
    @Value("${spring.ai.vectorstore.pgvector.dimensions}")
    private int dimensions;

    @Value("${app.rag.pgvector.initialize-schema:true}")
    private boolean initializeSchema;

    @Value("${app.rag.pgvector.seed-on-startup:false}")
    private boolean seedOnStartup;

    @Value("${app.rag.pgvector.seed-if-empty-only:true}")
    private boolean seedIfEmptyOnly;

    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate,
                                           @Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel) {
        PgVectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(dimensions)              // Required: dimensions of the embedding vectors
                .distanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                .indexType(HNSW)                     // Optional: defaults to HNSW
                .initializeSchema(initializeSchema)  // Optional: defaults to false
                .schemaName(DEFAULT_SCHEMA)          // Optional: defaults to "public"
                .vectorTableName(DEFAULT_TABLE)      // Optional: defaults to "vector_store"
                .batchingStrategy(this::batchDocumentsForEmbedding)
                .maxDocumentBatchSize(maxDocumentBatchSize)
                .build();

        vectorStore.afterPropertiesSet();
        if (shouldSeedOnStartup(jdbcTemplate)) {
            List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
            vectorStore.add(documents);
        }
        return vectorStore;
    }

    private boolean shouldSeedOnStartup(JdbcTemplate jdbcTemplate) {
        if (!seedOnStartup) {
            return false;
        }
        if (!seedIfEmptyOnly) {
            return true;
        }
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + DEFAULT_SCHEMA + "." + DEFAULT_TABLE,
                Long.class
        );
        return count == null || count == 0;
    }

    private List<List<Document>> batchDocumentsForEmbedding(List<Document> documents) {
        List<List<Document>> batches = new ArrayList<>();
        for (int i = 0; i < documents.size(); i += maxDocumentBatchSize) {
            int end = Math.min(i + maxDocumentBatchSize, documents.size());
            batches.add(new ArrayList<>(documents.subList(i, end)));
        }
        return batches;
    }
}
