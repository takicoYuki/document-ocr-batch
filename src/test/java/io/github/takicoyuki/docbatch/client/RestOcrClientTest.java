package io.github.takicoyuki.docbatch.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(RestOcrClient.class)
@EnableConfigurationProperties(OcrProperties.class)
@TestPropertySource(properties = {
        "app.ocr.base-url=http://ocr.example.com",
        "app.ocr.review-threshold=0.8"
})
class RestOcrClientTest {

    @Autowired
    RestOcrClient client;

    @Autowired
    MockRestServiceServer server;

    @Test
    void OCR結果をJSONで受け取れる() {
        server.expect(requestTo("http://ocr.example.com/ocr"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"documentId": 1, "content": "請求書"}
                        """))
                .andRespond(withSuccess("""
                        {"text": "請求書", "confidence": 0.95}
                        """, MediaType.APPLICATION_JSON));

        OcrClient.OcrResult result = client.recognize(1L, "請求書");

        assertThat(result.text()).isEqualTo("請求書");
        assertThat(result.confidence()).isEqualTo(0.95);
        server.verify();
    }

    @Test
    void サーバーエラーはOcrClientExceptionに変換する() {
        server.expect(requestTo("http://ocr.example.com/ocr"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.recognize(1L, "請求書"))
                .isInstanceOf(OcrClientException.class)
                .hasMessageContaining("documentId=1");
    }
}
