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

package com.neep.neepmeat.block;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.util.Wrenchable;
import com.neep.neepmeat.block.entity.MetalBarrelBlockEntity;
import com.neep.neepmeat.init.NMBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class MetalBarrelBlock extends BarrelBlock implements MeatlibBlock, Wrenchable
{
    public static final EnumProperty<Type> TYPE = EnumProperty.create("type", Type.class);

    public MetalBarrelBlock(RegistrationContext ctx, Properties settings)
    {
        super(settings);
        ctx.append(this, new BaseBlockItem(this, ctx, ItemSettings.block()));
        registerDefaultState(getStateDefinition().any().setValue(TYPE, Type.SINGLE).setValue(OPEN, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.METAL_BARREL.create(pos, state);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        if (itemStack.hasCustomHoverName() && world.getBlockEntity(pos) instanceof MetalBarrelBlockEntity be)
        {
            be.setCustomName(itemStack.getHoverName());
        }
    }

    public static @Nullable MetalBarrelBlockEntity getOther(Level world, BlockPos pos, BlockState state)
    {
        Type type = state.getValue(TYPE);
        Direction facing = state.getValue(FACING);

        if (type == Type.SINGLE)
            return null;


        return world.getBlockEntity(
                    pos.relative(type == Type.TOP ? facing.getOpposite() : facing),
                    NMBlockEntities.METAL_BARREL)
                .orElse(null);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (world.isClientSide)
            return InteractionResult.SUCCESS;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof MetalBarrelBlockEntity be)
        {
            @Nullable MetalBarrelBlockEntity be2 = getOther(world, pos, state);
            if (state.getValue(TYPE) == Type.TOP)
            {
                player.openMenu(be.openHandler(player, world, pos, state, be, be2));
                return InteractionResult.CONSUME;
            }
            else if (state.getValue(TYPE) == Type.BOTTOM)
            {
                player.openMenu(be.openHandler(player, world, pos, state, be2, be));
                return InteractionResult.CONSUME;
            }

            player.openMenu(be.openHandler(player, world, pos, state, be, null));
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        Direction side = ctx.getClickedFace();
        BlockState state = this.defaultBlockState().setValue(FACING, ctx.getClickedFace());

        BlockState offsetState = ctx.getLevel().getBlockState(ctx.getClickedPos().relative(side.getOpposite()));

        if (offsetState.is(this)
                && offsetState.getValue(TYPE) == Type.SINGLE
                && offsetState.getValue(FACING) == side)
        {
            return state.setValue(TYPE, Type.TOP);
        }

        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos)
    {
        BlockState superState = super.updateShape(state, direction, neighborState, world, pos, neighborPos);
        Direction facing = state.getValue(FACING);
        Type prevType = state.getValue(TYPE);

        BlockState frontState = world.getBlockState(pos.relative(facing));
        BlockState backState = world.getBlockState(pos.relative(facing.getOpposite()));

        // Become bottom if previously single
        if (frontState.is(this)
            && prevType == Type.SINGLE
            && frontState.getValue(TYPE) == Type.TOP)
        {
            return superState.setValue(TYPE, Type.BOTTOM);
        }
        else if ((!frontState.is(this)
                || frontState.getValue(TYPE) != Type.TOP)
                && prevType == Type.BOTTOM)
        {
            return superState.setValue(TYPE, Type.SINGLE);
        }
        else if ((!backState.is(this)
                || backState.getValue(FACING) != facing)
                && prevType == Type.TOP)
        {
            return superState.setValue(TYPE, Type.SINGLE);
        }

        return superState;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(TYPE);
    }

    @Override
    public boolean onWrench(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, BlockHitResult hitResult, ItemStack stack)
    {
        if (state.getValue(TYPE) != Type.SINGLE)
        {
            return false;
        }
        return Wrenchable.super.onWrench(world, pos, state, player, hand, hitResult, stack);
    }

    public enum Type implements StringRepresentable
    {
        TOP,
        BOTTOM,
        SINGLE;

        @Override
        public String getSerializedName()
        {
            return this.toString().toLowerCase(Locale.ROOT);
        }
    }
}
