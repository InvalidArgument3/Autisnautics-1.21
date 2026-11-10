/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Screen registration hook. The upstream auto-layout widget kit is not ported in this
 * milestone; menus registered later subscribe here.
 */
public final class MeatLibClient {
    private MeatLibClient() {}

    public static void onScreens(RegisterMenuScreensEvent event) {
        MeatLib.LOG.info("meatlib client screen hook ready");
    }
}
