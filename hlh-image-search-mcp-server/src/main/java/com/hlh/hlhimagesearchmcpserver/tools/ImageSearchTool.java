package com.hlh.hlhimagesearchmcpserver.tools;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImageSearchTool {

    // 替换为你的 Pexels API 密钥（需从官网申请）
    private static final String API_KEY = "你的 Pexels API 密钥";

    // Pexels 常规搜索接口（请以文档为准）
    private static final String API_URL = "https://api.pexels.com/v1/search";

//    @Tool(description = "search image from web")
//    public String searchImage(@ToolParam(description = "Search query keyword") String query) {
//        try {
//            return String.join(",", searchMediumImages(query));
//        } catch (Exception e) {
//            return "Error search image: " + e.getMessage();
//        }
//    }


    @McpTool(description = "Search for images using Pexels API based on a text query. Returns a list of direct image URLs.")
    public String searchImages(
        @McpToolParam(description = "The search query term (e.g., 'nature', 'office')") String query,
        @McpToolParam(description = "Number of results to return (optional, default 10)") Integer perPage) {
        try {
            return String.join(",", searchMediumImages(query,perPage));
        } catch (Exception e) {
            return "Error search image: " + e.getMessage();
        }
    }

     /**
     * 搜索中等尺寸的图片列表
     *
     * @param query
     * @param perPage
     * @return
     */
    public List<String> searchMediumImages(String query, Integer perPage) {
        // 设置请求头（包含API密钥）
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", API_KEY);

        // 设置请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        if (perPage != null && perPage > 0) {
            params.put("per_page", Math.min(perPage, 80));
        }

        // 发送 GET 请求
        String response = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form(params)
                .execute()
                .body();

        // 解析响应JSON（假设响应结构包含"photos"数组，每个元素包含"medium"字段）
        return JSONUtil.parseObj(response)
                .getJSONArray("photos")
                .stream()
                .map(photoObj -> (JSONObject) photoObj)
                .map(photoObj -> photoObj.getJSONObject("src"))
                .map(photo -> photo.getStr("medium"))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }
}

