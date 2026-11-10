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

import com.mojang.datafixers.util.Pair;
import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.processing.MeatFluidUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FoodCartonItem extends BaseItem
{
    public FoodCartonItem(TooltipSupplier tooltip, Properties settings)
    {
        super(tooltip, settings);
    }

    @Override
    public boolean isEdible()
    {
        return true;
    }

    @Override
    public SoundEvent getEatingSound()
    {
        return SoundEvents.HONEYCOMB_WAX_ON;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.EAT;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        return super.use(world, user, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user)
    {
        if (this.isEdible())
        {
            return eatFood(user, world, stack);
        }
        return stack;
    }

    // This is necessary because Item::getFoodComponent does not require the ItemStack as an argument, making it
    // impossible to return a custom FoodComponent based on the stack's NBT.
    public ItemStack eatFood(LivingEntity user, Level world, ItemStack stack)
    {
        ItemStack stackCopy = stack.copy();
        if (stack.isEdible())
        {
            world.gameEvent(user, GameEvent.EAT, user.getEyePosition(0));
            world.playSound(null, user.getX(), user.getY(), user.getZ(), getEatingSound(), SoundSource.NEUTRAL, 1.0f, 1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.4f);
            applyFoodEffects(user, stackCopy, world);
            if (!(user instanceof Player player && player.isCreative()))
            {
                stack.shrink(1);
            }

            if (user instanceof Player player)
            {
                var entry = getHungerSaturation(ItemVariant.of(stackCopy));
                player.getFoodData().eat(entry.food, entry.saturation);
            }

            user.gameEvent(GameEvent.EAT);
        }
        return stack;
    }

    public record Entry(int food, float saturation)
    {
        public Entry(FoodProperties foodComponent)
        {
            this(foodComponent.getNutrition(), foodComponent.getSaturationModifier());
        }
    }

    public static Entry getHungerSaturation(ItemVariant variant)
    {
        if (variant.getItem() instanceof FoodCartonItem && variant.getNbt() != null)
        {
            CompoundTag nbt = variant.getNbt();
            int food = (int) MeatFluidUtil.getHunger(nbt);
            float sat = MeatFluidUtil.getSaturation(nbt);
            return new Entry(food, sat);
        }
        else if (variant.getItem().getFoodProperties() != null)
        {
            return new Entry(variant.getItem().getFoodProperties());
        }
        else
        {
            return new Entry(0, 0);
        }
    }

    protected void applyFoodEffects(LivingEntity user, ItemStack stack, Level world)
    {
        Item item = stack.getItem();
        if (item.isEdible())
        {
            List<Pair<MobEffectInstance, Float>> list = item.getFoodProperties().getEffects();
            for (Pair<MobEffectInstance, Float> pair : list)
            {
                if (world.isClientSide || pair.getFirst() == null || !(world.random.nextFloat() < pair.getSecond())) continue;
                user.addEffect(new MobEffectInstance(pair.getFirst()));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Level world, List<Component> tooltip, TooltipFlag tooltipContext)
    {
        CompoundTag nbt = itemStack.getTag();
        float hunger = MeatFluidUtil.getHunger(nbt);
        float saturation = MeatFluidUtil.getSaturation(nbt);
        tooltip.add(Component.translatable("item." + NeepMeat.NAMESPACE + ".food_carton.hunger", hunger));
        tooltip.add(Component.translatable("item." + NeepMeat.NAMESPACE + ".food_carton.saturation", saturation));
        super.appendHoverText(itemStack, world, tooltip, tooltipContext);
    }

    @Override
    public @Nullable FoodProperties getFoodProperties()
    {
        // This method is not stack-aware, so we can't do anything.
        return super.getFoodProperties();
    }

    public int getTint(ItemStack stack, int index)
    {
        if (index == 1)
        {
            int fullCol = MeatFluidUtil.FULL_COL;
            CompoundTag nbt = stack.getTag();
            if (nbt != null)
            {
                float hunger = MeatFluidUtil.getHunger(nbt);
                float f = hunger / MeatFluidUtil.MAX_HUNGER;
                int r = (int) org.joml.Math.lerp(0, (fullCol >> 16) & 0xFF, f);
                int g = (int) org.joml.Math.lerp(0, (fullCol >> 8) & 0xFF, f);
                int b = (int) org.joml.Math.lerp(0, (fullCol) & 0xFF, f);
                return (r << 16) | (g << 8) | b;
            }
        }
        return -1;
    }
}
