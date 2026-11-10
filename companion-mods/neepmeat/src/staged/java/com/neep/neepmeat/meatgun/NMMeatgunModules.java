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

package com.neep.neepmeat.meatgun;

import com.neep.meatweapons.meatgun.SlotType;
import com.neep.meatweapons.meatgun.module.AttributeContainer;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMToolMaterials;
import com.neep.neepmeat.meatgun.module.DrillChassisBaseModule;
import com.neep.neepmeat.meatgun.module.MiningMeatgunModuleType;
import com.neep.neepmeat.meatgun.module.ReciprocatingDrillHeadModule;
import com.neep.neepmeat.meatgun.module.RotaryDrillHeadModule;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

import static com.neep.meatweapons.meatgun.module.MeatgunModules.register;

public class NMMeatgunModules
{
    public static final MeatgunModule.Type<DrillChassisBaseModule> BASE_ROCK_DRILL = register(
            ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "base_rock_drill"),
            (l, p, stack) -> new DrillChassisBaseModule(l), DrillChassisBaseModule::fromNbt);
    public static final MeatgunModule.Type<ReciprocatingDrillHeadModule> ROCK_DRILL_HEAD = register(
            ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "rock_drill_head"),
            new MiningMeatgunModuleType<>(
                    Tiers.IRON,
                    new AttributeContainer().complexity(12)
                    .applicable(SlotType.DRILL),
            (l, p, stack) -> new ReciprocatingDrillHeadModule(l), ReciprocatingDrillHeadModule::fromNbt));

    public static final MiningMeatgunModuleType<RotaryDrillHeadModule> IRON_ROTARY_DRILL_HEAD = register(
            ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "iron_rotary_drill_head"),
            makeRotaryHead(NMToolMaterials.EMBOSSED_MEAT_STEEL, 12, () -> NMMeatgunModules.IRON_ROTARY_DRILL_HEAD));
    public static final MiningMeatgunModuleType<RotaryDrillHeadModule> MEAT_STEEL_ROTARY_DRILL_HEAD = register(
            ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "meat_steel_rotary_drill_head"),
            makeRotaryHead(Tiers.DIAMOND, 12, () -> NMMeatgunModules.MEAT_STEEL_ROTARY_DRILL_HEAD));
    public static final MiningMeatgunModuleType<RotaryDrillHeadModule> DIAMOND_ROTARY_DRILL_HEAD = register(
            ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "diamond_rotary_drill_head"),
            makeRotaryHead(Tiers.NETHERITE, 12, () -> NMMeatgunModules.DIAMOND_ROTARY_DRILL_HEAD));

    public static MiningMeatgunModuleType<RotaryDrillHeadModule> makeRotaryHead(Tier toolMaterial, int complexity, Supplier<MiningMeatgunModuleType<?>> typeSupplier)
    {
        return new MiningMeatgunModuleType<>(
                toolMaterial,
                new AttributeContainer().complexity(complexity)
                        .applicable(SlotType.DRILL),
                (l, p, stack) -> new RotaryDrillHeadModule(typeSupplier.get(), l),
                (l, n) -> RotaryDrillHeadModule.fromNbt(typeSupplier.get(), l, n));
    }

    public static void init()
    {

    }
}
