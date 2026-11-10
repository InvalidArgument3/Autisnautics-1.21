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

package com.neep.neepmeat.transport.block.energy_transport.entity;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import com.neep.neepbus.NeepBus;
import com.neep.neepbus.block.entity.ConfigProvider;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.screen_handler.VascularCondenserScreenHandler;
import com.neep.neepmeat.transport.api.pipe.BalancingBloodAcceptor;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import com.neep.neepmeat.transport.block.energy_transport.VascularCondenserBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class VascularCondenserBlockEntity extends SyncableBlockEntity implements ConfigProvider
{
    protected long storedAbs;
    protected long capacity = 1000 * PowerUtil.BASE_POWER;

    private final BloodAcceptor acceptor = new Acceptor();

    protected int target;
    public boolean destroyExcess;

    protected long networkInternal;

    public boolean justPlaced = false;

    private final NeepBusConfig config = NeepBusConfig.builder(this::setChanged)
            .applyChanges(this).build();

    public VascularCondenserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public @Nullable BloodAcceptor getBloodAcceptor(Direction direction)
    {
        return acceptor;
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putLong("stored", storedAbs);
        nbt.putInt("target", target);
        nbt.putBoolean("destroy_excess", destroyExcess);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.storedAbs = nbt.getLong("stored");
        this.target = nbt.getInt("target");
        this.destroyExcess = nbt.getBoolean("destroy_excess");
    }

    public long getStored()
    {
        return storedAbs;
    }

    public long getCapacity()
    {
        return capacity;
    }

    public void openScreen(Player player)
    {
        player.openMenu((SimpleScreenHandlerFactory) (syncId, playerInventory, player1) ->
                new VascularCondenserScreenHandler(playerInventory, syncId, this));
    }

    public void setTarget(int target)
    {
        this.target = target;
    }

    public int getTarget()
    {
        return target;
    }

    public long getStoredAbs()
    {
        return storedAbs;
    }

    public long getNetworkInternal()
    {
        return networkInternal;
    }

    public void setDestroy(boolean d)
    {
        destroyExcess = d;
        setChanged();
    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    private class Acceptor implements BloodAcceptor, BalancingBloodAcceptor
    {
        public Mode getMode()
        {
            return Mode.BALANCE;
        }

        @Override
        public long update(long internalAbs)
        {
            networkInternal = internalAbs;

            if (justPlaced)
            {
                target = (int) networkInternal;
                justPlaced = false;
                setChanged();
            }

            if (getBlockState().getValue(VascularCondenserBlock.ACTIVE))
            {
                long difference = internalAbs - target;
                if (difference > 0)
                {
                    long absorb = Math.min(capacity - storedAbs, difference);
                    storedAbs += absorb;

                    if (!destroyExcess)
                    {
                        return -absorb;
                    }
                    else
                    {
                        return -difference;
                    }
                }
                else if (difference < 0)
                {
                    long inject = Math.min(storedAbs, -difference);
                    storedAbs -= inject;
                    return inject;
                }
            }

            return 0;
        }

        @Override
        public int priority()
        {
            return target;
        }
    }
}
