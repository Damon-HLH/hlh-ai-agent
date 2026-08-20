package com.hlh.hlhaiagent.tools;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import com.hlh.hlhaiagent.constant.FileConstant;

import cn.hutool.core.io.FileUtil;

/**
 * 文件操作工具类，提供文件读写常用操作
 * 工具类 的方法 返回值都用 String
 *     因为操作完成后，返回给 AI 工具执行完成的结果；不需要类型转换，提高性能
 */
public class FileOperationTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of a file to read") String filename){
        String filePath = FILE_DIR + "/" + filename;
        try {
            return FileUtil.readUtf8String(filePath);
        }catch (Exception e){
            return "Error reading file:" + e.getMessage();
        }
    }

    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = "Name of a file to write") String filename,
                            @ToolParam(description = "Content to write to the file") String content){
        String filePath = FILE_DIR + "/" + filename;
        try {
            // 创建目录并校验可写性，失败时抛出带操作指引的异常
            // （生产Linux环境 java 进程用户常对工作目录无写权限）
            FileConstant.ensureDir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "File written successfully to:" + filename;
        }catch (Exception e){
            return "Error writing file:" + e.getMessage();
        }
    }
}
