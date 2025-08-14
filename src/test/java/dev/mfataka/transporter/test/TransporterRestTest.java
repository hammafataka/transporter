package dev.mfataka.transporter.test;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 15.05.2023 10:58
 */
public class TransporterRestTest extends AbstractTransporterTest {


//    @Test
//    public void shouldSendSoapRequestAndReceiveDataBuffer() {
//        final var body = new NameDayResponse(1, 1, new NameDayResponse.NameDay("today"), "cz");
//        final var addResponse = restTransporter
//                .bodyValue(body)
//                .sendAndReceive("https://jsonplaceholder.typicode.com/posts", TransporterMethod.POST)
//                .bufferData()
//                .blockLast();
//
//
//        Assertions.assertNotNull(addResponse);
//        final var bytes = new byte[addResponse.readableByteCount()];
//        addResponse.read(bytes);
//        DataBufferUtils.release(addResponse);
//        final var responseString = new String(bytes, StandardCharsets.UTF_8);
//        Assertions.assertTrue(responseString.contains("{\n" +
//                "  \"day\": 1,\n" +
//                "  \"month\": 1,\n" +
//                "  \"nameDay\": {\n" +
//                "    \"cz\": \"today\"\n" +
//                "  },\n" +
//                "  \"country\": \"cz\""));
//    }
//
//    @Test
//    public void shouldSendRestRequestAndReceiveMono() {
//        final var addResponse = restTransporter
//                .withUrlParam("country", "cz")
//                .withUrlParam("timezone", "Europe/Prague")
//                .sendAndReceive("https://nameday.abalin.net/api/V1/today", TransporterMethod.POST)
//                .monoData(NameDayResponse.class);
//
//        StepVerifier.create(addResponse)
//                .expectSubscription()
//                .assertNext(response -> {
//                    Assertions.assertTrue(Objects.nonNull(response));
//                    Assertions.assertEquals("cz", response.getCountry());
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    public void shouldSendRestRequestAndReceiveResponseEntity() {
//        final var body = new NameDayResponse(1, 1, new NameDayResponse.NameDay("cz"), "cz");
//        final var addResponse = restTransporter
//                .bodyValue(body)
//                .sendAndReceive("https://jsonplaceholder.typicode.com/posts", TransporterMethod.POST)
//                .entityData(NameDayResponse.class);
//
//        StepVerifier.create(addResponse)
//                .expectSubscription()
//                .assertNext(responseEntity -> {
//                    Assertions.assertTrue(Objects.nonNull(responseEntity));
//                    Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
//                    Assertions.assertNotNull(responseEntity.getBody());
//                    Assertions.assertEquals(body.getCountry(), responseEntity.getBody().getCountry());
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    public void shouldSendRestRequestAndReceiveTransporterData() {
//        final var body = new NameDayResponse(1, 1, new NameDayResponse.NameDay("cz"), "cz");
//        final var addResponse = restTransporter
//                .bodyValue(body)
//                .sendAndReceive("https://jsonplaceholder.typicode.com/posts", TransporterMethod.POST)
//                .data(NameDayResponse.class);
//
//        StepVerifier.create(addResponse)
//                .expectSubscription()
//                .assertNext(responseEntity -> {
//                    Assertions.assertTrue(Objects.nonNull(responseEntity));
//                    Assertions.assertTrue(responseEntity.getStatus().is2xxSuccessful());
//                    Assertions.assertNotNull(responseEntity.data());
//                    Assertions.assertEquals(body.getCountry(), responseEntity.data().getCountry());
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    public void shouldSendRestRequestAndReceiveString() {
//        final var body = new NameDayResponse(1, 1, new NameDayResponse.NameDay("today"), "cz");
//        final var response = restTransporter
//                .bodyValue(body)
//                .sendAndReceive("https://jsonplaceholder.typicode.com/posts", TransporterMethod.POST)
//                .data();
//
//        Assertions.assertTrue(response.isOk());
//        Assertions.assertTrue(response.getStatus().is2xxSuccessful());
//        Assertions.assertEquals("{\n" +
//                "  \"day\": 1,\n" +
//                "  \"month\": 1,\n" +
//                "  \"nameDay\": {\n" +
//                "    \"cz\": \"today\"\n" +
//                "  },\n" +
//                "  \"country\": \"cz\",\n" +
//                "  \"id\": 101\n" +
//                "}", response.data());
//    }
}
