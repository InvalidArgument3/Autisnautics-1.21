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

package com.neep.meatweapons.client.renderer.meatgun;

import com.neep.meatweapons.client.MWExtraModels;
import com.neep.meatweapons.meatgun.module.BoneStealerModule;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.meatweapons.meatgun.module.MeatgunModules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import java.util.IdentityHashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class MeatgunModuleRenderers
{
    private static final Map<MeatgunModule.Type<?>, MeatgunModuleRenderer.Factory<?>> RENDERER_FACTORIES = new IdentityHashMap<>();
    private static final Map<MeatgunModule.Type<? extends MeatgunModule>, MeatgunModuleRenderer<?>> RENDERERS = new IdentityHashMap<>();

    public static <T extends MeatgunModule> void register(MeatgunModule.Type<T> type, MeatgunModuleRenderer.Factory<T> factory)
    {
        RENDERER_FACTORIES.put(type, factory);
    }

    public static <T extends MeatgunModule> void register1(MeatgunModule.Type<T> type, MeatgunModuleRenderer.Factory<? super MeatgunModule> factory)
    {
        RENDERER_FACTORIES.put(type, factory);
    }

    public static <T extends MeatgunModule> MeatgunModuleRenderer<T> get(T module)
    {
        return (MeatgunModuleRenderer<T>)
                RENDERERS.computeIfAbsent(module.getType(), id -> RENDERER_FACTORIES.get(module.getType()).create(Minecraft.getInstance()));
    }

    public static void init()
    {
        register1(MeatgunModules.BASIC_PISTOL_BASE, c -> new SimpleModuleRenderer<>(c.getItemRenderer(), MWExtraModels.BASIC_PISTOL_BASE));
        register1(MeatgunModules.BASE_PISTOL, c -> new SimpleModuleRenderer<>(c.getItemRenderer(), MWExtraModels.MEATGUN_BASE));
        register1(MeatgunModules.BONE_STEALER, c -> new SimpleModuleRenderer<>(c.getItemRenderer(), MWExtraModels.BONE_STEALER));
        register(MeatgunModules.BASE_STAFF, BaseStaffModuleRenderer::new);
        register1(MeatgunModules.BASE_LONG_GUN, c -> new SimpleModuleRenderer<>(c.getItemRenderer(), MWExtraModels.LONG_GUN_BASE));
        register(MeatgunModules.PISTOL, PistolModuleRenderer::new);
        register(MeatgunModules.CHUGGER, ChuggerModuleRenderer::new);
        register(MeatgunModules.BOSHER, c -> new SimpleModuleRenderer<>(c.getItemRenderer(), MWExtraModels.BOSHER));
        register(MeatgunModules.LONG_BOI, LongBoiModuleRenderer::new);
        register(MeatgunModules.GRENADE_LAUNCHER, GrenadeLauncherModuleRenderer::new);
        register(MeatgunModules.BLOODTHROWER, BloodthrowerModuleRenderer::new);
        register(MeatgunModules.SHORT_PHAGE_RAY, ShortPhageRayModuleRenderer::new);
        register(MeatgunModules.TRIPLE_CAROUSEL, TripleCarouselModuleRenderer::new);
        register(MeatgunModules.DOUBLE_CAROUSEL, DoubleCarouselModuleRenderer::new);
        register(MeatgunModules.UNDERBARREL, UnderbarrelModuleRenderer::new);
        register(MeatgunModules.BATTERY, BatteryModuleRenderer::new);
        register(MeatgunModules.HOMING_BRAIN, HomingModuleRenderer::new);
        register(MeatgunModules.SLOW_BARREL, c -> new SimpleModuleRenderer<>(c.getItemRenderer(), MWExtraModels.SLOW_BARREL));
        register(MeatgunModules.BASIC_STOCK, c -> new SimpleModuleRenderer<>(c.getItemRenderer(), MWExtraModels.BASIC_STOCK));
        register(MeatgunModules.PHAGE_RAY_SPEED_MODIFIER, PhageRaySpeedModifierModuleRenderer::new);
        register(MeatgunModules.HALBERD, HalberdModuleRenderer::new);
        register(MeatgunModules.SHOCK_STAFF, ShockStaffModuleRenderer::new);
    }
}
