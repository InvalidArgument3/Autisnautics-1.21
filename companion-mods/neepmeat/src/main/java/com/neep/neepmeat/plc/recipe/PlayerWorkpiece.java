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

package com.neep.neepmeat.plc.recipe;

import com.google.common.collect.Lists;
import com.neep.neepmeat.api.plc.recipe.ManufactureStep;
import com.neep.neepmeat.api.plc.recipe.Workpiece;
import com.neep.neepmeat.init.NMComponents;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class PlayerWorkpiece implements Workpiece, AutoSyncedComponent
{
    protected final List<ManufactureStep<?>> steps = Lists.newArrayList();
    protected final Entity entity;

    public PlayerWorkpiece(Player entity)
    {
        this.entity = entity;
    }

    private void sync()
    {
        NMComponents.WORKPIECE.sync(entity);
    }

    @Override
    public void addStep(ManufactureStep<?> step)
    {
        steps.add(step);
        sync();
    }

    @Override
    public List<ManufactureStep<?>> getSteps()
    {
        return steps;
    }

    @Override
    public void clearSteps()
    {
        steps.clear();
        sync();
    }

    @Override
    public void removeStep(int i)
    {
        steps.remove(i);
        sync();
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag)
    {
        steps.clear();
        ListTag list = tag.getList("steps", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); ++i)
        {
            CompoundTag stepNbt = list.getCompound(i);

            ResourceLocation id = ResourceLocation.tryParse(stepNbt.getString("id"));
            var provider = ManufactureStep.REGISTRY.get(id);
            if (provider != null)
            {
                ManufactureStep<?> step = provider.create(stepNbt.getCompound("step"));
                steps.add(step);
            }
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag)
    {
        ListTag list = new ListTag();
        for (var step : steps)
        {
            CompoundTag stepNbt = new CompoundTag();
            stepNbt.putString("id", step.getId().toString());
            stepNbt.put("step", step.toNbt());
            list.add(stepNbt);
        }
        tag.put("steps", list);
    }
}
