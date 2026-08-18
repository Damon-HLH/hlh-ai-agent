package com.hlh.hlhaiagent.tools;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;


/**
 * Google SerpApi 搜索工具
 * 提供基于 SerpApi 的 Google 网络搜索功能
 */
public class GoogleWebSearchTool {

    private static final Logger log = LoggerFactory.getLogger(GoogleWebSearchTool.class);

    private final String serpApiKey;

    // 搜索调用次数限制
    private static final int MAX_SEARCH_CALLS = 3;

    // 当前会话中的搜索调用次数
    private static int searchCallCount = 0;

    private static final String SEARCH_API_URL = "https://serpapi.com/search.json";

    public GoogleWebSearchTool(String serpApiKey) {
        this.serpApiKey = serpApiKey;
    }


    /**
     * 获取剩余的搜索调用次数
     */
    public static int getRemainingSearchCalls() {
        return Math.max(0, MAX_SEARCH_CALLS - searchCallCount);
    }

    /**
     * 重置搜索调用次数计数器
     */
    public static void resetSearchCallCount() {
        searchCallCount = 0;
        log.info("搜索调用次数计数器已重置");
    }

    /**
     * 执行 Google 网络搜索
     *
     * @param searchQuery 搜索内容
     * @return 搜索结果摘要列表
     */
    @Tool(description = "使用 SerpApi 提供的 Google 搜索功能进行网络搜索 (Limited to 3 calls per session)")
    public String googleSearch(
            @ToolParam(description = "搜索内容")
            String searchQuery) {
        log.info("调用 SerpApi Google 搜索关键词：{}", searchQuery);
        // 检查调用次数限制
        if (searchCallCount >= MAX_SEARCH_CALLS) {
            return "搜索次数已达到限制（" + MAX_SEARCH_CALLS + "次），无法继续使用搜索工具";
        }

        // 增加调用计数
        searchCallCount++;
        log.info("执行搜索查询: '{}' (调用 {}/{})", searchQuery, searchCallCount, MAX_SEARCH_CALLS);

        try {
            // 1. 构建请求 URL（使用 GET 查询参数）
            String url = SEARCH_API_URL + "?engine=google"
                    + "&q=" + java.net.URLEncoder.encode(searchQuery, "UTF-8")
                    + "&location=China"
                    + "&hl=zh-cn"
                    + "&gl=cn"
                    + "&google_domain=google.com"
                    + "&api_key=" + serpApiKey;
            log.info("请求 URL：{}", url);

            // 2. 发送 GET 请求
            HttpResponse response = HttpRequest.get(url).execute();

            // 3. 获取响应状态码和内容
            int status = response.getStatus();
            String body = response.body();

            if (status != 200 || ObjectUtil.isEmpty(body)) {
                log.error("请求失败，状态码：{}，响应内容：{}", status, body);
                return "请求失败或无返回内容（状态码: " + status + "）";
            }

            JSONObject jsonResponse = JSONUtil.parseObj(body);

            // 检查API是否返回错误
            if (jsonResponse.containsKey("error")) {
                String errorMsg = jsonResponse.getStr("error", "Unknown API error");
                log.warn("搜索API返回错误: {}", errorMsg);
                return "搜索API返回错误: " + errorMsg;
            }

            // 获取 organic_results（谷歌自然搜索结果），添加空检查
            if (!jsonResponse.containsKey("organic_results") || jsonResponse.isNull("organic_results")) {
                log.warn("搜索结果中没有 organic_results 字段或为空");
                return "未找到相关结果";
            }

            JSONArray resultsArray = jsonResponse.getJSONArray("organic_results");
            if (resultsArray == null || resultsArray.isEmpty()) {
                log.info("搜索结果为空数组");
                return "没有找到与 '" + searchQuery + "' 相关的搜索结果";
            }

            // 格式化搜索结果为更友好的格式
            return formatSearchResults(resultsArray, searchQuery);

        } catch (Exception e) {
            log.error("调用 SerpApi Google 搜索服务时发生错误", e);
            return "搜索时发生错误: " + e.getMessage();
        }
    }

    /**
     * 格式化搜索结果为更友好的输出格式
     */
    private String formatSearchResults(JSONArray results, String query) {
        StringBuilder sb = new StringBuilder();
        sb.append("搜索 '").append(query).append("' 结果 (剩余调用次数: ")
                .append(MAX_SEARCH_CALLS - searchCallCount).append("):\n\n");

        int count = Math.min(results.size(), 5);
        for (int i = 0; i < count; i++) {
            JSONObject result = results.getJSONObject(i);

            sb.append(i + 1).append(". ");

            // 标题
            if (result.containsKey("title")) {
                sb.append(result.getStr("title")).append("\n");
            }

            // 链接
            if (result.containsKey("link")) {
                sb.append("链接: ").append(result.getStr("link")).append("\n");
            }

            // 摘要
            if (result.containsKey("snippet")) {
                sb.append("摘要: ").append(ObjectUtil.defaultIfNull(result.getStr("snippet"), "无摘要信息")).append("\n");
            }

            // 日期（如果有）
            if (result.containsKey("date")) {
                sb.append("日期: ").append(result.getStr("date")).append("\n");
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}
