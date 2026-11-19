package dev.mfataka.transporter.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class MemoizerTest {

    @Test
    void runsSupplierOnce_andReturnsCachedValueOnSubsequentCalls() {
        var calls = new AtomicInteger();
        var memo = new Memoizer<>(() -> { calls.incrementAndGet(); return "v"; });

        assertThat(memo.get()).isEqualTo("v");
        assertThat(memo.get()).isEqualTo("v");
        assertThat(memo.get()).isEqualTo("v");
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void doesNotCacheFailure_retriesOnNextCall() {
        var calls = new AtomicInteger();
        var memo = new Memoizer<String>(() -> {
            if (calls.incrementAndGet() == 1) throw new IllegalStateException("boom");
            return "v";
        });

        assertThatThrownBy(memo::get).isInstanceOf(IllegalStateException.class);
        assertThat(memo.get()).isEqualTo("v");
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void concurrentFirstCalls_allReceiveSameValue_supplierMayRunMultipleTimes() throws Exception {
        var supplied = new AtomicInteger();
        var memo = new Memoizer<>(() -> { supplied.incrementAndGet(); return new Object(); });

        int N = 32;
        var pool = Executors.newFixedThreadPool(N);
        var go = new CountDownLatch(1);
        var done = new CountDownLatch(N);
        var firstResult = new Object[]{null};

        for (int i = 0; i < N; i++) {
            pool.submit(() -> {
                try { go.await(); } catch (InterruptedException e) { return; }
                var v = memo.get();
                synchronized (firstResult) {
                    if (firstResult[0] == null) firstResult[0] = v;
                    else assertThat(v).isSameAs(firstResult[0]);
                }
                done.countDown();
            });
        }
        go.countDown();
        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();
    }
}
