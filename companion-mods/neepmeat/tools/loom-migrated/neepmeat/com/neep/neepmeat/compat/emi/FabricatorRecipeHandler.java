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

package com.neep.neepmeat.compat.emi;

import com.google.common.collect.Lists;
import com.neep.neepmeat.machine.fabricator.FabricatorScreen;
import com.neep.neepmeat.machine.fabricator.FabricatorScreenHandler;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import java.util.List;

@Environment(EnvType.CLIENT)
public class FabricatorRecipeHandler implements StandardRecipeHandler<FabricatorScreenHandler>
{
    @Override
    public List<Slot> getInputSources(FabricatorScreenHandler handler)
    {
        List<Slot> list = Lists.newArrayList();
        int invStart = 8;
        for (int i = invStart; i < invStart + 36; i++)
        {
            list.add(handler.getSlot(i));
        }
        return list;
//        return List.of();
    }

    @Override
    public List<Slot> getCraftingSlots(FabricatorScreenHandler handler)
    {
        List<Slot> list = Lists.newArrayList();
        for (int i = 0; i < 9; i++)
        {
            list.add(handler.getSlot(i));
        }
        return list;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<FabricatorScreenHandler> context)
    {
//        return StandardRecipeHandler.super.canCraft(recipe, context);
        return true;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<FabricatorScreenHandler> context)
    {
        StandardRecipeHandler.super.craft(recipe, context);

        context.getScreenHandler().fillRecipe.emitter().accept(recipe.getId());

        return true;
    }

    public void clickSlot(FabricatorScreenHandler handler, int slotId, ItemStack newStack)
    {
        Minecraft client = Minecraft.getInstance();
        NonNullList<Slot> defaultedList = handler.slots;
        int size = defaultedList.size();
        List<ItemStack> list = Lists.newArrayListWithCapacity(size);

        int click = 1;

        for (Slot slot : defaultedList)
        {
            list.add(slot.getItem().copy());
        }

//        handler.onSlotClick(slotId, click, SlotActionType.SWAP, client.player);
        handler.slots.get(slotId).setByPlayer(newStack);

        Int2ObjectMap<ItemStack> int2ObjectMap = new Int2ObjectOpenHashMap<>();

        for (int j = 0; j < size; ++j)
        {
            ItemStack itemStack = list.get(j);
            ItemStack itemStack2 = defaultedList.get(j).getItem();
            if (!ItemStack.matches(itemStack, itemStack2))
            {
                int2ObjectMap.put(j, itemStack2.copy());
            }
        }

        client.getConnection()
                .send(new ServerboundContainerClickPacket(handler.containerId, handler.getStateId(), slotId, click, ClickType.SWAP, newStack, int2ObjectMap));
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe)
    {
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING;
    }
}
