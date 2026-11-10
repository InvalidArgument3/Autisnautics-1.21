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

import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.block.multi.TallerBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.registry.annotation.RegisterMe;
import com.neep.neepmeat.NMItemGroups;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.FluidPump;
import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block_2.BigBlock2Structure;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.api.live_machine.PropertyValue;
import com.neep.neepmeat.api.live_machine.StructureProperty;
import com.neep.neepmeat.block.MachineBlock;
import com.neep.neepmeat.block.OpenMachineBlock;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.machine.charnel_pump.CharnelPumpBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.*;
import com.neep.neepmeat.machine.live_machine.block.entity.*;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import java.util.List;
import java.util.Map;

import static com.neep.neepmeat.init.NMBlockEntities.register;
import static com.neep.neepmeat.init.NMBlocks.*;
import static com.neep.neepmeat.machine.live_machine.LivingMachineComponents.tooltip;

@RegisterMe(NeepMeat.NAMESPACE)
public class LivingMachines
{
    public static final RegistrationContext C = new RegistrationContext(NeepMeat.NAMESPACE, () -> NMItemGroups.GENERAL);

    public static final List<Item> ITEM_PORTS = new ObjectArrayList<>();

    public static Block LIVING_MACHINE_CONTROLLER = C.withItem(new TestLivingMachineBlock(MeatlibBlockSettings.copyOf(MACHINE_SETTINGS)), ItemSettings.block());

    public static MachineBlock MACHINE_BLOCK = new MachineBlock(C, block(), Map.of(
            StructureProperty.MAX_POWER, new PropertyValue(300f),
                    StructureProperty.MASS, new PropertyValue(PropertyValue.Function.ADD, 100)), MeatlibBlockSettings.copyOf(OPAQUE_MACHINE_SETTINGS),
            () -> LivingMachines.OPEN_MACHINE_BLOCK);
    public static Block BASE_MACHINE_BLOCK = new MachineBlock(C, block().tooltip(TooltipSupplier.simple(1)), Map.of(
            StructureProperty.MAX_POWER, new PropertyValue(300f),
            StructureProperty.MASS, new PropertyValue(PropertyValue.Function.ADD, 100)), MeatlibBlockSettings.copyOf(OPAQUE_MACHINE_SETTINGS));
    public static Block BLOOD_BUBBLE_MACHINE_BLOCK = new MachineBlock(C, block(), Map.of(
            StructureProperty.MAX_POWER, new PropertyValue(PropertyValue.Function.ADD, -5),
            StructureProperty.MASS, new PropertyValue(PropertyValue.Function.ADD, 100f),
            StructureProperty.SELF_REPAIR, new PropertyValue(PropertyValue.Function.ADD, 1e-6f)), MeatlibBlockSettings.copyOf(OPAQUE_MACHINE_SETTINGS),
            () -> LivingMachines.OPEN_BLOOD_BUBBLE_MACHINE_BLOCK);
    public static Block MEAT_STEEL_MACHINE_BLOCK = new MachineBlock(C, block(), Map.of(
            StructureProperty.MAX_POWER, new PropertyValue(500f),
            StructureProperty.SELF_REPAIR, new PropertyValue(PropertyValue.Function.ADD, 0.5e-6f),
            StructureProperty.MASS, new PropertyValue(PropertyValue.Function.ADD, 100f)), MeatlibBlockSettings.copyOf(OPAQUE_MACHINE_SETTINGS),
            () -> LivingMachines.OPEN_MEAT_STEEL_MACHINE_BLOCK);
    public static Block MEAT_STEEL_MACHINE_BLOCK_2 = new MachineBlock(C, block().tooltip(TooltipSupplier.simple(1)), Map.of(
            StructureProperty.MAX_POWER, new PropertyValue(500f),
            StructureProperty.SELF_REPAIR, new PropertyValue(PropertyValue.Function.ADD, 0.5e-6f),
            StructureProperty.MASS, new PropertyValue(PropertyValue.Function.ADD, 100f)
    ), MeatlibBlockSettings.copyOf(OPAQUE_MACHINE_SETTINGS),
            () -> LivingMachines.OPEN_MEAT_STEEL_MACHINE_BLOCK_2);
    public static Block SKIN_MACHINE_BLOCK = new MachineBlock(C, block(), Map.of(
            StructureProperty.MAX_POWER, new PropertyValue(500f),
            StructureProperty.SELF_REPAIR, new PropertyValue(PropertyValue.Function.ADD, 1.5e-6f),
            StructureProperty.MASS, new PropertyValue(PropertyValue.Function.ADD, 100f)
    ), MeatlibBlockSettings.copyOf(OPAQUE_MACHINE_SETTINGS),
            () -> LivingMachines.OPEN_SKIN_MACHINE_BLOCK
            );

    public static OpenMachineBlock OPEN_MACHINE_BLOCK = new OpenMachineBlock(C, MACHINE_BLOCK);
//    public static Block OPEN_BASE_MACHINE_BLOCK = new OpenMachineBlock(C, (MachineBlock) BASE_MACHINE_BLOCK);
    public static OpenMachineBlock OPEN_MEAT_STEEL_MACHINE_BLOCK = new OpenMachineBlock(C, (MachineBlock) MEAT_STEEL_MACHINE_BLOCK);
    public static OpenMachineBlock OPEN_MEAT_STEEL_MACHINE_BLOCK_2 = new OpenMachineBlock(C, (MachineBlock) MEAT_STEEL_MACHINE_BLOCK_2);
    public static OpenMachineBlock OPEN_SKIN_MACHINE_BLOCK = new OpenMachineBlock(C, (MachineBlock) SKIN_MACHINE_BLOCK);
    public static OpenMachineBlock OPEN_BLOOD_BUBBLE_MACHINE_BLOCK = new OpenMachineBlock(C, (MachineBlock) BLOOD_BUBBLE_MACHINE_BLOCK);

    public static final Block MOTOR_PORT = new PortBlock<>(C, ItemSettings.block().factory(ItemPortBlockItem.forList(ITEM_PORTS)).tooltip(tooltip(LivingMachineComponents.MOTOR_PORT)),
            () ->  LivingMachines.MOTOR_PORT_BE, MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    public static final Block INTEGRATION_PORT = new IntegrationPortBlock(C, ItemSettings.block().tooltip(tooltip(LivingMachineComponents.INTEGRATION_PORT).append(TooltipSupplier.simple(1))),
            MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    public static final Block SERVICE_PORT = new PortBlock<>(C, ItemSettings.block(),
            () -> LivingMachines.SERVICE_PORT_BE, MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    public static final Block ITEM_INPUT_PORT = new ItemInputPortBlock(C, ItemSettings.block().tooltip(tooltip(LivingMachineComponents.ITEM_INPUT)),
            () -> LivingMachines.ITEM_INPUT_PORT_BE, MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    public static final Block ITEM_OUTPUT_PORT = new ItemOutputPortBlock(C, ItemSettings.block().tooltip(tooltip(LivingMachineComponents.ITEM_OUTPUT)),
            () -> LivingMachines.ITEM_OUTPUT_PORT_BE, MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    public static final Block FLUID_INPUT_PORT = new FluidPortBlock<>(C, ItemSettings.block().tooltip(tooltip(LivingMachineComponents.FLUID_INPUT)),
            () -> LivingMachines.FLUID_INPUT_PORT_BE, MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    public static final Block FLUID_OUTPUT_PORT = new FluidPortBlock<>(C, ItemSettings.block().tooltip(tooltip(LivingMachineComponents.FLUID_OUTPUT)),
            () -> LivingMachines.FLUID_OUTPUT_PORT_BE, MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    public static final BigBlock<CrusherSegmentBlock.CrusherSegmentStructureBlock> CRUSHER_SEGMENT = new CrusherSegmentBlock(C, MeatlibBlockSettings.copyOf(MACHINE_SETTINGS),
            ItemSettings.block().tooltip(tooltip(LivingMachineComponents.CRUSHER_SEGMENT).append(TooltipSupplier.simple(1))));
//    public static final Block REPROCESSOR = new ReprocessorBlock(C,
//            ItemSettings.block().tooltip(LivingMachineComponents.tooltip(LivingMachineComponents.REPROCESSOR).append(TooltipSupplier.simple(1))),
//            MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    public static final BigBlock<?> LARGE_TROMMEL = new LargeTrommelBlock(C,MeatlibBlockSettings.copyOf(MACHINE_SETTINGS), ItemSettings.block().tooltip(tooltip(LivingMachineComponents.LARGE_TROMMEL)));
    public static final BigBlock<LargestHopperBlock.StructureBlock> LARGEST_HOPPER = new LargestHopperBlock(C, MeatlibBlockSettings.copyOf(MACHINE_SETTINGS), ItemSettings.block().tooltip(tooltip(LivingMachineComponents.ITEM_INPUT)));
    public static final BigBlock<TreeVacuumBlock.Structure> TREE_VACUUM = new TreeVacuumBlock(C, ItemSettings.block().tooltip(tooltip(LivingMachineComponents.TREE_VACUUM).append(TooltipSupplier.simple(1))), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    public static final LargeCompressorBlock LARGE_COMPRESSOR = new LargeCompressorBlock(C, ItemSettings.block().tooltip(tooltip(LivingMachineComponents.LARGE_COMPRESSOR).append(TooltipSupplier.simple(1))), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    public static final StomachBlock STOMACH = new StomachBlock(C, ItemSettings.block().tooltip(tooltip(LivingMachineComponents.STOMACH).append(TooltipSupplier.simple(1))), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    public static final MetaboliserSegmentBlock METABOLISER_SEGMENT = new MetaboliserSegmentBlock(C, ItemSettings.block().tooltip(tooltip(LivingMachineComponents.METABOLISER_SEGMENT)), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
//    public static final GroundInfuserBlock GROUND_INFUSER = new GroundInfuserBlock(C, ItemSettings.block().tooltip(tooltip(LivingMachineComponents.METABOLISER_SEGMENT)), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));

    public static final BallMillBlock BALL_MILL = new BallMillBlock(C, ItemSettings.block().tooltip(tooltip(LivingMachineComponents.BALL_MILL).append(TooltipSupplier.simple(1))), MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));

    public static final TallerBlock LUCKY_ONE = new LuckyOneBlock(C,
            ItemSettings.block().tooltip(tooltip(LivingMachineComponents.LUCKY_ONE).append(TooltipSupplier.simple(1))),
            MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));

    public static final Block EXTRACTOR = new ExtractorBlock(C,
            ItemSettings.block().tooltip(LivingMachineComponents.tooltip(LivingMachineComponents.EXTRACTOR).append(TooltipSupplier.simple(1))),
            MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));
    public static final Block SILKY_EXTRACTOR = new SilkyExtractorBlock(C,
            ItemSettings.block().tooltip(LivingMachineComponents.tooltip(LivingMachineComponents.SILKY_EXTRACTOR).append(TooltipSupplier.simple(1))),
            MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));

    public static final DistributorPointBlock DISTRIBUTOR_POINT = new DistributorPointBlock(C,
            ItemSettings.block().tooltip(LivingMachineComponents.tooltip(LivingMachineComponents.DISTRIBUTOR_POINT).append(TooltipSupplier.simple(1))),
            MeatlibBlockSettings.copyOf(MACHINE_SETTINGS));

    public static BlockEntityType<MotorPortBlockEntity> MOTOR_PORT_BE;
    public static BlockEntityType<IntegrationPortBlockEntity> INTEGRATION_PORT_BE;
    public static BlockEntityType<ServicePortBlockEntity> SERVICE_PORT_BE;
    public static BlockEntityType<CrusherSegmentBlockEntity> CRUSHER_SEGMENT_BE;
    public static BlockEntityType<ReprocessorBlockEntity> REPROCESSOR_BE;
    public static BlockEntityType<LargeTrommelBlockEntity> LARGE_TROMMEL_BE;
    public static BlockEntityType<LargestHopperBlockEntity> LARGEST_HOPPER_BE;
    public static BlockEntityType<LargeCompressorBlockEntity> LARGE_COMPRESSOR_BE;
    public static BlockEntityType<StomachBlockEntity> STOMACH_BE;
    public static BlockEntityType<MetaboliserSegmentBlockEntity> METABOLISER_SEGMENT_BE;
    public static BlockEntityType<BallMillBlockEntity> BALL_MILL_BE;

    public static BlockEntityType<LivingMachineControllerBlockEntity> LIVING_MACHINE_CONTROLLER_BE;
    public static BlockEntityType<ItemInputPortBlockEntity> ITEM_INPUT_PORT_BE;
    public static BlockEntityType<ItemOutputPortBlockEntity> ITEM_OUTPUT_PORT_BE;
    public static BlockEntityType<FluidInputPortBlockEntity> FLUID_INPUT_PORT_BE;
    public static BlockEntityType<FluidOutputPortBlockEntity> FLUID_OUTPUT_PORT_BE;

    public static BlockEntityType<LuckyOneBlockEntity> LUCKY_ONE_BE;
    public static BlockEntityType<ExtractorBlockEntity> EXTRACTOR_BE;
    public static BlockEntityType<SilkyExtractorBlockEntity> SILKY_EXTRACTOR_BE;

    public static BlockEntityType<DistributorPointBlockEntity> DISTRIBUTOR_POINT_BE;

    public static BlockEntityType<TreeVacuumBlockEntity> TREE_VACUUM_BE;
    public static BlockEntityType<CharnelPumpBlockEntity> CHARNEL_PUMP_BE;

    public static void init()
    {
        MOTOR_PORT_BE = register("motor_port", (p, s) -> new MotorPortBlockEntity(MOTOR_PORT_BE, p, s), MOTOR_PORT);
        LivingMachineComponent.LOOKUP.registerSelf(LivingMachines.MOTOR_PORT_BE);
        INTEGRATION_PORT_BE = register("integration_port", (p, s) -> new IntegrationPortBlockEntity(INTEGRATION_PORT_BE, p, s), INTEGRATION_PORT);
        LivingMachineComponent.LOOKUP.registerSelf(LivingMachines.INTEGRATION_PORT_BE);
        FluidStorage.SIDED.registerForBlockEntity(IntegrationPortBlockEntity::getFluidStorage, LivingMachines.INTEGRATION_PORT_BE);
        SERVICE_PORT_BE = register("service_port", (p, s) -> new ServicePortBlockEntity(SERVICE_PORT_BE, p, s), SERVICE_PORT);
        LivingMachineComponent.LOOKUP.registerSelf(LivingMachines.SERVICE_PORT_BE);
        CRUSHER_SEGMENT_BE = register("crusher_segment", (p, s) -> new CrusherSegmentBlockEntity(CRUSHER_SEGMENT_BE, p, s), CRUSHER_SEGMENT);
        LivingMachineComponent.LOOKUP.registerSelf(LivingMachines.CRUSHER_SEGMENT_BE);
//        REPROCESSOR_BE = register("reporcessor", (p, s) -> new ReprocessorBlockEntity(REPROCESSOR_BE, p, s), REPROCESSOR);
//        LivingMachineComponent.LOOKUP.registerSelf(LivingMachines.REPROCESSOR_BE);
        LARGE_TROMMEL_BE = register("large_trommel", (p, s) -> new LargeTrommelBlockEntity(LARGE_TROMMEL_BE, p, s), LARGE_TROMMEL);
        LivingMachineComponent.LOOKUP.registerSelf(LivingMachines.LARGE_TROMMEL_BE);
        LARGEST_HOPPER_BE = register("largest_hopper", (p, s) -> new LargestHopperBlockEntity(LARGEST_HOPPER_BE, p, s), LARGEST_HOPPER);
        LivingMachineComponent.LOOKUP.registerSelf(LivingMachines.LARGEST_HOPPER_BE);
        ItemStorage.SIDED.registerForBlockEntity(LargestHopperBlockEntity::getStorage, LARGEST_HOPPER_BE);
        LARGE_COMPRESSOR_BE = register("large_compressor", (p, s) -> new LargeCompressorBlockEntity(LARGE_COMPRESSOR_BE, p, s), LARGE_COMPRESSOR);
        LivingMachineComponent.LOOKUP.registerSelf(LivingMachines.LARGE_COMPRESSOR_BE);
        FluidStorage.SIDED.registerForBlockEntity(LargeCompressorBlock.StructureBlockEntity::getOutputStorage, LARGE_COMPRESSOR.getStructure().getBlockEntityType());
        FluidPump.SIDED.registerForBlockEntity(LargeCompressorBlock.StructureBlockEntity::getFluidPump, LARGE_COMPRESSOR.getStructure().getBlockEntityType());

        STOMACH_BE = register("stomach", (p, s) -> new StomachBlockEntity(STOMACH_BE, p, s), STOMACH);
        LivingMachineComponent.LOOKUP.registerSelf(LivingMachines.STOMACH_BE);
        FluidStorage.SIDED.registerForBlocks(BigBlock2Structure.getApi(FluidStorage.SIDED), STOMACH.structure);

        METABOLISER_SEGMENT_BE = register("metaboliser_segment", (p, s) -> new MetaboliserSegmentBlockEntity(METABOLISER_SEGMENT_BE, p, s), METABOLISER_SEGMENT);
        LivingMachineComponent.LOOKUP.registerSelf(LivingMachines.METABOLISER_SEGMENT_BE);
        BloodAcceptor.SIDED.registerForBlocks(BigBlock2Structure.getApi(BloodAcceptor.SIDED), METABOLISER_SEGMENT.structure);

        BALL_MILL_BE = register("ball_mill", (p, s) -> new BallMillBlockEntity(BALL_MILL_BE, p, s), BALL_MILL);
        LivingMachineComponent.LOOKUP.registerSelf(LivingMachines.BALL_MILL_BE);

        TREE_VACUUM_BE = register("tree_vacuum", (p, s) -> new TreeVacuumBlockEntity(TREE_VACUUM_BE, p, s), TREE_VACUUM);
        LivingMachineComponent.LOOKUP.registerSelf(LivingMachines.TREE_VACUUM_BE);

        CHARNEL_PUMP_BE = register("charnel_pump", (p, s) -> new CharnelPumpBlockEntity(LivingMachines.CHARNEL_PUMP_BE, p, s), NMBlocks.CHARNEL_PUMP);
        LivingMachineComponent.LOOKUP.registerSelf(CHARNEL_PUMP_BE);

        ITEM_INPUT_PORT_BE = register("item_input_port", (p, s) -> new ItemInputPortBlockEntity(ITEM_INPUT_PORT_BE, p, s), ITEM_INPUT_PORT);
        LivingMachineComponent.LOOKUP.registerSelf(LivingMachines.ITEM_INPUT_PORT_BE);
        ItemStorage.SIDED.registerForBlockEntity(ItemInputPortBlockEntity::getStorage, ITEM_INPUT_PORT_BE);

        ITEM_OUTPUT_PORT_BE = register("item_output_port", (p, s) -> new ItemOutputPortBlockEntity(ITEM_OUTPUT_PORT_BE, p, s), ITEM_OUTPUT_PORT);
        LivingMachineComponent.LOOKUP.registerSelf(LivingMachines.ITEM_OUTPUT_PORT_BE);
        ItemStorage.SIDED.registerForBlockEntity(ItemOutputPortBlockEntity::getStorage, ITEM_OUTPUT_PORT_BE);

        FLUID_INPUT_PORT_BE = register("fluid_input_port", (p, s) -> new FluidInputPortBlockEntity(FLUID_INPUT_PORT_BE, p, s), FLUID_INPUT_PORT);
        LivingMachineComponent.LOOKUP.registerSelf(FLUID_INPUT_PORT_BE);
        FluidStorage.SIDED.registerForBlockEntity(FluidInputPortBlockEntity::getStorage, FLUID_INPUT_PORT_BE);

        FLUID_OUTPUT_PORT_BE = register("fluid_output_port", (p, s) -> new FluidOutputPortBlockEntity(FLUID_OUTPUT_PORT_BE, p, s), FLUID_OUTPUT_PORT);
        LivingMachineComponent.LOOKUP.registerSelf(FLUID_OUTPUT_PORT_BE);
        FluidStorage.SIDED.registerForBlockEntity(FluidOutputPortBlockEntity::getStorage, FLUID_OUTPUT_PORT_BE);
        FluidPump.SIDED.registerForBlockEntity(FluidOutputPortBlockEntity::getPump, FLUID_OUTPUT_PORT_BE);

        LIVING_MACHINE_CONTROLLER_BE = register("living_machine_controller", (p, s) -> new LivingMachineControllerBlockEntity(LivingMachines.LIVING_MACHINE_CONTROLLER_BE, p, s), LIVING_MACHINE_CONTROLLER);

        LUCKY_ONE_BE = register("lucky_one", (p, s) -> new LuckyOneBlockEntity(LivingMachines.LUCKY_ONE_BE, p, s), LUCKY_ONE);
        LivingMachineComponent.LOOKUP.registerSelf(LUCKY_ONE_BE);
        BloodAcceptor.SIDED.registerForBlockEntity(LuckyOneBlockEntity::getAcceptor, LUCKY_ONE_BE);

        EXTRACTOR_BE = register("extractor", (p, s) -> new ExtractorBlockEntity(LivingMachines.EXTRACTOR_BE, p, s), EXTRACTOR);
        LivingMachineComponent.LOOKUP.registerSelf(EXTRACTOR_BE);

        SILKY_EXTRACTOR_BE = register("silky_extractor", (p, s) -> new SilkyExtractorBlockEntity(LivingMachines.SILKY_EXTRACTOR_BE, p, s), SILKY_EXTRACTOR);
        LivingMachineComponent.LOOKUP.registerSelf(SILKY_EXTRACTOR_BE);

        DISTRIBUTOR_POINT_BE = register("distributor_point", (p, s) -> new DistributorPointBlockEntity(LivingMachines.DISTRIBUTOR_POINT_BE, p, s), DISTRIBUTOR_POINT);
        LivingMachineComponent.LOOKUP.registerSelf(DISTRIBUTOR_POINT_BE);

        Processes.init();
    }
}
