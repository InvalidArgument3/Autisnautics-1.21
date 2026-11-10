/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib for the Autisnautics port. Upstream headers release the
 * work under GPL-3.0-or-later. The LICENSE file shipped with NeepMeat is LGPL-3.0.
 * This port does not relicense the work.
 */

package com.neep.meatlib;

import com.mojang.logging.LogUtils;
import com.neep.meatlib.attachment.MeatlibAttachments;
import com.neep.meatlib.component.MeatlibComponents;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.network.MeatlibPayloads;
import com.neep.meatlib.registry.MeatlibProbe;
import com.neep.meatlib.screen.MeatlibMenus;
import com.neep.meatlib.transfer.MeatlibCapabilities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(MeatLib.ID)
public final class MeatLib {
    public static final String ID = "meatlib";
    public static final Logger LOG = LogUtils.getLogger();
    /** Loom sources call {@code LOGGER}. */
    public static final Logger LOGGER = LOG;
    public static final com.neep.meatlib.config.MeatlibConfig CONFIG = new com.neep.meatlib.config.MeatlibConfig();
    public static final String[] METRIC_PREFIXES = {"f", "p", "n", "µ", "m", "", "k", "M", "G", "T"};

    public MeatLib(ModContainer container, Dist dist) {
        IEventBus bus = container.getEventBus();
        MeatlibSounds.SOUNDS.register(bus);
        MeatlibComponents.COMPONENTS.register(bus);
        MeatlibAttachments.ATTACHMENTS.register(bus);
        MeatlibMenus.MENUS.register(bus);
        MeatlibProbe.CONTEXT.register(bus);
        bus.addListener(MeatlibPayloads::register);
        bus.addListener(MeatlibCapabilities::register);
        NeoForge.EVENT_BUS.addListener(MeatlibEvents::onWrench);
        NeoForge.EVENT_BUS.addListener(MeatlibEvents::afterDatapack);
        if (dist.isClient()) {
            bus.addListener(MeatLibClient::onScreens);
        }
        com.neep.meatlib.config.ConfigFiles.register(container, net.neoforged.fml.config.ModConfig.Type.COMMON, "meatlib.toml", CONFIG);
        LOG.info("meatlib {} loaded", container.getModInfo().getVersion());
    }

    /**
     * Engineering notation, copied from upstream meatlib. Pure Java.
     */
    public static String formatEngineering(double value, int dp, boolean usePrefix, String unit) {
        if (value == 0) {
            return String.format("%." + dp + "f%s", 0.0, unit);
        }
        double log10 = Math.log10(Math.abs(value));
        int threes = (int) Math.floor(log10 / 3);
        value /= Math.pow(10, threes * 3.0);
        int idx = threes + 5;
        if (usePrefix && idx > 0 && idx < METRIC_PREFIXES.length) {
            return String.format("%." + dp + "f%s%s", value, METRIC_PREFIXES[idx], unit);
        }
        return String.format("%." + dp + "fE%d%s", value, threes * 3, unit);
    }
}
