package io.github.takicoyuki.docbatch.document;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Document register(String fileName, String content) {
        return repository.save(new Document(fileName, content));
    }

    public Document get(Long id) {
        return repository.findById(id).orElseThrow(() -> new DocumentNotFoundException(id));
    }

    public List<Document> list(DocumentStatus status) {
        return status == null
                ? repository.findAllByOrderByIdAsc()
                : repository.findByStatusOrderByIdAsc(status);
    }
}
