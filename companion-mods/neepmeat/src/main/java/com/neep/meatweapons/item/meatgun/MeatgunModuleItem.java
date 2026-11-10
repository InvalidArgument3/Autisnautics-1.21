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

import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class MeatgunModuleItem extends Item
{
    private static final Map<MeatgunModuleItem, MeatgunModule.Type<?>> ITEM_TO_TYPE = new HashMap<>();
    private static final Map<MeatgunModule.Type<?>, MeatgunModuleItem> TYPE_TO_ITEM = new HashMap<>();
    public final MeatgunModule.Type<?> type;
    @Nullable private final TooltipSupplier tooltipSupplier;
    private String translationKey;

    public MeatgunModuleItem(MeatgunModule.Type<?> type, @Nullable TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(settings.stacksTo(1));
        this.type = type;
        this.tooltipSupplier = tooltipSupplier;
        register(this, type);
    }

    public MeatgunModuleItem(MeatgunModule.Type<?> type, Properties settings)
    {
        this(type, null, settings);
    }

    public static void register(MeatgunModuleItem item, MeatgunModule.Type<?> type)
    {
        if (TYPE_TO_ITEM.containsKey(type))
            throw new IllegalArgumentException("Module type '" + type + "' is already bound to item '" + TYPE_TO_ITEM.get(type) + "'");

        ITEM_TO_TYPE.put(item, type);
        TYPE_TO_ITEM.put(type, item);
    }

    public static ItemStack get(MeatgunModule module)
    {
        MeatgunModuleItem item = TYPE_TO_ITEM.get(module.getType());
        if (item != null)
        {
            return item.createStack(module);
        }
        return ItemStack.EMPTY;
    }

    protected ItemStack createStack(MeatgunModule module)
    {
        return module.createStack(this);
    }

    public static MeatgunModule.Type<?> get(ItemStack stack)
    {
        if (stack.getItem() instanceof MeatgunModuleItem moduleItem)
        {
            MeatgunModule.Type<?> type = ITEM_TO_TYPE.get(moduleItem);
            if (type != null)
            {
                return type;
            }
        }
        return MeatgunModule.DEFAULT_TYPE;
    }

    @Override
    protected String getOrCreateDescriptionId()
    {
        if (this.translationKey == null)
        {
            this.translationKey = Util.makeDescriptionId("meatgun_module", BuiltInRegistries.ITEM.getKey(this));
        }

        return this.translationKey;
    }

    @Override
    public String getDescriptionId()
    {
        return this.getOrCreateDescriptionId();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext world, List<Component> tooltip, TooltipFlag context)
    {
//        if (Screen.hasShiftDown())
//        {
//            tooltip.add(Text.translatable("tooltip.meatweapons.meatgun_module_1").formatted(Formatting.GOLD).formatted(Formatting.BOLD));
//            if (type.complexity() != 0)
//                tooltip.add(Text.translatable("tooltip.meatweapons.meatgun_module.complexity", type.complexity()).formatted(Formatting.GRAY));

            if (tooltipSupplier != null)
                tooltipSupplier.apply(stack, tooltip);

        super.appendHoverText(stack, world, tooltip, context);
    }

    // TODO: Why is this necessary? This item should do nothing.
    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack)
    {
        return false;
    }
}
