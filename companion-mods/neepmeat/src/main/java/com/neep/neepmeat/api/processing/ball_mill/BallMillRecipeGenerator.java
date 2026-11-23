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

package com.neep.neepmeat.api.processing.ball_mill;

import com.neep.meatlib.api.event.DataPackPostProcess;
import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodecs;
import com.neep.meatlib.network.GlobalChannelManager;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.meatlib.recipe.ingredient.RecipeOutputs;
import com.neep.meatlib.recipe.ingredient.RegistryRecipeInput;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMrecipeTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.neoforged.api.distmarker.Dist;
import com.neep.meatlib.transfer.ItemVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import java.util.*;
import java.util.function.Consumer;

public class BallMillRecipeGenerator
{
    private final Map<ResourceLocation, BallMillRecipe> syntheticRecipes = new HashMap<>();
    private final List<RegistryRecipeInput<Item>> usedInputs = new ObjectArrayList<>();

    private static final GlobalChannelManager<Consumer<Map<ResourceLocation, BallMillRecipe>>> SYNC = GlobalChannelManager.create(
            ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "sync_ball_mill"),
            ChannelFormat.builder(ParamCodecs.<Consumer<Map<ResourceLocation, BallMillRecipe>>>as(Consumer.class))
                    .param(ParamCodecs.map(ParamCodecs.as(Map.class), ParamCodecs.IDENTIFIER, DistinctBallMillRecipe.PARAM_CODEC))
                    .build());

    public static final BallMillRecipeGenerator INSTANCE = new BallMillRecipeGenerator();

    public static void init()
    {

    }

    private BallMillRecipeGenerator()
    {
        DataPackPostProcess.AFTER_DATA_PACK_LOAD.register(DataPackPostProcess.FIRST, this::generate);
        DataPackPostProcess.SYNC.register(this::sync);

        SYNC.receiver(Dist.CLIENT, m ->
        {
            syntheticRecipes.clear();
            syntheticRecipes.putAll(m);
        });
    }

    private void generate(MinecraftServer server)
    {
        syntheticRecipes.clear();
        usedInputs.clear();

        // Determine which inputs are already present to disable automatic generation for them
        MeatlibRecipes.getInstance().getAllValuesOfType(NMrecipeTypes.BALL_MILL)
                .forEach(r -> usedInputs.add(r.getInput()));
    }

    private void sync(MinecraftServer server, Set<ServerPlayer> serverPlayerEntities)
    {
        serverPlayerEntities.forEach(p -> SYNC.emitter(p).accept(syntheticRecipes));
    }

    public void add(String name, RegistryRecipeInput<Item> input, ItemVariant output)
    {
        if (usedInputs.stream().noneMatch(i -> i.resourcesMatch(input)))
        {
            ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "/synthetic/" + name + "_ball_mill");
            syntheticRecipes.put(
                    recipeId,
                    new DistinctBallMillRecipe(recipeId, input, List.of(
                            RecipeOutputs.of(output, 1, 1, 1),
                            RecipeOutputs.of(output, 1, 1, 0.2f)
                    ), 40)
            );
        }
    }

    public void clear()
    {
        syntheticRecipes.clear();
    }

    public Optional<BallMillRecipe> getFirstMatch(BallMillRecipe.BallMillContext context)
    {
        var first = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.BALL_MILL, context);
        if (first.isPresent())
            return first;

        return syntheticRecipes.values().stream().filter(r -> r.matches(context)).findFirst();
    }

    public Collection<BallMillRecipe> getSynthetic()
    {
        return syntheticRecipes.values();
    }
}
