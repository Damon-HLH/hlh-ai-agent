package com.hlh.hlhaiagent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 集中的工具注册类
 */
@Configuration
public class ToolsRegistration {

    @Value("${tools.search.searchapi.api-key}")
    private String searchApiKey;

    @Value("${tools.search.google.api-key}")
    private String googleApiKey;

    @Value("${tools.pexels.api-key}")
    private String pexelsApiKey;

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        GoogleWebSearchTool googleWebSearchTool = new GoogleWebSearchTool(googleApiKey);
        DateTimeTool dateTimeTool = new DateTimeTool();
        EmailSendingTool emailSendingTool = new EmailSendingTool();
        TerminateTool terminateTool = new TerminateTool();
        ImageSearchTool imageSearchTool = new ImageSearchTool(pexelsApiKey);

        return ToolCallbacks.from(
                fileOperationTool,
//            webSearchTool,  用谷歌搜索
                webScrapingTool,
                resourceDownloadTool,
                terminalOperationTool,
                googleWebSearchTool,
                pdfGenerationTool,
                dateTimeTool,
                emailSendingTool,
                imageSearchTool,
                terminateTool
        );
    }
}

