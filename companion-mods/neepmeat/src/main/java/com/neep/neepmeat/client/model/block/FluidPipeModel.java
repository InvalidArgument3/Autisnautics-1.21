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

package com.neep.neepmeat.client.model.block;
import net.minecraft.client.resources.model.ModelResourceLocation;


import com.mojang.math.Transformation;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.transport.api.pipe.FluidPipe;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.neep.meatlib.client.renderer.model.FabricBakedModel;
import com.neep.meatlib.client.renderer.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class FluidPipeModel implements UnbakedModel, BakedModel, FabricBakedModel
{
    private static final ResourceLocation SIDE_ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/rusty_pipe/pipe_side_up");
    private static final ResourceLocation STRAIGHT_ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/rusty_pipe/pipe_straight_up");
    private static final ResourceLocation CENTRE_ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/rusty_pipe/pipe_centre");

    private static final Material PARTICLE_SPRITE_ID = new Material(InventoryMenu.BLOCK_ATLAS, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "block/rusty_pipe/rusty_pipe_centre"));
    private static TextureAtlasSprite PARTICLE_SPRITE;

    private final Triple<BakedModel, Float, Float>[] straight = (Triple<BakedModel, Float, Float>[]) Array.newInstance(Triple.class, 6);
    private final Triple<BakedModel, Float, Float>[] connectors = (Triple<BakedModel, Float, Float>[]) Array.newInstance(Triple.class, 6);
    private BakedModel centre;

    /* UnbakedModel */
    @Override
    public Collection<ResourceLocation> getDependencies()
    {
        return List.of(SIDE_ID, STRAIGHT_ID, CENTRE_ID);
    }

//    @Override
//    public Collection<Material> getTextureDependencies(Function<ResourceLocation, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences)
//    {
//        return List.of(PARTICLE_SPRITE_ID, new Material(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new ResourceLocation(NeepMeat.NAMESPACE, "block/rusty_pipe/rusty_pipe_straight")));
//    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelLoader)
    {

    }

    @Nullable
    @Override
    public BakedModel bake(ModelBaker loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer)
    {
        ResourceLocation modelId = null;
        PARTICLE_SPRITE = textureGetter.apply(PARTICLE_SPRITE_ID);
        UnbakedModel centreModel = loader.getModel(ModelResourceLocation.standalone(CENTRE_ID));
        this.centre = centreModel.bake(loader, textureGetter, rotationContainer, CENTRE_ID);

//        Arrays.stream(CENTRE_IDS).forEach(id ->
//        {
//            BakedModel bakedPart = loader.getOrLoadModel(id).bake(loader, textureGetter, rotationContainer, modelId);
//            if (bakedPart != null) parts.add(bakedPart);
//        });

        try
        {
            addPart(connectors, Direction.NORTH, SIDE_ID, 0f, 0f, loader, textureGetter, modelId);
            addPart(connectors, Direction.SOUTH, SIDE_ID, 0f, 0f, loader, textureGetter, modelId);
            addPart(connectors, Direction.EAST, SIDE_ID, 270f, 0f, loader, textureGetter, modelId);
            addPart(connectors, Direction.WEST, SIDE_ID, 270f, 0f, loader, textureGetter, modelId);
            addPart(connectors, Direction.UP, SIDE_ID, 270f, 270f, loader, textureGetter, modelId);
            addPart(connectors, Direction.DOWN, SIDE_ID, 270f, 270f, loader, textureGetter, modelId);

            addPart(straight, Direction.NORTH, STRAIGHT_ID, 0f, 0f, loader, textureGetter, modelId);
            addPart(straight, Direction.SOUTH, STRAIGHT_ID, 0f, 0f, loader, textureGetter, modelId);
            addPart(straight, Direction.EAST, STRAIGHT_ID, 270f, 0f, loader, textureGetter, modelId);
            addPart(straight, Direction.WEST, STRAIGHT_ID, 270f, 0f, loader, textureGetter, modelId);
            addPart(straight, Direction.UP, STRAIGHT_ID, 270f, 270f, loader, textureGetter, modelId);
            addPart(straight, Direction.DOWN, STRAIGHT_ID, 270f, 270f, loader, textureGetter, modelId);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return this;
    }

    protected void addPart(Triple<BakedModel, Float, Float>[] parts, Direction face, ResourceLocation id, float x, float y, ModelBaker loader, Function<Material, TextureAtlasSprite> textureGetter, ResourceLocation modelId)
    {
        UnbakedModel unbaked = loader.getModel(ModelResourceLocation.standalone(id));
        Variant settings = new Variant(id, new Transformation(null, null, null, face.getRotation()), false, 1);
        parts[face.get3DDataValue()] = Triple.of(unbaked.bake(loader, textureGetter, settings, modelId), x, y);
    }

    /* FabricBakedModel */
    @Override
    public boolean isVanillaAdapter() {return false;}

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context)
    {
        if (blockView == null || pos == null || state == null)
        {
            emitItemShape(context, randomSupplier.get());
            return;
        }
        for (Direction direction : Direction.values())
        {
            boolean forward = FluidPipe.isConnectedIn(blockView, pos, state, direction);
            if (!forward) continue;

            BlockState offsetState = blockView.getBlockState(pos.relative(direction));
            if (!FluidPipe.isConnectedIn(blockView, pos, state, direction.getOpposite()) || !(offsetState.getBlock() instanceof FluidPipe) || !FluidPipe.isConnectedIn(blockView, pos, offsetState, direction))
            {
                ((FabricBakedModel) connectors[direction.get3DDataValue()].getLeft()).emitBlockQuads(blockView, state, pos, randomSupplier, context);
            }
            else if (FluidPipe.isConnectedIn(blockView, pos, offsetState, direction))
            {
                ((FabricBakedModel) straight[direction.get3DDataValue()].getLeft()).emitBlockQuads(blockView, state, pos, randomSupplier, context);
            }
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context)
    {
        emitItemShape(context, randomSupplier.get());
    }

    /** Item json is a centre cube plus the north and south arms. */
    private void emitItemShape(RenderContext context, RandomSource random)
    {
        com.neep.meatlib.client.renderer.render.CollectingRenderContext.emitModel(context, centre, random);
        emitArm(context, Direction.NORTH, random);
        emitArm(context, Direction.SOUTH, random);
    }

    private void emitArm(RenderContext context, Direction direction, RandomSource random)
    {
        Triple<BakedModel, Float, Float> part = connectors[direction.get3DDataValue()];
        if (part != null)
        {
            com.neep.meatlib.client.renderer.render.CollectingRenderContext.emitModel(context, part.getLeft(), random);
        }
    }

    /* BakedModel */
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random)
    {
        return getQuads(state, face, random, net.neoforged.neoforge.client.model.data.ModelData.EMPTY, null);
    }

    @Override
    public boolean useAmbientOcclusion()
    {
        return false;
    }

    @Override
    public boolean isGui3d()
    {
        return false;
    }

    @Override
    public boolean usesBlockLight()
    {
        return false;
    }

    @Override
    public boolean isCustomRenderer()
    {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon()
    {
        return PARTICLE_SPRITE;
    }

    @Override
    public ItemTransforms getTransforms()
    {
        return null;
    }

    @Override
    public ItemOverrides getOverrides()
    {
        return null;
    }

//    @OnlyIn(Dist.CLIENT)
//    public static class Builder
//    {
//        private final List<Pair<Predicate<BlockState>, BakedModel>> components = Lists.newArrayList();
//
//        public void addComponent(Predicate<BlockState> predicate, BakedModel model)
//        {
//            this.components.add(Pair.of(predicate, model));
//        }
//
//        public BakedModel build()
//        {
//            return new MultipartBakedModel(this.components);
//        }
//    }
}
