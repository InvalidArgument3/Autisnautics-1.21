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

import com.mojang.datafixers.util.Pair;
import com.neep.meatlib.client.renderer.Renderer;
import com.neep.meatlib.client.renderer.RendererAccess;
import com.neep.meatlib.client.renderer.mesh.Mesh;
import com.neep.meatlib.client.renderer.mesh.MeshBuilder;
import com.neep.meatlib.client.renderer.mesh.MutableQuadView;
import com.neep.meatlib.client.renderer.mesh.QuadEmitter;
import com.neep.meatlib.client.renderer.model.FabricBakedModel;
import com.neep.meatlib.client.renderer.render.RenderContext;
import net.minecraft.client.render.model.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ScaffoldBottomModel implements UnbakedModel, BakedModel, FabricBakedModel
{

    private final Material[] SPRITE_IDS = new Material[1];
    private final TextureAtlasSprite[] SPRITES = new TextureAtlasSprite[1];

    private Mesh outerMesh;
    private Mesh innerMesh;
    private final Block block;

    public ScaffoldBottomModel(ResourceLocation sideTexture, Block block)
    {
        SPRITE_IDS[0] = new Material(TextureAtlas.LOCATION_BLOCKS, sideTexture);
        this.block = block;
    }

    @Override
    public Collection<ResourceLocation> getDependencies()
    {
        return Collections.emptyList(); // This model does not depend on other models.
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelLoader)
    {

    }

    @Nullable
    @Override
    public BakedModel bake(ModelBaker loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer, ResourceLocation modelId)
    {

        SPRITES[0] = textureGetter.apply(SPRITE_IDS[0]);

        // Build the mesh using the Renderer API
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();

        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter();

        for(Direction direction : Direction.values())
        {
            emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
            emitter.spriteBake(0, SPRITES[0], MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            emitter.emit();
        }

        outerMesh = builder.build();

        for (Direction direction : Direction.values())
        {

            emitter.square(direction.getOpposite(), 0.0f, 0.0f, 1.0f, 1.0f, 0.999f);
            emitter.nominalFace(direction);
            emitter.spriteBake(0, SPRITES[0], MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            emitter.emit();
        }

        innerMesh = builder.build();

        return this;
    }

    @Override
    public boolean isVanillaAdapter()
    {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context)
    {
        context.pushTransform(mesh -> ScaffoldTopModel.getFaces(mesh, blockView, pos, block));
        context.meshConsumer().accept(outerMesh);
        context.meshConsumer().accept(innerMesh);
        context.popTransform();
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context)
    {
        context.meshConsumer().accept(outerMesh);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion()
    {
        return true;
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
        return SPRITES[0];
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
}
