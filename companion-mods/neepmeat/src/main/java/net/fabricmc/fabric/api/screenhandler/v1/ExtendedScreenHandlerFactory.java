/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */


package net.fabricmc.fabric.api.screenhandler.v1;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

public interface ExtendedScreenHandlerFactory extends MenuProvider {
    void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf);
}
