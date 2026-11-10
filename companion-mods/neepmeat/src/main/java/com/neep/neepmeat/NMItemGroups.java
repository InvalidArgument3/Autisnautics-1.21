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

package com.neep.neepmeat;

import com.neep.meatlib.util.MeatlibItemGroups;
import com.neep.neepmeat.transport.FluidTransport;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public class NMItemGroups
{
//    public static final ItemGroup GENERAL = FabricItemGroup.builder(new Identifier(NeepMeat.NAMESPACE, "general"))
//            .icon(() -> new ItemStack(FluidTransport.PUMP)).build();
//
    public static final MeatlibItemGroups.SpecialEntryCollector GENERAL_EXTRA = new MeatlibItemGroups.SpecialEntryCollector();

    public static final CreativeModeTab GENERAL = FabricItemGroup.builder()
            .title(Component.translatable("itemGroup." + NeepMeat.NAMESPACE + ".general"))
            .icon(() -> FluidTransport.PUMP.asItem().getDefaultInstance())
            .displayItems(GENERAL_EXTRA)
            .build();

    public static final CreativeModeTab INGREDIENTS = GENERAL;
    public static final CreativeModeTab FOOD = GENERAL;
    public static final CreativeModeTab BUILDING = GENERAL;

//    public static final ItemGroup INGREDIENTS = FabricItemGroup.builder( new Identifier(NeepMeat.NAMESPACE, "ingredients"))
//            .icon(() -> new ItemStack(NMItems.INTERNAL_COMPONENTS)).build();
//
//    public static final ItemGroup FOOD = FabricItemGroup.builder( new Identifier(NeepMeat.NAMESPACE, "food"))
//            .icon(() -> new ItemStack(NMItems.COOKED_MEAT_BRICK)).build();

//    public static final ItemGroup INGREDIENTS = FabricItemGroupBuilder.build( new Identifier(NeepMeat.NAMESPACE, "ingredients"),
//            () -> new ItemStack(NMItems.INTERNAL_COMPONENTS));
//
//    public static final ItemGroup FOOD = FabricItemGroupBuilder.build( new Identifier(NeepMeat.NAMESPACE, "food"),
//            () -> new ItemStack(NMItems.COOKED_MEAT_BRICK));

    public static void init()
    {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "general"), GENERAL);

    }
}
