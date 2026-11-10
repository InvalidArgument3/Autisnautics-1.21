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

package com.neep.neepmeat.machine.large_motor;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BigBlockStructure;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class LargeMotorStructureBlock extends BigBlockStructure<LargeMotorStructureEntity>
{
    public LargeMotorStructureBlock(BigBlock<?> parent, Properties settings)
    {
        super(parent, settings);
    }

    protected BlockEntityType<LargeMotorStructureEntity> registerBlockEntity()
    {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "large_motor_structure"),
                FabricBlockEntityTypeBuilder.create(
                        (p, s) -> new LargeMotorStructureEntity(getBlockEntityType(), p, s),
                        this).build());
    }
}
