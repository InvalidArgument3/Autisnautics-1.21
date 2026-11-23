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

package com.neep.neepmeat.implant.entity;

import com.neep.neepmeat.init.NMComponents;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class MobImplantManager implements EntityImplantManager, ServerTickingComponent, AutoSyncedComponent
{
    private final Map<ResourceLocation, EntityImplant> installed = new HashMap<>();
    private final Mob mobEntity;

    public MobImplantManager(Mob mobEntity)
    {
        this.mobEntity = mobEntity;
    }

    @Override
    public boolean installImplant(ResourceLocation implantId)
    {
        addImplant(implantId);
        NMComponents.IMPLANT_MANAGER.sync(mobEntity);
        return true;
    }

    private void addImplant(ResourceLocation id)
    {
        var constructor = ImplantRegistry.REGISTRY.get(id);
        if (constructor != null)
        {
            EntityImplant implant = constructor.create(mobEntity);
            installed.put(id, implant);
            implant.onInstall();

            var modifiers = implant.getModifiers();

            if (!modifiers.isEmpty())
                mobEntity.getAttributes().addTransientAttributeModifiers(modifiers);

            NMComponents.IMPLANT_MANAGER.sync(mobEntity);
        }
    }

    @Override
    public boolean removeImplant(ResourceLocation id)
    {
        EntityImplant implant = installed.remove(id);
        if (implant != null)
        {
            implant.onUninstall();
            var modifiers = implant.getModifiers();
            if (!modifiers.isEmpty())
                mobEntity.getAttributes().removeAttributeModifiers(modifiers);

            NMComponents.IMPLANT_MANAGER.sync(mobEntity);

            return true;
        }

        return false;
    }

    @Override
    public <T extends EntityImplant> @Nullable T getImplant(ResourceLocation id)
    {
        if (installed.isEmpty())
            return null;

        // EEk!
        return (T) installed.get(id);
    }

    @Override
    public Set<ResourceLocation> getInstalled()
    {
        return installed.keySet();
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag nbt)
    {
        ListTag list = nbt.getList("installed", Tag.TAG_STRING);
        installed.clear();
        for (int i = 0; i < list.size(); ++i)
        {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            addImplant(id);
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag nbt)
    {
        ListTag list = new ListTag();
        installed.forEach((id, implant) ->
        {
            list.add(StringTag.valueOf(id.toString()));
        });

        nbt.put("installed", list);
    }

    @Override
    public void serverTick()
    {
        var it = installed.values().iterator();
        while (it.hasNext())
        {
            it.next().tick(it::remove);
        }
    }

    @Override
    public void onDeath()
    {
        if (!mobEntity.level().isClientSide())
        {
            var it = installed.values().iterator();
            while (it.hasNext())
            {
                it.next().onDeath(it::remove);
            }
        }
    }
}
