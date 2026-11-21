/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Each Cardinal component key is a synced CompoundTag attachment.
 */

package com.neep.neepmeat.component;

import com.neep.neepmeat.NeepMeat;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class SyncedComponents {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, NeepMeat.ID);

    private SyncedComponents() {}

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CompoundTag>> ENLIGHTENMENT =
            ATTACHMENTS.register("enlightenment_manager", () ->
                    AttachmentType.<CompoundTag>builder(() -> new CompoundTag())
                            .serialize(CompoundTag.CODEC)
                            .sync(ByteBufCodecs.COMPOUND_TAG)
                            .build());

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }

    public static ComponentKey<TagComponent> key(String name) {
        DeferredHolder<AttachmentType<?>, AttachmentType<CompoundTag>> holder = ATTACHMENTS.register(name, () ->
                AttachmentType.<CompoundTag>builder(() -> new CompoundTag())
                        .serialize(CompoundTag.CODEC)
                        .sync(ByteBufCodecs.COMPOUND_TAG)
                        .build());
        return new Key(holder);
    }

    /** NBT view of a synced attachment. Callers that still need a typed component read this tag. */
    public static final class TagComponent implements Component {
        private final CompoundTag tag;

        public TagComponent(CompoundTag tag) {
            this.tag = tag;
        }

        public CompoundTag tag() {
            return tag;
        }

        @Override
        public void readFromNbt(CompoundTag incoming) {
            for (String key : incoming.getAllKeys()) {
                tag.put(key, incoming.get(key));
            }
        }

        @Override
        public void writeToNbt(CompoundTag outgoing) {
            for (String key : tag.getAllKeys()) {
                outgoing.put(key, tag.get(key));
            }
        }
    }

    private static final class Key implements ComponentKey<TagComponent> {
        private final DeferredHolder<AttachmentType<?>, AttachmentType<CompoundTag>> holder;

        private Key(DeferredHolder<AttachmentType<?>, AttachmentType<CompoundTag>> holder) {
            this.holder = holder;
        }

        @Override
        public AttachmentType<CompoundTag> attachmentType() {
            return holder.get();
        }

        @Override
        public net.minecraft.resources.ResourceLocation getId() {
            return holder.getId();
        }

        @Override
        public TagComponent get(Object provider) {
            if (provider instanceof IAttachmentHolder attachmentHolder) {
                return new TagComponent(attachmentHolder.getData(attachmentType()));
            }
            return new TagComponent(new CompoundTag());
        }
    }
}
