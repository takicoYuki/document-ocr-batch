package io.github.takicoyuki.docbatch.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByStatusOrderByIdAsc(DocumentStatus status);

    List<Document> findAllByOrderByIdAsc();
}
