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

package com.neep.neepmeat.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class NMFoodComponents
{
    public static final FoodProperties ENLIGHTENED_BRAIN = new FoodProperties.Builder().nutrition(6).saturationModifier(0.6f).effect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 1), 1).meat().build();
    public static final FoodProperties MEAT_SCRAP = new FoodProperties.Builder().nutrition(1).saturationModifier(0.3f).meat().build();
    public static final FoodProperties MEAT_BRICK = new FoodProperties.Builder().nutrition(2).saturationModifier(0.3f).effect(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.7f).meat().build();
    public static final FoodProperties COOKED_MEAT_BRICK = new FoodProperties.Builder().nutrition(10).saturationModifier(0.8f).meat().build();
    public static final FoodProperties WHISPER_BREAD = new FoodProperties.Builder().nutrition(5).saturationModifier(0.6f).effect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 1), 0.3f).build();
    public static final FoodProperties PINKDRINK = new FoodProperties.Builder().nutrition(4).saturationModifier(0.4f).effect(new MobEffectInstance(MobEffects.REGENERATION, 40, 2), 1f).build();
    public static final FoodProperties BLOOD_BUBBLE = new FoodProperties.Builder().nutrition(3).saturationModifier(0.2f).effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20, 1), 1f).build();
    public static final FoodProperties MILK_BOTTLE = new FoodProperties.Builder().nutrition(5).saturationModifier(0.2f).build();
    public static final FoodProperties MEAT_CARTON = new FoodProperties.Builder().nutrition(0).saturationModifier(0.0f).build();
    public static final FoodProperties ASBESTOS_DUST = new FoodProperties.Builder().nutrition(2).saturationModifier(0.1f).effect(new MobEffectInstance(MobEffects.DIG_SPEED, 10, 1), 1f).build();
}
