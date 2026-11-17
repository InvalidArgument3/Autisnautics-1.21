package dev.wyseman.moltensea;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * A concurrent map that still stores null values. {@code ConcurrentHashMap} refuses a null
 * value, and {@code DimensionDataStorage.get} caches null when the data file is missing.
 * Callers see the same null as a {@code HashMap}: {@code get} returns null, {@code containsKey}
 * stays true, and {@code forEach} passes a null value through.
 */
public final class NullSafeConcurrentMap<K, V> extends AbstractMap<K, V> {
    private static final Object NULL = new Object();
    private final ConcurrentHashMap<K, Object> map = new ConcurrentHashMap<>();

    private Object encode(V value) {
        return value == null ? NULL : value;
    }

    @SuppressWarnings("unchecked")
    private V decode(Object value) {
        return value == NULL ? null : (V) value;
    }

    @Override
    public V get(Object key) {
        return decode(map.get(key));
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public V put(K key, V value) {
        return decode(map.put(key, encode(value)));
    }

    @Override
    public V remove(Object key) {
        return decode(map.remove(key));
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean containsValue(Object value) {
        return value == null ? map.containsValue(NULL) : map.containsValue(value);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        map.forEach((key, value) -> action.accept(key, decode(value)));
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                Iterator<Entry<K, Object>> raw = map.entrySet().iterator();
                return new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return raw.hasNext();
                    }

                    @Override
                    public Entry<K, V> next() {
                        Entry<K, Object> entry = raw.next();
                        return new SimpleEntry<>(entry.getKey(), decode(entry.getValue()));
                    }

                    @Override
                    public void remove() {
                        raw.remove();
                    }
                };
            }

            @Override
            public int size() {
                return map.size();
            }

            @Override
            public void clear() {
                map.clear();
            }
        };
    }
}
