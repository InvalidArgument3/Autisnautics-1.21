/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.attachment;

import com.neep.meatlib.MeatLib;
import net.minecraft.core.UUIDUtil;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Entity and player attachments. Upstream stored these with Cardinal Components.
 * A fresh UUID is assigned the first time the attachment is read.
 */
public final class MeatlibAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MeatLib.ID);

    public static final Supplier<AttachmentType<UUID>> UNIQUE_ID = ATTACHMENTS.register("unique_id",
            () -> AttachmentType.builder(UUID::randomUUID)
                    .serialize(UUIDUtil.CODEC)
                    .copyOnDeath()
                    .build());

    private MeatlibAttachments() {}
}
