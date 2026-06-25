package com.study.langchain4jspringboot.convert;

import com.study.langchain4jspringboot.controller.chat.vo.RetrievedRecordResponse;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static dev.langchain4j.data.document.Document.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * ContentConvert 单元测试 —— 验证文本段到检索记录的转换逻辑。
 *
 * @author kisssheep
 */
class ContentConvertTest {

    @Test
    @DisplayName("空列表应返回空集合")
    void shouldReturnEmptySet_whenInputIsEmpty() {
        assertThat(ContentConvert.convertToRecord(Collections.emptyList())).isEmpty();
        assertThat(ContentConvert.convertToRecord0(Collections.emptyList())).isEmpty();
    }

    @Test
    @DisplayName("正常文本段应转换为正确的记录")
    void shouldConvertTextSegmentsToRecords() {
        Metadata metadata = new Metadata()
                .put(URL, "https://example.com/doc/test.pdf")
                .put(FILE_NAME, "test.pdf")
                .put(ABSOLUTE_DIRECTORY_PATH, "/docs")
                .put("file_id", "123");

        TextSegment segment = TextSegment.from("这是一段检索命中的文本内容", metadata);

        Set<RetrievedRecordResponse> result = ContentConvert.convertToRecord0(List.of(segment));

        assertThat(result).hasSize(1);
        RetrievedRecordResponse record = result.iterator().next();
        assertThat(record.fileName()).isEqualTo("test.pdf");
        assertThat(record.url()).isEqualTo("https://example.com/doc/test.pdf");
        assertThat(record.fileId()).isEqualTo("123");
        assertThat(record.absolutePath()).isEqualTo("/docs");
        assertThat(record.texts()).contains("这是一段检索命中的文本内容");
    }

    @Test
    @DisplayName("相同 URL 的多个文本段应合并到一个记录中")
    void shouldMergeSegmentsWithSameUrl() {
        Metadata metadata = new Metadata()
                .put(URL, "https://example.com/doc/same.pdf")
                .put(FILE_NAME, "same.pdf");

        TextSegment segment1 = TextSegment.from("第一段文本", metadata);
        TextSegment segment2 = TextSegment.from("第二段文本", metadata);

        Set<RetrievedRecordResponse> result = ContentConvert.convertToRecord0(List.of(segment1, segment2));

        assertThat(result).hasSize(1);
        RetrievedRecordResponse record = result.iterator().next();
        assertThat(record.texts()).hasSize(2)
                .contains("第一段文本", "第二段文本");
    }

    @Test
    @DisplayName("URL 为空的文本段应被跳过")
    void shouldSkipSegmentsWithBlankUrl() {
        Metadata emptyUrlMetadata = new Metadata().put(URL, "");
        TextSegment segment = TextSegment.from("无 URL 的文本", emptyUrlMetadata);

        Set<RetrievedRecordResponse> result = ContentConvert.convertToRecord0(List.of(segment));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("缺少 file_id 时应使用默认值 '0'")
    void shouldUseDefaultFileId_whenFileIdMissing() {
        Metadata metadata = new Metadata()
                .put(URL, "https://example.com/test.pdf")
                .put(FILE_NAME, "test.pdf");

        TextSegment segment = TextSegment.from("文本内容", metadata);

        Set<RetrievedRecordResponse> result = ContentConvert.convertToRecord0(List.of(segment));

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().fileId()).isEqualTo("0");
    }
}
