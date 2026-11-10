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

import com.neep.meatlib.block.BaseHorFacingBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ToiletBlock extends BaseHorFacingBlock
{
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final VoxelShape OPEN_SHAPE = Block.box(0, 0, 0, 16, 14, 16);
//    public static final VoxelShape CLOSED_SHAPE = makeClosedShape();

    public ToiletBlock(RegistrationContext itemName, ItemSettings itemSettings, Properties settings)
    {
        super(itemName, itemSettings, settings.noOcclusion());
        registerDefaultState(defaultBlockState().setValue(OPEN, false));
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
    {
        if (world.hasNeighborSignal(pos) && state.getValue(OPEN))
        {
            spawnParticles(world, pos);

            // Find a living entity in the block above and teleport it one block below the toilet block.
            AABB box = new AABB(pos.above());
            List<LivingEntity> entityList = world.getEntities(EntityTypeTest.forClass(LivingEntity.class), box, e -> true);
            if (entityList.size() > 0)
            {
                Vec3 downCorner = Vec3.atLowerCornerOf(pos.below(3));
                LivingEntity entity = entityList.get(0);
                double y = pos.getY() - 1 - entity.getBbHeight();
                if (!world.getBlockState(new BlockPos(pos.getX(), (int) Math.floor(y), pos.getZ())).blocksMotion() && world.noCollision(entity.getBoundingBox().move(downCorner)))
                {
                    entity.teleportTo(pos.getX() + 0.5, y, pos.getZ() + 0.5);
                    world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1f, 1f);
                }
            }
        }
        super.neighborChanged(state, world, pos, block, fromPos, notify);
    }

    protected void spawnParticles(Level world, BlockPos pos)
    {
        if (world instanceof ServerLevel serverWorld)
        {
            serverWorld.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 10, 0.1, 0, 0.1, 0.1);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockState def = super.getStateForPlacement(context).setValue(OPEN, false);
        return def.setValue(FACING, def.getValue(FACING).getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        world.setBlockAndUpdate(pos, state.cycle(OPEN));
        world.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return OPEN_SHAPE;
    }

    // Auto-generated
    public static VoxelShape makeOpenShape()
    {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.or(shape, Shapes.box(0.1875, 0, 0.125, 0.8125, 0.375, 1.4375));
        shape = Shapes.or(shape, Shapes.box(0.125, 0.375, 0.0625, 0.875, 0.6875, 1.5));
        shape = Shapes.or(shape, Shapes.box(0.875, 0.5, 0, 1, 0.8125, 1));
        shape = Shapes.or(shape, Shapes.box(0, 0.5, 0, 0.125, 0.8125, 1));
        shape = Shapes.or(shape, Shapes.box(0.125, 0.5, 0, 0.875, 0.8125, 0.125));
        shape = Shapes.or(shape, Shapes.box(0, 0.828125, 0.875, 1, 1.765625, 0.9375));
        shape = Shapes.or(shape, Shapes.box(0.0625, 0.6875, 0.9375, 0.9375, 0.875, 1.5625));
        shape = Shapes.or(shape, Shapes.box(0.03125, 1.125, 0.9375, 0.96875, 1.9375, 1.5));
        shape = Shapes.or(shape, Shapes.box(0.125, 0.875, 0.96875, 0.875, 1.125, 1.40625));
        shape = Shapes.or(shape, Shapes.box(0.6875, 0.8125, 0.84375, 0.875, 0.9375, 0.96875));
        shape = Shapes.or(shape, Shapes.box(0.125, 0.8125, 0.84375, 0.3125, 0.9375, 0.96875));

        return shape;
    }

    // Auto-generated
    public static VoxelShape makeClosedShape()
    {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.or(shape, Shapes.box(0.1875, 0, 0.125, 0.8125, 0.375, 1.4375));
        shape = Shapes.or(shape, Shapes.box(0.125, 0.375, 0.0625, 0.875, 0.6875, 1.5));
        shape = Shapes.or(shape, Shapes.box(0.875, 0.5, 0, 1, 0.8125, 1));
        shape = Shapes.or(shape, Shapes.box(0, 0.5, 0, 0.125, 0.8125, 1));
        shape = Shapes.or(shape, Shapes.box(0.125, 0.5, 0, 0.875, 0.8125, 0.125));
        shape = Shapes.or(shape, Shapes.box(0, 0.828125, 0, 1, 0.890625, 0.9375));
        shape = Shapes.or(shape, Shapes.box(0.0625, 0.6875, 0.9375, 0.9375, 0.875, 1.5625));
        shape = Shapes.or(shape, Shapes.box(0.03125, 1.125, 0.9375, 0.96875, 1.9375, 1.5));
        shape = Shapes.or(shape, Shapes.box(0.125, 0.875, 0.96875, 0.875, 1.125, 1.40625));
        shape = Shapes.or(shape, Shapes.box(0.6875, 0.8125, 0.84375, 0.875, 0.9375, 0.96875));
        shape = Shapes.or(shape, Shapes.box(0.125, 0.8125, 0.84375, 0.3125, 0.9375, 0.96875));

        return shape;
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN);
    }
}
