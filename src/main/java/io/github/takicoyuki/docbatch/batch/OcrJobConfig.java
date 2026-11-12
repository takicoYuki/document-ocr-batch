package io.github.takicoyuki.docbatch.batch;

import io.github.takicoyuki.docbatch.client.OcrClientException;
import io.github.takicoyuki.docbatch.document.Document;
import io.github.takicoyuki.docbatch.document.DocumentStatus;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

/**
 * OCR連携ジョブ。
 * <p>
 * 受付済み（RECEIVED）の書類を読み込み → 外部OCR APIを呼び出し → 結果をDBへ反映する。
 * 通信エラーは3回までリトライし、それでも失敗した書類はスキップして FAILED にする
 * （1件の失敗でジョブ全体を止めない）。
 */
@Configuration
public class OcrJobConfig {

    public static final String JOB_NAME = "ocrJob";
    static final int CHUNK_SIZE = 10;
    static final int RETRY_LIMIT = 3;
    static final int SKIP_LIMIT = 1_000;

    @Bean
    public Job ocrJob(JobRepository jobRepository, Step ocrStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(ocrStep)
                .build();
    }

    @Bean
    public Step ocrStep(JobRepository jobRepository,
                        PlatformTransactionManager transactionManager,
                        JpaCursorItemReader<Document> ocrReader,
                        OcrItemProcessor ocrProcessor,
                        JpaItemWriter<Document> ocrWriter,
                        OcrSkipListener skipListener) {
        return new StepBuilder("ocrStep", jobRepository)
                .<Document, Document>chunk(CHUNK_SIZE, transactionManager)
                .reader(ocrReader)
                .processor(ocrProcessor)
                .writer(ocrWriter)
                .faultTolerant()
                .retry(OcrClientException.class)
                .retryLimit(RETRY_LIMIT)
                .skip(OcrClientException.class)
                .skipLimit(SKIP_LIMIT)
                .listener(skipListener)
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<Document> ocrReader(EntityManagerFactory entityManagerFactory) {
        return new JpaCursorItemReaderBuilder<Document>()
                .name("ocrReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select d from Document d where d.status = :status order by d.id")
                .parameterValues(Map.of("status", DocumentStatus.RECEIVED))
                .build();
    }

    @Bean
    public JpaItemWriter<Document> ocrWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<Document>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
