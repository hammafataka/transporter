package dev.mfataka.transporter.test;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import dev.mfataka.transporter.Application;
import dev.mfataka.transporter.config.TransporterConfiguration;
import dev.mfataka.transporter.imp.Transporter;
import dev.mfataka.transporter.imp.TransporterBuilder;

/**
 * @author HAMMA FATAKA
 */
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractTransporterTest {

    @LocalServerPort
    protected int localPort;
    protected final Transporter cxfTransporter;
    protected final Transporter restTransporter;

    protected AbstractTransporterTest() {
        this.cxfTransporter = getTransporterBuilder()
                .soapService()
                .build();
        this.restTransporter = getTransporterBuilder()
                .build();

    }

    @NotNull
    protected static TransporterBuilder getTransporterBuilder() {
        return TransporterBuilder.newBuilder(
                TransporterConfiguration.builder()
                        .loggerEnabled(true)
                        .timeUnit(TimeUnit.SECONDS)
                        .build());
    }
}
