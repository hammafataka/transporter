package dev.mfataka.transporter.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import dev.mfataka.transporter.config.TransporterConfiguration;
import dev.mfataka.transporter.enums.TransporterMethod;
import dev.mfataka.transporter.imp.Transporter;
import dev.mfataka.transporter.imp.TransporterBuilder;

/**
 * Verifies that the bounded ConnectionProvider configured via
 * {@code TransporterConfiguration.maxConnections} actually limits concurrent
 * in-flight HTTP requests and fails fast when the pool is exhausted.
 */
class TransporterConnectionPoolTest extends AbstractTransporterTest {

    /**
     * With maxConnections=1, pendingAcquireTimeout=50ms, and a 1500ms slow
     * endpoint, only the first caller can hold the connection; any caller
     * still waiting after 50ms must fail fast. A CyclicBarrier synchronises
     * all threads at the acquire point so JVM warmup can't mask the race.
     */
    @Test
    void boundedPool_failsFastWhenExhausted() throws Exception {
        final Transporter bounded = TransporterBuilder.testInstance(
                        TransporterConfiguration.builder()
                                .maxConnections(1)
                                .pendingAcquireTimeout(Duration.ofMillis(50))
                                .pendingAcquireMaxCount(2)
                                .build())
                .build();

        final var failures = new ConcurrentLinkedQueue<Throwable>();
        final var successes = new AtomicInteger();
        final int N = 4;
        final var pool = Executors.newFixedThreadPool(N);
        final var ready = new CyclicBarrier(N);
        final var done = new CountDownLatch(N);

        for (int i = 0; i < N; i++) {
            pool.submit(() -> {
                try {
                    ready.await(5, TimeUnit.SECONDS);
                    bounded.sendAndReceive("http://localhost:" + localPort + "/demo/slow/1500",
                                    TransporterMethod.GET)
                            .monoData(String.class)
                            .block();
                    successes.incrementAndGet();
                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    done.countDown();
                }
            });
        }

        assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();

        assertThat(failures)
                .as("with maxConnections=1 and pendingAcquireTimeout=50ms, "
                        + "at least one of %d concurrent callers must fail fast", N)
                .isNotEmpty();
        assertThat(successes.get())
                .as("at most one connection slot may be in use at a time")
                .isLessThan(N);
    }

    /**
     * With a generous maxConnections=10 and acquireTimeout=2s, all three
     * concurrent calls against the slow endpoint should succeed — the pool
     * has room and we're not contending.
     */
    @Test
    void boundedPool_allowsConcurrencyUpToMax() throws Exception {
        final Transporter bounded = TransporterBuilder.testInstance(
                        TransporterConfiguration.builder()
                                .maxConnections(10)
                                .pendingAcquireTimeout(Duration.ofSeconds(2))
                                .build())
                .build();

        final var failures = new ConcurrentLinkedQueue<Throwable>();
        final var successes = new AtomicInteger();
        final int N = 3;
        final var pool = Executors.newFixedThreadPool(N);
        final var done = new CountDownLatch(N);

        for (int i = 0; i < N; i++) {
            pool.submit(() -> {
                try {
                    bounded.sendAndReceive("http://localhost:" + localPort + "/demo/slow/200",
                                    TransporterMethod.GET)
                            .monoData(String.class)
                            .block();
                    successes.incrementAndGet();
                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    done.countDown();
                }
            });
        }

        assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();

        assertThat(failures).as("pool size is enough for the fan-out").isEmpty();
        assertThat(successes.get()).isEqualTo(N);
    }
}
