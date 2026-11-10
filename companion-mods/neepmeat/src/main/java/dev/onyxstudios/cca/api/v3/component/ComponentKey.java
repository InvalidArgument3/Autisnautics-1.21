package dev.onyxstudios.cca.api.v3.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

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

    /** Registry id of this key. Null when the key is not backed by an attachment. */
    default @Nullable ResourceLocation getId() {
        return null;
    }

    /** Synced compound attachment for this key, when one is registered. */
    default @Nullable AttachmentType<CompoundTag> attachmentType() {
        return null;
    }

    /**
     * Writes the component and pushes it to tracking clients.
     * NeoForge 21.1 delivers that through {@link IAttachmentHolder#syncData}.
     */
    default void sync(Object provider) {
        AttachmentType<CompoundTag> type = attachmentType();
        if (!(provider instanceof IAttachmentHolder holder) || type == null) {
            return;
        }
        T value = getNullable(provider);
        if (value != null) {
            CompoundTag tag = new CompoundTag();
            value.writeToNbt(tag);
            holder.setData(type, tag);
        }
        holder.syncData(type);
    }
}
