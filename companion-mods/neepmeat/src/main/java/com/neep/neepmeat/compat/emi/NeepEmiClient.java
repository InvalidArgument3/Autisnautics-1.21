/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.compat.emi;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.machine.NeepMachines;
import com.neep.neepmeat.recipe.NeepRecipes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class NeepEmiClient {
    public static final EmiRecipeCategory ALLOY = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "alloy_smelting"),
            EmiStack.of(new ItemStack(NeepMachines.ALLOY_KILN_ITEM.get())));

    private NeepEmiClient() {}

    public static void register(EmiRegistry registry) {
        registry.addCategory(ALLOY);
        registry.addWorkstation(ALLOY, EmiStack.of(new ItemStack(NeepMachines.ALLOY_KILN_ITEM.get())));
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        minecraft.level.getRecipeManager().getAllRecipesFor(NeepRecipes.ALLOY.get())
                .forEach(holder -> registry.addRecipe(new AlloyEmiRecipe(holder)));
    }

    public static Component title() {
        return Component.translatable("category.neepmeat.alloy_smelting");
    }
}
