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

import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.entity.bovine_horror.BovineHorrorEntity;
import com.neep.neepmeat.implant.entity.EntityImplantInstaller;
import com.neep.neepmeat.init.NMEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class ChrysalisItem extends BaseItem implements EntityImplantInstaller
{
    public ChrysalisItem(RegistrationContext registryName, TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(tooltipSupplier, settings.rarity(Rarity.RARE));
    }

    @Override
    public void install(Entity entity)
    {
        entity.discard();
        Level world = entity.getCommandSenderWorld();
        if (!world.isClientSide())
        {
            BovineHorrorEntity bovineHorror = NMEntities.BOVINE_HORROR.create(world);
            bovineHorror.setPos(entity.getX(), entity.getY() + 1, entity.getZ());
            world.addFreshEntity(bovineHorror);
        }
    }

}
