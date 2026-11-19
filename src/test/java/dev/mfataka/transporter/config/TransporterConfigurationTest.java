package dev.mfataka.transporter.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class TransporterConfigurationTest {

    @Test
    void builderProducesImmutableInstance_originalUntouchedByDerivedCopy() {
        var c1 = TransporterConfiguration.builder()
                .baseUrl("http://a")
                .timeout(10L)
                .timeUnit(TimeUnit.SECONDS)
                .build();

        var c2 = c1.toBuilder().baseUrl("http://b").timeout(20L).build();

        assertThat(c1.getBaseUrl()).isEqualTo("http://a");
        assertThat(c1.getTimeout()).isEqualTo(10L);
        assertThat(c2.getBaseUrl()).isEqualTo("http://b");
        assertThat(c2.getTimeout()).isEqualTo(20L);
        assertThat(c1).isNotSameAs(c2);
    }

    @Test
    void getDataLimitOrDefault_returnsDefaultWhenNull() {
        var c = TransporterConfiguration.builder().build();
        assertThat(c.getDataLimitOrDefault()).isEqualTo(256 * 1024);
    }

    @Test
    void getDataLimitOrDefault_returnsConfiguredValue() {
        var c = TransporterConfiguration.builder().dataLimit(99).build();
        assertThat(c.getDataLimitOrDefault()).isEqualTo(99);
    }

    @Test
    void pendingAcquireTimeoutOrDefault_returnsTenSecondsWhenNull() {
        var c = TransporterConfiguration.builder().build();
        assertThat(c.getPendingAcquireTimeoutOrDefault()).isEqualTo(Duration.ofSeconds(10));
    }

    @Test
    void pendingAcquireTimeoutOrDefault_returnsConfiguredValue() {
        var c = TransporterConfiguration.builder().pendingAcquireTimeout(Duration.ofMillis(250)).build();
        assertThat(c.getPendingAcquireTimeoutOrDefault()).isEqualTo(Duration.ofMillis(250));
    }

    @Test
    void pendingAcquireMaxCountOrDefault_isTwiceMaxConnections() {
        var c = TransporterConfiguration.builder().maxConnections(30).build();
        assertThat(c.getPendingAcquireMaxCountOrDefault()).isEqualTo(60);
    }

    @Test
    void pendingAcquireMaxCountOrDefault_respectsExplicitOverride() {
        var c = TransporterConfiguration.builder()
                .maxConnections(30)
                .pendingAcquireMaxCount(7)
                .build();
        assertThat(c.getPendingAcquireMaxCountOrDefault()).isEqualTo(7);
    }
}
