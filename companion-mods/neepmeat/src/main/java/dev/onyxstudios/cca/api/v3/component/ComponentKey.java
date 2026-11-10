package dev.onyxstudios.cca.api.v3.component;

public interface ComponentKey<T extends Component> {
    T get(Object provider);
}
