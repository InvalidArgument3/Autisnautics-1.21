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

package com.neep.neepmeat.api.multiblock2;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.item.MeatlibItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import com.neep.neepmeat.init.NMBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TestMultiblock extends Multiblock2ControllerBlock<TestMultiblock.TMStructureBlock> implements MeatlibBlock
{
    private final BigBlockPattern assembledPattern;
    private final MultiblockUnassembledPattern unassembledPattern;

    private final VoxelShape shape = Shapes.box(-1, 0, -1, 2, 3, 2);

    public TestMultiblock(RegistrationContext ctx, Properties settings)
    {
        super(ctx, settings);

        assembledPattern = new BigBlockPattern().oddCylinder(
                1, 1, 2, () -> getStructure().defaultBlockState()
        );

        unassembledPattern = new MultiblockUnassembledPattern().oddCylinder(
                1, 1, 2, () -> NMBlocks.MEAT_STEEL_BLOCK.defaultBlockState()
        );

        ctx.append(this, new BaseBlockItem(this, ctx, ItemSettings.block(), new MeatlibItemSettings()));
    }

    @Override
    protected TMStructureBlock registerStructureBlock(RegistrationContext ctx)
    {
        return ctx.append(this, new TMStructureBlock(this, MeatlibBlockSettings.copyOf(properties)), "test_multiblock_structure");
    }

    @Override
    protected BigBlockPattern getAssembledPattern(BlockState blockState)
    {
        return assembledPattern;
    }

    @Override
    protected MultiblockUnassembledPattern getUnassembledPattern(BlockState blockState)
    {
        return unassembledPattern;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return Shapes.block();
    }

    public static class TMStructureBlock extends MultiBlockStructure<TMStructureBlockEntity>
    {
        public TMStructureBlock(Multiblock2ControllerBlock<?> parent, Properties settings)
        {
            super(parent, settings);
        }

        @Override
        protected BlockEntityType<TMStructureBlockEntity> registerBlockEntity()
        {
            return Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "test_multiblock_structure"),
                    FabricBlockEntityTypeBuilder.create(
                                    (p, s) -> new TMStructureBlockEntity(getBlockEntityType(), p, s),this)
                            .build());
        }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
        {
            return Shapes.block();
        }
    }

    public static class TMStructureBlockEntity extends BigBlockStructureEntity
    {

        public TMStructureBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
        {
            super(type, pos, state);
        }
    }
}
