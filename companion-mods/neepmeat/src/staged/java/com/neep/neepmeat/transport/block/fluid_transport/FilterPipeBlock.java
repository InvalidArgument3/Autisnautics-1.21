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

import com.neep.meatlib.client.MeatlibFluidRendering;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.item.FluidComponentItem;
import com.neep.neepmeat.transport.api.pipe.AbstractAxialFluidPipe;
import com.neep.neepmeat.transport.block.fluid_transport.entity.FilterPipeBlockEntity;
import com.neep.neepmeat.transport.fluid_network.node.BlockFluidPipeVertex;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.FluidVariantAttributes;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageUtil;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class FilterPipeBlock extends AbstractAxialFluidPipe implements EntityBlock
{
    public FilterPipeBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings.factory(FluidComponentItem::new), settings.noOcclusion());
        this.registerDefaultState(this.getStateDefinition().any());
    }

    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        FilterPipeBlockEntity be = world.getBlockEntity(pos, NMBlockEntities.FILTER_PIPE).orElse(null);
        if (be == null)
        {
            NeepMeat.LOGGER.error("Filter pipe block entity has been removed.");
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide() && be != null)
        {
            Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ContainerItemContext.ofPlayerHand(player, hand));
            if (player.isShiftKeyDown())
            {
                // Clear filter
                be.setFilterFluid(FluidVariant.BLANK);
            }
            if (storage != null)
            {
                // Try to set filter
                try (Transaction transaction = Transaction.openOuter())
                {
                    FluidVariant variant = StorageUtil.findExtractableResource(storage, transaction);

                    if (variant != null && !variant.isBlank())
                    {
                        be.setFilterFluid(variant);
                    }
                    transaction.commit();
                }
            }
            sendMessage(player, be.getFilterVariant());
        }
        if (world.isClientSide())
        {
//            world.updateListeners(pos, state, state, Block.REDRAW_ON_MAIN_THREAD);
            world.setBlocksDirty(pos, state, state);
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    public static void sendMessage(Player player, FluidVariant variant)
    {
        player.displayClientMessage(Component.translatable("message." + NeepMeat.NAMESPACE + ".filter_pipe.filter", FluidVariantAttributes.getName(variant)), true);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
       return new FilterPipeBlockEntity(pos, state);
    }

    public static class FilterFluidPipeVertex extends BlockFluidPipeVertex
    {
        public FilterFluidPipeVertex(FluidPipeBlockEntity<FilterFluidPipeVertex> fluidPipeBlockEntity)
        {
            super(fluidPipeBlockEntity);
        }

        @Override
        public long canInsert(ServerLevel world, int inDir, FluidVariant variant, long maxAmount)
        {
            if (parent instanceof FilterPipeBlockEntity be)
            {
                return be.getFilterVariant().isBlank() || variant.equals(be.getFilterVariant()) ? maxAmount : 0;
            }
            return 0;
        }

        @Override
        public boolean canSimplify()
        {
            return false;
        }
    }

    @Environment(value=Dist.CLIENT)
    public static int getTint(BlockState state, BlockAndTintGetter world, BlockPos pos, int index)
    {
        if (world.getBlockEntity(pos) instanceof FilterPipeBlockEntity be)
        {
            return MeatlibFluidRendering.getColor(be.getFilterVariant(), true);
        }
        return 0;
    }
}
