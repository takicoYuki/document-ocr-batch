package io.github.takicoyuki.docbatch.batch;

import io.github.takicoyuki.docbatch.document.Document;
import io.github.takicoyuki.docbatch.document.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

/** リトライしても処理できなかった書類を FAILED として記録する。 */
@Component
public class OcrSkipListener implements SkipListener<Document, Document> {

    private static final Logger log = LoggerFactory.getLogger(OcrSkipListener.class);

    private final DocumentRepository repository;

    public OcrSkipListener(DocumentRepository repository) {
        this.repository = repository;
    }

    @Override
    public void onSkipInProcess(Document item, Throwable t) {
        log.warn("OCR skipped: documentId={}, reason={}", item.getId(), t.getMessage());
        item.markFailed(t.getMessage());
        repository.save(item);
    }
}
