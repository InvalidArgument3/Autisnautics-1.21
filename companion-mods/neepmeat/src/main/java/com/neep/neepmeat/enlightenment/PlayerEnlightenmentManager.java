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

import com.neep.neepmeat.api.enlightenment.EnlightenmentEvent;
import com.neep.neepmeat.init.NMComponents;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.List;

public class PlayerEnlightenmentManager implements EnlightenmentManager, ClientTickingComponent, ServerTickingComponent, AutoSyncedComponent
{
    private final Player player;
    private double acuteEnlightenment;
    private double chronicEnlightenment;

    public static final int FILTER_LENGTH = 4;
    private final ByteBuffer dosesBytes = ByteBuffer.allocate(FILTER_LENGTH * Double.BYTES);
    private final DoubleBuffer doses = dosesBytes.asDoubleBuffer();
    private int current = 0;
    private double dose = 0;

    // Events
    private final List<EnlightenmentEvent> events = new ObjectArrayList<>();

    // Event-related flags that don't need to be in a special data structure
    public boolean randomClock;
    public boolean scareVillagers;
    public boolean objectAbove;

    private final RandomSource random = RandomSource.create();

    public PlayerEnlightenmentManager(Player player)
    {
        this.player = player;
    }

    @Override
    public int getTotal()
    {
        return (int) (acuteEnlightenment + chronicEnlightenment);
    }

    @Override
    public void exposeDose(float base, double sqDistance, double split)
    {
        // TODO: half chronic with preventatives

        double corrected = inverseSquare(base, sqDistance);
        acuteEnlightenment += (1 + corrected) * (1 - split);
        chronicEnlightenment += corrected * split;

        dose += corrected;
    }

    public static double inverseSquare(float base, double sqDistance)
    {
//        return base * Math.exp(-Math.sqrt(sqDistance));
        if (sqDistance == 0)
            return base;

        return Mth.clamp(base * 1 / sqDistance, 0, base);
    }

    @Override
    public void addChronic(float amount)
    {
        chronicEnlightenment += amount;
        dose += amount;
    }

    @Override
    public double lastDose()
    {
        // Use a moving average so that emitters can query entities every other tick.

        double value = 0;
        for (int i = 0; i < doses.capacity(); ++i)
            value += doses.get(i);

        return value;
    }

    @Override
    public double getAcute()
    {
        return acuteEnlightenment;
    }

    @Override
    public double getChronic()
    {
        return chronicEnlightenment;
    }

    @Override
    public void setAcute(double amount)
    {
        acuteEnlightenment = amount;
    }

    @Override
    public void setChronic(double amount)
    {
        chronicEnlightenment = amount;
    }

    @Override
    public void serverTick()
    {
        // Gradually reduce these
        acuteEnlightenment = Math.max(0, acuteEnlightenment - 1);
//        chronicEnlightenment += acuteEnlightenment / EnlightenmentUtil.THRESHOLD_NEGLIGIBLE / 50;
        chronicEnlightenment = Math.max(0, chronicEnlightenment - 0.0001);

        doses.put(current, dose);
        current = (current + 1) % doses.capacity();

//        if (lastDose != dose)
//        {
//            lastDose = dose;
            NMComponents.ENLIGHTENMENT_MANAGER.sync(player);
//        }

        dose = 0;

        tickEvents();
    }

    public void addEvent(EnlightenmentEvent event)
    {
        events.add(event);
    }

    private void tickEvents()
    {
        var it = events.iterator();
        while (it.hasNext())
        {
            var event = it.next();

            event.tick();

            if (event.isRemoved())
                it.remove();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientTick()
    {
        Minecraft client = Minecraft.getInstance();
        if (client.player == player)
        {
            if (lastDose() >= 0.3 && random.nextFloat() < 0.5f)
            {
                throw new UnsupportedOperationException("TODO com.neep.neepmeat.client.hud.HUDOverlays");
            }

            if (client.level.getGameTime() % 4 == 0)
            {
                throw new UnsupportedOperationException("TODO com.neep.neepmeat.item.DosimeterItem");
            }
        }
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag)
    {
        this.acuteEnlightenment = tag.getDouble("acute");
        this.chronicEnlightenment = tag.getDouble("chronic");
        this.dose = tag.getDouble("dose");
        byte[] read = tag.getByteArray("doses");

        if (read.length == dosesBytes.capacity())
            System.arraycopy(tag.getByteArray("doses"), 0, dosesBytes.array(), 0, dosesBytes.capacity());

        this.randomClock = tag.getBoolean("randomClock");
        this.objectAbove = tag.getBoolean("objectAbove");

        ListTag eventsNbt = tag.getList("events", Tag.TAG_COMPOUND);
        if (player.level() instanceof ServerLevel serverWorld)
        {
            events.clear();
            for (int i = 0; i < eventsNbt.size(); ++i)
            {
                CompoundTag eventNbt = eventsNbt.getCompound(i);
                ResourceLocation id = ResourceLocation.tryParse(eventNbt.getString("id"));
                var type = EnlightenmentEventManager.EVENTS.get(id);
                if (type != null)
                {
                    events.add(type.create(serverWorld, (ServerPlayer) player));
                }
            }
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag)
    {
        tag.putDouble("acute", acuteEnlightenment);
        tag.putDouble("chronic", chronicEnlightenment);
        tag.putDouble("dose", dose);
        tag.putByteArray("doses", dosesBytes.array());

        tag.putBoolean("randomClock", randomClock);
        tag.putBoolean("objectAbove", objectAbove);

        if (player.level() instanceof ServerLevel)
        {
            ListTag eventsNbt = new ListTag();
            for (var event : events)
            {
                var type = event.getType();
                if (type != null)
                {
                    CompoundTag eventNbt = new CompoundTag();
                    ResourceLocation id = EnlightenmentEventManager.EVENTS.getKey(type);
                    eventNbt.putString("id", id.toString());
                    eventsNbt.add(eventNbt);
                }
            }

            tag.put("events", eventsNbt);
        }
    }

    public static RespawnCopyStrategy<PlayerEnlightenmentManager> RESPAWN_STRATEGY = (from, to, lossless, keepInventory) ->
    {
        to.acuteEnlightenment = 0;
        to.chronicEnlightenment = from.chronicEnlightenment;
        to.randomClock = from.randomClock;
        to.scareVillagers = from.scareVillagers;
        to.objectAbove = from.objectAbove;
    };

    public void sync()
    {
        NMComponents.ENLIGHTENMENT_MANAGER.sync(player);
    }
}
