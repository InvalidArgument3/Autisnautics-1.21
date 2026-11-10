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

package com.neep.neepmeat.machine.deployer;

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.machine.motor.MotorBlock;
import com.neep.neepmeat.screen_handler.DeployerScreenHandler;
import com.neep.meatlib.transfer.StorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class DeployerBlock extends BaseFacingBlock implements EntityBlock
{
    public DeployerBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings.noOcclusion());
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (world.getBlockEntity(pos) instanceof DeployerBlockEntity be)
        {
//            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), be.getResource().toStack((int) be.getAmount()));
            Containers.dropContents(world, pos, be.getInventory());
            world.updateNeighbourForOutputSignal(pos,this);
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (player.getItemInHand(hand).getItem() instanceof BlockItem blockitem
            && blockitem.getBlock() instanceof MotorBlock)
        {
            return InteractionResult.PASS;
        }

        if (world.getBlockEntity(pos) instanceof DeployerBlockEntity be && !world.isClientSide())
        {
            player.openMenu((SimpleScreenHandlerFactory) (syncId, playerInventory, player1) -> new DeployerScreenHandler(playerInventory, be.getInventory(), be, syncId));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state)
    {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos)
    {
        if (world.getBlockEntity(pos) instanceof DeployerBlockEntity be)
        {
            return StorageUtil.calculateComparatorOutput(be.getStorage(null));
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.DEPLOYER.create(pos, state);
    }

}
