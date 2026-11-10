/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine.live_machine;

import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.LivingMachineBlock;
import com.neep.neepmeat.building.BuildingBlocks;
import com.neep.neepmeat.machine.live_machine.block.BallMillBlock;
import com.neep.neepmeat.machine.live_machine.block.CrusherSegmentBlock;
import com.neep.neepmeat.machine.live_machine.block.MetaboliserSegmentBlock;
import com.neep.neepmeat.machine.live_machine.block.StomachBlock;
import com.neep.neepmeat.machine.live_machine.block.ExtractorBlock;
import com.neep.neepmeat.machine.live_machine.block.LargeTrommelBlock;
import com.neep.neepmeat.machine.live_machine.block.LuckyOneBlock;
import com.neep.neepmeat.machine.live_machine.block.FluidPortBlock;
import com.neep.neepmeat.machine.live_machine.block.ItemInputPortBlock;
import com.neep.neepmeat.machine.live_machine.block.ItemOutputPortBlock;
import com.neep.neepmeat.machine.live_machine.block.PortBlock;
import com.neep.neepmeat.machine.live_machine.block.ServicePortBlock;
import com.neep.neepmeat.machine.live_machine.block.SilkyExtractorBlock;
import com.neep.neepmeat.machine.live_machine.block.entity.BallMillBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.LivingMachineControllerBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.MotorPortBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.ExtractorBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.LargeTrommelBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.FluidInputPortBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.FluidOutputPortBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.ItemInputPortBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.ItemOutputPortBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.ReprocessorBlock;
import com.neep.neepmeat.machine.live_machine.block.entity.ReprocessorBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.ServicePortBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.entity.SilkyExtractorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LivingMachines {
    public static final java.util.List<net.minecraft.world.item.Item> ITEM_PORTS = new java.util.ArrayList<>();
    private static final RegistrationContext CONTEXT = new RegistrationContext(NeepMeat.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NeepMeat.ID);

    public static final DeferredBlock<LivingMachineBlock> CONTROLLER = CONTEXT.block("living_machine_controller", LivingMachineBlock::new);
    public static final DeferredItem<BlockItem> CONTROLLER_ITEM = CONTEXT.item("living_machine_controller",
            () -> new BlockItem(CONTROLLER.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LivingMachineControllerBlockEntity>> CONTROLLER_BE =
            BLOCK_ENTITIES.register("living_machine_controller", () -> BlockEntityType.Builder
                    .of(LivingMachineControllerBlockEntity::new, CONTROLLER.get()).build(null));

    public static final DeferredBlock<MachineBlock> MACHINE_BLOCK = CONTEXT.block("machine_block", () -> new MachineBlock(300f, 100f));
    public static final DeferredItem<BlockItem> MACHINE_BLOCK_ITEM = CONTEXT.item("machine_block",
            () -> new BlockItem(MACHINE_BLOCK.get(), new Item.Properties()));

    public static final DeferredBlock<MotorPortBlock> MOTOR_PORT = CONTEXT.block("motor_port", MotorPortBlock::new);
    public static final DeferredItem<BlockItem> MOTOR_PORT_ITEM = CONTEXT.item("motor_port",
            () -> new BlockItem(MOTOR_PORT.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MotorPortBlockEntity>> MOTOR_PORT_BE =
            BLOCK_ENTITIES.register("motor_port", () -> BlockEntityType.Builder
                    .of(MotorPortBlockEntity::new, MOTOR_PORT.get()).build(null));

    public static final DeferredBlock<StomachBlock> STOMACH = CONTEXT.block("stomach", StomachBlock::new);
    public static final DeferredBlock<com.neep.neepmeat.api.big_block_2.BigBlockStructureBlock> STOMACH_STRUCTURE =
            CONTEXT.block("stomach_structure", () -> new com.neep.neepmeat.api.big_block_2.BigBlockStructureBlock(STOMACH, false));
    public static final DeferredItem<BlockItem> STOMACH_ITEM = CONTEXT.item("stomach",
            () -> new BlockItem(STOMACH.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.neep.neepmeat.machine.live_machine.block.entity.StomachBlockEntity>> STOMACH_BE =
            BLOCK_ENTITIES.register("stomach", () -> BlockEntityType.Builder
                    .of(com.neep.neepmeat.machine.live_machine.block.entity.StomachBlockEntity::new, STOMACH.get()).build(null));

    public static final DeferredBlock<MetaboliserSegmentBlock> METABOLISER = CONTEXT.block("metaboliser_segment", MetaboliserSegmentBlock::new);
    public static final DeferredBlock<com.neep.neepmeat.api.big_block_2.BigBlockStructureBlock> METABOLISER_STRUCTURE =
            CONTEXT.block("metaboliser_segment_structure", () -> new com.neep.neepmeat.api.big_block_2.BigBlockStructureBlock(METABOLISER, true));
    public static final DeferredItem<BlockItem> METABOLISER_ITEM = CONTEXT.item("metaboliser_segment",
            () -> new BlockItem(METABOLISER.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.neep.neepmeat.machine.live_machine.block.entity.MetaboliserSegmentBlockEntity>> METABOLISER_BE =
            BLOCK_ENTITIES.register("metaboliser_segment", () -> BlockEntityType.Builder
                    .of(com.neep.neepmeat.machine.live_machine.block.entity.MetaboliserSegmentBlockEntity::new, METABOLISER.get()).build(null));

    public static final DeferredBlock<LuckyOneBlock> LUCKY_ONE = CONTEXT.block("lucky_one", () -> new LuckyOneBlock(CONTEXT, ItemSettings.block(), portSettings()));
    public static final DeferredBlock<GroundInfuserBlock> GROUND_INFUSER = CONTEXT.block("ground_infuser", () -> new GroundInfuserBlock(CONTEXT, ItemSettings.block(), portSettings()));
    public static final DeferredItem<BlockItem> LUCKY_ONE_ITEM = CONTEXT.item("lucky_one",
            () -> new BlockItem(LUCKY_ONE.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.neep.neepmeat.machine.live_machine.block.entity.LuckyOneBlockEntity>> LUCKY_ONE_BE =
            BLOCK_ENTITIES.register("lucky_one", () -> BlockEntityType.Builder
                    .of(com.neep.neepmeat.machine.live_machine.block.entity.LuckyOneBlockEntity::new, LUCKY_ONE.get()).build(null));

    public static final DeferredBlock<ExtractorBlock> EXTRACTOR = CONTEXT.block("extractor", () -> new ExtractorBlock(CONTEXT, ItemSettings.block(), portSettings()));
    public static final DeferredBlock<SilkyExtractorBlock> SILKY_EXTRACTOR = CONTEXT.block("silky_extractor", () -> new SilkyExtractorBlock(CONTEXT, ItemSettings.block(), portSettings()));
    public static final DeferredBlock<ServicePortBlock<ServicePortBlockEntity>> SERVICE_PORT = CONTEXT.block("service_port",
            () -> new ServicePortBlock<>(CONTEXT, ItemSettings.block(), ServicePortBlockEntity::new, portSettings()));
    public static final DeferredBlock<ItemInputPortBlock> ITEM_INPUT = CONTEXT.block("item_input_port",
            () -> new ItemInputPortBlock(CONTEXT, ItemSettings.block(), ItemInputPortBlockEntity::new, portSettings()));
    public static final DeferredBlock<ItemOutputPortBlock> ITEM_OUTPUT = CONTEXT.block("item_output_port",
            () -> new ItemOutputPortBlock(CONTEXT, ItemSettings.block(), ItemOutputPortBlockEntity::new, portSettings()));
    public static final DeferredBlock<ReprocessorBlock> REPROCESSOR = CONTEXT.block("reprocessor", () -> new ReprocessorBlock(CONTEXT, portSettings()));
    public static final DeferredBlock<FluidPortBlock<FluidInputPortBlockEntity>> FLUID_INPUT = CONTEXT.block("fluid_input_port",
            () -> new FluidPortBlock<>(CONTEXT, ItemSettings.block(), FluidInputPortBlockEntity::new, portSettings()));
    public static final DeferredBlock<FluidPortBlock<FluidOutputPortBlockEntity>> FLUID_OUTPUT = CONTEXT.block("fluid_output_port",
            () -> new FluidPortBlock<>(CONTEXT, ItemSettings.block(), FluidOutputPortBlockEntity::new, portSettings()));
    public static final DeferredBlock<CrusherSegmentBlock> CRUSHER = CONTEXT.block("crusher_segment", () -> new CrusherSegmentBlock(CONTEXT, portSettings()));
    public static final DeferredBlock<LargeCompressorBlock> COMPRESSOR = CONTEXT.block("large_compressor", () -> new LargeCompressorBlock(CONTEXT, portSettings()));
    public static final DeferredBlock<com.neep.neepmeat.api.big_block_2.BigBlockStructureBlock> BALL_MILL_STRUCTURE =
            CONTEXT.block("ball_mill_structure", () -> new com.neep.neepmeat.api.big_block_2.BigBlockStructureBlock(ballMillBlock(), false));
    public static final DeferredBlock<LargeTrommelBlock> TROMMEL = CONTEXT.block("large_trommel", () -> new LargeTrommelBlock(CONTEXT, portSettings()));
    public static final DeferredBlock<BallMillBlock> BALL_MILL = CONTEXT.block("ball_mill", BallMillBlock::new);
    public static final DeferredBlock<com.neep.neepmeat.machine.live_machine.block.IntegrationPortBlock> INTEGRATION =
            CONTEXT.block("integration_port", com.neep.neepmeat.machine.live_machine.block.IntegrationPortBlock::new);
    public static final DeferredBlock<com.neep.neepmeat.machine.live_machine.block.LargestHopperBlock> HOPPER =
            CONTEXT.block("largest_hopper", () -> new com.neep.neepmeat.machine.live_machine.block.LargestHopperBlock(CONTEXT, portSettings()));
    public static final DeferredBlock<com.neep.neepmeat.machine.live_machine.block.TreeVacuumBlock> TREE_VACUUM =
            CONTEXT.block("tree_vacuum", com.neep.neepmeat.machine.live_machine.block.TreeVacuumBlock::new);
    public static final DeferredBlock<com.neep.neepmeat.machine.live_machine.block.DistributorPointBlock> DISTRIBUTOR =
            CONTEXT.block("distributor_point", () -> new com.neep.neepmeat.machine.live_machine.block.DistributorPointBlock(CONTEXT, portSettings()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtractorBlockEntity>> EXTRACTOR_BE =
            BLOCK_ENTITIES.register("extractor", () -> BlockEntityType.Builder.of(ExtractorBlockEntity::new, EXTRACTOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SilkyExtractorBlockEntity>> SILKY_EXTRACTOR_BE =
            BLOCK_ENTITIES.register("silky_extractor", () -> BlockEntityType.Builder.of(SilkyExtractorBlockEntity::new, SILKY_EXTRACTOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ServicePortBlockEntity>> SERVICE_PORT_BE =
            BLOCK_ENTITIES.register("service_port", () -> BlockEntityType.Builder.of(ServicePortBlockEntity::new, SERVICE_PORT.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemInputPortBlockEntity>> ITEM_INPUT_BE =
            BLOCK_ENTITIES.register("item_input_port", () -> BlockEntityType.Builder.of(ItemInputPortBlockEntity::new, ITEM_INPUT.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReprocessorBlockEntity>> REPROCESSOR_BE =
            BLOCK_ENTITIES.register("reprocessor", () -> BlockEntityType.Builder.of(ReprocessorBlockEntity::new, REPROCESSOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidInputPortBlockEntity>> FLUID_INPUT_BE =
            BLOCK_ENTITIES.register("fluid_input_port", () -> BlockEntityType.Builder.of(FluidInputPortBlockEntity::new, FLUID_INPUT.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemOutputPortBlockEntity>> ITEM_OUTPUT_BE =
            BLOCK_ENTITIES.register("item_output_port", () -> BlockEntityType.Builder.of(ItemOutputPortBlockEntity::new, ITEM_OUTPUT.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LargeTrommelBlockEntity>> TROMMEL_BE =
            BLOCK_ENTITIES.register("large_trommel", () -> BlockEntityType.Builder.of(LargeTrommelBlockEntity::new, TROMMEL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.neep.neepmeat.machine.live_machine.block.entity.IntegrationPortBlockEntity>> INTEGRATION_BE =
            BLOCK_ENTITIES.register("integration_port", () -> BlockEntityType.Builder.of(com.neep.neepmeat.machine.live_machine.block.entity.IntegrationPortBlockEntity::new, INTEGRATION.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.neep.neepmeat.machine.live_machine.block.entity.LargestHopperBlockEntity>> HOPPER_BE =
            BLOCK_ENTITIES.register("largest_hopper", () -> BlockEntityType.Builder.of(com.neep.neepmeat.machine.live_machine.block.entity.LargestHopperBlockEntity::new, HOPPER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.neep.neepmeat.machine.live_machine.block.entity.DistributorPointBlockEntity>> DISTRIBUTOR_BE =
            BLOCK_ENTITIES.register("distributor_point", () -> BlockEntityType.Builder.of(com.neep.neepmeat.machine.live_machine.block.entity.DistributorPointBlockEntity::new, DISTRIBUTOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.neep.neepmeat.machine.live_machine.block.entity.TreeVacuumBlockEntity>> TREE_VACUUM_BE =
            BLOCK_ENTITIES.register("tree_vacuum", () -> BlockEntityType.Builder.of(com.neep.neepmeat.machine.live_machine.block.entity.TreeVacuumBlockEntity::new, TREE_VACUUM.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BallMillBlockEntity>> BALL_MILL_BE =
            BLOCK_ENTITIES.register("ball_mill", () -> BlockEntityType.Builder.of(BallMillBlockEntity::new, BALL_MILL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidOutputPortBlockEntity>> FLUID_OUTPUT_BE =
            BLOCK_ENTITIES.register("fluid_output_port", () -> BlockEntityType.Builder.of(FluidOutputPortBlockEntity::new, FLUID_OUTPUT.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.neep.neepmeat.machine.live_machine.block.entity.CrusherSegmentBlockEntity>> CRUSHER_BE =
            BLOCK_ENTITIES.register("crusher_segment", () -> BlockEntityType.Builder.of(com.neep.neepmeat.machine.live_machine.block.entity.CrusherSegmentBlockEntity::new, CRUSHER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.neep.neepmeat.machine.live_machine.block.entity.LargeCompressorBlockEntity>> COMPRESSOR_BE =
            BLOCK_ENTITIES.register("large_compressor", () -> BlockEntityType.Builder.of(com.neep.neepmeat.machine.live_machine.block.entity.LargeCompressorBlockEntity::new, COMPRESSOR.get()).build(null));

    static {
        LivingMachineComponents.MOTOR_PORT.getBitIdx();
    }

    private static DeferredBlock<BallMillBlock> ballMillBlock() {
        return BALL_MILL;
    }

    private static net.minecraft.world.level.block.state.BlockBehaviour.Properties portSettings() {
        return MeatlibBlockSettings.create().strength(2.0F).properties();
    }

    private LivingMachines() {}

    public static void register(IEventBus bus) {
        CONTEXT.register(bus);
        BLOCK_ENTITIES.register(bus);
        bus.addListener(LivingMachines::creative);
    }

    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == BuildingBlocks.TAB.getKey()) {
            event.accept(CONTROLLER_ITEM.get());
            event.accept(MACHINE_BLOCK_ITEM.get());
            event.accept(MOTOR_PORT_ITEM.get());
            event.accept(STOMACH_ITEM.get());
            event.accept(METABOLISER_ITEM.get());
            event.accept(LUCKY_ONE_ITEM.get());
        }
    }
}
