package dev.mfataka.transporter.imp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import dev.mfataka.transporter.config.TransporterConfiguration;

class DefaultTransporterBuilderTest {

    private TransporterConfiguration seed() {
        return TransporterConfiguration.builder().timeUnit(TimeUnit.SECONDS).build();
    }

    @Test
    void withBaseUrl_returnsNewInstance_doesNotMutateParent() {
        var b1 = DefaultTransporterBuilder.of(seed());
        var b2 = b1.withBaseUrl("http://a");
        assertThat(b1).isNotSameAs(b2);
    }

    @Test
    void parallelForks_haveIndependentWebClients() {
        var parent = DefaultTransporterBuilder.of(seed());
        var a = parent.withBaseUrl("http://a");
        var b = parent.withBaseUrl("http://b");
        assertThat(a.getWebClient()).isNotSameAs(b.getWebClient());
    }

    @Test
    void webClientIsCached_perBuilderInstance() {
        var b = DefaultTransporterBuilder.of(seed());
        var first = b.getWebClient();
        var second = b.getWebClient();
        assertThat(first).isSameAs(second);
    }
}
