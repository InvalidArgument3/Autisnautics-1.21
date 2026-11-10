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

package com.neep.neepmeat.transport.block.fluid_transport;

import com.google.common.collect.Lists;
import com.neep.meatlib.block.BaseColumnBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.transport.block.fluid_transport.machine.TankBlockEntity;
import com.neep.neepmeat.transport.block.fluid_transport.machine.TankType;
import com.neep.neepmeat.util.ItemUtil;
import com.neep.meatlib.util.Wrenchable;
import net.fabricmc.fabric.api.block.BlockPickInteractionAware;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantItemStorage;
import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TankBlock extends BaseColumnBlock implements EntityBlock, BlockPickInteractionAware, Wrenchable
{

    public static final VoxelShape X_SHAPE = Shapes.or(
            Block.box(0, 0, 2, 16, 16, 14),
            Block.box(0, 1, 1, 16, 15, 15),
            Block.box(0, 2, 0, 16, 14, 16)
    );
    public static final VoxelShape Y_SHAPE = Shapes.or(
            Block.box(0, 0, 2, 16, 16, 14),
            Block.box(1, 0, 1, 15, 16, 15),
            Block.box(2, 0, 0, 14, 16, 16)
    );
    public static final VoxelShape Z_SHAPE = Shapes.or(
            Block.box(2, 0, 0, 14, 16, 16),
            Block.box(1, 1, 0, 15, 15, 16),
            Block.box(0, 2, 0, 16, 14, 16));

    public TankBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings.maxCount(1), settings);
    }

    @Override
    public Item asItem()
    {
        return super.asItem();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return NMBlockEntities.TANK.create(pos, state);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state)
    {
        return new ItemStack(this);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return switch (state.getValue(AXIS))
        {
            case X -> X_SHAPE;
            case Y -> Y_SHAPE;
            case Z -> Z_SHAPE;
        };
    }

    @Override
    public ItemStack getPickedStack(BlockState state, BlockGetter view, BlockPos pos, Player player, HitResult result)
    {
        if (player.isCreative())
        {
            ItemStack stack = super.getCloneItemStack(view, pos, state);
            if (view.getBlockEntity(pos) instanceof TankBlockEntity be)
            {
                if (!be.getStorage(null).isResourceBlank())
                {
                    be.saveToItem(stack);
                }
            }
            return stack;
        }
        else
        {
            if (view.getBlockEntity(pos) instanceof TankBlockEntity be)
            {
                FluidVariant storedVariant = be.getStorage(null).getResource();

                // Allow picking an empty tank in survival mode. Prioritise stacks with the same fluid.
                Item asItem = this.asItem();
                List<ItemStack> matching = Lists.newArrayList();
                List<ItemStack> matchingFluid = Lists.newArrayList();
                for (int i = 0; i < player.getInventory().getContainerSize(); ++i)
                {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.is(asItem))
                    {
                        matching.add(stack);

                        CompoundTag stackNbt = stack.getTagElement("BlockEntityTag");
                        if (stackNbt != null)
                        {
                            FluidVariant fluidVariant = WritableSingleFluidStorage.readFluidVariant(stackNbt);
                            if (fluidVariant.getFluid() == storedVariant.getFluid())
                                matchingFluid.add(stack);
                        }
                    }
                }

                if (!matchingFluid.isEmpty())
                {
                    return matchingFluid.get(0);
                }
                else if (!matching.isEmpty())
                {
                    return matching.get(0);
                }
            }

            return getCloneItemStack(view, pos, state);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder)
    {
        List<ItemStack> stacks = super.getDrops(state, builder);
        BlockEntity blockEntity = builder.getParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof TankBlockEntity be)
        {
            if (!be.getStorage(null).isResourceBlank())
                stacks.forEach(be::saveToItem);

            return stacks;
        }
        return stacks;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (ItemUtil.playerHoldingPipe(player, hand))
            return InteractionResult.PASS;

        if (world.getBlockEntity(pos) instanceof TankBlockEntity tank && tank.onUse(player, hand))
        {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    protected BlockState rotate(BlockState state, BlockHitResult hitResult)
    {
        Direction side = hitResult.getDirection();
        Direction.Axis facing = state.getValue(AXIS);

        return state.setValue(AXIS, Wrenchable.rotateAxis(side.getAxis(), facing));
    }

    public static ItemApiLookup.ItemApiProvider<Storage<FluidVariant>, ContainerItemContext> createStorageProvider(TankType tankType)
    {
        return (stack, ctx) -> new TankItemFluidStorage(stack, ctx, tankType);
    }

    public static class TankItemFluidStorage extends SingleVariantItemStorage<FluidVariant>
    {
        protected final ItemStack stack;
        protected final ContainerItemContext context;
        private final long capacity;

        public TankItemFluidStorage(ItemStack stack, ContainerItemContext context, TankType tankType)
        {
            super(context);
            this.stack = stack;
            this.context = context;
            this.capacity = tankType.capacity();
        }

        @Override
        protected FluidVariant getBlankResource()
        {
            return FluidVariant.BLANK;
        }

        @Nullable
        private CompoundTag getBlockEntityTag(ItemVariant itemVariant)
        {
            CompoundTag nbt = itemVariant.getNbt();
            if (nbt == null || !nbt.contains("BlockEntityTag"))
            {
                return null;
            }
            return nbt.getCompound("BlockEntityTag");
        }

        @Override
        protected FluidVariant getResource(ItemVariant currentVariant)
        {
            CompoundTag nbt = getBlockEntityTag(currentVariant);
            if (nbt == null)
                return FluidVariant.BLANK;

            return WritableSingleFluidStorage.readFluidVariant(nbt);
        }

        @Override
        protected long getAmount(ItemVariant currentVariant)
        {
            CompoundTag nbt = getBlockEntityTag(currentVariant);
            if (nbt == null)
                return 0;

            return WritableSingleFluidStorage.readAmount(nbt);
        }

        @Override
        protected long getCapacity(FluidVariant variant)
        {
//            NbtCompound nbt = getBlockEntityTag(ItemVariant.of(stack));
//            if (nbt == null)
//                return 0;
//
//            return WritableSingleFluidStorage.readCapacity(nbt);
            return capacity;
        }

        @Override
        protected ItemVariant getUpdatedVariant(ItemVariant currentVariant, FluidVariant newResource, long newAmount)
        {
            ItemStack stack = currentVariant.toStack();

            if (newResource.isBlank() || newAmount == 0)
            {
                stack.removeTagKey("BlockEntityTag");
                return ItemVariant.of(stack);
            }

            CompoundTag subNbt = stack.getOrCreateTagElement("BlockEntityTag");

            subNbt.put(WritableSingleFluidStorage.KEY_RESOURCE, newResource.toNbt());
            subNbt.putLong(WritableSingleFluidStorage.KEY_AMOUNT, newAmount);

            return ItemVariant.of(stack);
        }
    }
}
