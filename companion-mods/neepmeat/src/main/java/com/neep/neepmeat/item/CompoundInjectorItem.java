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

import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.init.NMSounds;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import java.util.List;

public class CompoundInjectorItem extends BaseItem
{
    @Override
    public final int healsFor = 8; // Health replenished each use (discounting initial damage)
    public final int initialDamage = 1;

    public CompoundInjectorItem(RegistrationContext ctx, FabricItemSettings settings)
    {
        super((stack, tooltip) -> applyTooltip(stack, tooltip), settings.durability(3).defaultDurability(3));
    }

    @Override
    public int getBarColor(ItemStack stack)
    {
        return Mth.hsvToRgb(0f, 1.0F, 1.0F);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return hasUses(stack) ? UseAnim.BLOCK : UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity)
    {
        return 15;
    }

    @Override
    public boolean isBarVisible(ItemStack stack)
    {
        return true;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user)
    {
        if (!world.isClientSide && user instanceof Player)
        {
            use(stack, (Player) user);
        }
        if (world.isClientSide && user instanceof LocalPlayer)
        {
//            ClientPlayerEntity clientPlayer = (ClientPlayerEntity) user;
//            MinecraftClient.getInstance().player.
        }
        return user.eat(world, stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        ItemStack stack = user.getItemInHand(hand);
        if (hasUses(stack))
        {
            user.startUsingItem(hand);
        }
        return InteractionResultHolder.fail(user.getItemInHand(hand));
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference)
    {
        ItemStack cursorStack = cursorStackReference.get();
        if (cursorStack.getItem().equals(NMItems.CRUDE_INTEGRATION_CHARGE) && stack.getDamageValue() != 0)
        {
            stack.setDamageValue(0);
            cursorStack.shrink(1);
            return true;
        }
        return false;
    }

    public boolean hasUses(ItemStack stack)
    {
        return stack.getDamageValue() < this.getMaxDamage();
    }


    protected static void applyTooltip(ItemStack item, List<Component> tooltip)
    {
        if (Screen.hasShiftDown())
        {
            tooltip.add(Component.translatable(item.getDescriptionId() + "_lore_0").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(item.getDescriptionId() + "_lore_1", NMItems.CRUDE_INTEGRATION_CHARGE.getDescription()).withStyle(ChatFormatting.YELLOW));
        }
        else TooltipSupplier.shiftForInfo(tooltip);
    }

    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected)
    {
        int heals = getHealsRemaining(stack);
        if (heals > 0 && entity instanceof Player)
        {
            ((Player) entity).heal(1);
            setHealsRemaining(stack, heals - 1);
        }
    }

    public static void setHealsRemaining(ItemStack stack, int heals)
    {
        CompoundTag nbt = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        nbt.putInt("Heals", heals);
    }

    public static int getHealsRemaining(ItemStack stack)
    {
        CompoundTag nbt = stack.getTag();
        return nbt != null ? nbt.getInt("Heals") : 0;
    }

    public int getHealsFor()
    {
        return healsFor;
    }

    private void use(ItemStack stack, Player player)
    {
        if (hasUses(stack))
        {
            player.getCommandSenderWorld().playSound(null, player.blockPosition(), NMSounds.COMPOUND_INJECTOR.get(), SoundSource.PLAYERS, 1f, 1f);
            if (!player.isCreative())
            {
                stack.setDamageValue(stack.getDamageValue() + 1);
            }
            // Damage player by one heart.
//            player.damage(player.getDamageSources().magic(), initialDamage);
            player.hurt(player.level().damageSources().generic(), initialDamage);
            setHealsRemaining(stack, healsFor + initialDamage);
        }
    }
}
