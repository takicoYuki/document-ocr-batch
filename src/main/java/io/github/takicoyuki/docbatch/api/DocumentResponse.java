package io.github.takicoyuki.docbatch.api;

import io.github.takicoyuki.docbatch.document.Document;
import io.github.takicoyuki.docbatch.document.DocumentStatus;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long id,
        String fileName,
        DocumentStatus status,
        String ocrText,
        Double confidence,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static DocumentResponse from(Document d) {
        return new DocumentResponse(
                d.getId(), d.getFileName(), d.getStatus(), d.getOcrText(),
                d.getConfidence(), d.getErrorMessage(), d.getCreatedAt(), d.getUpdatedAt());
    }
}
