package io.github.takicoyuki.docbatch.batch;

import io.github.takicoyuki.docbatch.client.OcrClient;
import io.github.takicoyuki.docbatch.client.OcrProperties;
import io.github.takicoyuki.docbatch.document.Document;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class OcrItemProcessor implements ItemProcessor<Document, Document> {

    private final OcrClient ocrClient;
    private final OcrProperties properties;

    public OcrItemProcessor(OcrClient ocrClient, OcrProperties properties) {
        this.ocrClient = ocrClient;
        this.properties = properties;
    }

    @Override
    public Document process(Document document) {
        OcrClient.OcrResult result = ocrClient.recognize(document.getId(), document.getContent());
        document.applyOcrResult(result.text(), result.confidence(), properties.reviewThreshold());
        return document;
    }
}
