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

package com.neep.neepmeat.machine.charnel_pump;

import com.google.common.collect.ImmutableMap;
import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.meatlib.util.MiscUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CharnelPumpBlock extends BigBlock<CharnelPumpStructure> implements MeatlibBlock, EntityBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final Map<Direction, BigBlockPattern> patternMap;

    public CharnelPumpBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, settings);
        ctx.append(this, new BaseBlockItem(this, ctx, itemSettings));
        BigBlockPattern volume = BigBlockPattern.makeOddCylinder(1, 0, 7, getStructure().defaultBlockState());
//                .set(-2, 0, 0, getStructure().getDefaultState())
//                .set(-2, 1, 0, getStructure().getDefaultState());
//                .enableApi(-2, 1, 0, MotorisedBlock.LOOKUP)
//                .enableApi(0, 0, -1, FluidStorage.SIDED);

        patternMap = ImmutableMap.of(
                Direction.NORTH, volume,
                Direction.EAST, volume.rotateY(90),
                Direction.SOUTH, volume.rotateY(180),
                Direction.WEST, volume.rotateY(270)
        );
    }

    @Override
    public VoxelShape getBreakParticleShape(Level world, BlockPos pos, BlockState state)
    {
        return getVolume(state).toVoxelShape();
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
    {
        if (world.getBlockEntity(pos) instanceof CharnelPumpBlockEntity be && be.canRun())
        {
            int range = 32;
            for (BlockPos randomPos : BlockPos.randomInCube(random, 3, pos, range))
            {
                int surface = world.getChunk(randomPos).getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE).getFirstAvailable(randomPos.getX() & 15, randomPos.getZ() & 15) - 1;
                BlockPos surfacePos = new BlockPos(randomPos.getX(), surface, randomPos.getZ());

                float dx = randomPos.getX() - pos.getX();
                float dz = randomPos.getZ() - pos.getZ();
                float radius = Math.max(0, range - Mth.sqrt(dx * dx + dz * dz)) / (float) range;

                // Shape the curve so that it tails off at a range of 32 blocks
                float prob = radius * radius;

                // Shift things up a bit
                prob = Math.min(1, prob * 2);

                if (random.nextFloat() < prob)
                {
                    BlockState prevState = world.getBlockState(surfacePos);
                    if (prevState.is(NMTags.CONTAMINATED_RUBBLE_REPLACABLE))
                    {
                        world.setBlockAndUpdate(surfacePos, NMBlocks.CONTAMINATED_RUBBLE.defaultBlockState());
                    }
                }
            }
        }

        super.randomTick(state, world, pos, random);
    }

    @Override
    protected CharnelPumpStructure registerStructureBlock(RegistrationContext ctx)
    {
        return ctx.append(this, new CharnelPumpStructure(this, MeatlibBlockSettings.copyOf(this)), "charnel_pump_structure_1");
    }

    @Override
    public BigBlockPattern getVolume(BlockState blockState)
    {
        return patternMap.get(blockState.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return Shapes.block();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return LivingMachines.CHARNEL_PUMP_BE.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
    {
        return MiscUtil.checkType(type, LivingMachines.CHARNEL_PUMP_BE, null, CharnelPumpBlockEntity::clientTick, world);
    }
}
