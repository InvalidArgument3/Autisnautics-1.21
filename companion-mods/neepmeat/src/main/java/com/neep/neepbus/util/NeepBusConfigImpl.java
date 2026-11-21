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

package com.neep.neepbus.util;

import com.neep.meatlib.blockentity.BlockEntityClientSerializable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NeepBusConfigImpl implements NeepBusConfig
{
    @Nullable private PortMap portMap = null;

    private final List<InputEntry> inputs;
    private final List<OutputEntry> outputs;

    private final List<WritePort> writePorts;
    private final List<ReadPort> readPorts;

    private final Runnable onOutputChanged;
    private final Runnable markDirty;
    private final Runnable applyChanges;

    private final List<ConfigDelegate> delegates = new ObjectArrayList<>();

    public NeepBusConfigImpl(List<ConfigEntry> inputs, List<ConfigEntry> outputs, List<WritePort> writePorts,
                             List<ReadPort> readPorts, Runnable onOutputsChanged, Runnable markDirty, Runnable applyChanges)
    {
        this.inputs = inputs.stream().map(InputEntry::new).toList();
        this.outputs = outputs.stream().map(OutputEntry::new).toList();
        this.writePorts = writePorts;
        this.readPorts = readPorts;
        this.onOutputChanged = onOutputsChanged;
        this.markDirty = markDirty;
        this.applyChanges = applyChanges;
    }

    @Override
    public List<? extends ConfigEntry> getInputs()
    {
        return inputs;
    }

    @Override
    public List<? extends ConfigEntry> getOutputs()
    {
        return outputs;
    }

    @Override
    public void applyChanges()
    {
        applyChanges.run();
    }

    @Override
    public PortMap getPorts()
    {
        if (portMap == null)
        {
//            portMap = new PortMap(
//                    IntStream.range(0, writePorts.size()).boxed()
//                    .collect(Collectors.<Integer, String, WritePort>toMap(i -> inputs.get(i).getAddress(), writePorts::get)),
//                    IntStream.range(0, readPorts.size()).boxed()
//                        .collect(Collectors.<Integer, String, ReadPort>toMap(i -> outputs.get(i).getAddress(), readPorts::get))
//            );
            portMap = PortMap.arrayMap(
                    inputs.stream().map(ConfigEntry::getAddress).toArray(String[]::new), writePorts.toArray(new WritePort[0]),
                    outputs.stream().map(ConfigEntry::getAddress).toArray(String[]::new), readPorts.toArray(new ReadPort[0])
            );
        }

        return portMap;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        ListTag inputs = new ListTag();
        for (var input : this.inputs)
        {
            CompoundTag entry = new CompoundTag();
            entry.putString("address", input.getAddress());

            inputs.add(entry);
        }
        nbt.put("inputs", inputs);

        ListTag outputs = new ListTag();
        for (var output : this.outputs)
        {
            CompoundTag entry = new CompoundTag();
            entry.putString("address", output.getAddress());

            outputs.add(entry);
        }
        nbt.put("outputs", outputs);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        ListTag inputs = nbt.getList("inputs", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(this.inputs.size(), inputs.size()); i++)
        {
            CompoundTag entry = inputs.getCompound(i);

            // Directly set the entry's address to avoid updating the other stuff.
            this.inputs.get(i).entry.setAddress(entry.getString("address"));
        }

        ListTag outputs = nbt.getList("outputs", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(this.outputs.size(), outputs.size()); i++)
        {
            CompoundTag entry = outputs.getCompound(i);

            // Directly set the entry's address to avoid updating the other stuff.
            this.outputs.get(i).entry.setAddress(entry.getString("address"));
        }
    }

    public ConfigDelegate delegate(int port)
    {
        ConfigDelegate delegate = new ConfigDelegate(this, port);
        delegates.add(delegate);
        return delegate;
    }

    private void invalidate()
    {
        portMap = null;
        for (var delegate : delegates)
            delegate.invalidate();;
    }

    public boolean hasOutput(String address)
    {
        return outputs.stream().anyMatch(e -> e.getAddress().equals(address));
    }

    private class InputEntry implements ConfigEntry
    {
        private final ConfigEntry entry;

        private InputEntry(ConfigEntry entry)
        {
            this.entry = entry;
        }

        @Override
        public String getName()
        {
            return entry.getName();
        }

        @Override
        public String getAddress()
        {
            return entry.getAddress();
        }

        @Override
        public void setAddress(String address)
        {
            entry.setAddress(address);
            invalidate();
            markDirty.run();
        }

        @Override
        public Component getTooltip()
        {
            return entry.getTooltip();
        }
    }

    private class OutputEntry implements ConfigEntry
    {
        private final ConfigEntry entry;

        private OutputEntry(ConfigEntry entry)
        {
            this.entry = entry;
        }

        @Override
        public String getName()
        {
            return entry.getName();
        }

        @Override
        public String getAddress()
        {
            return entry.getAddress();
        }

        @Override
        public void setAddress(String address)
        {
            entry.setAddress(address);
            onOutputChanged.run();
            invalidate();
            markDirty.run();
        }

        @Override
        public Component getTooltip()
        {
            return entry.getTooltip();
        }
    }

    public static class Builder
    {
        protected final Runnable markDirty;
        protected List<ConfigEntry> inputs = new ObjectArrayList<>();
        protected List<ConfigEntry> outputs = new ObjectArrayList<>();
        protected List<WritePort> writePorts = new ObjectArrayList<>();
        protected List<ReadPort> readPorts = new ObjectArrayList<>();
        protected Runnable onOutputsChanged = () -> {};
        protected Runnable applyChanges = () -> {};

        public Builder(Runnable markDirty)
        {
            this.markDirty = markDirty;
        }

        public NeepBusConfigImpl build()
        {
            return new NeepBusConfigImpl(inputs, outputs, writePorts, readPorts, onOutputsChanged, markDirty, applyChanges);
        }

        public Builder inputs(List<ConfigEntry> inputs, List<WritePort> inputPorts)
        {
            this.inputs = inputs;
            this.writePorts = inputPorts;
            return this;
        }

        public Builder input(ConfigEntry input, WritePort inputPort)
        {
            this.inputs.add(input);
            this.writePorts.add(inputPort);
            return this;
        }

        public Builder outputs(List<ConfigEntry> outputs, List<ReadPort> readPorts)
        {
            this.outputs = outputs;
            this.readPorts = readPorts;
            return this;
        }

        public Builder outputs(SimpleReadPort... ports)
        {
            for (var p : ports)
            {
                readPorts.add(p);
                outputs.add(p.entry());
            }
            return this;
        }

        public Builder output(ConfigEntry output, ReadPort readPort)
        {
            this.readPorts.add(readPort);
            this.outputs.add(output);
            return this;
        }

        public Builder onOutputsChanged(Runnable changed)
        {
            this.onOutputsChanged = changed;
            return this;
        }

        @Override
        public Builder applyChanges(BlockEntity be)
        {
            applyChanges = () ->
            {
                if (be instanceof BlockEntityClientSerializable becs)
                    becs.sync();

                NeepBusUtil.floodUpdate(be.getLevel(), be.getBlockPos());
            };
            return this;
        }
    }
}
