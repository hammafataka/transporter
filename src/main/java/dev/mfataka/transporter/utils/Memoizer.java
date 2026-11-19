package dev.mfataka.transporter.utils;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Lazy, thread-safe single-value holder. The supplier is invoked on the first
 * successful {@link #get()}; subsequent calls return the cached result.
 *
 * <p>If the supplier throws, the failure is NOT cached — the next {@code get()}
 * invokes the supplier again. This is intentional for transient setup failures
 * (e.g. SSL context construction races during startup).</p>
 *
 * <p>Concurrent first calls may race and run the supplier more than once. The
 * first successful result wins; all callers receive the same instance.</p>
 */
public final class Memoizer<T> {
    private final Supplier<T> supplier;
    private final AtomicReference<T> cache = new AtomicReference<>();

    public Memoizer(final Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        final T cached = cache.get();
        if (cached != null) return cached;
        final T fresh = supplier.get();
        final T witnessed = cache.compareAndExchange(null, fresh);
        return witnessed == null ? fresh : witnessed;
    }
}
