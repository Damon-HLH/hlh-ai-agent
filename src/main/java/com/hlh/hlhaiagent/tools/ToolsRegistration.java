package com.hlh.hlhaiagent.tools;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
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

    // MCP工具回调提供者（它会根据配置文件 mcp-servers.json 自动配置工具到服务中）
//    @Resource
//    private ToolCallbackProvider toolCallbackProvider;

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
        HtmlGenerationTool htmlGenerationTool = new HtmlGenerationTool();

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
                htmlGenerationTool,
                terminateTool
//                toolCallbackProvider.getToolCallbacks()  //应该也可以直接将MCP服务中的工具提取出来当做工具，即可实现支持MCP协议的超级智能体
        );
    }
}

