package io.github.takicoyuki.docbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class DocumentOcrBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentOcrBatchApplication.class, args);
    }
}
