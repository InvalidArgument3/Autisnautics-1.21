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

package com.neep.neepmeat.item;

import com.neep.meatlib.item.MeatlibItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;

public class BaseMinecartItem extends MinecartItem implements MeatlibItem
{
    private final MinecartFactory factory;

    public BaseMinecartItem(Properties settings, MinecartFactory factory)
    {
        super(AbstractMinecart.Type.RIDEABLE, settings);
        this.factory = factory;
        DispenserBlock.registerBehavior(this, vanillaDispenserBehaviour(factory));
    }

    public static DispenseItemBehavior vanillaDispenserBehaviour(MinecartFactory factory)
    {
        return new DefaultDispenseItemBehavior()
        {
            private final DefaultDispenseItemBehavior defaultBehavior = new DefaultDispenseItemBehavior();

            @Override
            public ItemStack execute(BlockSource pointer, ItemStack stack)
            {
                BlockState blockState2;
                double g;
                RailShape railShape;
                Direction direction = pointer.getBlockState().getValue(DispenserBlock.FACING);
                ServerLevel world = pointer.getLevel();
                double d = pointer.x() + (double) direction.getStepX() * 1.125;
                double e = Math.floor(pointer.y()) + (double) direction.getStepY();
                double f = pointer.z() + (double) direction.getStepZ() * 1.125;
                BlockPos blockPos = pointer.getPos().relative(direction);
                BlockState blockState = world.getBlockState(blockPos);
                railShape = blockState.getBlock() instanceof BaseRailBlock railBlock ? blockState.getValue(railBlock.getShapeProperty()) : RailShape.NORTH_SOUTH;
                if (blockState.is(BlockTags.RAILS))
                {
                    g = railShape.isAscending() ? 0.6 : 0.1;
                }
                else if (blockState.isAir() && world.getBlockState(blockPos.below()).is(BlockTags.RAILS))
                {
                    RailShape railShape22;
                    blockState2 = world.getBlockState(blockPos.below());
                    railShape22 = blockState2.getBlock() instanceof BaseRailBlock railBlock
                            ? blockState2.getValue(railBlock.getShapeProperty())
                            : RailShape.NORTH_SOUTH;
                    g = direction == Direction.DOWN || !railShape22.isAscending() ? -0.9 : -0.4;
                }
                else
                {
                    return this.defaultBehavior.dispense(pointer, stack);
                }

                AbstractMinecart minecart = factory.create(world, d, e + g, f);
                if (stack.hasCustomHoverName())
                {
                    minecart.setCustomName(stack.getHoverName());
                }
                world.addFreshEntity(minecart);
                stack.shrink(1);
                return stack;
            }

            @Override
            protected void playSound(BlockSource pointer)
            {
                pointer.getLevel().levelEvent(LevelEvent.SOUND_DISPENSER_DISPENSE, pointer.getPos(), 0);
            }
        };
    }

    protected InteractionResult vanillaPlacement(UseOnContext context)
    {
        BlockPos blockPos;
        Level world = context.getLevel();
        BlockState blockState = world.getBlockState(blockPos = context.getClickedPos());

        if (!blockState.is(BlockTags.RAILS))
            return InteractionResult.FAIL;

        ItemStack itemStack = context.getItemInHand();
        if (!world.isClientSide)
        {
            RailShape railShape = blockState.getBlock() instanceof BaseRailBlock ? blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
            double d = 0.0;
            if (railShape.isAscending())
            {
                d = 0.5;
            }

            AbstractMinecart minecart = factory.create(world, blockPos.getX() + 0.5, blockPos.getY() + 0.0625, blockPos.getZ() + 0.5);

            if (itemStack.hasCustomHoverName())
                minecart.setCustomName(itemStack.getHoverName());

            world.addFreshEntity(minecart);
            world.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
        }
        itemStack.shrink(1);
        return InteractionResult.sidedSuccess(world.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        return vanillaPlacement(context);
    }

    @FunctionalInterface
    public interface MinecartFactory
    {
        AbstractMinecart create(Level world, double x, double y, double z);
    }
}
