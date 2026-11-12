package io.github.takicoyuki.docbatch.client;

/** 外部OCRサービスの呼び出し口。実装を差し替えられるようインターフェースにしている。 */
public interface OcrClient {

    /**
     * @throws OcrClientException 通信エラー・異常レスポンスの場合
     */
    OcrResult recognize(Long documentId, String content);

    record OcrResult(String text, double confidence) {
    }
}
