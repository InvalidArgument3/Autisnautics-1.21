package dev.wyseman.moltensea;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class NullSafeConcurrentMapTest {
    /** The same branches as {@code DimensionDataStorage.get}. */
    private static String load(Map<String, String> cache, String key, Supplier<String> read, AtomicInteger reads) {
        String saved = cache.get(key);
        if (saved == null && !cache.containsKey(key)) {
            reads.incrementAndGet();
            saved = read.get();
            cache.put(key, saved);
        }
        return saved;
    }

    @Test
    void missingFileStaysCachedAsNull() {
        NullSafeConcurrentMap<String, String> cache = new NullSafeConcurrentMap<>();
        AtomicInteger reads = new AtomicInteger();

        assertNull(load(cache, "sable", () -> null, reads));
        assertTrue(cache.containsKey("sable"));
        assertNull(cache.get("sable"));
        assertEquals(1, reads.get());

        assertNull(load(cache, "sable", () -> "should-not-read", reads));
        assertEquals(1, reads.get());
    }

    @Test
    void matchesHashMapNullSemantics() {
        Map<String, String> hash = new HashMap<>();
        Map<String, String> safe = new NullSafeConcurrentMap<>();
        hash.put("missing", null);
        safe.put("missing", null);
        hash.put("present", "data");
        safe.put("present", "data");

        assertEquals(hash.get("missing"), safe.get("missing"));
        assertEquals(hash.containsKey("missing"), safe.containsKey("missing"));
        assertEquals(hash.get("present"), safe.get("present"));
        assertEquals(hash.containsKey("absent"), safe.containsKey("absent"));
        assertEquals(hash.containsValue(null), safe.containsValue(null));

        List<String> hashed = new ArrayList<>();
        List<String> safed = new ArrayList<>();
        hash.forEach((key, value) -> hashed.add(key + "=" + value));
        safe.forEach((key, value) -> safed.add(key + "=" + value));
        hashed.sort(String::compareTo);
        safed.sort(String::compareTo);
        assertEquals(hashed, safed);

        assertNull(safe.put("missing", "loaded"));
        assertEquals("loaded", safe.get("missing"));
        assertEquals("loaded", safe.put("missing", null));
        assertNull(safe.get("missing"));
        assertTrue(safe.containsKey("missing"));
        assertNull(safe.remove("missing"));
        assertFalse(safe.containsKey("missing"));
    }

    @Test
    void forEachAllowsConcurrentPuts() throws Exception {
        NullSafeConcurrentMap<String, String> cache = new NullSafeConcurrentMap<>();
        cache.put("seed", null);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Thread iterating = new Thread(() -> {
            try {
                cache.forEach((key, value) -> {
                    started.countDown();
                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (Throwable t) {
                failure.set(t);
            }
        });
        iterating.start();
        started.await();
        for (int i = 0; i < 200; i++) {
            cache.put("k" + i, i % 5 == 0 ? null : "v" + i);
        }
        release.countDown();
        iterating.join();
        assertNull(failure.get());
        assertEquals(201, cache.size());
        assertNull(cache.get("k0"));
        assertTrue(cache.containsKey("k0"));
    }
}
