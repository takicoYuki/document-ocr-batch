package io.github.takicoyuki.docbatch.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * REST/JSON で外部OCRサービスを呼び出す実装。
 * タイムアウトは application.yml の spring.http.client.* で設定している。
 */
@Component
public class RestOcrClient implements OcrClient {

    private final RestClient restClient;

    public RestOcrClient(RestClient.Builder builder, OcrProperties properties) {
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
    }

    @Override
    public OcrResult recognize(Long documentId, String content) {
        try {
            OcrResponse response = restClient.post()
                    .uri("/ocr")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new OcrRequest(documentId, content))
                    .retrieve()
                    .body(OcrResponse.class);
            if (response == null || response.text() == null) {
                throw new OcrClientException("OCR response is empty: documentId=" + documentId);
            }
            return new OcrResult(response.text(), response.confidence());
        } catch (RestClientException e) {
            throw new OcrClientException("OCR request failed: documentId=" + documentId + ", " + e.getMessage(), e);
        }
    }

    record OcrRequest(Long documentId, String content) {
    }

    record OcrResponse(String text, double confidence) {
    }
}
