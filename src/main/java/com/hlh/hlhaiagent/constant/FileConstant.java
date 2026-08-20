package com.hlh.hlhaiagent.constant;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件常量
 */
public interface FileConstant {

    Logger FILE_LOG = LoggerFactory.getLogger(FileConstant.class);

    /**
     * 文件保存目录（类加载时解析一次）。
     * 解析优先级：
     * 1. JVM 启动参数 -Dhlh.file.save-dir=/path/to/dir
     * 2. 环境变量 HLH_FILE_SAVE_DIR
     * 3. {进程工作目录}/tmp（本地开发默认行为，与旧版本保持一致）
     * 4. 以上目录不可写时自动回退到 {用户主目录}/.hlh-ai-agent/files
     *    （Linux 服务器部署时，java 进程用户经常对 /www/wwwroot 等目录没有写权限）
     */
    String FILE_SAVE_DIR = resolveFileSaveDir();

    /**
     * 解析文件保存目录，保证返回的目录尽量可创建、可写
     */
    static String resolveFileSaveDir() {
        // 优先：JVM 参数 > 环境变量（生产环境推荐显式指定一个可写的数据目录）
        String configured = System.getProperty("hlh.file.save-dir");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("HLH_FILE_SAVE_DIR");
        }
        if (configured != null && !configured.isBlank()) {
            if (isWritableDir(configured)) {
                FILE_LOG.info("使用配置的文件保存目录: {}", configured);
                return configured;
            }
            FILE_LOG.warn("配置的文件保存目录 {} 无法创建或不可写，回退到默认目录", configured);
        }

        // 默认：进程工作目录/tmp（本地开发有完整权限，行为与旧版本一致）
        String defaultDir = System.getProperty("user.dir") + "/tmp";
        if (isWritableDir(defaultDir)) {
            return defaultDir;
        }
        FILE_LOG.warn("默认文件保存目录 {} 不可写（Linux 部署未给 java 进程用户写权限时常见），回退到用户主目录", defaultDir);

        // 兜底：用户主目录通常可写，避免线上出现 Permission denied / No such file or directory
        String fallbackDir = System.getProperty("user.home") + "/.hlh-ai-agent/files";
        if (isWritableDir(fallbackDir)) {
            FILE_LOG.info("文件保存目录已回退到: {}", fallbackDir);
            return fallbackDir;
        }

        // 极端情况：尝试系统临时目录
        String tmpDir = System.getProperty("java.io.tmpdir") + "/hlh-ai-agent-files";
        FILE_LOG.error("未找到可写的文件保存目录，尝试使用系统临时目录: {}", tmpDir);
        return tmpDir;
    }

    /**
     * 判断目录是否可写：不存在则尝试创建，并用探针文件做真实写入校验
     * （FileUtil.mkdir 失败时不抛异常，canWrite() 在部分权限模型下也不可靠）
     */
    static boolean isWritableDir(String dir) {
        try {
            File f = new File(dir);
            if (!f.exists() && !f.mkdirs()) {
                return false;
            }
            if (!f.isDirectory()) {
                return false;
            }
            File probe = new File(f, ".write_probe_" + System.nanoTime());
            if (probe.createNewFile()) {
                probe.delete();
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 确保子目录存在且为目录，失败时抛出带可操作指引的异常，
     * 让工具能把准确的错误原因返回给 AI/用户（而不是莫名奇妙的 Permission denied）
     */
    static File ensureDir(String dir) {
        File f = new File(dir);
        if (!f.exists() && !f.mkdirs()) {
            throw new IllegalStateException("无法创建目录 " + f.getAbsolutePath()
                    + "，请检查运行 java 的进程用户对该路径的写权限，"
                    + "或在启动参数中通过 -Dhlh.file.save-dir=/可写目录 指定文件保存目录");
        }
        if (!f.isDirectory()) {
            throw new IllegalStateException("路径不是目录: " + f.getAbsolutePath());
        }
        return f;
    }

    /**
     * 清洗文件名：去掉路径分隔符（防止路径穿越）、替换文件系统非法字符；
     * 中文字符会保留；为空时返回默认文件名
     */
    static String sanitizeFileName(String fileName, String defaultName) {
        if (fileName == null || fileName.isBlank()) {
            return defaultName;
        }
        String name = fileName.replace('\\', '/');
        int idx = name.lastIndexOf('/');
        if (idx >= 0) {
            name = name.substring(idx + 1);
        }
        name = name.replaceAll("[/:*?\"<>|]", "_").trim();
        if (name.isEmpty() || name.equals("..")) {
            return defaultName;
        }
        return name;
    }
}
