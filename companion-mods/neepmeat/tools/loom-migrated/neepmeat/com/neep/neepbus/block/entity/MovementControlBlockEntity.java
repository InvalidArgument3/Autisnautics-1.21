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

package com.neep.neepbus.block.entity;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepbus.NeepBus;
import com.neep.neepbus.input.InputInterceptor;
import com.neep.neepbus.util.CachingSender;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepbus.util.SimpleEntry;
import com.neep.neepbus.util.SimpleReadPort;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.util.EnumMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MovementControlBlockEntity extends SyncableBlockEntity implements ConfigProvider, InputInterceptor, MovementControl
{
    public static final ResourceLocation ID = new ResourceLocation(NeepBus.REGISTRY_NAMESPACE, "movement_control");

    private final CachingSender sender = new CachingSender(this::getLevel, worldPosition);

    private final SimpleReadPort forwards = new SimpleReadPort(new SimpleEntry("Forwards"), sender::send);
    private final SimpleReadPort backwards = new SimpleReadPort(new SimpleEntry("Backwards"), sender::send);
    private final SimpleReadPort left = new SimpleReadPort(new SimpleEntry("Left"), sender::send);
    private final SimpleReadPort right = new SimpleReadPort(new SimpleEntry("Right"), sender::send);
    private final SimpleReadPort up = new SimpleReadPort(new SimpleEntry("Up"), sender::send);
    private final SimpleReadPort down = new SimpleReadPort(new SimpleEntry("Down"), sender::send);

    private final EnumMap<Movement, Entry> sendMap = Util.make(new EnumMap<>(Movement.class), m ->
    {
        m.put(Movement.FORWARDS, new Entry(forwards));
        m.put(Movement.BACKWARDS, new Entry(backwards));
        m.put(Movement.LEFT, new Entry(left));
        m.put(Movement.RIGHT, new Entry(right));
        m.put(Movement.UP, new Entry(up));
        m.put(Movement.DOWN, new Entry(down));
    });

    private final NeepBusConfig config = NeepBusConfig.builder(this::sync)
            .output(forwards.entry(), forwards)
            .output(backwards.entry(), backwards)
            .output(left.entry(), left)
            .output(right.entry(), right)
            .output(up.entry(), up)
            .output(down.entry(), down)
            .build();


    public MovementControlBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    @Override
    public void movementInput(Player source, boolean action, Movement key)
    {
        Entry entry = sendMap.get(key);
        if (action)
        {
            entry.send();
        }
        else
        {
            entry.reset();
        }
    }

    @Override
    public ResourceLocation handlerId()
    {
        return ID;
    }

    @Override
    public void close(Player source)
    {
        for (Entry entry : sendMap.values())
        {
            entry.reset();
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        config.writeNbt(nbt);

        ListTag list = new ListTag();
        for (var entry : sendMap.values())
        {
            list.add(FloatTag.valueOf(entry.value));
        }

        nbt.put("values", list);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        config.readNbt(nbt);

        ListTag list = nbt.getList("values", Tag.TAG_FLOAT);

        int i = 0;
        for (var entry : sendMap.values())
        {
            entry.value = list.getFloat(i);
            ++i;
        }
    }

    public void invalidate()
    {
        sender.invalidate();
    }

    @Override
    public FloatList getValues()
    {
        return new FloatArrayList(sendMap.values().stream().map(e -> e.value).toList());
    }

    @Override
    public void setValues(FloatList list)
    {
        int i = 0;
        for (var entry : sendMap.values())
        {
            entry.value = list.getFloat(i);
            i++;
        }
        setChanged();
    }

    private static class Entry
    {
        public Entry(SimpleReadPort port)
        {
            this.port = port;
        }

        public void send()
        {
            port.send(value);
        }

        public void reset()
        {
            port.send(0);
        }

        public SimpleReadPort port;
        public float value = 1;
    }
}
