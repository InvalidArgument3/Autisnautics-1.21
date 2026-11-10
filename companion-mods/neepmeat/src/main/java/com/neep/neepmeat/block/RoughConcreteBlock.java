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

package com.neep.neepmeat.block;

import com.google.common.collect.ImmutableMap;
import com.neep.meatlib.block.BaseBuildingBlock;
import com.neep.meatlib.block.BaseWallBlock;
import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.datagen.MeatRecipeProvider;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.item.MeatlibItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NMItemGroups;
import com.neep.neepmeat.datagen.tag.NMTags;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RoughConcreteBlock extends BaseBuildingBlock
{
    private final DyeColor col;

    public RoughConcreteBlock(RegistrationContext ctx, boolean makeWall, DyeColor col, MeatlibBlockSettings settings)
    {
        super(ctx, makeWall, ItemSettings.block().factory(Item::new), settings);
        this.col = col;
    }

    @Override
    public void generateRecipes(Consumer<FinishedRecipe> exporter)
    {
        super.generateRecipes(exporter);
        MeatRecipeProvider.offerEightDyeingRecipe(exporter, "_dyeing", this, DyeItem.byColor(col), NMTags.ROUGH_CONCRETE);
    }

    @Override
    protected MeatlibBlock makeWall(RegistrationContext ctx)
    {
        return new ConcreteWall(ctx, properties);
    }

    private static class Item extends BaseBlockItem
    {
        public Item(Block block, RegistrationContext ctx, ItemSettings itemSettings)
        {
            super(block, ctx, itemSettings, new MeatlibItemSettings().tags(NMTags.ROUGH_CONCRETE).group(NMItemGroups.BUILDING));
        }
    }

    public static class ConcreteWall extends BaseWallBlock
    {
        private final Map<BlockState, VoxelShape> shapeMap;
        private final Map<BlockState, VoxelShape> collisionMap;

        public ConcreteWall(RegistrationContext ctx, Properties settings)
        {
            super(ctx, ItemSettings.block(), settings);

            this.shapeMap = this.getShapeMap(4.0F, 3.0F, 16.0F, 0.0F);
            this.collisionMap = this.getShapeMap(4.0F, 3.0F, 24.0F, 0.0F);
        }

        private static VoxelShape getVoxelShape(VoxelShape base, WallSide wallShape, VoxelShape tall)
        {
            return wallShape != WallSide.NONE ? Shapes.or(base, tall) : base;
        }

        private Map<BlockState, VoxelShape> getShapeMap(float f, float g, float h, float i)
        {
            float n = 8.0F - g;
            float o = 8.0F + g;

            VoxelShape postShape = Block.box(n, 0.0, n, o, h, o);

            VoxelShape voxelShape2 = Block.box(n, i, 0.0, o, h, o);
            VoxelShape voxelShape3 = Block.box(n, i, n, o, h, 16.0);
            VoxelShape voxelShape4 = Block.box(0.0, i, n, o, h, o);
            VoxelShape voxelShape5 = Block.box(n, i, n, 16.0, h, o);

            ImmutableMap.Builder<BlockState, VoxelShape> builder = ImmutableMap.builder();

            for (Boolean up : UP.getPossibleValues())
            {
                for (WallSide east : EAST_WALL.getPossibleValues())
                {
                    for (WallSide north : NORTH_WALL.getPossibleValues())
                    {
                        for (WallSide west : WEST_WALL.getPossibleValues())
                        {
                            for (WallSide south : SOUTH_WALL.getPossibleValues())
                            {
                                VoxelShape shape = Shapes.empty();
                                shape = getVoxelShape(shape, east, voxelShape5);
                                shape = getVoxelShape(shape, west, voxelShape4);
                                shape = getVoxelShape(shape, north, voxelShape2);
                                shape = getVoxelShape(shape, south, voxelShape3);

                                if (up)
                                {
                                    shape = Shapes.or(shape, postShape);
                                }

                                BlockState blockState = this.defaultBlockState()
                                        .setValue(UP, up)
                                        .setValue(EAST_WALL, east)
                                        .setValue(WEST_WALL, west)
                                        .setValue(NORTH_WALL, north)
                                        .setValue(SOUTH_WALL, south);
                                builder.put(blockState.setValue(WATERLOGGED, Boolean.FALSE), shape);
                                builder.put(blockState.setValue(WATERLOGGED, Boolean.TRUE), shape);
                            }
                        }
                    }
                }
            }

            return builder.build();
        }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
        {
            return shapeMap.get(state);
//            return this.getShapeMap(4.0F, 3.0F, 16.0F, 0.0F).get(state);
        }

        @Override
        protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
        {
            return collisionMap.get(state);
        }
    }
}
