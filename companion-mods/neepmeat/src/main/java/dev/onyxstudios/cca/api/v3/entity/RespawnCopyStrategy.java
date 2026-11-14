package dev.onyxstudios.cca.api.v3.entity;

import dev.onyxstudios.cca.api.v3.component.Component;

public interface RespawnCopyStrategy<T extends Component> {
    void copy(T from, T to, boolean lossless, boolean keepInventory);
}
