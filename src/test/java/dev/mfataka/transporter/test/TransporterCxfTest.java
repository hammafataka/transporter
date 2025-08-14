package dev.mfataka.transporter.test;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tempuri.Add;
import org.tempuri.AddResponse;

import reactor.test.StepVerifier;

import dev.mfataka.transporter.enums.TransporterMethod;
import dev.mfataka.transporter.imp.TransporterReceiver;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 14.05.2023 5:13
 */
public class TransporterCxfTest extends AbstractTransporterTest {

    @Test
    public void shouldSendSoapRequestAndReceiveMono() {
        final var addResponse = executeRequest()
                .monoData(AddResponse.class);

        StepVerifier.create(addResponse)
                .expectSubscription()
                .assertNext(response -> {
                    Assertions.assertTrue(Objects.nonNull(response));
                    Assertions.assertEquals(20, response.getAddResult());
                })
                .verifyComplete();
    }

    @Test
    public void shouldSendSoapRequestAndReceiveTransporterData() {
        final var addResponse = executeRequest()
                .transporterData(AddResponse.class);


        StepVerifier.create(addResponse)
                .expectSubscription()
                .assertNext(addResponseTransporterData -> {
                    Assertions.assertTrue(addResponseTransporterData.isOk());
                    Assertions.assertTrue(addResponseTransporterData.isOkAnd2xxStatus());
                    Assertions.assertEquals(20, addResponseTransporterData.data().getAddResult());
                })
                .verifyComplete();
    }

    @Test
    public void shouldSendSoapRequestAndReceiveString() {
        final var addResponseString = executeRequest()
                .transporterData();

        Assertions.assertTrue(addResponseString.isOk());
        Assertions.assertTrue(addResponseString.isOkAnd2xxStatus());
        Assertions.assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><soap:Body><AddResponse xmlns=\"http://tempuri.org/\"><AddResult>20</AddResult></AddResponse></soap:Body></soap:Envelope>",
                addResponseString.data());
    }

    @Test
    public void shouldSendSoapRequestAndReceiveResponseEntity() {
        final var responseEntityMono = executeRequest()
                .entityData(AddResponse.class);
        StepVerifier.create(responseEntityMono)
                .expectSubscription()
                .assertNext(responseEntity -> {
                    Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
                    Assertions.assertNotNull(responseEntity.getBody());
                    Assertions.assertEquals(20, responseEntity.getBody().getAddResult());
                })
                .verifyComplete();
    }


    private TransporterReceiver executeRequest() {
        final var addRequest = getAddRequest();
        return cxfTransporter
                .bodyValue(addRequest)
                .sendAndReceive("http://www.dneonline.com/calculator.asmx?wsdl", TransporterMethod.POST);
    }

    @NotNull
    private static Add getAddRequest() {
        final var addRequest = new Add();
        addRequest.setIntA(10);
        addRequest.setIntB(10);
        return addRequest;
    }


}
