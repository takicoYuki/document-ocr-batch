package io.github.takicoyuki.docbatch.api;

import io.github.takicoyuki.docbatch.batch.OcrJobRunner;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 運用時の手動実行用。通常はスケジューラから起動する。 */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final OcrJobRunner runner;

    public JobController(OcrJobRunner runner) {
        this.runner = runner;
    }

    @PostMapping("/ocr")
    public JobResult runOcrJob() throws Exception {
        return JobResult.from(runner.run());
    }

    public record JobResult(Long executionId, String status, long readCount, long writeCount, long skipCount) {

        static JobResult from(JobExecution execution) {
            long read = 0;
            long write = 0;
            long skip = 0;
            for (StepExecution step : execution.getStepExecutions()) {
                read += step.getReadCount();
                write += step.getWriteCount();
                skip += step.getSkipCount();
            }
            return new JobResult(execution.getId(), execution.getStatus().name(), read, write, skip);
        }
    }
}
