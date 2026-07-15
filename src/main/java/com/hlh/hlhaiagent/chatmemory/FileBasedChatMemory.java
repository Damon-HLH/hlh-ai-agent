package com.hlh.hlhaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件-based聊天记忆
 * 基于文件持久化的对话记忆存储库
 * 使用 Kryo 序列化存储聊天记录到文件
 */
public class FileBasedChatMemory implements ChatMemory {

    private final String BASE_DIR;
    private static final Kryo kryo = new Kryo();

    static{
        //设置不需要手动注册序列化的类
        kryo.setRegistrationRequired(false);
        //设置实例化策略，StdInstantiatorStrategy()是一个实例化策略，用于实例化对象
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    //构造对象时，指定文件保存目录
    public FileBasedChatMemory(String dir) {
        this.BASE_DIR = dir;
        File baseDir = new File(dir);
        if(!baseDir.exists()){
            baseDir.mkdirs();//如果这个文件不存在就创建出来,
        }
    }

    @Override
    public void add(String conversationId, Message message) {
        List<Message> conversationMessage = getOrCreateConversation(conversationId);
        conversationMessage.add(message); //追加单条消息
        saveConversation(conversationId,conversationMessage);
    }

    /**
     * Save the specified messages in the chat memory for the specified conversation.
     * @param conversationId
     * @param messages
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> conversation = getOrCreateConversation(conversationId);
        conversation.addAll(messages);
        saveConversation(conversationId, conversation);
    }

    /**
     * Get the messages in the chat memory for the specified conversation.
     *
     * @param conversationId
     */
    @Override
    public List<Message> get(String conversationId) {
        return getOrCreateConversation(conversationId);
    }

    /**
     * Clear the chat memory for the specified conversation.
     *
     * @param conversationId
     */
    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 获取或创建会话消息的列表
     * @param conversationId
     * @return
     */
    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if (file.exists()) {
            try (Input input = new Input(new FileInputStream(file))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    /**
     * 保存会话消息
     * @param conversationId
     * @param messages
     */
    private void saveConversation(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 每个会话文件单独保存 获取会话文件
     * @param conversationId
     * @return
     */
    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR, conversationId + ".kryo");
    }
}
