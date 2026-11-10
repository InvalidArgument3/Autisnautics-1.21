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

package com.neep.neepmeat.block;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.LivingMachineBlockEntity;
import com.neep.neepmeat.api.live_machine.LivingMachineStructure;
import com.neep.neepmeat.api.live_machine.PropertyValue;
import com.neep.neepmeat.api.live_machine.StructureProperty;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MachineBlock extends BaseBlock implements LivingMachineStructure
{
    private final EnumMap<StructureProperty, PropertyValue> properties;
    @Nullable private final Supplier<OpenMachineBlock> openVariant;

    public MachineBlock(RegistrationContext ctx, ItemSettings itemSettings, Map<StructureProperty, PropertyValue> properties, Properties settings, @Nullable Supplier<OpenMachineBlock> openVariant)
    {
        super(ctx, itemSettings.factory(MachineBlockItem::new), settings);
        this.properties = new EnumMap<>(properties);
//        properties.put(Property.MAX_POWER, 10f);
        this.openVariant = openVariant;
    }

    public MachineBlock(RegistrationContext ctx, ItemSettings itemSettings, Map<StructureProperty, PropertyValue> properties, Properties settings)
    {
        this(ctx, itemSettings, properties, settings, null);
    }

    @Override
    public boolean canOpen()
    {
        return openVariant != null;
    }

    @Override
    public boolean makeOpen(Level world, BlockPos pos, BlockState state, @Nullable LivingMachineBlockEntity be)
    {
        if (openVariant != null)
        {
            var openBlock = openVariant.get();
            LivingMachineStructure.super.makeOpen(world, pos, state, be);

            BlockPos.MutableBlockPos mutable = pos.mutable();
            for (Direction face : Direction.Plane.HORIZONTAL)
            {
                mutable.setWithOffset(pos, face);
                if (world.getBlockState(mutable).isAir())
                {
                    BlockState newState = openBlock.defaultBlockState().setValue(OpenMachineBlock.FACING, face);
                    world.setBlockAndUpdate(pos, newState);
                    openBlock.onCreate(world, pos, newState);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public EnumMap<StructureProperty, PropertyValue> getProperties()
    {
        return properties;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag options)
    {
        super.appendHoverText(stack, world, tooltip, options);

        if (!Screen.hasShiftDown())
        {
            tooltip.add(NeepMeat.translationKey("screen", "living_machine.block_hold_shift").withStyle(ChatFormatting.RED));
        }
        else
        {
            tooltip.add(NeepMeat.translationKey("screen", "living_machine.block").withStyle(ChatFormatting.RED));
            for (var entry : properties.entrySet())
            {
                MutableComponent base = Component.literal("   ")
                        .append(NeepMeat.translationKey("screen", "living_machine." + entry.getKey().name().toLowerCase(Locale.ROOT))
                                .withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(": ").withStyle(ChatFormatting.GOLD))
                        .append(entry.getKey().format(entry.getValue()))
                        .append(Component.literal(" (" + entry.getValue().function().name + ")"))
                        ;
                tooltip.add(base);
            }
        }
    }

    public static class MachineBlockItem extends BaseBlockItem
    {
        public MachineBlockItem(Block block, RegistrationContext ctx, ItemSettings itemSettings)
        {
            super(block, ctx, itemSettings);
        }
    }
}
