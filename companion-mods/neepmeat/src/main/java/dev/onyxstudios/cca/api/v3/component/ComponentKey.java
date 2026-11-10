package dev.onyxstudios.cca.api.v3.component;

import java.util.Optional;

public interface ComponentKey<T extends Component> {
    T get(Object provider);

    default T getNullable(Object provider) {
        return provider == null ? null : get(provider);
    }

    default Optional<T> maybeGet(Object provider) {
        T value = getNullable(provider);
        return value == null ? Optional.empty() : Optional.of(value);
    }

    /** Cardinal Components synced the holder. Attachments sync themselves, so this is a no-op. */
    default void sync(Object provider) {}
}
