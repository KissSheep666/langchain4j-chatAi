package com.study.langchain4jspringboot.controller.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.langchain4jspringboot.ai.assistant.QwenAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ChatController 单元测试 —— 验证流式对话与新会话创建的端点逻辑。
 *
 * @author kisssheep
 */
@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private QwenAssistant qwenAssistant;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ChatController controller;

    @BeforeEach
    void setUp() {
        controller = new ChatController(qwenAssistant, objectMapper);
    }

    @Test
    @DisplayName("创建新会话应返回 32 位 hex UUID")
    void shouldReturnValidUuid_whenNewSession() {
        String sessionId = controller.newSession();

        assertThat(sessionId)
                .isNotBlank()
                .matches("[0-9a-f]{32}");
    }

    @Test
    @DisplayName("Flux 流式聊天应正确代理到 QwenAssistant")
    void shouldDelegateToAssistant_whenChatStreamFlux() {
        when(qwenAssistant.chatStreamFlux(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Flux.just("你好", "，", "世界！"));

        Flux<String> result = controller.chatStreamFlux(
                "session-1", "智能助手", "你好", false, "");

        StepVerifier.create(result)
                .expectNext("你好", "，", "世界！")
                .verifyComplete();

        verify(qwenAssistant).chatStreamFlux("session-1", "智能助手", "你好", "");
    }

    @Test
    @DisplayName("Flux 流式聊天空问题也能正常返回")
    void shouldHandleEmptyQuestion_whenChatStreamFlux() {
        when(qwenAssistant.chatStreamFlux(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Flux.empty());

        Flux<String> result = controller.chatStreamFlux("s1", "助手", "", false, "");

        StepVerifier.create(result).verifyComplete();
    }
}
