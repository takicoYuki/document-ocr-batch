package io.github.takicoyuki.docbatch.api;

import io.github.takicoyuki.docbatch.batch.OcrJobRunner;
import io.github.takicoyuki.docbatch.document.Document;
import io.github.takicoyuki.docbatch.document.DocumentNotFoundException;
import io.github.takicoyuki.docbatch.document.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {DocumentController.class, JobController.class})
class DocumentControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    DocumentService service;

    @MockitoBean
    OcrJobRunner jobRunner;

    @Test
    void 書類を登録すると201とLocationヘッダを返す() throws Exception {
        Document saved = new Document("invoice.pdf", "請求書 No.123");
        ReflectionTestUtils.setField(saved, "id", 1L);
        given(service.register("invoice.pdf", "請求書 No.123")).willReturn(saved);

        mvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileName": "invoice.pdf", "content": "請求書 No.123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/documents/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void 必須項目が空なら400と項目ごとのエラーを返す() throws Exception {
        mvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileName": "", "content": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.errors.fileName").exists())
                .andExpect(jsonPath("$.errors.content").exists());

        verify(service, never()).register(anyString(), anyString());
    }

    @Test
    void 対応外の拡張子は400を返す() throws Exception {
        mvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileName": "memo.txt", "content": "text"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fileName").exists());
    }

    @Test
    void 存在しないIDは404を返す() throws Exception {
        given(service.get(any())).willThrow(new DocumentNotFoundException(99L));

        mvc.perform(get("/api/documents/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Document Not Found"));
    }
}
