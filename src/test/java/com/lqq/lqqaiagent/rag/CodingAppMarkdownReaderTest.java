package com.lqq.lqqaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CodingAppMarkdownReaderTest {

    @Resource
    private CodingAppMarkdownReader myMarkdownReader;

    @Test
    void loadMarkdowns() {
        myMarkdownReader.loadMarkdown();
    }
}
