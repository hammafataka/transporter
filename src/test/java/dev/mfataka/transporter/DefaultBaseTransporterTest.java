package dev.mfataka.transporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import dev.mfataka.transporter.config.ConfigurationResolver;
import dev.mfataka.transporter.config.TransporterConfiguration;

class DefaultBaseTransporterTest {

    private static ConfigurationResolver passThroughResolver() {
        var resolver = mock(ConfigurationResolver.class);
        when(resolver.resolveBaseConfigs(any(TransporterConfiguration.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        return resolver;
    }

    @Test
    void withTimeOut_returnsNewBaseTransporter_doesNotMutateOriginal() {
        var b1 = new DefaultBaseTransporter(passThroughResolver());
        var b2 = b1.withTimeOut(5L, TimeUnit.SECONDS);
        assertThat(b1).isNotSameAs(b2);
    }

    @Test
    void withLogger_returnsNewBaseTransporter() {
        var b1 = new DefaultBaseTransporter(passThroughResolver());
        var b2 = b1.withLogger();
        assertThat(b1).isNotSameAs(b2);
    }

    @Test
    void trustAll_returnsNewBaseTransporter() {
        var b1 = new DefaultBaseTransporter(passThroughResolver());
        var b2 = b1.trustAll();
        assertThat(b1).isNotSameAs(b2);
    }

    @Test
    void withConnectionPool_returnsNewBaseTransporter() {
        var b1 = new DefaultBaseTransporter(passThroughResolver());
        var b2 = b1.withConnectionPool(20, Duration.ofSeconds(2));
        assertThat(b1).isNotSameAs(b2);
    }

    @Test
    void withConnectionPool_withMaxQueue_returnsNewBaseTransporter() {
        var b1 = new DefaultBaseTransporter(passThroughResolver());
        var b2 = b1.withConnectionPool(20, Duration.ofSeconds(2), 50);
        assertThat(b1).isNotSameAs(b2);
    }

    @Test
    void chainedMutators_doNotMutateAnyEarlierInstance() {
        var b1 = new DefaultBaseTransporter(passThroughResolver());
        var b2 = b1.withTimeOut(10L);
        var b3 = b2.withLogger();
        var b4 = b3.trustAll();

        assertThat(b1).isNotSameAs(b2).isNotSameAs(b3).isNotSameAs(b4);
        assertThat(b2).isNotSameAs(b3).isNotSameAs(b4);
        assertThat(b3).isNotSameAs(b4);
    }
}
