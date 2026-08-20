package com.hlh.hlhaiagent.tools;

import java.io.File;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import com.hlh.hlhaiagent.constant.FileConstant;

import cn.hutool.http.HttpUtil;

/**
 * 资源下载工具类
 */
public class ResourceDownloadTool {

    /**
     * 从URL下载资源
     */
    @Tool(description = "Download a resource from a given URL")
    public String downloadResource(
            @ToolParam(description = "URL of the resource to download") String url,
            @ToolParam(description = "Name of the file to save the downloaded resource") String fileName) {
        // 下载文件保存目录
        String fileDir = FileConstant.FILE_SAVE_DIR + "/download";
        // 清洗文件名：防止路径穿越与非法字符（保留中文）
        String safeFileName = FileConstant.sanitizeFileName(fileName, "resource_" + System.currentTimeMillis());
        String filePath = fileDir + "/" + safeFileName;

        try {
            // 创建目录并校验可写性（生产Linux环境 java 进程用户常对工作目录无写权限）
            FileConstant.ensureDir(fileDir);
            HttpUtil.downloadFile(url, new File(filePath));
            return "Resource downloaded successfully to: " + filePath;
        } catch (Exception e) {
            return "Error downloading resource: " + e.getMessage();
        }
    }
}

