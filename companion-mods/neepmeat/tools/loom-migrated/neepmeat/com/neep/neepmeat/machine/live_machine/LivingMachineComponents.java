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

package com.neep.neepmeat.machine.live_machine;

import com.neep.meatlib.item.TooltipSupplier;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.machine.charnel_pump.CharnelPumpBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.*;
import com.neep.neepmeat.machine.live_machine.component.*;
import com.neep.neepmeat.machine.phage_ray.PhageRayBlockEntity;
import com.neep.neepmeat.machine.reactor.block.entity.DisruptorPortBlockEntity;
import com.neep.neepmeat.machine.reactor.block.entity.DisruptorSegmentBlockEntity;
import com.neep.neepmeat.machine.reactor.disruptor.DisruptorNeedle;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

public class LivingMachineComponents
{
    public static final ComponentType<CrusherSegmentBlockEntity> CRUSHER_SEGMENT = register("crusher_segment", new ComponentType.Simple<>());
    public static final ComponentType<ReprocessorBlockEntity> REPROCESSOR = register("reprocessor", new ComponentType.Simple<>());
    public static final ComponentType<LargeTrommelBlockEntity> LARGE_TROMMEL = register("large_trommel", new ComponentType.Simple<>());
    public static final ComponentType<MotorPortBlockEntity> MOTOR_PORT = register("motor_port", new ComponentType.Simple<>());
    public static final ComponentType<IntegrationPortBlockEntity> INTEGRATION_PORT = register("integration_port", new ComponentType.Simple<>());
    public static final ComponentType<IntegrationPortBlockEntity> SERVICE_PORT = register("service_port", new ComponentType.Simple<>());
    public static final ComponentType<PhageRayBlockEntity> PHAGE_RAY = register("phage_ray", new ComponentType.Simple<>());
    public static final ComponentType<TreeVacuumBlockEntity> TREE_VACUUM = register("tree_vacuum", new ComponentType.Simple<>());
    public static final ComponentType<CharnelPumpBlockEntity> CHARNEL_PUMP = register("charnel_pump", new ComponentType.Simple<>());
    public static final ComponentType<LargeCompressorBlockEntity> LARGE_COMPRESSOR = register("large_compressor", new ComponentType.Simple<>());
    public static final ComponentType<StomachBlockEntity> STOMACH = register("stomach", new ComponentType.Simple<>());
    public static final ComponentType<MetaboliserSegmentComponent> METABOLISER_SEGMENT = register("metaboliser_segment", new ComponentType.Simple<>());
    public static final ComponentType<BallMillBlockEntity> BALL_MILL = register("ball_mill", new ComponentType.Simple<>());

    public static final ComponentType<HopperComponent> HOPPER = register("hopper", new ComponentType.Simple<>());
    public static final ComponentType<ItemInputComponent> ITEM_INPUT = register("item_input", new ComponentType.Simple<>());
    public static final ComponentType<ItemOutputComponent> ITEM_OUTPUT = register("item_output", new ComponentType.Simple<>());
    public static final ComponentType<FluidInputComponent> FLUID_INPUT = register("fluid_input", new ComponentType.Simple<>());
    public static final ComponentType<FluidOutputComponent> FLUID_OUTPUT = register("fluid_output", new ComponentType.Simple<>());

    public static final ComponentType<ExtractorBlockEntity> EXTRACTOR = register("extractor", new ComponentType.Simple<>());
    public static final ComponentType<SilkyExtractorBlockEntity> SILKY_EXTRACTOR = register("silky_extractor", new ComponentType.Simple<>());
    public static final ComponentType<DistributorPointBlockEntity> DISTRIBUTOR_POINT = register("distributor_point", new ComponentType.Simple<>());

    public static final ComponentType<LuckyOneBlockEntity> LUCKY_ONE = register("lucky_one", new ComponentType.Simple<>());

    public static final ComponentType<DisruptorSegmentBlockEntity> DISRUPTOR_SEGMENT = register("disruptor_segment", new ComponentType.Simple<>());
    public static final ComponentType<DisruptorPortBlockEntity> DISRUPTOR_PORT = register("disruptor_port", new ComponentType.Simple<>());
    public static final ComponentType<DisruptorNeedle> DISRUPTOR_NEEDLE = register("disruptor_needle", new ComponentType.Simple<>());

    public static <C extends LivingMachineComponent, T extends ComponentType<C>> T register(String path, T type)
    {
        return register(new ResourceLocation(NeepMeat.NAMESPACE, path), type);
    }

    public static <C extends LivingMachineComponent, T extends ComponentType<C>> T register(ResourceLocation id, T type)
    {
        ComponentType.ID_TO_TYPE.add(type.getBitIdx(), type);
        return Registry.register(ComponentType.REGISTRY, id, type);
    }

    public static void init()
    {
        LivingMachineComponent.LOOKUP.registerForBlocks((world, pos, state, blockEntity, context) ->
        {
            if (blockEntity instanceof HopperBlockEntity hopper)
                return new HopperComponent(hopper);
            else
                return null;
        }, Blocks.HOPPER);
    }

    public static TooltipSupplier tooltip(ComponentType<?> type)
    {
        return (stack, list) ->
        {
//            if (!Screen.hasShiftDown())
//            {
//                list.add(NeepMeat.translationKey("screen", "living_machine.component_hold_shift").formatted(Formatting.RED));
//            }
//            else
//            {
                list.add(NeepMeat.translationKey("screen", "living_machine.component", Component.translatable(nameTranslationKey(type)).getString()).setStyle(PLCCols.EMPHASIS_TEXT));
//            }
        };
    }

    public static String nameTranslationKey(ComponentType<?> type)
    {
        ResourceLocation id = ComponentType.REGISTRY.getKey(type);
        if (id == null)
            return "";

        return id.toLanguageKey("component");
    }
}
