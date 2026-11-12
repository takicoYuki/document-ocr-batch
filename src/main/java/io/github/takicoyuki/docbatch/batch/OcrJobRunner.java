package io.github.takicoyuki.docbatch.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OcrJobRunner {

    private final JobLauncher jobLauncher;
    private final Job ocrJob;

    public OcrJobRunner(JobLauncher jobLauncher, Job ocrJob) {
        this.jobLauncher = jobLauncher;
        this.ocrJob = ocrJob;
    }

    public JobExecution run() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLocalDateTime("requestedAt", LocalDateTime.now())
                .toJobParameters();
        return jobLauncher.run(ocrJob, params);
    }
}
