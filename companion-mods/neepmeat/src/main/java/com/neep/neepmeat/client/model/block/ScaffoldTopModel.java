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


import com.neep.meatlib.client.renderer.Renderer;
import com.neep.meatlib.client.renderer.RendererAccess;
import com.neep.meatlib.client.renderer.mesh.Mesh;
import com.neep.meatlib.client.renderer.mesh.MeshBuilder;
import com.neep.meatlib.client.renderer.mesh.MutableQuadView;
import com.neep.meatlib.client.renderer.mesh.QuadEmitter;
import com.neep.meatlib.client.renderer.model.FabricBakedModel;
import com.neep.meatlib.client.renderer.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ScaffoldTopModel implements UnbakedModel, BakedModel, FabricBakedModel
{

    private static final ResourceLocation DEFAULT_BLOCK_MODEL = ResourceLocation.parse("minecraft:block/block");
    private final Material[] SPRITE_IDS = new Material[2];
    private final TextureAtlasSprite[] SPRITES = new TextureAtlasSprite[2];
//            {
//            new Material(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new ResourceLocation(NeepMeat.NAMESPACE, "block/scaffold_side")),
//            new Material(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new ResourceLocation(NeepMeat.NAMESPACE, "block/scaffold_top")),
//    };
    private final Mesh[] SIDES = new Mesh[6];
    private final Mesh[] SIDES_INV = new Mesh[6];
    private final Block block;
    private ItemTransforms transformation;
    private Mesh outerMesh;
    private Mesh innerMesh;

    public ScaffoldTopModel(ResourceLocation sideTexture, ResourceLocation topTexture, Block block)
    {
        SPRITE_IDS[0] = new Material(TextureAtlas.LOCATION_BLOCKS, sideTexture);
        SPRITE_IDS[1] = new Material(TextureAtlas.LOCATION_BLOCKS, topTexture);
        this.block = block;
    }

    public ScaffoldTopModel(String namespace, String registryName, Block block)
    {
        SPRITE_IDS[0] = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(namespace, registryName + "_side"));
        SPRITE_IDS[0] = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.fromNamespaceAndPath(namespace, registryName + "_top"));
        this.block = block;
    }

    public static boolean getFaces(MutableQuadView quad, BlockAndTintGetter blockView, BlockPos pos, Block block)
    {
        Direction face = quad.nominalFace();
        BlockPos newPos = pos.relative(face);
        BlockState newState = blockView.getBlockState(newPos);
//        return !(newState.isOf(block) || newState.isSideSolidFullSquare(blockView, newPos, face.getOpposite()));
        return !(newState.is(block) || newState.isCollisionShapeFullBlock(blockView, newPos));
    }

    @Override
    public Collection<ResourceLocation> getDependencies()
    {
        return Arrays.asList(DEFAULT_BLOCK_MODEL);
    }

//    @Override
//    public Collection<Material> getTextureDependencies(Function<ResourceLocation, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences)
//    {
//        return Arrays.asList(SPRITE_IDS);
//    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelLoader)
    {

    }

    @Nullable
    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer, ResourceLocation modelId)
    {
        // Get the sprites
        for (int i = 0; i < 2; ++i)
        {
            SPRITES[i] = textureGetter.apply(SPRITE_IDS[i]);
        }
        // Build the mesh using the Renderer API
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();

        MeshBuilder builder = renderer.meshBuilder();
        QuadEmitter emitter = builder.getEmitter();

        // Create outer faces
        for (Direction direction : Direction.values())
        {
            int spriteIdx = direction == Direction.UP || direction == Direction.DOWN ? 1 : 0;

            emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f);
            emitter.spriteBake(0, SPRITES[spriteIdx], MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            emitter.emit();

        }
        outerMesh = builder.build();

        // Create inner faces
        for (Direction direction : Direction.values())
        {
//            int spriteIdx = direction == Direction.UP || direction == Direction.DOWN ? 1 : 0;
            int spriteIdx = direction == Direction.UP ? 1 : 0;

            emitter.square(direction.getOpposite(), 0.0f, 0.0f, 1.0f, 1.0f, 0.999f);
            emitter.nominalFace(direction);
            emitter.spriteBake(0, SPRITES[spriteIdx], MutableQuadView.BAKE_LOCK_UV);
            emitter.spriteColor(0, -1, -1, -1, -1);
            emitter.emit();

        }
        innerMesh = builder.build();

        BlockModel defaultBlockModel = (BlockModel) baker.getModel(ModelResourceLocation.standalone(DEFAULT_BLOCK_MODEL));
        transformation = defaultBlockModel.getTransforms();

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
        // Janky scaffolding rendering.
//        for (Direction direction : Direction.values())
//        {
//            if (!blockView.getBlockState(pos.offset(direction)).isOf(BlockInitialiser.SCAFFOLD_PLATFORM))
//            {
//                context.pushTransform(quad ->
//                {
//                    for (int i = 0; i < 4; ++i)
//                    {
//                        Vector3f vert = quad.copyPos(i, null);
//                        vert.add(-0.5f, -0.5f, -0.5f);
//                        vert.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(direction.asRotation()));
//                        vert.add(0.5f, 0.5f, 0.5f);
//                    }
//                    return true;
//                });
//                context.meshConsumer().accept(SIDES[0]);
//                context.popTransform();
//            }

//        context.pushTransform(quad ->
//        {
//            Direction face = quad.nominalFace();
//            if (blockView.getBlockState(pos.offset(face)).isOf(BlockInitialiser.SCAFFOLD_PLATFORM))
//            {
//                return false;
//            }
//            for (int i = 0; i < 4; ++i)
//            {
//                Vector3f vert = quad.copyPos(i, null);
//                vert.add(-0.5f, -0.5f, -0.5f);
//                vert.rotate(RotationAxis.POSITIVE_Y.rotationDegrees(face.asRotation()));
//                vert.add(0.5f, 0.5f, 0.5f);
//            }
//            return true;
//        });

        context.pushTransform(quad -> getFaces(quad, blockView, pos, block));
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
        return true;
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
        return transformation;
    }

    @Override
    public ItemOverrides getOverrides()
    {
        return ItemOverrides.EMPTY;
    }
}
