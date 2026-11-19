package dev.mfataka.transporter.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import dev.mfataka.transporter.config.TransporterConfiguration;
import dev.mfataka.transporter.imp.TransporterBuilder;

/**
 * End-to-end check of the kibana call logging: real WebClient, real HTTP
 * round trip against the demo controller, one structured record per call.
 */
class TransporterKibanaLoggingTest extends AbstractTransporterTest {

    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void attachAppender() {
        appender = new ListAppender<>();
        appender.start();
        kibanaLogger().addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        kibanaLogger().detachAppender(appender);
    }

    private static Logger kibanaLogger() {
        return (Logger) LoggerFactory.getLogger("kibana.KibanaLoggingFilter");
    }

    private TransporterBuilder kibanaEnabledBuilder() {
        return TransporterBuilder.testInstance(
                        TransporterConfiguration.builder()
                                .timeUnit(TimeUnit.SECONDS)
                                .kibanaLoggingEnabled(true)
                                .build())
                .operator("test-operator")
                .withBaseUrl("http://localhost:" + localPort + "/demo");
    }

    private TransporterBuilder kibanaAndLogbookEnabledBuilder() {
        return TransporterBuilder.testInstance(
                        TransporterConfiguration.builder()
                                .timeUnit(TimeUnit.SECONDS)
                                .kibanaLoggingEnabled(true)
                                .loggerEnabled(true)
                                .build())
                .operator("test-operator")
                .withBaseUrl("http://localhost:" + localPort + "/demo");
    }

    @Test
    void successfulCall_emitsSingleKibanaRecordWithAllMarkers() {
        var response = kibanaEnabledBuilder()
                .get(null)
                .sendAndReceive(uri -> uri.path("/greeting/{n}").build("kibana"))
                .transporterDataBlock(String.class);

        assertThat(response.isOk()).isTrue();
        assertThat(appender.list).hasSize(1);
        final var fields = appender.list.get(0).getMDCPropertyMap();
        assertThat(fields)
                .containsEntry("operator", "test-operator")
                .containsEntry("endpoint", "/demo/greeting/kibana")
                .containsEntry("http_status", "200")
                .containsEntry("result", "success")
                .containsKey("duration_s");
        assertThat(fields.get("correlation_id")).matches("[0-9a-f]{16}");
    }

    @Test
    void failedCall_emitsFailRecordWithStatus() {
        kibanaEnabledBuilder()
                .get(null)
                .sendAndReceive("/no-such-endpoint")
                .transporterDataBlock(String.class);

        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.get(0).getMDCPropertyMap())
                .containsEntry("http_status", "404")
                .containsEntry("result", "fail");
    }

    @Test
    void correlationIdIsGeneratedWhenAbsent() {
        kibanaEnabledBuilder()
                .get(null)
                .sendAndReceive(uri -> uri.path("/handshake").build())
                .transporterDataBlock(String.class);

        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.get(0).getMDCPropertyMap().get("correlation_id")).isNotBlank();
    }

    @Test
    void sharedCorrelationId_logbookRecordCarriesTheKibanaId() {
        final var logbookLogger = (Logger) LoggerFactory.getLogger("org.zalando.logbook.Logbook");
        final var previousLevel = logbookLogger.getLevel();
        final var logbookAppender = new ListAppender<ILoggingEvent>();
        logbookAppender.start();
        logbookLogger.setLevel(Level.TRACE);
        logbookLogger.addAppender(logbookAppender);
        try {
            kibanaAndLogbookEnabledBuilder()
                    .get(null)
                    .sendAndReceive(uri -> uri.path("/greeting/{n}").build("corr"))
                    .transporterDataBlock(String.class);

            assertThat(appender.list).hasSize(1);
            final var correlationId = appender.list.get(0).getMDCPropertyMap().get("correlation_id");
            assertThat(correlationId).matches("[0-9a-f]{16}");
            assertThat(logbookAppender.list)
                    .as("logbook request/response records should carry the kibana correlation id")
                    .isNotEmpty()
                    .anySatisfy(event -> assertThat(event.getFormattedMessage()).contains(correlationId));
        } finally {
            logbookLogger.detachAppender(logbookAppender);
            logbookLogger.setLevel(previousLevel);
        }
    }

    @Test
    void kibanaLoggingDisabled_emitsNothing() {
        restTransporter
                .method(dev.mfataka.transporter.enums.TransporterMethod.GET)
                .sendAndReceive("http://localhost:" + localPort + "/demo/handshake")
                .transporterDataBlock(String.class);

        assertThat(appender.list).isEmpty();
    }
}
