package io.github.takicoyuki.docbatch.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String fileName;

    /** 書類本文（実案件では画像ファイルを想定。サンプルでは簡略化してテキストで受け取る） */
    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;

    @Lob
    private String ocrText;

    private Double confidence;

    @Column(length = 500)
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Document() {
        // JPA
    }

    public Document(String fileName, String content) {
        this.fileName = fileName;
        this.content = content;
        this.status = DocumentStatus.RECEIVED;
    }

    /** OCR結果を反映する。信頼度がしきい値未満なら要確認にする。 */
    public void applyOcrResult(String text, double confidence, double reviewThreshold) {
        this.ocrText = text;
        this.confidence = confidence;
        this.errorMessage = null;
        this.status = confidence >= reviewThreshold ? DocumentStatus.COMPLETED : DocumentStatus.NEEDS_REVIEW;
    }

    public void markFailed(String message) {
        this.status = DocumentStatus.FAILED;
        this.errorMessage = message == null ? null : message.substring(0, Math.min(message.length(), 500));
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContent() {
        return content;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public String getOcrText() {
        return ocrText;
    }

    public Double getConfidence() {
        return confidence;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
