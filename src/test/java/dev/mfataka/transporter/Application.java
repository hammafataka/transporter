package dev.mfataka.transporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 14.05.2023 5:13
 */
@ActiveProfiles("test")
@SpringBootApplication(scanBasePackages = {"dev.mfataka.transporter"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
