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

package com.neep.neepmeat.enlightenment;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.enlightenment.EnlightenmentEvent;
import com.neep.neepmeat.api.enlightenment.EnlightenmentUtil;
import com.neep.neepmeat.api.network.EnlightenmentEventPacket;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.transport.interfaces.IServerWorld;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class EnlightenmentEventManager
{
    public static final ResourceLocation EMPTY_ID = new ResourceLocation(NeepMeat.NAMESPACE, "empty");

    public static Registry<EnlightenmentEvent.Factory> EVENTS = FabricRegistryBuilder.<EnlightenmentEvent.Factory>createSimple(
            ResourceKey.createRegistryKey(new ResourceLocation(NeepMeat.NAMESPACE, "enlightenment_event"))).buildAndRegister();

//    protected final List<EnlightenmentEvent> tickingEvents = new ArrayList<>(16);
    private final Random random;

//    public static final EnlightenmentEvent.Factory TEST_EVENT = register(new TickingEnlightenmentEvent.Factory());
    private int counter = 0;

    public EnlightenmentEventManager()
    {
        this.random = new Random();
    }

    public static void init()
    {
        ServerTickEvents.START_WORLD_TICK.register(world ->
        {
            ((IServerWorld) world).getEnlightenmentEventManager().tick(world);
        });
    }

    public static EnlightenmentEventManager get(ServerLevel world)
    {
        return ((IServerWorld) world).getEnlightenmentEventManager();
    }

    protected void tick(ServerLevel world)
    {
        ++counter;

        if (counter >= 40)
        {
            chooseEvent(world);
            counter = 0;
        }
//        tickEvents();
    }

//    protected void tickEvents()
//    {
//        tickingEvents.forEach(EnlightenmentEvent::tick);
//        tickingEvents.removeIf(EnlightenmentEvent::isRemoved);
//    }

    protected void chooseEvent(ServerLevel world)
    {
        for (ServerPlayer player : world.players())
        {
            float thing = EnlightenmentUtil.getEventProbability(player);
            float p = random.nextFloat();

//            double eventsPerHour = (60 * 60) / 2.0 * thing;
//            double eventsPer4Hour = 8 * (60 * 60) / 2.0 * thing;
//            NeepMeat.LOGGER.info("Events per hour: " + eventsPerHour + ", Events per 8 hours: " + eventsPer4Hour);

            if (p < thing)
            {
                if (EVENTS.size() == 0)
                    return;

                // Choose a random factory. If it can't spawn, find one that does.
                List<EnlightenmentEvent.Factory> factories = EVENTS.stream().collect(Collectors.toList());
                Collections.shuffle(factories);
                int i = 0;
                while (!factories.get(i).willSpawn(world, player))
                {
                    ++i;
                    if (i >= factories.size())
                        return;
                }
                EnlightenmentEvent.Factory factory = factories.get(i);
                spawnFactory(world, player, factory);
            }
        }
    }

    public void spawnFactory(ServerLevel world, ServerPlayer player, EnlightenmentEvent.Factory factory)
    {
        EnlightenmentEvent event = factory.create(world, player);
        EnlightenmentEventPacket.send(factory, world, player);

        if (NMComponents.ENLIGHTENMENT_MANAGER.getNullable(player) instanceof PlayerEnlightenmentManager manager)
        {
            manager.addEvent(event);
        }

        event.spawn();
    }

    static
    {
        Registry.register(EVENTS, EMPTY_ID, new EnlightenmentEvent.Factory()
        {
            @Override
            public EnlightenmentEvent create(ServerLevel world, ServerPlayer player)
            {
                return new EnlightenmentEvent()
                {
                    @Override
                    public void tick()
                    {

                    }

                    @Override
                    public boolean isRemoved()
                    {
                        return true;
                    }
                };
            }

            @Override
            public boolean willSpawn(ServerLevel world, ServerPlayer player)
            {
                return false;
            }
        });
    }

    public static class Suggestions implements SuggestionProvider<CommandSourceStack>
    {
        @Override
        public CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> getSuggestions(CommandContext<CommandSourceStack> commandContext, SuggestionsBuilder suggestionsBuilder) throws CommandSyntaxException
        {
            for (var id : EVENTS.keySet())
            {
                suggestionsBuilder.suggest(id.toString());
            }

            return suggestionsBuilder.buildFuture();
        }
    }
}
