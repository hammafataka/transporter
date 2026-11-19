package dev.mfataka.transporter.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import dev.mfataka.transporter.config.TransporterConfiguration;

/**
 * Layer-2 stress tests. Each test fails on the current (pre-refactor) code and
 * passes after the refactor. Uses the existing DemoController (/demo/greeting/{name}
 * and /demo/handshake) for round trips.
 */
class TransporterConcurrencyTest extends AbstractTransporterTest {

    private static final int THREADS = 16;
    private static final int CALLS_PER_THREAD = 200;

    /** Bug 1: DefaultTransporter has HashMap headers + LinkedMultiValueMap params + finally reset. */
    @Test
    void transporter_canBeSharedAcrossThreads() throws Exception {
        var base = getTransporterBuilder()
                .withBaseUrl("http://localhost:" + localPort + "/demo")
                .get(null)
                .withHeader("X-Shared", "yes");

        runConcurrently((threadId, callIdx) -> {
            var response = base
                    .sendAndReceive(uri -> uri.path("/greeting/{n}").build("t" + threadId))
                    .transporterDataBlock(String.class);
            assertThat(response).isNotNull();
            assertThat(response.isOk()).as("thread %d call %d", threadId, callIdx).isTrue();
            assertThat(response.data()).isEqualTo("hello t" + threadId);
        });
    }

    /** Bug 2: DefaultTransporter resets body/headers/params in a finally after send. */
    @Test
    void transporter_canBeReusedForManySequentialRequests() {
        var t = getTransporterBuilder()
                .withBaseUrl("http://localhost:" + localPort + "/demo")
                .get(null)
                .withHeader("X-Token", "abc123");

        for (int i = 0; i < 50; i++) {
            final int idx = i;
            var resp = t
                    .sendAndReceive(uri -> uri.path("/greeting/{n}").build("seq" + idx))
                    .transporterDataBlock(String.class);
            assertThat(resp).isNotNull();
            assertThat(resp.isOk()).as("call %d", idx).isTrue();
            assertThat(resp.data()).isEqualTo("hello seq" + idx);
        }
    }

    /** Bug 3: DefaultTransporterBuilder mutates the shared TransporterConfiguration. */
    @Test
    void builderForks_doNotContaminateEachOther() {
        var parent = getTransporterBuilder();
        var a = parent.withBaseUrl("http://localhost:" + localPort + "/demo/a");
        var b = parent.withBaseUrl("http://localhost:" + localPort + "/demo/b");

        assertThat(a.getWebClient()).isNotSameAs(b.getWebClient());
    }

    /** Bug 4: DefaultTransporterReceiver writes back to shared TransporterConfiguration. */
    @Test
    void receiverFlags_doNotLeakAcrossReceivers() {
        var sharedConfig = TransporterConfiguration.builder()
                .timeUnit(TimeUnit.SECONDS)
                .requiredNonNull(false)
                .checkRequiredFields(false)
                .build();

        var t = dev.mfataka.transporter.imp.TransporterBuilder
                .testInstance(sharedConfig)
                .withBaseUrl("http://localhost:" + localPort + "/demo")
                .get(null);

        var r1 = t.sendAndReceive(uri -> uri.path("/handshake").build());

        var r1Flipped = r1.requireNonNull(true);
        assertThat(sharedConfig.isRequiredNonNull())
                .as("shared config must not be mutated by receiver flag setter")
                .isFalse();
        assertThat(r1Flipped).isNotSameAs(r1);
    }

    /** Bug 5: DefaultBaseTransporter (Spring singleton) mutates a shared clientConfig. */
    @Test
    void baseTransporter_concurrentWithCalls_doNotRaceOnSharedConfig() throws Exception {
        var parent = getTransporterBuilder()
                .withBaseUrl("http://localhost:" + localPort + "/demo");

        var errors = new ConcurrentLinkedQueue<Throwable>();
        var done = new CountDownLatch(THREADS);
        var pool = Executors.newFixedThreadPool(THREADS);

        for (int i = 0; i < THREADS; i++) {
            final int id = i;
            pool.submit(() -> {
                try {
                    var t = parent.get(null).withHeader("X-Worker", String.valueOf(id));
                    for (int j = 0; j < CALLS_PER_THREAD; j++) {
                        var resp = t
                                .sendAndReceive(uri -> uri.path("/greeting/{n}").build("w" + id))
                                .transporterDataBlock(String.class);
                        if (resp == null || !resp.isOk()) {
                            throw new AssertionError("bad response for worker " + id);
                        }
                        if (!("hello w" + id).equals(resp.data())) {
                            throw new AssertionError("wrong body for worker " + id + ": " + resp.data());
                        }
                    }
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    done.countDown();
                }
            });
        }

        assertThat(done.await(120, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();
        assertThat(errors).isEmpty();
    }

    @FunctionalInterface
    private interface ThreadedCall {
        void run(int threadId, int callIdx) throws Exception;
    }

    private void runConcurrently(ThreadedCall body) throws Exception {
        var errors = new ConcurrentLinkedQueue<Throwable>();
        var done = new CountDownLatch(THREADS);
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        for (int i = 0; i < THREADS; i++) {
            final int id = i;
            pool.submit(() -> {
                try {
                    for (int j = 0; j < CALLS_PER_THREAD; j++) body.run(id, j);
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    done.countDown();
                }
            });
        }
        assertThat(done.await(120, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();
        assertThat(errors).isEmpty();
    }
}
