package io.github.takicoyuki.docbatch.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** app.ocr-job.schedule.enabled=true のときだけ定期実行する。 */
@Component
@ConditionalOnProperty(name = "app.ocr-job.schedule.enabled", havingValue = "true")
public class OcrJobScheduler {

    private static final Logger log = LoggerFactory.getLogger(OcrJobScheduler.class);

    private final OcrJobRunner runner;

    public OcrJobScheduler(OcrJobRunner runner) {
        this.runner = runner;
    }

    @Scheduled(cron = "${app.ocr-job.schedule.cron}")
    public void run() {
        try {
            JobExecution execution = runner.run();
            log.info("ocrJob finished: executionId={}, status={}", execution.getId(), execution.getStatus());
        } catch (Exception e) {
            log.error("ocrJob failed to start", e);
        }
    }
}
