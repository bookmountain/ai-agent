package com.book.aiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ResourceDownloadToolTest {

    @Test
    public void downloadResource() {
        ResourceDownloadTool tool = new ResourceDownloadTool();
        String url = "https://avatars.githubusercontent.com/u/57469770?v=4";
        String fileName = "logo.png";
        String result = tool.downloadResource(url, fileName);
        assertNotNull(result);
    }
}
