package com.book.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class WebScrapingToolTest {

    @Test
    public void scrapeWebPage() {
        WebScrapingTool tool = new WebScrapingTool();
        String url = "https://github.com/bookmountain";
        String result = tool.scrapeWebPage(url);
        Assertions.assertNotNull(result);
    }
}
