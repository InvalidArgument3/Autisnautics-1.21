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
import com.neep.neepmeat.entity.compressor_scutter.CompressorScutterEntity;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;

public class CompressorScutterItem extends ScutterItem<CompressorScutterEntity>
{
    public CompressorScutterItem(Supplier<EntityType<CompressorScutterEntity>> entityType, TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(entityType, tooltipSupplier, settings);
    }

    @Override
    protected void processEntity(CompressorScutterEntity entity, UseOnContext context)
    {
        super.processEntity(entity, context);

        Player playerEntity = context.getPlayer();
        if (playerEntity != null)
        {
            entity.setOwner(playerEntity.getUUID());
        }
    }
}
