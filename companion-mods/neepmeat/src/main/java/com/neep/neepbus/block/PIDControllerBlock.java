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

package com.neep.neepbus.block;

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepbus.NeepBus;
import com.neep.neepbus.block.entity.PIDControllerBlockEntity;
import com.neep.neepbus.screen.PIDControllerScreenHandler;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import com.neep.meatlib.util.MiscUtil;
import net.fabricmc.fabric.api.block.BlockPickInteractionAware;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PIDControllerBlock extends BaseFacingBlock implements DataCable, NeepBusProvider, EntityBlock, BlockPickInteractionAware
{
    public PIDControllerBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings.factory(BlockItem::new), settings);
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {
        if (world.getBlockEntity(pos) instanceof PIDControllerBlockEntity be)
        {
            be.invalidate();
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof PIDControllerBlockEntity be)
        {
            player.openMenu(PIDControllerScreenHandler.getFactory(be.getConfig(), be));
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder)
    {
        List<ItemStack> stacks = super.getDrops(state, builder);
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof PIDControllerBlockEntity be)
        {
            stacks.forEach(be::saveToItem);

            return stacks;
        }

        return super.getDrops(state, builder);
    }

    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag options)
    {
        super.appendHoverText(stack, world, tooltip, options);
        CompoundTag nbt = BlockItem.getBlockEntityData(stack);
        if (nbt != null)
        {
            int dt = nbt.getInt("dt");
            float kp = nbt.getFloat("kp");
            float ki = nbt.getFloat("ki");
            float kd = nbt.getFloat("kd");
            if (Screen.hasShiftDown())
            {
                tooltip.add(Component.translatable("tooltip.neepmeat.pid_controller.dt").append(": " + dt));
                tooltip.add(Component.translatable("tooltip.neepmeat.pid_controller.kp").append(": " + kp));
                tooltip.add(Component.translatable("tooltip.neepmeat.pid_controller.ki").append(": " + ki));
                tooltip.add(Component.translatable("tooltip.neepmeat.pid_controller.kd").append(": " + kd));
            }
        }
    }

    @Override
    public boolean isConnected(Level world, BlockPos pos, BlockState state, Direction direction, boolean outgoing)
    {
        return outgoing;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NeepBus.PID_CONTROLLER_BE.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, NeepBus.PID_CONTROLLER_BE, (world1, pos, state1, blockEntity) -> blockEntity.serverTick(), null, world);
    }

    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state)
    {
        return new ItemStack(this);
    }

    public ItemStack getPickedStack(BlockState state, BlockGetter view, BlockPos pos, Player player, HitResult result)
    {
        if (view.getBlockEntity(pos) instanceof PIDControllerBlockEntity be)
        {
            ItemStack stack = new ItemStack(this);
            be.saveToItem(stack);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    private static class BlockItem extends BaseBlockItem
    {
        public BlockItem(Block block, RegistrationContext ctx, ItemSettings itemSettings)
        {
            super(block, ctx, itemSettings);
        }

        @Override
        public Component getName(ItemStack stack)
        {
            if (getBlockEntityData(stack) != null)
            {
                return super.getName(stack).copy().append(" (Configured)");
            }
            return super.getName(stack);
        }

        public void appendHoverText(ItemStack itemStack, Level world, List<Component> tooltip, TooltipFlag tooltipContext)
        {
            super.appendHoverText(itemStack, world, tooltip, tooltipContext);
            getBlock().appendHoverText(itemStack, world, tooltip, tooltipContext);
        }
    }
}
