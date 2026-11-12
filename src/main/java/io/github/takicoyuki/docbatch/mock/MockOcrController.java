package io.github.takicoyuki.docbatch.mock;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ローカル動作確認用の疑似OCRサービス（local プロファイルのみ有効）。
 * 本文に "blurry" を含むと低信頼度を、"error" を含むと 500 エラーを返す。
 */
@RestController
@Profile("local")
@RequestMapping("/mock")
public class MockOcrController {

    @PostMapping("/ocr")
    public MockOcrResponse ocr(@RequestBody MockOcrRequest request) {
        String content = request.content() == null ? "" : request.content();
        if (content.contains("error")) {
            throw new IllegalStateException("mock OCR failure");
        }
        double confidence = content.contains("blurry") ? 0.42 : 0.97;
        return new MockOcrResponse(content.strip(), confidence);
    }

    public record MockOcrRequest(Long documentId, String content) {
    }

    public record MockOcrResponse(String text, double confidence) {
    }
}
