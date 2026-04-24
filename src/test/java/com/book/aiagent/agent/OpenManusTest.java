package com.book.aiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OpenManusTest {

    @Resource
    private OpenManus openManus;

    @Test
    void run() {
        String userPrompt = """
                My partner lives in Adelaide. Please help me find suitable date spots within 5 km,
                and create a detailed dating plan that includes some web images,
                then output it in PDF format.""";
        String answer = openManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}
