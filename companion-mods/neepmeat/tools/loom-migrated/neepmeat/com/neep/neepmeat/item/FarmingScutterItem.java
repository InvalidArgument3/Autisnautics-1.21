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

package com.neep.neepmeat.item;

import com.neep.meatlib.item.TooltipSupplier;
import com.neep.neepmeat.entity.scutter.FarmingScutter;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class FarmingScutterItem extends ScutterItem<FarmingScutter>
{
    public FarmingScutterItem(Supplier<EntityType<FarmingScutter>> entityType, TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(entityType, tooltipSupplier, settings);
    }

    @Override
    protected @Nullable Vec3 canPlaceAt(Level world, BlockPos pos, Vec3 hit, Direction side)
    {
        Vec3 epos = super.canPlaceAt(world, pos, hit, side);
        if (epos != null && FarmingScutter.hasMoisture(world.getBlockState(BlockPos.containing(epos).below())))
            return epos;

        return null;
    }

    @Override
    protected void processEntity(FarmingScutter entity, UseOnContext context)
    {
        entity.setHomePos(BlockPos.containing(entity.position()));

        if (ItemStorage.SIDED.find(context.getLevel(), context.getClickedPos(), context.getClickedFace()) != null)
        {
            entity.setStoragePos(context.getClickedPos());
        }
    }
}
