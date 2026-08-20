package com.hlh.hlhaiagent.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import com.hlh.hlhaiagent.constant.FileConstant;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;


/**
 * PDF 生成工具
 */
public class PDFGenerationTool {

    private static final Logger log = LoggerFactory.getLogger(PDFGenerationTool.class);

    // Markdown标题正则
    private static final Pattern HEADER_PATTERN = Pattern.compile("^(#{1,6})\\s+(.+)$");

    // Markdown图片正则
    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[(.*?)\\]\\((.*?)\\)");

    // 非BMP字符（如emoji）正则：STSong等CMap字体编码器只支持BMP，且中文字体一般不含emoji，需提前清洗
    private static final Pattern NON_BMP_PATTERN = Pattern.compile("[\\x{10000}-\\x{10FFFF}]");

    // 变体选择符、零宽字符等不可见字符正则
    private static final Pattern INVISIBLE_CHAR_PATTERN = Pattern.compile("[\\uFE00-\\uFE0F\\u200B-\\u200D\\uFEFF]");

    // 内置中文字体的classpath资源路径（随jar包打包，任何部署环境都保证存在，避免依赖系统字体）
    // 使用开源免费（SIL OFL许可，可商用可再分发）的思源黑体CN子集版，字体文件位于 src/main/resources/fonts 目录
    // 来源：https://github.com/adobe-fonts/source-han-sans/releases 的 SubsetOTF/CN 简体中文子集包
    private static final String EMBEDDED_FONT_PATH = "fonts/SourceHanSansCN-Regular.otf";

    // 内置粗体字体资源路径（用于标题加粗，缺失时回退用常规字体）
    private static final String EMBEDDED_BOLD_FONT_PATH = "fonts/SourceHanSansCN-Bold.otf";

    // 字体文件字节缓存（避免每次生成PDF都重复读取字体资源）
    private static final Map<String, byte[]> FONT_BYTES_CACHE = new ConcurrentHashMap<>();

    // 标记中文字体是否成功加载
    private static boolean chineseFontLoaded = false;

    // 系统字体候选路径（内置字体缺失时的回退方案；注意微软雅黑/宋体有版权，不可随应用分发，仅可运行时引用）
    private static final String[] CJK_FONT_CANDIDATES = {
            "C:/Windows/Fonts/msyh.ttc,0",
            "C:/Windows/Fonts/simsun.ttc,0",
            "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc,0",
            "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc,0"
    };

    // 粗体字体候选路径（用于标题加粗，提升美观度）
    private static final String[] CJK_BOLD_FONT_CANDIDATES = {
            "C:/Windows/Fonts/msyhbd.ttc,0",
            "C:/Windows/Fonts/simhei.ttf"
    };

    // 下载图片时的浏览器User-Agent（Pexels等CDN会拒绝JDK默认的Java UA）
    private static final String BROWSER_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";

    // 图片下载超时时间（毫秒）
    private static final int IMAGE_DOWNLOAD_TIMEOUT = 15000;

    @Tool(description = "Generate a beautifully formatted PDF file from markdown-like content. " +
            "Supports headers with # symbols and images with markdown syntax ![](url). " +
            "IMPORTANT: Unless the user explicitly requests another language, the PDF content MUST be written in Chinese (简体中文). " +
            "IMPORTANT: Do NOT use emoji characters in the content, use plain text instead.", returnDirect = false)
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF, written in Chinese by default. " +
                    "Supports markdown image syntax ![](url) and headers with # symbols. Do not include emoji characters.") String content) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        // 清洗文件名：防止路径穿越与文件系统非法字符（保留中文）
        String safeFileName = FileConstant.sanitizeFileName(fileName, "document_" + System.currentTimeMillis() + ".pdf");
        if (!safeFileName.toLowerCase().endsWith(".pdf")) {
            safeFileName = safeFileName + ".pdf";
        }
        String filePath = fileDir + "/" + safeFileName;
        // 创建目录并校验可写性：生产Linux环境 java 进程用户常对工作目录无写权限，
        // 提前校验并返回可操作的错误信息，避免 PdfWriter 抛出的误导性异常
        try {
            FileConstant.ensureDir(fileDir);
        } catch (Exception e) {
            log.error("PDF输出目录不可用: {}", fileDir, e);
            return "生成PDF时出错: " + e.getMessage();
        }

        // 清洗内容中的emoji等非BMP字符与不可见字符，避免编码异常
        content = sanitizeContent(content);

        // 创建 PdfWriter 和 PdfDocument 对象
        // 用局部变量记录字体警告状态（chineseFontLoaded 是 static 字段，并发场景下不可靠）
        boolean fontWarningNeeded = false;

        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {
            // 加载中文字体（优先内置字体，回退系统字体，最终兜底STSong）
            chineseFontLoaded = false;
            PdfFont font = createCjkFont(EMBEDDED_FONT_PATH, CJK_FONT_CANDIDATES);
            PdfFont boldFont = createCjkFont(EMBEDDED_BOLD_FONT_PATH, CJK_BOLD_FONT_CANDIDATES);
            if (boldFont == null) {
                boldFont = font;
            }
            document.setFont(font);
            document.setFontSize(11f);

            // 如果内容为空，添加默认内容
            if (content == null || content.trim().isEmpty()) {
                Paragraph emptyWarning = new Paragraph("未提供内容，PDF已创建但内容为空。");
                emptyWarning.setFont(font).setFontSize(14);
                document.add(emptyWarning);
            } else {
                // 检查中文字体是否正确加载，如果没有加载，给出提示
                if (!chineseFontLoaded) {
                    fontWarningNeeded = true;
                    Paragraph fontWarning = new Paragraph("注意：无法加载中文字体，文档中的中文可能无法正确显示。");
                    fontWarning.setFont(font).setFontSize(14);
                    document.add(fontWarning);
                    document.add(new Paragraph("\n"));
                }

                // 处理内容
                processContent(content, document, font, boldFont);
            }

        } catch (IOException e) {
            log.error("生成PDF时出错", e);
            String msg = e.getMessage() == null ? "" : e.getMessage();
            // 文件写入失败（权限/路径问题）与字体问题是两类不同问题，提示需区分，
            // 否则线上 Permission denied 会被误报成"请安装中文字体"
            if (msg.contains("Permission denied") || msg.contains("No such file")
                    || msg.contains("Access is denied") || msg.contains("拒绝访问") || msg.contains("找不到")) {
                return "生成PDF时出错: 无法写入文件 " + filePath + "（" + msg + "）。"
                        + "请检查运行 java 的进程用户对该目录的写权限，"
                        + "或在启动参数中通过 -Dhlh.file.save-dir=/可写目录 指定文件保存目录。";
            }
            return "生成PDF时出错: " + msg + "\n请检查是否需要安装中文字体，或将中文字体文件(.ttc/.ttf)复制到resources/fonts/目录下。";
        } catch (Exception e) {
            log.error("PDF生成过程中发生未知错误", e);
            return "PDF生成过程中发生未知错误: " + e.getMessage();
        }

        // try-with-resources 结束后，PdfWriter 已 close 并 flush 缓冲到磁盘，此时校验文件才有效
        if (FileUtil.exist(filePath) && FileUtil.size(new java.io.File(filePath)) > 0) {
            // 创建下载链接
            String pdfFileName = new java.io.File(filePath).getName();
            String downloadUrl = "/api/files/pdf/" + pdfFileName;

            StringBuilder result = new StringBuilder();
            result.append("PDF生成成功！\n");
            result.append("- 文件名: ").append(pdfFileName).append("\n");
            result.append("- 本地路径: ").append(filePath).append("\n");
            result.append("- 下载链接: [点击下载PDF](").append(downloadUrl).append(")\n");

            if (fontWarningNeeded) {
                result.append("\n 注意：未能加载中文字体，PDF中的中文可能无法正确显示。");
            }

            return result.toString();
        } else {
            return "PDF文件创建失败或为空文件: " + filePath;
        }
    }

    /**
     * 清洗内容：移除非BMP字符（如emoji）与不可见字符，保证内容完整写入PDF
     */
    private String sanitizeContent(String content) {
        if (content == null) {
            return "";
        }
        String sanitized = NON_BMP_PATTERN.matcher(content).replaceAll("");
        sanitized = INVISIBLE_CHAR_PATTERN.matcher(sanitized).replaceAll("");
        if (sanitized.length() != content.length()) {
            log.warn("PDF内容中包含emoji或不可见字符，已自动移除以保证生成成功");
        }
        return sanitized;
    }

    /**
     * 加载中文字体，优先级：
     * 1. 应用内置字体（classpath资源，随jar打包，任何部署环境都可用）
     * 2. 系统字体候选路径（Windows/Linux常见中文字体）
     * 3. itext-asian内置的STSong字体（兜底，仅支持BMP字符）
     * 均使用IDENTITY_H编码（内置/系统字体），支持完整BMP字符
     */
    private PdfFont createCjkFont(String embeddedResourcePath, String[] candidates) {
        // 1. 优先加载内置字体，保证上线环境的确定性
        byte[] fontBytes = loadFontResource(embeddedResourcePath);
        if (fontBytes != null) {
            try {
                PdfFont font = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                chineseFontLoaded = true;
                return font;
            } catch (Exception e) {
                log.warn("加载内置字体失败，尝试系统字体: {}", embeddedResourcePath, e);
            }
        }
        // 2. 回退：按候选路径加载系统字体
        for (String candidate : candidates) {
            // ttc路径带有",0"后缀，取逗号前的部分判断文件是否存在
            String fontFile = candidate.contains(",") ? candidate.substring(0, candidate.indexOf(',')) : candidate;
            if (!new File(fontFile).exists()) {
                continue;
            }
            try {
                PdfFont font = PdfFontFactory.createFont(candidate, PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                chineseFontLoaded = true;
                return font;
            } catch (Exception e) {
                log.warn("加载字体失败，尝试下一个候选字体: {}", candidate, e);
            }
        }
        // 3. 兜底：使用itext-asian提供的内置中文字体（仅支持BMP字符，emoji已被预先清洗）
        try {
            log.warn("内置字体与系统字体均不可用，回退到STSongStd-Light内置CMap字体");
            chineseFontLoaded = true;
            return PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
        } catch (IOException e) {
            throw new IllegalStateException("没有可用的中文字体", e);
        }
    }

    /**
     * 从classpath读取字体文件字节（带缓存）；资源不存在时返回null
     */
    private static byte[] loadFontResource(String resourcePath) {
        return FONT_BYTES_CACHE.computeIfAbsent(resourcePath, path -> {
            try (InputStream in = PDFGenerationTool.class.getClassLoader().getResourceAsStream(path)) {
                if (in == null) {
                    log.warn("未找到内置字体资源: {}，请将思源黑体CN子集字体文件放入 src/main/resources/fonts 目录", path);
                    return null;
                }
                return in.readAllBytes();
            } catch (IOException e) {
                log.warn("读取内置字体资源失败: {}", path, e);
                return null;
            }
        });
    }

    /**
     * 处理内容
     */
    private void processContent(String content, Document document, PdfFont font, PdfFont boldFont) {
        String[] lines = content.split("\n");
        StringBuilder textBuffer = new StringBuilder();

        for (String line : lines) {
            Matcher headerMatcher = HEADER_PATTERN.matcher(line);
            if (headerMatcher.matches()) {
                if (textBuffer.length() > 0) {
                    processTextWithImages(textBuffer.toString(), document);
                    textBuffer.setLength(0);
                }

                String headerMarker = headerMatcher.group(1);
                String headerText = headerMatcher.group(2);
                addHeader(document, headerText, headerMarker.length(), font, boldFont);
                continue;
            }

            textBuffer.append(line).append("\n");
        }

        if (textBuffer.length() > 0) {
            processTextWithImages(textBuffer.toString(), document);
        }
    }

    /**
     * 添加标题（使用粗体字体并按级别设置字号，提升排版美观度）
     */
    private void addHeader(Document document, String headerText, int level, PdfFont font, PdfFont boldFont) {
        Paragraph header = new Paragraph(headerText);

        float fontSize = switch (level) {
            case 1 -> 20f;
            case 2 -> 17f;
            case 3 -> 14f;
            case 4 -> 12f;
            default -> 10f;
        };

        TextAlignment alignment = (level == 1) ? TextAlignment.CENTER : TextAlignment.LEFT;

        header.setFont(boldFont != null ? boldFont : font)
                .setFontSize(fontSize)
                .setTextAlignment(alignment)
                .setMarginTop(8f)
                .setMarginBottom(4f);

        document.add(header);
    }

    /**
     * 处理包含图片的文本
     */
    private void processTextWithImages(String content, Document document) {
        Matcher matcher = IMAGE_PATTERN.matcher(content);

        int lastEnd = 0;

        while (matcher.find()) {
            String textBefore = content.substring(lastEnd, matcher.start());
            if (!textBefore.isEmpty()) {
                document.add(new Paragraph(textBefore));
            }

            String imageUrl = matcher.group(2).trim();
            addImage(document, imageUrl);

            lastEnd = matcher.end();
        }

        if (lastEnd < content.length()) {
            document.add(new Paragraph(content.substring(lastEnd)));
        }
    }

    /**
     * 下载并插入图片：带浏览器User-Agent下载（Pexels等CDN会拒绝JDK默认UA），并按页面尺寸等比缩放居中显示
     */
    private void addImage(Document document, String imageUrl) {
        try {
            byte[] imageBytes = downloadImage(imageUrl);
            ImageData imageData = ImageDataFactory.create(imageBytes);
            Image image = new Image(imageData);

            // 限制图片最大宽高（页面宽度的80%、高度的60%），等比缩放并居中，保证排版美观
            float maxWidth = document.getPdfDocument().getDefaultPageSize().getWidth() * 0.8f;
            float maxHeight = document.getPdfDocument().getDefaultPageSize().getHeight() * 0.6f;
            image.scaleToFit(maxWidth, maxHeight);
            image.setTextAlignment(TextAlignment.CENTER);
            image.setMarginTop(6f);
            image.setMarginBottom(6f);

            Paragraph imageParagraph = new Paragraph();
            imageParagraph.setTextAlignment(TextAlignment.CENTER);
            imageParagraph.add(image);
            document.add(imageParagraph);
        } catch (Exception e) {
            log.error("加载图片失败: {}", imageUrl, e);
            document.add(new Paragraph("无法加载图片: " + imageUrl));
        }
    }

    /**
     * 下载图片字节：先用浏览器UA请求，失败后再回退到无UA的直接下载
     */
    private byte[] downloadImage(String imageUrl) throws IOException {
        try (HttpResponse response = HttpRequest.get(imageUrl)
                .header("User-Agent", BROWSER_USER_AGENT)
                .header("Accept", "image/*,*/*;q=0.8")
                .timeout(IMAGE_DOWNLOAD_TIMEOUT)
                .execute()) {
            if (response.isOk()) {
                return response.bodyBytes();
            }
            log.warn("下载图片返回状态码 {}，尝试回退方式下载: {}", response.getStatus(), imageUrl);
        } catch (Exception e) {
            log.warn("带UA下载图片失败，尝试回退方式下载: {}", imageUrl, e);
        }
        // 回退：直接通过URL读取（兼容本地文件等场景）
        return ImageDataFactory.create(new java.net.URL(imageUrl)).getData();
    }
}
