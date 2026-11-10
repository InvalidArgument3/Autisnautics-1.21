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

package com.neep.neepmeat.client;

import com.neep.meatlib.MeatLib;
import com.neep.neepbus.NeepBus;
import com.neep.neepbus.client.instance.LinearLeverVisual;
import com.neep.neepbus.client.instance.VerticalGaugeVisual;
import com.neep.neepmeat.client.instance.*;
import com.neep.neepmeat.client.renderer.LinearOscillatorVisual;
import com.neep.neepmeat.client.renderer.block.DummyBlockEntityRenderer;
import com.neep.neepmeat.client.renderer.block.LargeTrommelRenderer;
import com.neep.neepmeat.client.renderer.block.LargestHopperRenderer;
import com.neep.neepmeat.client.renderer.entity.DummyEntityRenderer;
import com.neep.neepmeat.client.renderer.entity.LimbEntityVisual;
import com.neep.neepmeat.entity.follower.FollowerVisual;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMEntities;
import com.neep.neepmeat.machine.advanced_motor.AdvancedMotorVisual;
import com.neep.neepmeat.machine.charnel_pump.CharnelPumpVisual;
import com.neep.neepmeat.machine.fabricator.FabricatorVisual;
import com.neep.neepmeat.machine.grinder.CrusherVisual;
import com.neep.neepmeat.machine.grinder.CrusherRenderer;
import com.neep.neepmeat.machine.hydraulic_press.HydraulicPressVisual;
import com.neep.neepmeat.machine.large_crusher.CrusherSegmentVisual;
import com.neep.neepmeat.machine.large_motor.LargeMotorVisual;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.motor.MotorVisual;
import com.neep.neepmeat.machine.phage_ray.PhageRayVisual;
import com.neep.neepmeat.machine.pylon.PylonVisual;
import com.neep.neepmeat.machine.reactor.IntrusionReactor;
import com.neep.neepmeat.machine.stirling_engine.StirlingEngineVisual;
import com.neep.neepmeat.machine.surgical_controller.BuilderVisual;
import com.neep.neepmeat.machine.surgical_controller.PLCVisual;
import com.neep.neepmeat.plc.PLCBlocks;
import com.neep.neepmeat.transport.ItemTransport;
import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import dev.engine_room.flywheel.lib.visualization.SimpleEntityVisualizer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class NMVisuals
{
    public static void init()
    {
        // Flywheel
        boolean skipVanillaRender = !MeatLib.CONFIG.dontSkipVanillaRender.getRealValue();

        VisualizerRegistry.setVisualizer(NMBlockEntities.CRUSHER, new SimpleBlockEntityVisualizer<>(CrusherVisual::new, t -> false));
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(NMBlockEntities.CRUSHER, CrusherRenderer::new);

        VisualizerRegistry.setVisualizer(LivingMachines.CRUSHER_SEGMENT_BE, new SimpleBlockEntityVisualizer<>(CrusherSegmentVisual::new, t -> false));

        VisualizerRegistry.setVisualizer(LivingMachines.LARGE_TROMMEL_BE, new SimpleBlockEntityVisualizer<>(LargeTrommelVisual::new, t -> false));
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(LivingMachines.LARGE_TROMMEL_BE, LargeTrommelRenderer::new);

        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(LivingMachines.LARGEST_HOPPER_BE, LargestHopperRenderer::new);

        VisualizerRegistry.setVisualizer(LivingMachines.LUCKY_ONE_BE, new SimpleBlockEntityVisualizer<>(LuckyOneVisual::new, t -> skipVanillaRender));
        VisualizerRegistry.setVisualizer(IntrusionReactor.DISRUPTOR_SEGMENT_BE, new SimpleBlockEntityVisualizer<>(DisruptorSegmentVisual::new, t -> skipVanillaRender));
        VisualizerRegistry.setVisualizer(NMBlockEntities.MOTOR, new SimpleBlockEntityVisualizer<>(MotorVisual::new, t -> skipVanillaRender));
        VisualizerRegistry.setVisualizer(NMBlockEntities.ADVANCED_MOTOR, new SimpleBlockEntityVisualizer<>(AdvancedMotorVisual::new, t -> skipVanillaRender));
        VisualizerRegistry.setVisualizer(NMBlockEntities.LARGE_MOTOR, new SimpleBlockEntityVisualizer<>(LargeMotorVisual::new, t -> skipVanillaRender));
        VisualizerRegistry.setVisualizer(NMBlockEntities.STIRLING_ENGINE, new SimpleBlockEntityVisualizer<>(StirlingEngineVisual::new, t -> skipVanillaRender));
//        VisualizerRegistry.setVisualizer(NMBlockEntities.FLYWHEEL, new SimpleBlockEntityVisualizer<>(FlywheelVisual::new, t -> skipVanillaRender));
        VisualizerRegistry.setVisualizer(NMBlockEntities.HYDRAULIC_PRESS, new SimpleBlockEntityVisualizer<>(HydraulicPressVisual::new, t -> skipVanillaRender));
        VisualizerRegistry.setVisualizer(LivingMachines.CHARNEL_PUMP_BE, new SimpleBlockEntityVisualizer<>(CharnelPumpVisual::new, t -> false));
        VisualizerRegistry.setVisualizer(LivingMachines.BALL_MILL_BE, new SimpleBlockEntityVisualizer<>(BallMillVisual::new, t -> skipVanillaRender));

        VisualizerRegistry.setVisualizer(NMBlockEntities.FABRICATOR, new SimpleBlockEntityVisualizer<>(FabricatorVisual::new, t -> skipVanillaRender));

        VisualizerRegistry.setVisualizer(PLCBlocks.ROBOTIC_ARM_ENTITY, new SimpleBlockEntityVisualizer<>(RoboticArmVisual::new, t -> skipVanillaRender));
        VisualizerRegistry.setVisualizer(PLCBlocks.IMPLANTER_BE, new SimpleBlockEntityVisualizer<>(ImplanterVisual::new, t -> skipVanillaRender));

        VisualizerRegistry.setVisualizer(NMBlockEntities.PYLON, new SimpleBlockEntityVisualizer<>(PylonVisual::new, t -> skipVanillaRender));
        VisualizerRegistry.setVisualizer(PLCBlocks.PLC_ENTITY, new SimpleBlockEntityVisualizer<>(PLCVisual::new, t -> skipVanillaRender));
        VisualizerRegistry.setVisualizer(PLCBlocks.BUILDER_BE, new SimpleBlockEntityVisualizer<>(BuilderVisual::new, t -> skipVanillaRender));
        VisualizerRegistry.setVisualizer(NMBlockEntities.LINEAR_OSCILLATOR, new SimpleBlockEntityVisualizer<>(LinearOscillatorVisual::new, t -> skipVanillaRender));
        VisualizerRegistry.setVisualizer(NMBlockEntities.EJECTOR, new SimpleBlockEntityVisualizer<>(EjectorVisual::new, t -> skipVanillaRender));
        VisualizerRegistry.setVisualizer(NMBlockEntities.ITEM_PUMP, new SimpleBlockEntityVisualizer<>(EjectorVisual::new, t -> skipVanillaRender));
        VisualizerRegistry.setVisualizer(ItemTransport.FILTERED_EJECTOR_BE, new SimpleBlockEntityVisualizer<>(EjectorVisual::new, t -> skipVanillaRender));

        VisualizerRegistry.setVisualizer(NMBlockEntities.LARGE_FAN, new SimpleBlockEntityVisualizer<>(LargeFanVisual::new, t -> skipVanillaRender));

        VisualizerRegistry.setVisualizer(NeepBus.VERTICAL_GAUGE_BE, new SimpleBlockEntityVisualizer<>(VerticalGaugeVisual::new, t -> skipVanillaRender));
        VisualizerRegistry.setVisualizer(NeepBus.LINEAR_LEVER_BE, new SimpleBlockEntityVisualizer<>(LinearLeverVisual::new, t -> skipVanillaRender));

//        VisualizerRegistry.setVisualizer(NMEntities.PHAGE_RAY).skipVanillaRender(t -> skipVanillaRender).factory(PhageRayInstance::new).apply(true);
        VisualizerRegistry.setVisualizer(NMEntities.PHAGE_RAY, new SimpleEntityVisualizer<>(PhageRayVisual::new, t -> skipVanillaRender));
        VisualizerRegistry.setVisualizer(LivingMachines.TREE_VACUUM_BE, new SimpleBlockEntityVisualizer<>(TreeVacuumVisual::new, t -> skipVanillaRender));

        VisualizerRegistry.setVisualizer(NMEntities.FOLLOWER, new SimpleEntityVisualizer<>(FollowerVisual::new, t -> false));

        VisualizerRegistry.setVisualizer(NMEntities.LIMB, new SimpleEntityVisualizer<>(LimbEntityVisual::new, t -> false));

        VisualizerRegistry.setVisualizer(NMEntities.MACHINE_WORM, new SimpleEntityVisualizer<>(EscapedMachineVisual::new, t -> false));
        EntityRendererRegistry.register(NMEntities.MACHINE_WORM, DummyEntityRenderer::new);

        VisualizerRegistry.setVisualizer(NMEntities.SKYWORM, new SimpleEntityVisualizer<>(SkywormVisual::new, t -> false));
        EntityRendererRegistry.register(NMEntities.SKYWORM, DummyEntityRenderer::new);

        VisualizerRegistry.setVisualizer(NMEntities.FARMING_SCUTTER, new SimpleEntityVisualizer<>(FarmingScutterVisual::new, t -> false));
        EntityRendererRegistry.register(NMEntities.FARMING_SCUTTER, DummyEntityRenderer::new);
        VisualizerRegistry.setVisualizer(NMEntities.COMPRESSOR_SCUTTER, new SimpleEntityVisualizer<>(CompressorScutterVisual::new, t -> false));
        EntityRendererRegistry.register(NMEntities.COMPRESSOR_SCUTTER, DummyEntityRenderer::new);

        EntityRendererRegistry.register(NMEntities.PRESSURE_WASH, DummyEntityRenderer::new);

        if (MeatLib.CONFIG.registerDummyBERs.getRealValue())
        {
            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(LivingMachines.CRUSHER_SEGMENT_BE, DummyBlockEntityRenderer::new);

            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(LivingMachines.LUCKY_ONE_BE, DummyBlockEntityRenderer::new);
            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(IntrusionReactor.DISRUPTOR_SEGMENT_BE, DummyBlockEntityRenderer::new);
            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(NMBlockEntities.MOTOR, DummyBlockEntityRenderer::new);
            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(NMBlockEntities.ADVANCED_MOTOR, DummyBlockEntityRenderer::new);
            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(NMBlockEntities.LARGE_MOTOR, DummyBlockEntityRenderer::new);
            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(NMBlockEntities.STIRLING_ENGINE, DummyBlockEntityRenderer::new);
            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(NMBlockEntities.HYDRAULIC_PRESS, DummyBlockEntityRenderer::new);
            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(LivingMachines.CHARNEL_PUMP_BE, DummyBlockEntityRenderer::new);
            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(LivingMachines.BALL_MILL_BE, DummyBlockEntityRenderer::new);

            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(NMBlockEntities.FABRICATOR, DummyBlockEntityRenderer::new);

            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(PLCBlocks.ROBOTIC_ARM_ENTITY, DummyBlockEntityRenderer::new);
            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(PLCBlocks.IMPLANTER_BE, DummyBlockEntityRenderer::new);

            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(NMBlockEntities.PYLON, DummyBlockEntityRenderer::new);
            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(PLCBlocks.PLC_ENTITY, DummyBlockEntityRenderer::new);
            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(PLCBlocks.BUILDER_BE, DummyBlockEntityRenderer::new);
            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(NMBlockEntities.LINEAR_OSCILLATOR, DummyBlockEntityRenderer::new);
            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(NMBlockEntities.EJECTOR, DummyBlockEntityRenderer::new);
            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(NMBlockEntities.ITEM_PUMP, DummyBlockEntityRenderer::new);
            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(ItemTransport.FILTERED_EJECTOR_BE, DummyBlockEntityRenderer::new);

            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(NMBlockEntities.LARGE_FAN, DummyBlockEntityRenderer::new);

            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(NeepBus.VERTICAL_GAUGE_BE, DummyBlockEntityRenderer::new);
            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(NeepBus.LINEAR_LEVER_BE, DummyBlockEntityRenderer::new);

            net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(LivingMachines.TREE_VACUUM_BE, DummyBlockEntityRenderer::new);
        }
    }
}
