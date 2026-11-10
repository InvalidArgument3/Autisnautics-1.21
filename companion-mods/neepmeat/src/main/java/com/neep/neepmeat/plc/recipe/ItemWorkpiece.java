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

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.plc.recipe.ManufactureStep;
import com.neep.neepmeat.api.plc.recipe.Workpiece;
import com.neep.neepmeat.init.NMComponents;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import dev.onyxstudios.cca.api.v3.item.ItemTagInvalidationListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ItemWorkpiece extends ItemComponent implements Workpiece, ItemTagInvalidationListener
{
    // TODO: remove
    private static final String KEY = NeepMeat.NAMESPACE + ":work_piece";

    private List<ManufactureStep<?>> stepsCache = null;
    private boolean invalidated;

    public ItemWorkpiece(ItemStack stack)
    {
        super(stack);
    }

    // Checks for validity without implicitly constructing the component.
    public static boolean has(ItemStack stack)
    {
        return stack.getTagElement(NMComponents.WORKPIECE.getId().toString()) != null;
    }

    public void addStep(ManufactureStep<?> step)
    {
        ResourceLocation id = step.getId();
        CompoundTag nbt = getCompound(KEY);

        var list = getList(nbt);
        CompoundTag entry = new CompoundTag();
        entry.putString("id", id.toString());
        entry.put("sub", step.toNbt());

        list.add(entry);

        putCompound(KEY, nbt);
    }

    private ListTag getList(CompoundTag nbt)
    {
        // This silently returns a new empty list if it's not present. Very useful (not).
        var list = nbt.getList("steps", Tag.TAG_COMPOUND);
        if (list.isEmpty())
        {
            nbt.put("steps", list);
        }
        return list;
    }

    @Override
    public List<ManufactureStep<?>> getSteps()
    {
        if (stepsCache != null)
        {
            return stepsCache;
        }

        var nbt = getSubNbt(KEY);
        if (nbt == null)
            return Collections.emptyList();

        var list = getList(nbt);

        List<ManufactureStep<?>> steps = new ArrayList<>();
        for (int i = 0; i < list.size(); ++i)
        {
            var compound = list.getCompound(i);
            String id = compound.getString("id");

            var entry = ManufactureStep.REGISTRY.get(ResourceLocation.tryParse(id));
            if (entry != null)
            {
                steps.add(entry.create(compound.getCompound("sub")));
            }
        }

        stepsCache = steps;
        return steps;
    }

    @Override
    public void clearSteps()
    {
        stepsCache = null;
        var nbt = getSubNbt(KEY);
        if (nbt == null)
            return;

        var list = getList(nbt);
        list.clear();

        // Not sure if the above steps were necessary
        remove(KEY);
    }

    @Override
    public void removeStep(int i)
    {
        stepsCache = null;
        var nbt = getSubNbt(KEY);
        if (nbt == null)
            return;

        var list = getList(nbt);
        list.remove(i);
        nbt.put("steps", list);

        if (list.isEmpty())
        {
            remove(KEY);
        }
        else
        {
            putCompound(KEY, nbt);
        }
    }

    private CompoundTag getSubNbt(String key)
    {
        if (this.getRootTag() == null || !this.getRootTag().contains(key, Tag.TAG_COMPOUND))
        {
            return null;
        }
        return getCompound(key);
    }

    @Override
    public void onTagInvalidated()
    {
        super.onTagInvalidated();
        stepsCache = null;
    }
}
