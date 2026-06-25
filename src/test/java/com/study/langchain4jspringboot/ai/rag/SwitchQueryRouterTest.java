package com.study.langchain4jspringboot.ai.rag;

import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * SwitchQueryRouter 单元测试 —— 验证联网搜索开关的路由逻辑。
 *
 * @author kisssheep
 */
class SwitchQueryRouterTest {

    private ContentRetriever embeddingStoreRetriever;
    private WebSearchContentRetriever webSearchRetriever;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        embeddingStoreRetriever = query -> List.of();
        webSearchRetriever = mock(WebSearchContentRetriever.class);

        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("联网开关关闭时应只返回向量检索器")
    void shouldRouteToEmbeddingStoreOnly_whenWebSearchDisabled() {
        request.setParameter("webSearchEnable", "false");

        SwitchQueryRouter router = new SwitchQueryRouter(embeddingStoreRetriever, webSearchRetriever);
        Collection<ContentRetriever> result = router.route(Query.from("测试问题"));

        assertThat(result)
                .hasSize(1)
                .allMatch(r -> !(r instanceof WebSearchContentRetriever));
    }

    @Test
    @DisplayName("联网开关打开时应返回所有检索器（向量 + 联网）")
    void shouldRouteToAllRetrievers_whenWebSearchEnabled() {
        request.setParameter("webSearchEnable", "true");

        SwitchQueryRouter router = new SwitchQueryRouter(embeddingStoreRetriever, webSearchRetriever);
        Collection<ContentRetriever> result = router.route(Query.from("测试问题"));

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("缺少参数时默认不走联网检索")
    void shouldDefaultToEmbeddingStoreOnly_whenParameterMissing() {
        SwitchQueryRouter router = new SwitchQueryRouter(embeddingStoreRetriever, webSearchRetriever);
        Collection<ContentRetriever> result = router.route(Query.from("无参数查询"));

        assertThat(result)
                .allMatch(r -> !(r instanceof WebSearchContentRetriever));
    }
}
