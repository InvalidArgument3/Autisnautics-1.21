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

package com.neep.neepmeat.implant.item;

import com.google.common.collect.Sets;
import com.neep.meatweapons.implant.AmmunitionGeneratorImplant;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.neepmeat.implant.NMImplants;
import com.neep.neepmeat.implant.entity.EntityImplant;
import com.neep.neepmeat.implant.entity.ImplantManager;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ItemImplantManager extends ItemComponent implements ImplantManager
{
    public ItemImplantManager(ItemStack stack)
    {
        super(stack);
    }

    @Override
    public boolean installImplant(ResourceLocation implantId)
    {
        ListTag list = getList("implants", Tag.TAG_COMPOUND);

        // Fix pls
//        if (list.contains(implantId))
//            return;

        CompoundTag n = new CompoundTag();
        n.putString("id", implantId.toString());
        list.add(n);

        putList("implants", list);
        return true;
    }

    @Override
    public boolean removeImplant(ResourceLocation id)
    {
        ListTag list = getList("implants", Tag.TAG_COMPOUND);

        boolean removed = list.removeIf(nbt ->
                ((CompoundTag) nbt).getString("id").equals(id.toString()));

        putList("implants", list);

        return removed;
    }

    @Override
    public <T extends EntityImplant> @Nullable T getImplant(ResourceLocation id)
    {
        return null;
    }

    @Override
    public Set<ResourceLocation> getInstalled()
    {
        Set<ResourceLocation> ids = Sets.newHashSet();
        ListTag list = getList("implants", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i)
        {
            ids.add(ResourceLocation.tryParse(list.getCompound(i).getString("id")));
        }
        return ids;
    }

    @Override
    public AmmunitionType fuelType()
    {
        if (isInstalled(PneumaticMotorImplant.ID)) // TODO: abstract this to a slot mechanic
        {
            return AmmunitionType.AIR;
        }
        else if (isInstalled(LivingToolImplant.ID) || isInstalled(AmmunitionGeneratorImplant.ID))
        {
            return AmmunitionType.METABOLIC;
        }

        return null;
    }

    public ResourceLocation getCoreType()
    {
        if (isInstalled(PneumaticMotorImplant.ID))
        {
            return PneumaticMotorImplant.ID;
        }
        else if (isInstalled(LivingToolImplant.ID))
        {
            return LivingToolImplant.ID;
        }

        return NMImplants.EMPTY;
    }

    public void copyTo(ImplantManager other)
    {
        for (var installed : getInstalled())
        {
            other.installImplant(installed);
        }
    }
}
