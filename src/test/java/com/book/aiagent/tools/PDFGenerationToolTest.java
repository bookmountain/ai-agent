package com.book.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PDFGenerationToolTest {

    @Test
    public void generatePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "Book-Resume.pdf";
        String content = "Book is a software engineer with expertise in Java and Spring Boot. He has experience in building scalable applications and is passionate about learning new technologies.";
        String result = tool.generatePDF(fileName, content);
        Assertions.assertNotNull(result);
    }
}
