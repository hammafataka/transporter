package dev.mfataka.transporter.imp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import dev.mfataka.transporter.config.TransporterConfiguration;

class DefaultTransporterReceiverTest {

    private static TransporterConfiguration cfg() {
        return TransporterConfiguration.builder().build();
    }

    @Test
    void requireNonNull_returnsNewInstance_originalUnchanged() {
        var spec = mock(WebClient.RequestBodySpec.class);
        var r1 = DefaultTransporterReceiver.of(cfg(), spec);
        var r2 = r1.requireNonNull(true);
        assertThat(r1).isNotSameAs(r2);
    }

    @Test
    void checkRequiredFields_returnsNewInstance_originalUnchanged() {
        var spec = mock(WebClient.RequestBodySpec.class);
        var r1 = DefaultTransporterReceiver.of(cfg(), spec);
        var r2 = r1.checkRequiredFields(true);
        assertThat(r1).isNotSameAs(r2);
    }

    @Test
    void receiverFlagSetters_doNotMutateSharedConfig() {
        var sharedConfig = TransporterConfiguration.builder()
                .requiredNonNull(false)
                .checkRequiredFields(false)
                .build();
        var spec = mock(WebClient.RequestBodySpec.class);

        var r = DefaultTransporterReceiver.of(sharedConfig, spec);
        var r2 = r.requireNonNull(true);
        var r3 = r2.checkRequiredFields(true);

        assertThat(sharedConfig.isRequiredNonNull()).isFalse();
        assertThat(sharedConfig.isCheckRequiredFields()).isFalse();
        assertThat(r3).isNotSameAs(r2);
    }

    @Test
    void chainedFlagSetters_eachProduceNewInstance() {
        var spec = mock(WebClient.RequestBodySpec.class);
        var r1 = DefaultTransporterReceiver.of(cfg(), spec);
        var r2 = r1.requireNonNull(true);
        var r3 = r2.checkRequiredFields(true);
        var r4 = r3.requireNonNull(false);

        assertThat(r1).isNotSameAs(r2).isNotSameAs(r3).isNotSameAs(r4);
        assertThat(r2).isNotSameAs(r3).isNotSameAs(r4);
        assertThat(r3).isNotSameAs(r4);
    }
}
