package io.github.takicoyuki.docbatch.batch;

import io.github.takicoyuki.docbatch.client.OcrClient;
import io.github.takicoyuki.docbatch.client.OcrClientException;
import io.github.takicoyuki.docbatch.document.Document;
import io.github.takicoyuki.docbatch.document.DocumentRepository;
import io.github.takicoyuki.docbatch.document.DocumentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@SpringBatchTest
class OcrJobTest {

    @Autowired
    JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    DocumentRepository repository;

    @MockitoBean
    OcrClient ocrClient;

    @BeforeEach
    void setUp() {
        jobRepositoryTestUtils.removeJobExecutions();
        repository.deleteAll();
    }

    @Test
    void 信頼度に応じてCOMPLETEDかNEEDS_REVIEWになる() throws Exception {
        Document clear = repository.save(new Document("a.pdf", "clear"));
        Document blurry = repository.save(new Document("b.pdf", "blurry"));
        given(ocrClient.recognize(clear.getId(), "clear")).willReturn(new OcrClient.OcrResult("clear", 0.97));
        given(ocrClient.recognize(blurry.getId(), "blurry")).willReturn(new OcrClient.OcrResult("blurry", 0.40));

        JobExecution execution = jobLauncherTestUtils.launchJob(jobLauncherTestUtils.getUniqueJobParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(repository.findById(clear.getId()).orElseThrow().getStatus()).isEqualTo(DocumentStatus.COMPLETED);
        assertThat(repository.findById(blurry.getId()).orElseThrow().getStatus()).isEqualTo(DocumentStatus.NEEDS_REVIEW);
    }

    @Test
    void 通信エラーはリトライ後にスキップされ他の書類は処理される() throws Exception {
        Document ok = repository.save(new Document("ok.pdf", "ok"));
        Document ng = repository.save(new Document("ng.pdf", "ng"));
        given(ocrClient.recognize(ok.getId(), "ok")).willReturn(new OcrClient.OcrResult("ok", 0.99));
        given(ocrClient.recognize(eq(ng.getId()), eq("ng"))).willThrow(new OcrClientException("timeout"));

        JobExecution execution = jobLauncherTestUtils.launchJob(jobLauncherTestUtils.getUniqueJobParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        StepExecution step = execution.getStepExecutions().iterator().next();
        assertThat(step.getSkipCount()).isEqualTo(1);

        Document failed = repository.findById(ng.getId()).orElseThrow();
        assertThat(failed.getStatus()).isEqualTo(DocumentStatus.FAILED);
        assertThat(failed.getErrorMessage()).contains("timeout");
        assertThat(repository.findById(ok.getId()).orElseThrow().getStatus()).isEqualTo(DocumentStatus.COMPLETED);

        verify(ocrClient, atLeast(OcrJobConfig.RETRY_LIMIT)).recognize(eq(ng.getId()), eq("ng"));
    }

    @Test
    void 処理済みの書類は再処理しない() throws Exception {
        Document doc = repository.save(new Document("a.pdf", "clear"));
        given(ocrClient.recognize(doc.getId(), "clear")).willReturn(new OcrClient.OcrResult("clear", 0.97));

        jobLauncherTestUtils.launchJob(jobLauncherTestUtils.getUniqueJobParameters());
        jobLauncherTestUtils.launchJob(jobLauncherTestUtils.getUniqueJobParameters());

        verify(ocrClient, times(1)).recognize(anyLong(), eq("clear"));
    }
}
