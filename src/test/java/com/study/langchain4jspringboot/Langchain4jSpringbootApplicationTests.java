package com.study.langchain4jspringboot;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring 上下文加载测试 —— 需要真实的 API key，本地运行时取消 @Disabled 并配置环境变量。
 */
@SpringBootTest
@Disabled("需要配置 API_KEY_DASH_SCOPE 环境变量才能加载 Spring 上下文")
class Langchain4jSpringbootApplicationTests {

	@Test
	void contextLoads() {
	}

}
