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

package com.neep.meatlib.util;

import com.google.common.collect.Lists;
import com.neep.meatlib.screen.SimpleScreenHandlerPlayer;
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

public class MiscUtil
{
    @Nullable
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A>
    checkType(BlockEntityType<A> givenType, BlockEntityType<E> expectedType, @Nullable BlockEntityTicker<E> serverTicker, @Nullable BlockEntityTicker<E> clientTicker, Level world)
    {
        return expectedType == givenType ? world.isClientSide ? (BlockEntityTicker<A>) clientTicker : (BlockEntityTicker<A>) serverTicker
                : null;
    }

    public static void dropBlockWithNbt(Level world, Block block, BlockPos pos, BlockEntity be, Consumer<ItemStack> stackProcess)
    {
        ItemStack itemStack = block.asItem().getDefaultInstance();
        be.saveToItem(itemStack);
        stackProcess.accept(itemStack);

        ItemEntity itemEntity = new ItemEntity(world, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, itemStack);
        itemEntity.setDefaultPickUpDelay();
        world.addFreshEntity(itemEntity);
    }

    public static void dropBlockWithNbt(Level world, Block block, BlockPos pos, BlockEntity be)
    {
        dropBlockWithNbt(world, block, pos, be, s -> {});
    }

    public static boolean canPlayerModify(Player player, BlockPos pos)
    {
        return player.level().mayInteract(player, pos) && player.mayBuild();
    }

    public static <T extends Entity> T closestEntity(List<T> entityList, Vec3 pos)
    {
        T out;
        if (entityList.isEmpty())
        {
            return null;
        }
        else
        {
            out = entityList.get(0);
        }
        for (T entity : entityList)
        {
            double distance = pos.distanceToSqr(out.getX(), out.getY(), out.getZ());
            if (pos.distanceToSqr(entity.getX(), entity.getY(), entity.getZ()) < distance)
            {
                out = entity;
            }
        }
        return out;
    }

    public static long dropletsToMb(long droplets)
    {
        return Math.floorDiv(droplets, FluidConstants.BUCKET / 1000);
    }

    public static VoxelShape rotateShapeY(VoxelShape shape, float angle)
    {
        double s = Math.sin(Math.toRadians(angle));
        double c = Math.cos(Math.toRadians(angle));

        List<VoxelShape> newShapes = Lists.newArrayList();
        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) ->
        {
            x1 -= 0.5;
            z1 -= 0.5;
            x2 -= 0.5;
            z2 -= 0.5;

            double newX1 = (x1 * c - z1 * s);
            double newZ1 = (x1 * s + z1 * c);
            double newX2 = (x2 * c - z2 * s);
            double newZ2 = (x2 * s + z2 * c);

            newX1 += 0.5;
            newZ1 += 0.5;
            newX2 += 0.5;
            newZ2 += 0.5;

            if (newX1 > newX2)
            {
                var temp = newX1;
                newX1 = newX2;
                newX2 = temp;

            }

            if (newZ1 > newZ2)
            {
                var temp = newZ1;
                newZ1 = newZ2;
                newZ2 = temp;
            }

            newShapes.add(Shapes.box(newX1, y1, newZ1, newX2, y2, newZ2));
        });

        VoxelShape newShape = Shapes.empty();
        for (var shape1 : newShapes)
        {
            newShape = Shapes.join(newShape, shape1, BooleanOp.OR);
        }
        return newShape;
    }

    public static <T> T ifPresentOrNull(CompoundTag nbt, String key, Function<String, T> ifPresent)
    {
        if (nbt.contains(key))
            return ifPresent.apply(nbt.getString(key));
        else
            return null;
    }

    /// Don't use this except where it's already used
    /// I forgot why it's necessary, but it's probably necessary in those cases
    public static void openHandledScreen(Player player, @Nullable SimpleScreenHandlerFactory factory)
    {
        if (player instanceof ServerPlayer serverPlayer)
        {
            ((SimpleScreenHandlerPlayer) serverPlayer).meatlib$openHandledScreen(factory);
        }
    }

    public static <T extends Enum<T>> T cycle(T value, T[] values)
    {
        return values[(value.ordinal() + 1) % values.length];
    }

    public static BlockState placeFacingPredicate(BlockPlaceContext context, Block block, BiPredicate<Level, BlockPos> predicate)
    {
        Level world = context.getLevel();
        Direction facing = context.getClickedFace();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
        if (predicate.test(world, pos))
        {
            return block.defaultBlockState().setValue(DirectionalBlock.FACING, facing.getOpposite());
        }
        else
        {
            BlockPos.MutableBlockPos mutable = context.getClickedPos().mutable();
            for (Direction direction : Direction.values())
            {
                mutable.setWithOffset(context.getClickedPos(), direction);
                if (predicate.test(world, mutable))
                {
                    return block.defaultBlockState().setValue(DirectionalBlock.FACING, direction);
                }
            }
        }

        return block.defaultBlockState().setValue(
                DirectionalBlock.FACING, context.getPlayer().isShiftKeyDown() ?
                context.getNearestLookingDirection().getOpposite() : context.getNearestLookingDirection());
    }
}
