package io.github.takicoyuki.docbatch.api;

import io.github.takicoyuki.docbatch.document.Document;
import io.github.takicoyuki.docbatch.document.DocumentService;
import io.github.takicoyuki.docbatch.document.DocumentStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> register(@Valid @RequestBody DocumentRequest request) {
        Document saved = service.register(request.fileName(), request.content());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(DocumentResponse.from(saved));
    }

    @GetMapping("/{id}")
    public DocumentResponse get(@PathVariable Long id) {
        return DocumentResponse.from(service.get(id));
    }

    @GetMapping
    public List<DocumentResponse> list(@RequestParam(required = false) DocumentStatus status) {
        return service.list(status).stream().map(DocumentResponse::from).toList();
    }
}
