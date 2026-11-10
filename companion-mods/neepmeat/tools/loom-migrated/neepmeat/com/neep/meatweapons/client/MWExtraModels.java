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

package com.neep.meatweapons.client;

import com.neep.meatweapons.MeatWeapons;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.fabricmc.fabric.api.client.model.ExtraModelProvider;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Consumer;

public class MWExtraModels implements ModelLoadingPlugin
{
    public static MWExtraModels EXTRA_MODELS = new MWExtraModels();

    public static ResourceLocation MEATGUN_BASE = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/base_module");
    public static ResourceLocation BASIC_PISTOL_BASE = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/basic_pistol");
    public static ResourceLocation STAFF_BASE = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/staff");
    public static ResourceLocation LONG_GUN_BASE = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/long_gun_base");
    public static ResourceLocation BOSHER = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/bosher");
    public static ResourceLocation MEATGUN_PISTOL = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/pistol");
    public static ResourceLocation MEATGUN_CHUGGER = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/chugger");
    public static ResourceLocation LONG_BOI = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/long_boi");
    public static ResourceLocation BLOODTHROWER = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/bloodthrower");
    public static ResourceLocation GRENADE_LAUNCHER = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/grenade_launcher");
    public static ResourceLocation TRIPLE_CAROUSEL = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/triple_carousel");
    public static ResourceLocation DOUBLE_CAROUSEL = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/double_carousel");
    public static ResourceLocation UNDERBARREL = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/underbarrel");
    public static ResourceLocation BATTERY = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/battery");
    public static ResourceLocation HOMING_BRAIN = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/homing_brain");
    public static ResourceLocation BONE_STEALER = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/bone_stealer");
    public static ResourceLocation SLOW_BARREL = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/slow_barrel");
    public static ResourceLocation BASIC_STOCK = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/basic_stock");
    public static ResourceLocation PHAGE_RAY_SPEED_MODIFIER = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/phage_ray_speed_modifier");
    public static ResourceLocation HALBERD = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/halberd");
    public static ResourceLocation SHOCK_STAFF = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/shock_staff");
    public static ResourceLocation SHORT_PHAGE_RAY = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/short_phage_ray");
    public static ResourceLocation SHORT_PHAGE_RAY_PANEL = new ResourceLocation(MeatWeapons.NAMESPACE, "item/meatgun/short_phage_ray_panel");

    public static ResourceLocation ASSAULT_DRILL_HANDLE = new ResourceLocation(MeatWeapons.NAMESPACE, "item/assault_drill/assault_drill_handle");
    public static ResourceLocation ASSAULT_DRILL_PART_1 = new ResourceLocation(MeatWeapons.NAMESPACE, "item/assault_drill/drill_part_1");
    public static ResourceLocation ASSAULT_DRILL_PART_2 = new ResourceLocation(MeatWeapons.NAMESPACE, "item/assault_drill/drill_part_2");
    public static ResourceLocation ASSAULT_DRILL_PART_3 = new ResourceLocation(MeatWeapons.NAMESPACE, "item/assault_drill/drill_part_3");
    public static ResourceLocation ASSAULT_DRILL_PART_4 = new ResourceLocation(MeatWeapons.NAMESPACE, "item/assault_drill/drill_part_4");

    public static final PartialModel BOUNCE_GRENADE = PartialModel.of(new ResourceLocation(MeatWeapons.NAMESPACE, "entity/bounce_grenade"));

    public void provideExtraModels(Consumer<ResourceLocation> out)
    {
        out.accept(MEATGUN_BASE);
        out.accept(BASIC_PISTOL_BASE);
        out.accept(STAFF_BASE);
        out.accept(LONG_GUN_BASE);
        out.accept(MEATGUN_PISTOL);
        out.accept(BOSHER);
        out.accept(MEATGUN_CHUGGER);
        out.accept(LONG_BOI);
        out.accept(GRENADE_LAUNCHER);
        out.accept(TRIPLE_CAROUSEL);
        out.accept(DOUBLE_CAROUSEL);
        out.accept(UNDERBARREL);
        out.accept(BATTERY);
        out.accept(HOMING_BRAIN);
        out.accept(BONE_STEALER);
        out.accept(SLOW_BARREL);
        out.accept(BASIC_STOCK);
        out.accept(PHAGE_RAY_SPEED_MODIFIER);
        out.accept(HALBERD);

        out.accept(ASSAULT_DRILL_HANDLE);
        out.accept(ASSAULT_DRILL_PART_1);
        out.accept(ASSAULT_DRILL_PART_2);
        out.accept(ASSAULT_DRILL_PART_3);
        out.accept(ASSAULT_DRILL_PART_4);
    }

    @Override
    public void onInitializeModelLoader(Context pluginContext)
    {
        provideExtraModels(pluginContext::addModels);
    }
}
