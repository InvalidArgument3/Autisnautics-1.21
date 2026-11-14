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

package com.neep.neepmeat.block.entity;

import com.neep.neepmeat.api.DataPort;
import com.neep.neepmeat.api.DataResource;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.machine.advanced_integrator.AdvancedIntegratorBlockEntity;
import com.neep.neepmeat.machine.advanced_integrator.SimpleDataPort;
import com.neep.neepmeat.machine.integrator.Integrator;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AdvancedIntegratorStructureBlockEntity extends BigBlockStructureEntity implements Integrator
{
    @Nullable private AdvancedIntegratorBlockEntity parent;

    private final SimpleDataPort port = new SimpleDataPort(this)
    {
        @Override
        public long receive(DataResource variant, long amount, TransactionContext transaction)
        {
            var parent = getParent();
            if (parent != null)
            {
                return parent.getDataStorage().insert(variant, amount, transaction);
            }
            return 0;
        }
    };

    public AdvancedIntegratorStructureBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public DataPort getPort(Void unused)
    {
        return port;
    }

    public AdvancedIntegratorBlockEntity getParent()
    {
        if (parent == null)
        {
            parent = (AdvancedIntegratorBlockEntity) level.getBlockEntity(getControllerPos());
        }
        return parent;
    }

    @Override
    public BlockPos getBlockPos() { return getBlockPos(); }

    @Override
    public boolean canEnlighten() { return true; }

    @Override
    public void setLookPos(BlockPos pos)
    {

    }

    @Override
    public void spawnBeam(Level world, BlockPos pos)
    {
        Integrator.spawnBeam((ServerLevel) world, getControllerPos().above(4), pos);
        world.playSound(null, pos, NMSounds.ADVANCED_INTEGRATOR_CHARGE.get(), SoundSource.BLOCKS, 2, 0.8f);
    }

    @Override
    public long getData(DataResource variant)
    {
        return getParent().getDataStorage().getAmount();
    }

    @Override
    public float extract(DataResource variant, long amount, TransactionContext transaction)
    {
        return getParent().getDataStorage().extract(variant, amount, transaction);
    }
}
