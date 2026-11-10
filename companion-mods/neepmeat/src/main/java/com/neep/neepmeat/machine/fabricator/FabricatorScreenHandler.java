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

package com.neep.neepmeat.machine.fabricator;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.network.ChannelManager;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMScreenHandlers;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import com.neep.neepmeat.screen_handler.slot.PatternSlot;
import com.neep.neepmeat.transport.block.item_transport.entity.PersistentCraftingInventory;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class FabricatorScreenHandler extends BasicScreenHandler
{
    private boolean sync = true;

    private final Container inputInventory;
    private final FabricatorBlockEntity be;
    public final ChannelManager<SyncRecipe> syncRecipe;
    public final ChannelManager<Consumer<ResourceLocation>> fillRecipe;
    public final ChannelManager<BooleanConsumer> useExternal;
    public final ChannelManager<BooleanConsumer> disableInternal;

    public final List<Slot> patternSlots;
    public final List<Slot> inputSlots;
    public final List<Slot> playerSlots;

    private ItemStack prevBufferedStack = ItemStack.EMPTY;

    public FabricatorScreenHandler(int syncId, Inventory inventory, FriendlyByteBuf buf)
    {
        this(inventory, new PersistentCraftingInventory.Simple(9), new SimpleContainer(2 * 9), syncId,
                (FabricatorBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public FabricatorScreenHandler(Inventory playerInventory, CraftingContainer recipeInventory, Container inputInventory, int syncId, FabricatorBlockEntity be)
    {
        super(NMScreenHandlers.FABRICATOR, playerInventory, inputInventory, syncId, null);
        this.inputInventory = inputInventory;

        this.be = be;

        this.inputSlots = createSlotBlock(0, 2 * 9, 9, 2, inputInventory, 0, Slot::new);
        this.playerSlots = createPlayerSlots(8, 91 + 8, playerInventory);
        this.patternSlots = createSlotBlock(19, 19, 3, 3, recipeInventory, 0, PatternSlot::new);

        syncRecipe = addClosable(ChannelManager.create(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "fabricator/sync_recipe"), ChannelFormat.builder(SyncRecipe.class)
                .param(ParamCodec.ofNullable(ParamCodecs.IDENTIFIER))
                .param(ParamCodecs.ITEM_STACK)
                .param(ParamCodec.BOOLEAN)
                .param(ParamCodec.BOOLEAN)
                .build(), playerInventory.player));

        fillRecipe = addClosable(ChannelManager.create(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "fabricator/fill_recipe"), ChannelFormat.builder(ParamCodecs.<Consumer<ResourceLocation>>as(Consumer.class))
                .param(ParamCodecs.IDENTIFIER).build(), playerInventory.player));
        useExternal = addClosable(ChannelManager.create(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "fabricator/use_external"), ChannelFormat.BOOLEAN, playerInventory.player));
        disableInternal = addClosable(ChannelManager.create(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "fabricator/disable_internal"), ChannelFormat.BOOLEAN, playerInventory.player));

        if (playerInventory.player instanceof ServerPlayer serverPlayerEntity)
        {
            fillRecipe.receiver(this::receiveFill);
            useExternal.receiver(this::receiveUseExternal);
            disableInternal.receiver(this::receiveDisableInternal);
        }
    }

    private void receiveDisableInternal(boolean b)
    {
        be.setUseExternal(be.useExternal, b);
        sync = true;
    }

    private void receiveUseExternal(boolean useExternal)
    {
        be.setUseExternal(useExternal, be.useInternalForBOM);
        sync = true;
    }

    void receiveFill(ResourceLocation id)
    {
        if (be.getLevel().getRecipeManager().byKey(id).orElse(null) instanceof CraftingRecipe craftingRecipe)
        {
            fillRecipe(craftingRecipe);
        }
        sync = true;
    }

    void fillRecipe(CraftingRecipe recipe)
    {
        List<Ingredient> ingredients = recipe.getIngredients();

        for (Slot patternSlot : patternSlots)
        {
            patternSlot.setByPlayer(ItemStack.EMPTY);
        }

        if (recipe instanceof ShapedRecipe shapedRecipe)
        {
            int height = shapedRecipe.getHeight();
            int width = shapedRecipe.getWidth();

            for (int j = 0; j < height; ++j)
            {
                for (int i = 0; i < width; ++i)
                {
                    int slotIdx = j * 3 + i;
                    int idx = j * width + i;
                    if (idx < ingredients.size())
                    {
                        Ingredient ingredient = ingredients.get(idx);

                        if (!ingredient.isEmpty())
                            patternSlots.get(slotIdx).setByPlayer(firstValidMatchingStack(ingredient));
                    }
                }
            }
        }
        else
        {
            int slotIdx = 0;
            for (var ingredient : ingredients)
            {
                Slot slot = patternSlots.get(slotIdx);

                if (!ingredient.isEmpty())
                    slot.setByPlayer(ingredient.getItems()[0]);

                slotIdx++;
            }
        }
    }

    private ItemStack firstValidMatchingStack(Ingredient ingredient)
    {
        return ingredient.getItems()[0].copy();
//        // For some reason, empty stacks can be present in this array.
//        ItemStack[] stacks = ingredient.getMatchingStacks();
//        for (ItemStack stack : stacks)
//        {
//            if (!stack.isEmpty())
//                return stack;
//        }
//        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();

        if (ready && sync || !be.getStorage().getBufferedStack().equals(prevBufferedStack))
        {
            net.minecraft.world.item.crafting.RecipeHolder<?> recipe = be.getStorage().recipeHolder();

            syncRecipe.emitter().apply(recipe != null ? recipe.id() : null, be.getStorage().getBufferedStack(),
                    be.useExternal, be.useInternalForBOM);

            // Some utter jank
            this.prevBufferedStack = be.getStorage().getBufferedStack().copy();

            sync = false;
        }
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType actionType, Player player)
    {
//        if (actionType != net.minecraft.screen.slot.SlotActionType.QUICK_MOVE)
            super.clicked(slotIndex, button, actionType, player);
        sync = true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        if (patternSlots.contains(slots.get(index)))
            return ItemStack.EMPTY;

        return super.quickMoveStack(player, index);
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean fromLast)
    {
        // Assume that the pattern slots will be last (because they are)

        if (endIndex > patternSlots.get(0).index)
        {
            endIndex = patternSlots.get(0).index;
        }

        return super.moveItemStackTo(stack, startIndex, endIndex, fromLast);
    }

    public interface SyncRecipe
    {
        void apply(@Nullable ResourceLocation recipeId, ItemStack buffered, boolean useExternal, boolean useInternal);
    }
}
