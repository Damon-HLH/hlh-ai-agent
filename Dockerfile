# 使用轻量级 JDK21 运行环境
FROM openjdk:21-slim

# 工作目录
WORKDIR /app

# 复制已经打包好的JAR文件（假设已放在当前目录）
COPY target/hlh-ai-agent-0.0.1-SNAPSHOT.jar app.jar

# 暴露应用端口
EXPOSE 8123

# 文件生成目录（容器内固定数据目录，建议挂载卷持久化：-v /宿主机目录:/data/hlh-ai-agent）
ENV HLH_FILE_SAVE_DIR=/data/hlh-ai-agent \
    LANG=C.UTF-8

# 使用生产环境配置启动应用（UTF-8 编码支持中文文件名）
CMD ["java", "-Dfile.encoding=UTF-8", "-Dsun.jnu.encoding=UTF-8", "-jar", "app.jar", "--spring.profiles.active=prod"]
