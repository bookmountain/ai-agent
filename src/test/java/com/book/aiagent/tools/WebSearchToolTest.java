package com.book.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class WebSearchToolTest {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Test
    public void searchWeb() {
        WebSearchTool tool = new WebSearchTool(searchApiKey);
        String query = "What is the capital of Australia?";
        String result = tool.searchWeb(query);
        Assertions.assertNotNull(result);
    }
}
 