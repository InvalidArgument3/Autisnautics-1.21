/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Fabric call sites keep their shape. The body forwards to the NeoForge event.
 */


package net.fabricmc.fabric.api.command.v2;

import com.mojang.brigadier.CommandDispatcher;
import com.neep.meatlib.event.RegisterEvent;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

@FunctionalInterface
public interface CommandRegistrationCallback {
    RegisterEvent<CommandRegistrationCallback> EVENT = new RegisterEvent<>();
    void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment);
}
