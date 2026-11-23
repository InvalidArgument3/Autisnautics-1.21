/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib.init;

import com.neep.meatlib.MeatLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MeatlibSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MeatLib.ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> UI_BEEP = register("ui_beep");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLC_SELECT = register("plc_select");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLICK = register("click");
    public static final DeferredHolder<SoundEvent, SoundEvent> WRENCH_CLICK = register("wrench_click");

    private MeatlibSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String path) {
        return SOUNDS.register(path, () -> SoundEvent.createVariableRangeEvent(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MeatLib.ID, path)));
    }
}
