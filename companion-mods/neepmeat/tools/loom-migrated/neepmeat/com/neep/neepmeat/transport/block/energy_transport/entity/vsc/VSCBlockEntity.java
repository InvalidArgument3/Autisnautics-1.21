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

package com.neep.neepmeat.transport.block.energy_transport.entity.vsc;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepbus.NBTB;
import com.neep.neepbus.block.entity.ConfigProvider;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepbus.util.SimpleEntry;
import com.neep.neepbus.util.WritePort;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.neepmeat.transport.api.pipe.AbstractBloodAcceptor;
import com.neep.neepmeat.transport.api.pipe.BalancingBloodAcceptor;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import com.neep.neepmeat.transport.block.energy_transport.VSCBlock;
import com.neep.neepmeat.transport.blood_network.BloodTransferChangeListener;
import com.neep.neepmeat.transport.screen.VSCScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class VSCBlockEntity extends SyncableBlockEntity implements ExtendedScreenHandlerFactory, ConfigProvider
{
    private final WritePort inputPort = data -> setDesiredPower(data.intValue());

    private final NeepBusConfig config = NeepBusConfig.builder(this::setChanged)
            .input(new SimpleEntry("Power", new NBTB().w("Set VSC's target power to the given number").build()), inputPort)
            .applyChanges(this)
            .build();

    protected long powerToSource;
    private final LazyBlockApiCache<BloodAcceptor, Direction> cache;
    private long powerToSink;

    protected final AbstractBloodAcceptor normalSink = new NormalSinkAcceptor();
    protected final BloodAcceptor normalSource = new NormalSourceAcceptor();
    protected final AbstractBloodAcceptor balanceSink = new BalancingSinkAcceptor();
    protected final BloodAcceptor balanceSource = new BalancingSourceAcceptor();

    protected int desiredPower = 0;
    protected boolean activeWithRedstone = false;
    private int priority;
    private boolean cutOff = false;

    public VSCBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        cache = addListener(LazyBlockApiCache.of(BloodAcceptor.SIDED, this.worldPosition.relative(getBlockState().getValue(VSCBlock.FACING)), this::getLevel, () -> getBlockState().getValue(VSCBlock.FACING).getOpposite()));
    }

    public BloodAcceptor getBloodAcceptor(Direction face)
    {
        if (getBlockState().getValue(VSCBlock.FACING) == face)
        {
            return getSourceAcceptor();
        }
        return getSinkAcceptor();
    }

    protected BloodAcceptor getSourceAcceptor()
    {
        return mode() == VSCMode.NORMAL ? normalSource : balanceSource;
    }

    protected AbstractBloodAcceptor getSinkAcceptor()
    {
        return mode() == VSCMode.NORMAL ? normalSink : balanceSink;
    }

    public boolean isActive()
    {
        return getBlockState().getValue(VSCBlock.ACTIVE);
    }

    public void setDesiredPower(int power)
    {
        power = Math.max(0, power);

        if (desiredPower != power)
        {
            desiredPower = power;
            BloodTransferChangeListener network = getSinkAcceptor().getNetwork();
            if (network != null)
            {
                network.updateTransfer(null);
            }
        }
    }

    public int getDesiredPower()
    {
        return desiredPower;
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putInt("desired_power", desiredPower);
        nbt.putInt("priority", priority);
        nbt.putBoolean("active_with_redstone", activeWithRedstone);
        nbt.putBoolean("cut_off", cutOff);

        nbt.put("config", config.writeNbt(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.desiredPower = nbt.getInt("desired_power");
        this.priority = nbt.getInt("priority");
        this.activeWithRedstone = nbt.getBoolean("active_with_redstone");
        this.cutOff = nbt.getBoolean("cut_off");
//        VSCMode balance = VSCMode.values()[nbt.getInt("mode")];
//        updateBalance(getCachedState().get(VSCBlock.MODE));

        this.config.readNbt(nbt.getCompound("config"));
    }

    private void updateBalance(VSCMode balance)
    {
//        if (this.mode != balance)
//        {
//            this.mode = balance;
//
//            switch (mode)
//            {
//                case NORMAL ->
//                {
//                    sourceAcceptor = new BalancingSourceAcceptor();
//                    sinkAcceptor = new BalancingSinkAcceptor();
//                }
//                case BALANCE ->
//                {
//                    sourceAcceptor = new NormalSourceAcceptor();
//                    sinkAcceptor = new NormalSinkAcceptor();
//                }
//            }

            if (level != null)
            {
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(VSCBlock.MODE, balance));
            }
//        }
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf)
    {
        buf.writeInt(desiredPower);
    }

    @Override
    public Component getDisplayName()
    {
        return Component.nullToEmpty("Vascular Source Converter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player)
    {
        return new VSCScreenHandler(inv, syncId, this);
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
    }

    public void updateState(int power)
    {
        boolean prevActive = getBlockState().getValue(VSCBlock.ACTIVE);
        boolean nextActive = power > 0 == activeWithRedstone;
        if (nextActive != prevActive)
            updateActive(nextActive);

        updateFrontAcceptor();
    }

    private void updateActive(boolean nextActive)
    {
        getLevel().setBlockAndUpdate(worldPosition, getBlockState().setValue(VSCBlock.ACTIVE, nextActive));

        BloodTransferChangeListener network = getSinkAcceptor().getNetwork();
        if (network != null)
        {
            network.updateTransfer(null);
        }
    }

    public void updateFrontAcceptor()
    {
        BloodAcceptor found = cache.find();
        if (found != null)
        {
            found.updateInflux((float) powerToSource / PowerUtil.referencePower());

            // Not sure how this will behave across unloads, but here it is.
            // I've also forgotten what this is supposed to do.
            found.setChangeListener(this::changed);
        }
    }

    public void changed(@Nullable BloodAcceptor changed)
    {
        BloodTransferChangeListener sinkListener = getSinkAcceptor().getNetwork();
        if (sinkListener != null)
        {
            sinkListener.updateTransfer(null);
        }
    }

    public void changeMode()
    {
        activeWithRedstone = !activeWithRedstone;
        updateState(getLevel().getBestNeighborSignal(worldPosition));
        setChanged();
    }

    public void setMode(VSCMode mode)
    {
        updateBalance(mode);
        setChanged();
    }

    public NeepBusConfig getConfig()
    {
        return config;
    }

    public void invalidatePortCache()
    {
        // Nothing to do here as there is no sender
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
        setChanged();
    }

    public VSCMode mode()
    {
        return getBlockState().getValue(VSCBlock.MODE);
    }

    public boolean cutOff()
    {
        return cutOff;
    }

    public void setProperties(int power, int priority, boolean cutOff, VSCMode mode)
    {
        this.desiredPower = power;
        this.priority = priority;
        this.cutOff = cutOff;
        this.setMode(mode);
        updateActive(isActive());
        setChanged();
    }

    // Represents the sink with respect to this block - the side that absorbs power
    private class NormalSinkAcceptor extends AbstractBloodAcceptor
    {
        @Override
        public Mode getMode()
        {
            return Mode.ACTIVE_SINK;
        }

        @Override
        public float updateInflux(float influx)
        {
            long absInflux = (long) (influx * PowerUtil.BASE_POWER);
            if (isActive() && (absInflux >= desiredPower || !cutOff))
            {
                var minimum = Math.min(absInflux, desiredPower);
                powerToSource = minimum;
                updateFrontAcceptor();
                return (float) minimum / PowerUtil.BASE_POWER;
            }
            else
            {
                powerToSource = 0;
                updateFrontAcceptor();
                return 0;
            }
        }
    }

    private class NormalSourceAcceptor implements BloodAcceptor
    {
        @Override
        public Mode getMode()
        {
            return Mode.SOURCE;
        }

        @Override
        public long getOutput()
        {
            return powerToSource;
        }

    }

    private class BalancingSinkAcceptor extends AbstractBloodAcceptor implements BalancingBloodAcceptor
    {

        @Override
        public long update(long internalAbs)
        {
            if (isActive())
            {
                if (powerToSink < 0)
                {
                    // Power is only removed from this network when overflow < 0
                    if (internalAbs >= desiredPower || !cutOff)
                    {
                        var minimum = Math.min(internalAbs, desiredPower);
                        VSCBlockEntity.this.powerToSource = minimum;
                        return -minimum;
                    }
                    else
                    {
                        VSCBlockEntity.this.powerToSource = 0;
                        return 0;
                    }
                }
                else
                {
                    // We don't need to remove power from this network
                    powerToSource = desiredPower;

                    // Inject overflow
                    return powerToSink;
                }
            }

            return 0;
        }

        @Override
        public int priority()
        {
            return 0;
        }
    }

    private class BalancingSourceAcceptor implements BalancingBloodAcceptor
    {
        @Override
        public long update(long internalAbs)
        {
            if (isActive())
            {
                // A change of paradigm
                // Balancing instead of emitting
                powerToSink = internalAbs - desiredPower;
                if (powerToSink > 0)
                {
                    return -powerToSink;
                }
                else if (internalAbs < desiredPower)
                {
                    return powerToSource;
                }
            }
            else
            {
                powerToSource = 0;
            }

            return 0;
        }

        @Override
        public int priority()
        {
            return desiredPower;
        }
    }
}