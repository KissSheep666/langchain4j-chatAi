package com.study.langchain4jspringboot.messaging;

import com.study.langchain4jspringboot.dto.DocumentTaskMessage;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.study.langchain4jspringboot.config.RabbitMQConfig.QUEUE;
import static dev.langchain4j.data.document.Document.FILE_NAME;

/**
 * 文档处理消息消费者 —— 从 RabbitMQ 获取任务，异步完成文档解析→向量化→存储。
 *
 * @author kisssheep
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentProcessingConsumer {

    private final DocumentParser documentParser;
    private final DocumentSplitter documentSplitter;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    @RabbitListener(queues = QUEUE)
    public void handle(DocumentTaskMessage message) {
        log.info("Received document processing task: taskId={}, sourceType={}, fileName={}",
                message.taskId(), message.sourceType(), message.fileName());

        try {
            Document document = loadDocument(message);
            if (document == null) {
                log.error("Failed to load document for task {}", message.taskId());
                return;
            }

            document.metadata().put("file_id", message.taskId());
            document.metadata().put(FILE_NAME, message.fileName());
            document.metadata().put("scope", 1);

            // 切分
            List<TextSegment> segments = documentSplitter.splitAll(List.of(document));
            log.info("Task {} — split into {} segments", message.taskId(), segments.size());

            // 向量化
            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

            // 存储
            List<String> ids = embeddingStore.addAll(embeddings, segments);
            log.info("Task {} — stored {} segments in vector db, ids: {}", message.taskId(), ids.size(), ids);

        } catch (Exception e) {
            log.error("Task {} — processing failed: {}", message.taskId(), e.getMessage(), e);
        }
    }

    private Document loadDocument(DocumentTaskMessage message) throws IOException {
        return switch (message.sourceType()) {
            case "FILE" -> {
                Path filePath = Path.of(message.source());
                try (InputStream is = Files.newInputStream(filePath)) {
                    yield documentParser.parse(is);
                }
            }
            case "URL" -> {
                URI uri = URI.create(message.source());
                yield dev.langchain4j.data.document.loader.UrlDocumentLoader.load(uri.toURL(), documentParser);
            }
            default -> throw new IllegalArgumentException("Unknown source type: " + message.sourceType());
        };
    }
}
