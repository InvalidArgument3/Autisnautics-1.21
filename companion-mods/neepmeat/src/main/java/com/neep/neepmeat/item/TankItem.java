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
import net.minecraft.world.item.Item;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.client.MeatlibFluidRendering;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.transport.block.fluid_transport.machine.TankType;
import com.neep.meatlib.client.fluid.FluidClient.FluidVariantRendering;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.FluidVariantAttributes;
import com.neep.meatlib.transfer.ResourceAmount;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class TankItem extends FluidComponentItem
{
    private final TankType tankType;

    public static MeatlibBlock.ItemFactory factory(TankType tankType)
    {
        return (block, ctx, settings) -> new TankItem(block,ctx, settings, tankType);
    }

    public TankItem(Block block, RegistrationContext ctx, ItemSettings itemSettings, TankType tankType)
    {
        super(block, ctx, itemSettings);
        this.tankType = tankType;
    }

    public ResourceAmount<FluidVariant> getAmount(ItemStack stack)
    {
        CompoundTag blockNbt = com.neep.neepmeat.util.BlockEntityDataUtil.get(stack);
        if (blockNbt != null)
        {
            FluidVariant variant = FluidVariant.fromNbt(blockNbt.getCompound(WritableSingleFluidStorage.KEY_RESOURCE));
            long amount = 0;
            if (!variant.isBlank())
            {
                amount = blockNbt.getLong(WritableSingleFluidStorage.KEY_AMOUNT);
            }

            return new ResourceAmount<>(variant, amount);
        }

        return new ResourceAmount<>(FluidVariant.BLANK, 0);
    }

    public long getCapacity(ItemStack stack)
    {
//        NbtCompound blockNbt = getBlockEntityNbt(stack);
//        if (blockNbt == null)
//            return 0;

//        return blockNbt.getLong(WritableSingleFluidStorage.KEY_CAPACITY);
        return tankType.capacity();
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext world, List<Component> tooltip, TooltipFlag tooltipContext)
    {
        super.appendHoverText(itemStack, world, tooltip, tooltipContext);
        CompoundTag blockNbt = com.neep.neepmeat.util.BlockEntityDataUtil.get(itemStack);
        if (blockNbt == null)
            return;

        var amount = getAmount(itemStack);
        if (!amount.resource().isBlank())
        {
            long mb = Math.floorDiv(amount.amount(), FluidConstants.BUCKET / 1000);
            int col = FluidVariantRendering.getColor(amount.resource());
            MutableComponent text = FluidVariantAttributes.getName(amount.resource()).copy();
            text.append(": " + mb + "mb");
            text.setStyle(text.getStyle().withColor(col));
            tooltip.add(text);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack)
    {
        return getAmount(stack).amount() > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack)
    {
        long amount = getAmount(stack).amount();
        long capacity = tankType.capacity();
        if (amount > 0)
            return (int) Math.max(13 * amount / (double) capacity, 1);

        return 0;
    }

    @Override
    public int getBarColor(ItemStack stack)
    {
        return MeatlibFluidRendering.getColor(getAmount(stack).resource(), true);
    }

    @Override
    public Component getName(ItemStack stack)
    {
        CompoundTag blockNbt = com.neep.neepmeat.util.BlockEntityDataUtil.get(stack);
        FluidVariant variant;
        if (blockNbt != null && !(variant = FluidVariant.fromNbt(blockNbt.getCompound(WritableSingleFluidStorage.KEY_RESOURCE))).isBlank())
        {
            return Component.translatable(this.getDescriptionId() + ".filled", FluidVariantAttributes.getName(variant));
        }
        return super.getName(stack);
    }
}
