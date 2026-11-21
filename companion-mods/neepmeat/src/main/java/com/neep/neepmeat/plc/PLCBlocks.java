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

package com.neep.neepmeat.plc;

import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.item.MeatlibItemSettings;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.registry.annotation.Path;
import com.neep.meatlib.registry.annotation.RegisterMe;
import com.neep.neepmeat.NMItemGroups;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.item.NMItemSettings;
import com.neep.neepmeat.machine.surgical_controller.PLCBlock;
import com.neep.neepmeat.plc.arm.RoboticArmBlock;
import com.neep.neepmeat.plc.arm.RoboticArmBlockEntity;
import com.neep.neepmeat.plc.block.*;
import com.neep.neepmeat.plc.block.entity.*;
import com.neep.neepmeat.plc.item.ProgramCardItem;
import com.neep.meatlib.transfer.ItemStorage;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

@RegisterMe(NeepMeat.NAMESPACE)
public class PLCBlocks
{
    public static final RegistrationContext C = new RegistrationContext(NeepMeat.NAMESPACE, () -> NMItemGroups.GENERAL);

    public static BlockEntityType<PLCRedstoneInterfaceBlockEntity> REDSTONE_INTERFACE_ENTITY;
    public static BlockEntityType<ProgramCabinetBlockEntity> PROGRAM_CABINET_BE;
    public static BlockEntityType<RoboticArmBlockEntity> ROBOTIC_ARM_ENTITY;
    public static BlockEntityType<ExecutorBlockEntity> EXECUTOR_ENTITY;
    public static BlockEntityType<PLCBlockEntity> PLC_ENTITY;
    public static BlockEntityType<BuilderBlockEntity> BUILDER_BE;
    public static BlockEntityType<ProcessorBlockEntity> PROCESSOR_BE;
    public static BlockEntityType<PLCNeepBusInterfaceBlockEntity> NEEPBUS_INTERFACE_BE;
    public static BlockEntityType<ImplanterBlockEntity> IMPLANTER_BE;

    public static final Item PROGRAM_CARD = new ProgramCardItem(TooltipSupplier.hidden(1), new MeatlibItemSettings().group(NMItemGroups.GENERAL));

    @Path("robotic_arm") public static final Block ROBOTIC_ARM = new RoboticArmBlock(C, NMItemSettings.block().tooltip(TooltipSupplier.hidden(1)).requiresMotor().plcActuator(), MeatlibBlockSettings.of().destroyTime(3).properties());
    public static final Block IMPLANTER = new ImplanterBlock(C, NMItemSettings.block().tooltip(TooltipSupplier.hidden(1)).plcActuator(), MeatlibBlockSettings.of().destroyTime(3).properties());
    @Path("redstone_interface") public static final Block REDSTONE_INTERFACE = new PLCRedstoneInterface(C, MeatlibBlockSettings.of().destroyTime(2).properties());
    @Path("executor") public static final Block EXECUTOR = new ExecutorBlock(C, ItemSettings.block().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.of().noOcclusion().destroyTime(2).properties());
    @Path("plc") public static final Block PLC = new PLCBlock(C, NMBlocks.block().retainsInv().neepbus(), MeatlibBlockSettings.copyOf(NMBlocks.MACHINE_SETTINGS).properties());
    public static final Block BUILDER = new BuilderBlock(C, NMBlocks.block().plcActuator().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(PLC).properties());
    public static final Block PROCESSOR = new ProcessorBlock(C, NMBlocks.block().neepbus(), MeatlibBlockSettings.copyOf(NMBlocks.MACHINE_SETTINGS).properties());
    public static final Block PROGRAM_CABINET = new ProgramCabinetBlock(C, NMBlocks.block().retainsInv().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(NMBlocks.MACHINE_SETTINGS).properties());
    @Path("plc_neepbus_interface") public static final Block NEEPBUS_INTERFACE = new PLCNeepBusInterfaceBlock(C, NMBlocks.block().neepbus().plcInterrupt().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(NMBlocks.MACHINE_SETTINGS).properties());
//    public static final Block SMALL_MEMORY = new MemoryBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(NMBlocks.MACHINE_SETTINGS).properties());

    public static void init()
    {
        ROBOTIC_ARM_ENTITY = NMBlockEntities.register("robotic_arm", (pos, state) -> new RoboticArmBlockEntity(ROBOTIC_ARM_ENTITY, pos, state), ROBOTIC_ARM);
        PROGRAM_CABINET_BE = NMBlockEntities.register("program_cabinet", (p, s) -> new ProgramCabinetBlockEntity(PLCBlocks.PROGRAM_CABINET_BE, p, s), PROGRAM_CABINET);
        IMPLANTER_BE = NMBlockEntities.register("implanter", (pos, state) -> new ImplanterBlockEntity(PLCBlocks.IMPLANTER_BE, pos, state), IMPLANTER);
        REDSTONE_INTERFACE_ENTITY = NMBlockEntities.register("redstone_interface", (pos, state) -> new PLCRedstoneInterfaceBlockEntity(REDSTONE_INTERFACE_ENTITY, pos, state), REDSTONE_INTERFACE);
        RedstoneInterface.LOOKUP.registerSelf(REDSTONE_INTERFACE_ENTITY);
        PLC_ENTITY = NMBlockEntities.register("plc", (pos, state) -> new PLCBlockEntity(PLCBlocks.PLC_ENTITY, pos, state), PLC);
        BUILDER_BE = NMBlockEntities.register("builder", (pos, state) -> new BuilderBlockEntity(PLCBlocks.BUILDER_BE, pos, state), BUILDER);
        ItemStorage.SIDED.registerForBlockEntity(BuilderBlockEntity::getStorage, BUILDER_BE);
        PROCESSOR_BE = NMBlockEntities.register("processor", (p, s) -> new ProcessorBlockEntity(PLCBlocks.PROCESSOR_BE, p, s), PROCESSOR);
        EXECUTOR_ENTITY = NMBlockEntities.register("executor", (pos, state) -> new ExecutorBlockEntity(PLCBlocks.EXECUTOR_ENTITY, pos, state), EXECUTOR);
        NEEPBUS_INTERFACE_BE = NMBlockEntities.register("neepbus_interface", (pos, state) -> new PLCNeepBusInterfaceBlockEntity(PLCBlocks.NEEPBUS_INTERFACE_BE, pos, state), NEEPBUS_INTERFACE);
    }
}
