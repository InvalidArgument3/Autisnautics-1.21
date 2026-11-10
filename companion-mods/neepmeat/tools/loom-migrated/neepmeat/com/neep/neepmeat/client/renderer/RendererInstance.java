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

package com.neep.neepmeat.client.renderer;

import com.neep.neepmeat.init.NMComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.collections4.map.AbstractReferenceMap;
import org.apache.commons.collections4.map.ReferenceMap;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public interface RendererInstance
{
    Map<Long, RendererInstance> INSTANCES = new ReferenceMap<>(AbstractReferenceMap.ReferenceStrength.WEAK, AbstractReferenceMap.ReferenceStrength.SOFT, true);
    Map<Item, Supplier<RendererInstance>> FACTORIES = new IdentityHashMap<>();

    static void register(Item item, Supplier<RendererInstance> factory)
    {
        FACTORIES.put(item, factory);
    }

    /**
     * @return This may be null if the ID has not been created/synced from the server.
     */
    @Nullable
    static <T extends RendererInstance> T getNullable(ItemStack stack)
    {
//        NbtCompound nbt = stack.getNbt();
//        if (nbt != null)
//        {
//            long id = nbt.getLong("meatlib:id");
//            if (id != 0)
//            {
//                RendererInstance instance = INSTANCES.get(id);
//                if (instance == null)
//                {
//                    // Try to create the RendererInstance from the item or look up a factory
//                    if (stack.getItem() instanceof RendererInstanceProvider provider)
//                    {
//                        instance = (RendererInstance) provider.createRendererInstance();
//                        INSTANCES.put(id, instance);
//                    }
//                    else
//                    {
//                        var factory = FACTORIES.get(stack.getItem());
//                        if (factory != null)
//                        {
//                            instance = factory.get();
//                        }
//                        else
//                        {
//                            throw new IllegalArgumentException(String.format("RendererInstance requested for  tem %s that does not implement RendererInstanceProvider", stack.getItem()));
//                        }
//                    }
//                }
//
//                return (T) instance;
//            }
//        }

        long id = NMComponents.UNIQUE_ID.get(stack).getId();
        return (T) INSTANCES.computeIfAbsent(id, i -> FACTORIES.get(stack.getItem()).get());
    }
}
