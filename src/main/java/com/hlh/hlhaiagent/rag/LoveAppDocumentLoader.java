package com.hlh.hlhaiagent.rag;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 恋爱大师应用文档加载器
 * 负责读取所有的 markdown 文件并转换成 Document 列表
 */
@Component
@Slf4j
public class LoveAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public List<Document> loadMarkDowns(){
        List<Document> allDocuments  = new ArrayList<>();
        // 加载多篇 Markdown 文档，并切分成不同的文档 chunks
        try{
            Resource[] resources = resourcePatternResolver.getResources("classpath*:document/*.md");
            for (Resource resource : resources){
                String filename = resource.getFilename();
                // 把每个md文件的内容拆分成了不同的文档 chunks
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                                                            .withHorizontalRuleCreateDocument(true)
                                                            .withIncludeCodeBlock(false)
                                                            .withIncludeBlockquote(false)
                                                            .withAdditionalMetadata("filename", filename) //添加标题为文档的元信息，便于后续按照文件名搜索
                                                            .build();
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(markdownDocumentReader.get()); //放入被切分好的所有文档
            }
        }catch (IOException e) {
            log.error("markdown 文件加载失败", e);
            throw new RuntimeException(e);
        }
        return allDocuments;
    }
}
