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

package com.neep.neepmeat.machine.transducer;

import com.neep.meatlib.api.block.NormalPlacementSoundBlock;
import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;

public class TransducerBlock extends BaseBlock implements EntityBlock, NormalPlacementSoundBlock
{
    public TransducerBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        world.getBlockEntity(pos, NMBlockEntities.TRANSDUCER).ifPresent(TransducerBlockEntity::update);
        super.neighborChanged(state, world, pos, block, fromPos, notify);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.TRANSDUCER.create(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.TRANSDUCER, TransducerBlockEntity::serverTick, null, world);
    }

    @Override
    public void destroy(LevelAccessor world, BlockPos pos, BlockState state)
    {
        if (world.isClientSide())
        {
            var rand = world.getRandom();
            for (int i = 0; i < 20; i++)
            {
                world.addParticle(NMParticles.BLOOD_SHOWER,
                        pos.getX() + rand.nextFloat(),
                        pos.getY() + rand.nextFloat(),
                        pos.getZ() + rand.nextFloat(),
                        (rand.nextFloat() - 0.5) * 0.1f,
                        (rand.nextFloat() - 0.5) * 0.1f,
                        (rand.nextFloat() - 0.5) * 0.1f);
            }
        }
        super.destroy(world, pos, state);
    }
}
