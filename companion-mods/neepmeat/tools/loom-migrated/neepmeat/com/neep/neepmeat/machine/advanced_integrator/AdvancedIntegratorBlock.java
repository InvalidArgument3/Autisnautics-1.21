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

package com.neep.neepmeat.machine.advanced_integrator;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;

public class AdvancedIntegratorBlock extends BigBlock<AdvancedIntegratorStructure> implements MeatlibBlock, EntityBlock, DataCable
{
    private final BigBlockPattern volume;

    public AdvancedIntegratorBlock(RegistrationContext ctx, Properties settings)
    {
        super(ctx, settings);
        ctx.append(this, new BaseBlockItem(this, ctx, ItemSettings.block()));
        volume = BigBlockPattern.makeOddCylinder(1, 0, 0, getStructure().defaultBlockState());
    }

    @Override
    protected AdvancedIntegratorStructure registerStructureBlock(RegistrationContext ctx)
    {
        var block =  new AdvancedIntegratorStructure(this, properties);
        return ctx.append(this, block, "advanced_integrator_structure");
    }

    @Override
    public BigBlockPattern getVolume(BlockState blockState)
    {
        return volume;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (player.getItemInHand(hand).isEmpty())
        {
            AdvancedIntegratorBlockEntity be = world.getBlockEntity(pos, NMBlockEntities.ADVANCED_INTEGRATOR).orElse(null);
            if (be != null && !world.isClientSide())
            {
                be.onUse(player);
            }
            return InteractionResult.SUCCESS;
        }

        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return super.getShape(state, world, pos, context);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.ADVANCED_INTEGRATOR.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.ADVANCED_INTEGRATOR,
                (w, pos, state1, be) -> be.serverTick(),
                (w, pos, state1, be) -> be.clientTick(),
                world);
    }
}
