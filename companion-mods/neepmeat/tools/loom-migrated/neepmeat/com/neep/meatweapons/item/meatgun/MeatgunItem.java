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

package com.neep.meatweapons.item.meatgun;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.init.MWComponents;
import com.neep.meatweapons.item.GunItem;
import com.neep.meatweapons.item.WeakTwoHanded;
import com.neep.meatweapons.meatgun.AmmunitionProvider;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.item.DrillChassisItem;
import com.neep.neepmeat.item.FoodCartonItem;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class MeatgunItem extends BaseItem implements Meatgun, WeakTwoHanded, GunItem
{
    public MeatgunItem(TooltipSupplier tooltipSupplier, Properties settings)
    {
        // Can't call maxCount here because implementations might want damage and settings will throw an exception if damage is set before stack size
        super(tooltipSupplier, settings);
        FluidStorage.ITEM.registerForItems(this::getStorage, this);
    }

    private Storage<FluidVariant> getStorage(ItemStack stack, ContainerItemContext containerItemContext)
    {
        return new DrillChassisItem.FoodStorage(containerItemContext);
    }

    public static final TooltipSupplier MAX_COMPLEXITY = (stack, tooltip) ->
    {
        tooltip.add(Component.translatable("tooltip.meatweapons.meatgun_module.max_complexity", ((Meatgun) stack.getItem()).getMaxComplexity(stack))
                .withStyle(ChatFormatting.GRAY));
    };

    @Override
    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack)
    {
        return false;
    }

    @Override
    public boolean displayArmFirstPerson(ItemStack stack, InteractionHand hand)
    {
        return true;
    }

    @Override
    public void trigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        MWComponents.MEATGUN.get(stack).trigger(world, player, stack, id, pitch, yaw, handType, hand);
    }

    @Override
    public void release(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        MWComponents.MEATGUN.get(stack).release(world, player, stack, id, pitch, yaw, handType, hand);
    }

    @Override
    public void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        MWComponents.MEATGUN.get(stack).tickTrigger(world, player, stack, id, pitch, yaw, handType, hand);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected)
    {
        if (entity instanceof Player player)
        {
            @Nullable MeatgunComponent component = MWComponents.MEATGUN.getNullable(stack);
            if (component != null)
            {
                // Newly created meatguns are immediately assigned a UUID when they enter the player's inventory.
                component.tickUUIDResolution(player);

//                if (player.isHolding(stack::equals))
                {
                    component.commonTick(player);

                    if (world.isClientSide())
                        component.clientTick(player);
                }
            }
            else
            {
                MeatLib.LOGGER.error("Meatgun component has not been registered for item {}", this);
            }
        }
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickType, Player player)
    {
        return super.overrideStackedOnOther(stack, slot, clickType, player);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Level world, List<Component> tooltip, TooltipFlag tooltipContext)
    {
        MeatgunComponent meatgun = getMeatgunComponent(itemStack);
        if (meatgun.getRootHolder().moduleCache().getModules().size() == 1)
            super.appendHoverText(itemStack, world, tooltip, tooltipContext);

        if (meatgun.getRootHolder().coreImplants().fuelType() == AmmunitionType.METABOLIC)
        {
            tooltip.add(Component.nullToEmpty("METAB: "
                            + meatgun.getRootHolder().moduleCache().getAmmo(AmmunitionType.METABOLIC)
                            + "/"
                            + meatgun.getRootHolder().moduleCache().getCapacity(AmmunitionType.METABOLIC))
                    .copy().setStyle(Style.EMPTY.withColor(PLCCols.MEAT_ITEM_BAR_COL)));
        }

        if (tooltipContext.isAdvanced())
            tooltip.add(Component.literal("UUID: " + MWComponents.MEATGUN.get(itemStack).getUuid()).withStyle(ChatFormatting.DARK_GRAY));
    }

    private static @NotNull MeatgunComponent getMeatgunComponent(ItemStack itemStack)
    {
        return MWComponents.MEATGUN.get(itemStack);
    }

    @Override
    public Vec3 getMuzzleOffset(LivingEntity entity, ItemStack stack)
    {
        return Vec3.ZERO;
    }

    @Override
    public void playSound(Level world, LivingEntity entity, GunSounds sound)
    {

    }

    @Override
    public int getBarColor(ItemStack stack)
    {
        return PLCCols.MEAT_ITEM_BAR_COL;
    }

    @Override
    public boolean isBarVisible(ItemStack stack)
    {
        return MWComponents.MEATGUN.get(stack).getCore().getResource() == AmmunitionType.METABOLIC;
//        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack)
    {
        var component = MWComponents.MEATGUN.get(stack);
        var core = component.getCore();
        return (int) (13. * core.getAmount() / (double) core.getCapacity());
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, @NotNull Player player, SlotAccess cursorStackReference)
    {
        ItemStack cursorStack = cursorStackReference.get();
        AmmunitionProvider provider = AmmunitionProvider.LOOKUP.find(cursorStack, cursorStackReference::set);
        if (provider != null)
        {
            MeatgunComponent component = MWComponents.MEATGUN.get(stack);
            if (!player.level().isClientSide())
            {
                component.getRootHolder().reload(provider, player, true, null);
            }
            return true;
        }

        Item item = cursorStack.getItem();
        FoodProperties food = item.getFoodProperties();
        if (item instanceof FoodCartonItem || food != null)
        {
            int amount = clickType == ClickAction.PRIMARY ? cursorStack.getCount() : 1;
            return eatFood(player.level(), player, stack, cursorStack,
                    amount, cursorStackReference);
        }

        return super.overrideStackedOnOther(stack, slot, clickType, player);
    }

    protected boolean eatFood(Level world, Player user, ItemStack meatgunStack, ItemStack foodStack, int amount, SlotAccess slot)
    {
        MeatgunComponent meatgun = MWComponents.MEATGUN.getNullable(meatgunStack);
        if (meatgun != null)
        {
            if (!world.isClientSide())
            {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.HONEYCOMB_WAX_ON, SoundSource.PLAYERS,
                        1.0f, 1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.4f);
                try (Transaction transaction = Transaction.openOuter())
                {
                    int toInsert = Math.min(16, amount);
                    long inserted = meatgun.getCore().acceptResource(ItemVariant.of(foodStack), toInsert, transaction);
                    if (inserted > 0)
                    {
                        transaction.commit();
                        foodStack.shrink((int) inserted);
                        slot.set(foodStack);
                    }
                }
            }

            return true;
        }

        return false;
    }
}
