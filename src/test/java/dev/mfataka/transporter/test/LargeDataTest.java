package dev.mfataka.transporter.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBufferLimitException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import dev.mfataka.transporter.config.TransporterConfiguration;
import dev.mfataka.transporter.enums.TransporterMethod;
import dev.mfataka.transporter.imp.Transporter;
import dev.mfataka.transporter.imp.TransporterBuilder;
import dev.mfataka.transporter.model.TransporterData;
import dev.mfataka.transporter.models.LargeDataResponse;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 31.05.2023 14:09
 */

public class LargeDataTest extends AbstractTransporterTest {

    @Test
    public void shouldThrowDataBufferLimitException() {
        final Mono<LargeDataResponse> publisher = restTransporter.sendAndReceive("http://localhost:" + localPort + "/large/data", TransporterMethod.GET)
                .monoData(LargeDataResponse.class);

        StepVerifier.create(publisher)
                .expectSubscription()
                .expectError(DataBufferLimitException.class)
                .verify();
    }


    /**
     * in case of large data you MUST block the mono, so the main thread waits until all data arrives
     */
    @Test
    public void shouldNotThrowDataBufferLimitException() {
        final Transporter largeDataCapableTransporter = TransporterBuilder.newBuilder(TransporterConfiguration.builder()
                        .debugEnabled(true)
                        .build()
                )
                .dataLimit(500_000)
                .build();
        final TransporterData<LargeDataResponse> response = largeDataCapableTransporter
                .sendAndReceive("http://localhost:" + localPort + "/large/data", TransporterMethod.GET)
                .transporterData(LargeDataResponse.class)
                .block();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.isOk());
        Assertions.assertTrue(response.isOkAnd2xxStatus());
        Assertions.assertNotNull(response.data());
        Assertions.assertEquals(200, response.data().getLargeDataList().size());
    }

    @Test
    public void shouldNotThrowDataBufferLimitExceptionFlatMapped() {
        final Transporter largeDataCapableTransporter = TransporterBuilder.newBuilder(TransporterConfiguration.builder()
                        .debugEnabled(true)
                        .build()
                )
                .dataLimit(500_000)
                .build();


        final TransporterData<LargeDataResponse> response = Mono.just("some data")
                .flatMap(ignored -> largeDataCapableTransporter
                        .sendAndReceive("http://localhost:" + localPort + "/large/data", TransporterMethod.GET)
                        .transporterData(LargeDataResponse.class)
                )
                .block();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.isOk());
        Assertions.assertTrue(response.isOkAnd2xxStatus());
        Assertions.assertNotNull(response.data());
        Assertions.assertEquals(200, response.data().getLargeDataList().size());
    }
}
