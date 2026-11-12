package io.github.takicoyuki.docbatch.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 外部OCRサービスの接続設定。
 *
 * @param baseUrl         OCR API のベースURL
 * @param reviewThreshold この信頼度未満の結果は要確認（NEEDS_REVIEW）にする
 */
@ConfigurationProperties(prefix = "app.ocr")
public record OcrProperties(String baseUrl, double reviewThreshold) {
}
