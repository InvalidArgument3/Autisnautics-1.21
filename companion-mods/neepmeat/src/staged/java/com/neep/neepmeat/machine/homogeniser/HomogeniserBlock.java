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

package com.neep.neepmeat.machine.homogeniser;

import com.neep.meatlib.block.BaseHorFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.util.ItemUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.neep.meatlib.util.MiscUtil;
import com.neep.meatlib.util.Wrenchable;
import org.jetbrains.annotations.Nullable;

public class HomogeniserBlock extends BaseHorFacingBlock implements EntityBlock, Wrenchable
{
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 24, 16);

    public HomogeniserBlock(RegistrationContext itemName, ItemSettings itemSettings, Properties settings)
    {
        super(itemName, itemSettings, settings);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type)
    {
        return false;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        var be = world.getBlockEntity(pos, NMBlockEntities.HOMOGENISER).orElse(null);
        if (be != null)
        {
            return InteractionResult.sidedSuccess(ItemUtil.singleVariantInteract(player, hand, be.getItemStorage(null)));
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.HOMOGENISER.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.HOMOGENISER, null, (world1, pos, state1, be) -> be.clientTick(), world);
    }
}
