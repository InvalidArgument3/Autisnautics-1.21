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

package com.neep.neepmeat.machine.live_machine.block;

import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block_2.BigBlock2;
import com.neep.neepmeat.api.big_block_2.BigBlock2Structure;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.block.entity.MetaboliserSegmentBlockEntity;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import com.neep.neepmeat.transport.block.energy_transport.VascularConduitBlock;
import com.neep.neepmeat.util.ItemUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class MetaboliserSegmentBlock extends BigBlock2 implements EntityBlock
{
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

//    private static final BigBlockPattern NORTH_PATTERN = BigBlockPattern.makeRange(-1, -1, 0, 1, 1, 0, Blocks.AIR.getDefaultState());
//    private static final BigBlockPattern UP_PATTERN = BigBlockPattern.makeOddCylinder(1, 0, 0, Blocks.AIR.getDefaultState());

    private final EnumMap<Direction.Axis, Int2ObjectMap<Vec3i>> offsetMap;
    private final EnumMap<Direction.Axis, BigBlockPattern> patternMap;

    public MetaboliserSegmentBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings, 8);

        offsetMap = new EnumMap<>(Direction.Axis.class);
        patternMap = new EnumMap<>(Direction.Axis.class);

        var x = BigBlockPattern.makeRange(0, -1, -1, 0, 1, 1, Blocks.AIR.defaultBlockState())
                .enableApi(0, -1, 0, BloodAcceptor.SIDED)
                .enableApi(0, 1, 0, BloodAcceptor.SIDED)
                .enableApi(0, 0, -1, BloodAcceptor.SIDED)
                .enableApi(0, 0, 1, BloodAcceptor.SIDED);
        var y = BigBlockPattern.makeOddCylinder(1, 0, 0, Blocks.AIR.defaultBlockState())
                .enableApi(-1, 0, 0, BloodAcceptor.SIDED)
                .enableApi(1, 0, 0, BloodAcceptor.SIDED)
                .enableApi(0, 0, -1, BloodAcceptor.SIDED)
                .enableApi(0, 0, 1, BloodAcceptor.SIDED);
        var z = BigBlockPattern.makeRange(-1, -1, 0, 1, 1, 0, Blocks.AIR.defaultBlockState())
                .enableApi(-1, 0, 0, BloodAcceptor.SIDED)
                .enableApi(1, 0, 0, BloodAcceptor.SIDED)
                .enableApi(0, -1, 0, BloodAcceptor.SIDED)
                .enableApi(0, 1, 0, BloodAcceptor.SIDED);

        patternMap.put(Direction.Axis.X, x);
        offsetMap.put(Direction.Axis.X, x.encode());
        patternMap.put(Direction.Axis.Y, y);
        offsetMap.put(Direction.Axis.Y, y.encode());
        patternMap.put(Direction.Axis.Z, z);
        offsetMap.put(Direction.Axis.Z, z.encode());
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        return this.defaultBlockState().setValue(AXIS, ctx.getClickedFace().getAxis());
    }

    @Override
    public BigBlockPattern getVolume(BlockState state)
    {
        return patternMap.get(state.getValue(AXIS));
    }

    @Override
    public Int2ObjectMap<Vec3i> getIndexToOffset(BlockState state)
    {
        return offsetMap.get(state.getValue(AXIS));
    }

    @Override
    protected BigBlock2Structure makeStructure(MeatlibBlockSettings settings)
    {
        return new MSBStructure(this, settings);
    }

    @Override
    protected BlockState getDefaultStructureState(BlockState parentState)
    {
        return structure.defaultBlockState().setValue(AXIS, parentState.getValue(AXIS));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return LivingMachines.METABOLISER_SEGMENT_BE.create(pos, state);
    }

    @Override
    protected void spawnDestroyParticles(Level world, Player player, BlockPos pos, BlockState state)
    {
        super.spawnDestroyParticles(world, player, pos, state);
    }

    @Override
    public @Nullable <T, C> T getApi(Level world, BlockPos pos, BlockState state, Vec3i offset, BlockApiLookup<T, C> api)
    {
        if (apiEnabled(state, offset, api))
        {
            if (api == BloodAcceptor.SIDED && world.getBlockEntity(pos) instanceof MetaboliserSegmentBlockEntity be)
            {
                return (T) be.getBloodAcceptor();
            }
        }

        return null;
    }

    public static class MSBStructure extends BigBlock2Structure
    {
        public MSBStructure(BigBlock2 parent, Properties settings)
        {
            super(parent, settings);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
        {
            super.createBlockStateDefinition(builder);
            builder.add(AXIS);
        }

        @Override
        public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
        {
            if (ItemUtil.playerHoldingPipe(player, hand) || VascularConduitBlock.matches(player.getItemInHand(hand)))
                return InteractionResult.PASS;

            if (world instanceof ServerLevel && world.getBlockEntity(getOrigin(pos, state)) instanceof MetaboliserSegmentBlockEntity be)
            {
                String energy = PowerUtil.formatPower(be.getEnergy(), 1);
                player.displayClientMessage(NeepMeat.translationKey("message", "metaboliser_segment.output", energy), true);
            }
            return InteractionResult.SUCCESS;
        }

        //        @Override
//        public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
//        {
//            return VoxelShapes.fullCube();
//        }
    }
}
