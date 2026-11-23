/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Player data that upstream stored in Cardinal Components. copyOnDeath keeps it.
 */

package com.neep.neepmeat.component;

import com.mojang.serialization.Codec;
import com.neep.neepmeat.NeepMeat;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class NMAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, NeepMeat.ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> COMPRESSED_AIR = ATTACHMENTS.register("compressed_air",
            () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG).copyOnDeath().build());

    private NMAttachments() {}
}
