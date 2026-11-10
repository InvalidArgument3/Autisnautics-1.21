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

package com.neep.neepmeat.client;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.model.block.EncasedConduitModel;
import com.neep.neepmeat.client.model.block.FluidPipeModel;
import com.neep.neepmeat.client.model.block.ScaffoldBottomModel;
import com.neep.neepmeat.client.model.block.ScaffoldTopModel;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.transport.client.model.VascularConduitModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class NeepMeatModelProvider implements ModelLoadingPlugin
{
    public static final NeepMeatModelProvider INSTANCE = new NeepMeatModelProvider();

    public static final Map<ResourceLocation, Supplier<UnbakedModel>> MODELS = new HashMap<>();

    public @Nullable UnbakedModel loadModelResource(ResourceLocation resourceId)
    {
        Supplier<UnbakedModel> model = MODELS.get(resourceId);
        if (model != null)
            return model.get();
        else
            return null;
    }

    public void onInitializeModelLoader(Context context)
    {
        context.resolveModel().register((context1) ->
        {
            return loadModelResource(context1.id());
        });
    }

    static
    {
        ResourceLocation RUSTED_SCAFFOLD_SIDE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/rusted_metal_scaffold_side");
        ResourceLocation RUSTED_SCAFFOLD_TOP = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/rusted_metal_scaffold_top");

        ResourceLocation BLUE_SCAFFOLD_SIDE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/blue_metal_scaffold_side");
        ResourceLocation BLUE_SCAFFOLD_TOP = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/blue_metal_scaffold_top");

        ResourceLocation YELLOW_SCAFFOLD_SIDE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/yellow_metal_scaffold_side");
        ResourceLocation YELLOW_SCAFFOLD_TOP = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/yellow_metal_scaffold_top");

        ResourceLocation CLEAR_TANK_SIDE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/clear_tank_wall");
        ResourceLocation CLEAR_TANK_TOP = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/clear_tank_wall");

        putScaffoldModels(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/rusted_metal_scaffold_top"),
                ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/rusted_metal_scaffold_bottom"),
                ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "item/rusted_metal_scaffold"),
                RUSTED_SCAFFOLD_SIDE,
                RUSTED_SCAFFOLD_TOP,
                NMBlocks.SCAFFOLD_PLATFORM);

        putScaffoldModels(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/blue_metal_scaffold_top"),
                ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/blue_metal_scaffold_bottom"),
                ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "item/blue_metal_scaffold"),
                BLUE_SCAFFOLD_SIDE,
                BLUE_SCAFFOLD_TOP,
                NMBlocks.BLUE_SCAFFOLD);

        putScaffoldModels(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/yellow_metal_scaffold_top"),
                ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/yellow_metal_scaffold_bottom"),
                ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "item/yellow_metal_scaffold"),
                YELLOW_SCAFFOLD_SIDE,
                YELLOW_SCAFFOLD_TOP,
                NMBlocks.YELLOW_SCAFFOLD);

        putScaffoldModels(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/clear_tank_wall"),
                ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/clear_tank_wall"),
                ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "item/clear_tank_wall"),
                CLEAR_TANK_SIDE,
                CLEAR_TANK_TOP,
                NMBlocks.YELLOW_SCAFFOLD);

//        MODELS.put(new Identifier(NeepMeat.NAMESPACE, "block/slope_test"), SlopeTest::new);
        MODELS.put(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/rusty_pipe/pipe_sides"), FluidPipeModel::new);
        MODELS.put(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/vascular_conduit/pipe_sides"), VascularConduitModel::new);

        MODELS.put(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/encased_conduit"), EncasedConduitModel::new);
    }

    public static void putScaffoldModels(ResourceLocation topId, ResourceLocation bottomId, ResourceLocation itemId, ResourceLocation sideTexture, ResourceLocation topTexture, Block block)
    {
        // Top block
        MODELS.put(topId, () -> new ScaffoldTopModel(sideTexture, topTexture, block));
        MODELS.put(itemId, () -> new ScaffoldTopModel(sideTexture, topTexture, block));
        MODELS.put(bottomId, () -> new ScaffoldBottomModel(sideTexture, block));
    }

    private NeepMeatModelProvider()
    {

    }
}