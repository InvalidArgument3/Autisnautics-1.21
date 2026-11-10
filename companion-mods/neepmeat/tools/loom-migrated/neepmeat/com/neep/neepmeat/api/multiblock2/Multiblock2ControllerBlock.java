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

import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.init.NMSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class Multiblock2ControllerBlock<T extends MultiBlockStructure<?>> extends Block
{
    private final T structureBlock;
    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");

    public Multiblock2ControllerBlock(RegistrationContext ctx, Properties settings)
    {
        super(settings);
        this.structureBlock = registerStructureBlock(ctx);
        registerDefaultState(defaultBlockState().setValue(ASSEMBLED, false));
    }

    protected abstract BigBlockPattern getAssembledPattern(BlockState blockState);
    protected abstract MultiblockUnassembledPattern getUnassembledPattern(BlockState blockState);

    protected abstract T registerStructureBlock(RegistrationContext ctx);

    public T getStructure()
    {
        return structureBlock;
    }

    public VoxelShape getAssembledShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return getShape(state, world, pos, context);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
//        if (player.isSneaking())
//        {
//            getUnassembledPattern(state).placeBlocks(world, pos, pos);
//            return ActionResult.SUCCESS;
//        }

        if (assemble(world, pos, state))
            return InteractionResult.SUCCESS;

        return super.use(state, world, pos, player, hand, hit);
    }

    public boolean assemble(Level world, BlockPos controllerPos, BlockState state)
    {
        MultiblockUnassembledPattern assemblePattern = getUnassembledPattern(state);
        if (assemblePattern.test(world, controllerPos) && checkSpaceForAssembly(world, controllerPos, state))
        {
            getAssembledPattern(state).placeBlocks(world, controllerPos, controllerPos);
            world.setBlockAndUpdate(controllerPos, state.setValue(ASSEMBLED, true));
            world.playSound(null, controllerPos, NMSounds.MULTIBLOCK_ASSEMBLE, SoundSource.BLOCKS);
            return true;
        }
        return false;
    }

    public void disassemble(Level world, BlockPos controllerPos, BlockState state, BlockPos origin)
    {
        MultiblockUnassembledPattern pattern = getUnassembledPattern(state);
        pattern.placeBlocks(world, controllerPos, controllerPos);
        world.playSound(null, origin, NMSounds.MULTIBLOCK_DISASSEMBLE, SoundSource.BLOCKS);
    }

    protected boolean checkSpaceForAssembly(Level world, BlockPos origin, BlockState state)
    {
        BigBlockPattern pattern = getAssembledPattern(state);
        BlockPos.MutableBlockPos mutable = origin.mutable();
        for (var entry : pattern.entries())
        {
            mutable.setWithOffset(origin, entry.key());

            if (world.getBlockState(mutable).is(structureBlock))
                return false;
        }
        return true;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!newState.is(this) && state.getValue(ASSEMBLED))
        {
            disassemble(world, pos, state, pos);
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(ASSEMBLED);
    }
}
