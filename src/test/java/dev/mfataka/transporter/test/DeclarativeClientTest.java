package dev.mfataka.transporter.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

/**
 * @author HAMMA FATAKA
 */
public class DeclarativeClientTest extends AbstractTransporterTest {


    @Test
    void shouldCreateDeclarativeClient() {
        final var transporterBuilder = getTransporterBuilder()
                .withBaseUrl("http://localhost:" + localPort+"/demo");

        final var demoController = transporterBuilder.declareClient(DemoClientController.class);
        final var response = demoController.greet("bob");

        StepVerifier.create(response)
                .assertNext(s -> Assertions.assertEquals("hello bob", s))
                .verifyComplete();


        final var handshake = demoController.handshake();
        StepVerifier.create(handshake)
                .assertNext(s -> Assertions.assertEquals("OK", s))
                .verifyComplete();

    }
}
