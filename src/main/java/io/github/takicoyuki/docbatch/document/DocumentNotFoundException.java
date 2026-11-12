package io.github.takicoyuki.docbatch.document;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(Long id) {
        super("Document not found: id=" + id);
    }
}
