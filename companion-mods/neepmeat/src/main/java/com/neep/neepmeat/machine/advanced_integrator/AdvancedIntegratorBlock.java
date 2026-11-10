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

package com.neep.neepmeat.machine.advanced_integrator;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockPattern;
import com.neep.neepmeat.api.big_block.BigBlockStructure;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import com.neep.neepmeat.api.big_block.StructureBlockEntities;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AdvancedIntegratorBlock extends BigBlock<BigBlockStructure.Simple<BigBlockStructureEntity>> implements MeatlibBlock, EntityBlock, DataCable {
    private final BigBlockPattern volume;

    public AdvancedIntegratorBlock(RegistrationContext ctx, Properties settings) {
        super(ctx, settings.noOcclusion());
        volume = BigBlockPattern.makeOddCylinder(1, 0, 0, getStructure().defaultBlockState());
    }

    @Override
    protected BigBlockStructure.Simple<BigBlockStructureEntity> registerStructureBlock(RegistrationContext ctx) {
        BlockEntityType[] box = new BlockEntityType[1];
        BigBlockStructure.BlockEntityRegisterererer<BigBlockStructureEntity> register = structure -> {
            box[0] = BlockEntityType.Builder.of((pos, state) -> new BigBlockStructureEntity(box[0], pos, state), structure).build(null);
            return StructureBlockEntities.register("advanced_integrator_structure", box[0]);
        };
        return ctx.append(this, new CableStructure(this, MeatlibBlockSettings.copyOf(properties), register,
                (pos, state) -> new BigBlockStructureEntity(box[0], pos, state)), "advanced_integrator_structure");
    }

    @Override
    public BigBlockPattern getVolume(BlockState state) {
        return volume;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return getVolume(state).toVoxelShape();
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (world, pos, blockState, entity) -> {
            if (entity instanceof AdvancedIntegratorBlockEntity integrator) {
                integrator.serverTick();
            }
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedIntegratorBlockEntity(pos, state);
    }

    @Override
    public BlockItem blockItem() {
        return (BlockItem) net.minecraft.world.item.Item.BY_BLOCK.get(this);
    }

    public static final class CableStructure extends BigBlockStructure.Simple<BigBlockStructureEntity> implements DataCable {
        public CableStructure(BigBlock<?> parent, Properties settings, BlockEntityRegisterererer<BigBlockStructureEntity> register,
                              java.util.function.BiFunction<BlockPos, BlockState, BigBlockStructureEntity> factory) {
            super(parent, settings, register, factory);
        }
    }
}
