/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.init;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.enlightenment.EnlightenmentEventManager;
import com.neep.neepmeat.enlightenment.EnlightenmentManager;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.mixin.command.ArgumentTypesAccessor;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;

public class NMCommands
{
    public static void init()
    {
        ArgumentTypeRegistry.registerArgumentType(
                ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "enlightenment_type"), EnlightenmentArgumentType.class, SingletonArgumentInfo.contextFree(EnlightenmentArgumentType::new));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
        {

            dispatcher.register(Commands.literal(NeepMeat.NAMESPACE).then(
                    Commands.literal("enlightenment").requires(s -> s.hasPermission(1))
                            .then(Commands.literal("query").executes(context ->
            {
                Player player = context.getSource().getPlayer();
                if (player != null)
                {
                    EnlightenmentManager manager = player.getComponent(NMComponents.ENLIGHTENMENT_MANAGER);
                    context.getSource().sendSuccess(() -> Component.nullToEmpty(String.format("Acute: %f, chronic: %f", manager.getAcute(), manager.getChronic())), false);
                }
                return 1;
            }))
            .then(Commands.literal("event")
            .then(Commands.argument("id", StringArgumentType.greedyString())
            .suggests(new EnlightenmentEventManager.Suggestions())
            .executes(context ->
            {
                String id = StringArgumentType.getString(context, "id");
                ServerPlayer player = context.getSource().getPlayer();
                if (player != null)
                {
                    var factory = EnlightenmentEventManager.EVENTS.get(ResourceLocation.tryParse(id));
                    if (factory != null)
                    {
                        var world = context.getSource().getLevel();
                        var manager = EnlightenmentEventManager.get(world);
                        manager.spawnFactory(world, player, factory);
                    }
                    else
                    {
                        context.getSource().sendSuccess(() -> Component.nullToEmpty("No such event"), false);
                    }
                }
                return 1;
            })))
            .then(Commands.literal("set")
                    .then(Commands.argument("type", new EnlightenmentArgumentType())
                            .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                    .executes(context ->
                                    {
                                        Type type = context.getArgument("type", Type.class);
                                        double value = DoubleArgumentType.getDouble(context, "value");
                                        ServerPlayer player = context.getSource().getPlayer();
                                        if (player != null)
                                        {
                                            EnlightenmentManager manager = player.getComponent(NMComponents.ENLIGHTENMENT_MANAGER);
                                            switch (type)
                                            {
                                                case ACUTE -> manager.setAcute(value);
                                                case CHRONIC -> manager.setChronic(value);
                                            }
                                            return 0;
                                        }
                                        return 1;
                                    }))))
            ));
        });
    }

    public enum Type implements StringRepresentable
    {
        ACUTE("acute"),
        CHRONIC("chronic")
        ;

        private final String id;

        Type(String id)
        {
            this.id = id;
        }

        @Override
        public String getSerializedName()
        {
            return id;
        }
    }

    public static final class EnlightenmentArgumentType extends StringRepresentableArgument<Type>
    {
        public EnlightenmentArgumentType()
        {
            super(StringRepresentable.fromEnum(Type::values), Type::values);
        }
    }
}
