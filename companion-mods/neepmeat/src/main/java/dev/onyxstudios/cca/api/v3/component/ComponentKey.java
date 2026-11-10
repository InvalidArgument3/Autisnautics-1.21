package dev.onyxstudios.cca.api.v3.component;

public interface ComponentKey<T extends Component> {
    T get(Object provider);

    default T getNullable(Object provider) {
        return provider == null ? null : get(provider);
    }
}
