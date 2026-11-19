package dev.mfataka.transporter.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import dev.mfataka.transporter.BaseTransporter;
import dev.mfataka.transporter.enums.TransporterMethod;
import dev.mfataka.transporter.imp.Transporter;
import dev.mfataka.transporter.model.TransporterData;
import dev.mfataka.transporter.models.LargeDataResponse;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 31.05.2023 15:36
 */
public class TransporterLargeDataPropertyTest extends AbstractTransporterTest {

    @Autowired
    private BaseTransporter baseTransporter;

    /**
     * this is for webclient that does not use transporter
     *
     * @param registry
     */
    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("transporter.dataLimit", () -> "500000");
    }

    @Test
    public void shouldNotThrowDataBufferLimitException() {
        final Transporter largeDataCapableTransporter = baseTransporter.toBuilder()
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
}
