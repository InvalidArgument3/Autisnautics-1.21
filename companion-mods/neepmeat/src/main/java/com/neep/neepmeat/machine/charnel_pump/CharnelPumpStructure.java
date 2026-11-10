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

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockStructure;
import com.neep.neepmeat.api.big_block.BigBlockStructureEntity;
import com.neep.neepmeat.api.live_machine.LivingMachineStructure;
import com.neep.neepmeat.api.live_machine.PropertyValue;
import com.neep.neepmeat.api.live_machine.StructureProperty;
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
import java.util.EnumMap;

public class CharnelPumpStructure extends BigBlockStructure<CharnelPumpStructure.CPSBlockEntity> implements LivingMachineStructure
{
    public CharnelPumpStructure(BigBlock<?> parent, Properties settings)
    {
        super(parent, settings);
    }

    @Override
    protected BlockEntityType<CPSBlockEntity> registerBlockEntity()
    {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "charnel_pump_structure"),
                FabricBlockEntityTypeBuilder.create(
                        (p, s) -> new CharnelPumpStructure.CPSBlockEntity(getBlockEntityType(), p, s),
                        this).build());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return Shapes.block();
    }

    @Override
    public EnumMap<StructureProperty, PropertyValue> getProperties()
    {
        return StructureProperty.EMPTY;
    }

    public static class CPSBlockEntity extends BigBlockStructureEntity
    {
        public CPSBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
        {
            super(type, pos, state);
        }
    }
}
