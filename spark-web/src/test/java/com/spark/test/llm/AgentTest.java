package com.spark.test.llm;

import com.spark.llm.IAgent;
import com.spark.llm.agent.AgentFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Agent功能测试类
 * 
 * @author wangdiankun
 * @since 2026/05/14
 */
@SpringBootTest
public class AgentTest {
    
    @Autowired
    private AgentFactory agentFactory;
    
    /**
     * 测试基础对话Agent
     */
    @Test
    public void testChatAgent() {
        System.out.println("========== 测试基础对话Agent ==========");
        // 使用Agent ID构建，假设ID为1的Agent存在且已配置语言模型
        IAgent agent = agentFactory.build(1L);
        String question = "你好，请介绍一下你自己";
        String reply = agent.chat("test-chat-agent", question);
        System.out.println("用户: " + question);
        System.out.println("AI: " + reply);
    }

    /**
     * 测试RAG Agent（文档检索）
     */
    @Test
    public void testRAGAgent() {
        System.out.println("\n========== 测试RAG Agent ==========");
        // 使用Agent ID构建，假设ID为2的Agent配置了search_knowledge工具
        IAgent agent = agentFactory.build(2L);
        String question = "公司的请假政策是什么？";
        String reply = agent.chat("test-rag-agent", question);
        System.out.println("用户: " + question);
        System.out.println("AI: " + reply);
    }

    /**
     * 测试流式响应
     */
    @Test
    public void testStreamChat() throws InterruptedException {
        System.out.println("\n========== 测试流式响应 ==========");
        // 使用Agent ID和模型ID构建
        IAgent agent = agentFactory.build(1L);

        String question = "请写一首关于春天的诗";
        System.out.println("用户: " + question);
        System.out.print("AI: ");

        // 流式输出（TokenStream API）
        agent.streamChat("test-stream-chat", question)
            .onPartialResponse(chunk -> {
                System.out.print(chunk);
                try {
                    Thread.sleep(50); // 模拟打字效果
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            })
            .onToolExecuted(toolExecution -> {
                System.out.println("\n[工具调用: " + toolExecution.request().name() + "]");
            })
            .onCompleteResponse(completeResponse -> {
                System.out.println("\n\n[流式响应完成]");
            })
            .onError(Throwable::printStackTrace)
            .start();

        // 等待流式完成
        Thread.sleep(30000);
    }
}
