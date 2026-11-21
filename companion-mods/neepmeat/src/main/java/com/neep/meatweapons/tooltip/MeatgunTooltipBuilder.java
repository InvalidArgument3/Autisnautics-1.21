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

package com.neep.meatweapons.tooltip;

import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.tooltip.TooltipBuilder;
import com.neep.meatweapons.item.meatgun.MeatgunModuleItem;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.SlotType;
import com.neep.meatweapons.meatgun.module.AttributeContainer;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import com.neep.meatlib.theme.PLCCols;
import org.jetbrains.annotations.Nullable;

public class MeatgunTooltipBuilder extends TooltipBuilder
{
    @Nullable private AmmunitionType ammoType;
    private boolean desc;

    public MeatgunTooltipBuilder()
    {

    }

    public MeatgunTooltipBuilder ammoType(AmmunitionType type)
    {
        this.ammoType = type;
        return this;
    }

    public MeatgunTooltipBuilder desc()
    {
        this.desc = true;
        return this;
    }

    @Override
    public TooltipSupplier build()
    {
        return (stack, tooltip) ->
        {
            tooltip.add(Component.translatable( "tooltip.meatweapons.meatgun_module_1").withStyle(ChatFormatting.GRAY));
            if (!Screen.hasShiftDown())
            {
                TooltipSupplier.shiftForInfo(tooltip);
            }
            else if (stack.getItem() instanceof MeatgunModuleItem mmi)
            {
                MeatgunModule.Type<?> type = mmi.type;
                AttributeContainer attributes = type.attributes();

                if (attributes.map.containsKey(MeatgunModule.Attribute.COMPLEXITY))
                {
                    tooltip.add(Component.translatable("tooltip.meatweapons.meatgun_module.complexity", attributes.get(MeatgunModule.Attribute.COMPLEXITY)).withStyle(ChatFormatting.GRAY));
                }

                if (!attributes.applicable.containsAll(SlotType.ALL_SLOTS))
                {
                    tooltip.add(Component.translatable("tooltip.meatweapons.applicable",
                            attributes.applicable.stream()
                                    .map(t -> Component.translatable(t.translationKey()))
                                    .<MutableComponent>collect(() -> Component.empty().copy(), (text, t) -> text.append(t).append(" "), MutableComponent::append)
                            ).setStyle(PLCCols.NORMAL_TEXT));
                }

                if (attributes.map.containsKey(MeatgunModule.Attribute.ATTACK_DAMAGE))
                {
                    tooltip.add(Component.translatable("tooltip.meatweapons.damage", attributes.get(MeatgunModule.Attribute.ATTACK_DAMAGE)).withStyle(ChatFormatting.GRAY));
                }

                if (ammoType != null)
                {
                    tooltip.add(Component.translatable("tooltip.meatweapons.ammunition_type", ammoType.name().toUpperCase()).withStyle(ChatFormatting.GRAY));
                }

                if (attributes.map.containsKey(MeatgunModule.Attribute.AMOUNT_PER_USE))
                {
                    tooltip.add(Component.translatable("tooltip.meatweapons.per_use", attributes.get(MeatgunModule.Attribute.AMOUNT_PER_USE)).withStyle(ChatFormatting.GRAY));
                }

                if (attributes.map.containsKey(MeatgunModule.Attribute.CAPACITY))
                {
                    TooltipSupplier.wrapLines(tooltip, Component.translatable("tooltip.meatweapons.capacity", attributes.get(MeatgunModule.Attribute.CAPACITY)).withStyle(ChatFormatting.GRAY));
                }

                if (attributes.map.containsKey(MeatgunModule.Attribute.COOLDOWN))
                {
                    TooltipSupplier.wrapLines(tooltip, Component.translatable("tooltip.meatweapons.cooldown", attributes.get(MeatgunModule.Attribute.COOLDOWN)).withStyle(ChatFormatting.GRAY));
                }

                if (desc)
                {
                    TooltipSupplier.wrapLines(tooltip, Component.translatable(stack.getDescriptionId() + ".desc").withStyle(ChatFormatting.YELLOW));
                }
            }
        };
    }
}
