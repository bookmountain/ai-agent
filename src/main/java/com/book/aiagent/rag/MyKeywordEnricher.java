package com.book.aiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MyKeywordEnricher {

    private final ChatModel openAiChatModel;

    public MyKeywordEnricher(@Qualifier("openAiChatModel") ChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    public List<Document> enrichDocuments(List<Document> documents) {
        KeywordMetadataEnricher keywordMetadataEnricher = new KeywordMetadataEnricher(openAiChatModel, 5);
        List<Document> enrichedDocuments = new ArrayList<>(documents.size());
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            log.info("Enriching keywords for document {}/{}: {}", i + 1, documents.size(), document.getId());
            try {
                keywordMetadataEnricher.apply(List.of(document));
                log.info("Enriched document {} with keywords: {}", document.getId(),
                        document.getMetadata().get("excerpt_keywords"));
                enrichedDocuments.add(document);
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to enrich document " + document.getId(), e);
            }
        }
        return enrichedDocuments;
    }
}
