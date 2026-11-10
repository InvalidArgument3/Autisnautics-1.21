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

package com.neep.neepmeat.machine.power_flower;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.util.ItemUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;

public class PowerFlowerControllerBlock extends BaseBlock implements EntityBlock, PowerFlower
{
    public static final BooleanProperty VALID = BooleanProperty.create("valid");

    public PowerFlowerControllerBlock(RegistrationContext ctx, ItemSettings block, Properties settings)
    {
        super(ctx, block, settings.noOcclusion());
        registerDefaultState(defaultBlockState().setValue(VALID, true));
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context)
    {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.POWER_FLOWER_CONTROLLER.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NMBlockEntities.POWER_FLOWER_CONTROLLER, (world1, pos, state1, blockEntity) -> blockEntity.serverTick(), null, world);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (ItemUtil.playerHoldingPipe(player, hand))
            return InteractionResult.PASS;

        if (!world.isClientSide() && world.getBlockEntity(pos) instanceof PowerFlowerControllerBlockEntity be)
        {
            if (be.multipleControllers)
                player.displayClientMessage(NeepMeat.translationKey("message", "power_flower.multiple_controllers"), true);
            else
                player.displayClientMessage(NeepMeat.translationKey("message", "power_flower.status", be.getNumBlocks(), NeepMeat.CONFIG.powerFlower.maxBlocks.getRealValue(), be.getTop()), true);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(VALID);
    }
}
