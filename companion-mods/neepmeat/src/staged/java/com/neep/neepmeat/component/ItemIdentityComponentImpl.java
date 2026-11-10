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

package com.neep.neepmeat.component;

import com.neep.neepmeat.init.NMComponents;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemIdentityComponentImpl extends ItemComponent implements ItemIdentityComponent
{
    public static void register(Item item, ItemComponentFactoryRegistry registry)
    {
        registry.register(item, NMComponents.UNIQUE_ID, s -> new ItemIdentityComponentImpl(s, NMComponents.UNIQUE_ID));
    }

    @Nullable private Long id = null;

    public ItemIdentityComponentImpl(ItemStack stack, ComponentKey<?> key)
    {
        super(stack, key);
        getId(); // Assign the ID here
    }

    @Override
    public long getId()
    {
        if (id == null)
        {
            // Hopefully creating the tag here doesn't cause prahblums when done on the client
            if (getOrCreateRootTag().contains("id"))
            {
                id = getLong("id");
            }
            else
            {
                // TODO: Change to something sensible please
                id = new Random().nextLong();
                putLong("id", id);
            }
        }

        return id;
    }

    @Override
    public void onTagInvalidated()
    {
        id = null;
    }
}
