/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.machine;

import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.transfer.MeatlibCapabilities;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.building.BuildingBlocks;
import com.neep.neepmeat.machine.alloy.AlloyKilnBlock;
import com.neep.neepmeat.plc.arm.RoboticArmBlock;
import com.neep.neepmeat.plc.arm.RoboticArmBlockEntity;
import com.neep.neepmeat.plc.block.BuilderBlock;
import com.neep.neepmeat.plc.block.ExecutorBlock;
import com.neep.neepmeat.plc.block.PLCBlock;
import com.neep.neepmeat.plc.block.PLCRedstoneInterfaceBlock;
import com.neep.neepmeat.plc.block.entity.BuilderBlockEntity;
import com.neep.neepmeat.plc.block.entity.ExecutorBlockEntity;
import com.neep.neepmeat.plc.block.entity.PLCBlockEntity;
import com.neep.neepmeat.plc.block.entity.PLCRedstoneInterfaceBlockEntity;
import com.neep.neepmeat.plc.screen.PLCMenu;
import com.neep.neepmeat.machine.alloy.AlloyKilnBlockEntity;
import com.neep.neepmeat.machine.alloy.AlloyKilnMenu;
import com.neep.neepmeat.machine.transport.BasicTankBlock;
import com.neep.neepmeat.machine.transport.ColoredFluidPipeBlock;
import com.neep.neepmeat.machine.transport.FilterItemPipeBlock;
import com.neep.neepmeat.machine.transport.FilterPipeMenu;
import com.neep.neepmeat.machine.transport.FluidPipeBlock;
import com.neep.neepmeat.machine.transport.NetworkCrafterBlock;
import com.neep.neepmeat.machine.transport.NetworkCrafterBlockEntity;
import com.neep.neepmeat.machine.transport.ItemRequesterBlock;
import com.neep.neepmeat.machine.transport.ItemRequesterBlockEntity;
import com.neep.neepmeat.machine.transport.StorageBusBlock;
import com.neep.neepmeat.machine.transport.MergePipeBlock;
import com.neep.neepmeat.machine.transport.PipeDriverBlock;
import com.neep.neepmeat.machine.transport.PipeDriverBlockEntity;
import com.neep.neepmeat.machine.transport.PumpBlock;
import com.neep.neepmeat.machine.transport.PumpBlockEntity;
import com.neep.neepmeat.machine.transport.FluidPipeBlockEntity;
import com.neep.neepmeat.machine.transport.FluidTankBlock;
import com.neep.neepmeat.machine.transport.FluidTankBlockEntity;
import com.neep.neepmeat.machine.transport.ItemPipeBlock;
import com.neep.neepmeat.machine.transport.ItemPipeBlockEntity;
import com.neep.neepmeat.recipe.AlloyKilnRecipe;
import com.neep.neepmeat.recipe.NeepRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class NeepMachines {
    public static final RegistrationContext CONTEXT = new RegistrationContext(NeepMeat.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NeepMeat.ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, NeepMeat.ID);

    public static final DeferredBlock<AlloyKilnBlock> ALLOY_KILN = CONTEXT.block("alloy_kiln", AlloyKilnBlock::new);
    public static final DeferredItem<BlockItem> ALLOY_KILN_ITEM = CONTEXT.item("alloy_kiln", () -> new BlockItem(ALLOY_KILN.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlloyKilnBlockEntity>> ALLOY_KILN_BE = BLOCK_ENTITIES.register("alloy_kiln",
            () -> BlockEntityType.Builder.of(AlloyKilnBlockEntity::new, ALLOY_KILN.get()).build(null));
    public static final DeferredBlock<RoboticArmBlock> ROBOTIC_ARM = CONTEXT.block("robotic_arm", RoboticArmBlock::new);
    public static final DeferredItem<BlockItem> ROBOTIC_ARM_ITEM = CONTEXT.item("robotic_arm", () -> new BlockItem(ROBOTIC_ARM.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RoboticArmBlockEntity>> ROBOTIC_ARM_BE = BLOCK_ENTITIES.register("robotic_arm",
            () -> BlockEntityType.Builder.of(RoboticArmBlockEntity::new, ROBOTIC_ARM.get()).build(null));

    public static final DeferredBlock<PLCRedstoneInterfaceBlock> REDSTONE_INTERFACE = CONTEXT.block("redstone_interface", PLCRedstoneInterfaceBlock::new);
    public static final DeferredItem<BlockItem> REDSTONE_INTERFACE_ITEM = CONTEXT.item("redstone_interface", () -> new BlockItem(REDSTONE_INTERFACE.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PLCRedstoneInterfaceBlockEntity>> REDSTONE_INTERFACE_BE = BLOCK_ENTITIES.register("redstone_interface",
            () -> BlockEntityType.Builder.of(PLCRedstoneInterfaceBlockEntity::new, REDSTONE_INTERFACE.get()).build(null));

    public static final DeferredBlock<PLCBlock> PLC = CONTEXT.block("plc", PLCBlock::new);
    public static final DeferredItem<BlockItem> PLC_ITEM = CONTEXT.item("plc", () -> new BlockItem(PLC.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PLCBlockEntity>> PLC_BE = BLOCK_ENTITIES.register("plc",
            () -> BlockEntityType.Builder.of(PLCBlockEntity::new, PLC.get()).build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<PLCMenu>> PLC_MENU = MENUS.register("plc",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> {
                BlockPos pos = buf.readBlockPos();
                String source = buf.readUtf();
                String error = buf.readUtf();
                PLCBlockEntity plc = inventory.player.level().getBlockEntity(pos) instanceof PLCBlockEntity entity ? entity : null;
                return new PLCMenu(id, pos, source, error, plc);
            }));

    public static final DeferredBlock<ExecutorBlock> EXECUTOR = CONTEXT.block("executor", ExecutorBlock::new);
    public static final DeferredItem<BlockItem> EXECUTOR_ITEM = CONTEXT.item("executor", () -> new BlockItem(EXECUTOR.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExecutorBlockEntity>> EXECUTOR_BE = BLOCK_ENTITIES.register("executor",
            () -> BlockEntityType.Builder.of(ExecutorBlockEntity::new, EXECUTOR.get()).build(null));

    public static final DeferredBlock<BuilderBlock> BUILDER = CONTEXT.block("builder", BuilderBlock::new);
    public static final DeferredItem<BlockItem> BUILDER_ITEM = CONTEXT.item("builder", () -> new BlockItem(BUILDER.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BuilderBlockEntity>> BUILDER_BE = BLOCK_ENTITIES.register("builder",
            () -> BlockEntityType.Builder.of(BuilderBlockEntity::new, BUILDER.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<AlloyKilnMenu>> ALLOY_MENU = MENUS.register("alloy_kiln",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> {
                BlockPos pos = buf.readBlockPos();
                AlloyKilnBlockEntity kiln = (AlloyKilnBlockEntity) inventory.player.level().getBlockEntity(pos);
                return new AlloyKilnMenu(id, inventory, kiln);
            }));

    public static final DeferredBlock<ItemPipeBlock> ITEM_PIPE = CONTEXT.block("item_pipe", ItemPipeBlock::new);
    public static final DeferredItem<BlockItem> ITEM_PIPE_ITEM = CONTEXT.item("item_pipe", () -> new BlockItem(ITEM_PIPE.get(), new Item.Properties()));
    public static final DeferredBlock<FilterItemPipeBlock> FILTER_ITEM_PIPE = CONTEXT.block("filter_item_pipe", FilterItemPipeBlock::new);
    public static final DeferredItem<BlockItem> FILTER_ITEM_PIPE_ITEM = CONTEXT.item("filter_item_pipe", () -> new BlockItem(FILTER_ITEM_PIPE.get(), new Item.Properties()));
    public static final DeferredBlock<MergePipeBlock> MERGE_PIPE = CONTEXT.block("merge_item_pipe", MergePipeBlock::new);
    public static final DeferredItem<BlockItem> MERGE_PIPE_ITEM = CONTEXT.item("merge_item_pipe", () -> new BlockItem(MERGE_PIPE.get(), new Item.Properties()));
    public static final DeferredBlock<StorageBusBlock> STORAGE_BUS = CONTEXT.block("storage_bus", StorageBusBlock::new);
    public static final DeferredItem<BlockItem> STORAGE_BUS_ITEM = CONTEXT.item("storage_bus", () -> new BlockItem(STORAGE_BUS.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemPipeBlockEntity>> ITEM_PIPE_BE = BLOCK_ENTITIES.register("item_pipe",
            () -> BlockEntityType.Builder.of(ItemPipeBlockEntity::new, ITEM_PIPE.get(), FILTER_ITEM_PIPE.get(), MERGE_PIPE.get(), STORAGE_BUS.get()).build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<FilterPipeMenu>> FILTER_MENU = MENUS.register("filter_item_pipe",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> {
                BlockPos pos = buf.readBlockPos();
                ItemPipeBlockEntity pipe = (ItemPipeBlockEntity) inventory.player.level().getBlockEntity(pos);
                return new FilterPipeMenu(id, inventory, pipe);
            }));
    public static final DeferredBlock<ItemRequesterBlock> ITEM_REQUESTER = CONTEXT.block("item_requester", ItemRequesterBlock::new);
    public static final DeferredItem<BlockItem> ITEM_REQUESTER_ITEM = CONTEXT.item("item_requester", () -> new BlockItem(ITEM_REQUESTER.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemRequesterBlockEntity>> ITEM_REQUESTER_BE = BLOCK_ENTITIES.register("item_requester",
            () -> BlockEntityType.Builder.of(ItemRequesterBlockEntity::new, ITEM_REQUESTER.get()).build(null));
    public static final DeferredBlock<NetworkCrafterBlock> NETWORK_CRAFTER = CONTEXT.block("network_crafter", NetworkCrafterBlock::new);
    public static final DeferredItem<BlockItem> NETWORK_CRAFTER_ITEM = CONTEXT.item("network_crafter", () -> new BlockItem(NETWORK_CRAFTER.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NetworkCrafterBlockEntity>> NETWORK_CRAFTER_BE = BLOCK_ENTITIES.register("network_crafter",
            () -> BlockEntityType.Builder.of(NetworkCrafterBlockEntity::new, NETWORK_CRAFTER.get()).build(null));

    public static final DeferredBlock<PipeDriverBlock> PIPE_DRIVER = CONTEXT.block("pipe_driver", PipeDriverBlock::new);
    public static final DeferredItem<BlockItem> PIPE_DRIVER_ITEM = CONTEXT.item("pipe_driver", () -> new BlockItem(PIPE_DRIVER.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PipeDriverBlockEntity>> PIPE_DRIVER_BE = BLOCK_ENTITIES.register("pipe_driver",
            () -> BlockEntityType.Builder.of(PipeDriverBlockEntity::new, PIPE_DRIVER.get()).build(null));

    public static final DeferredBlock<FluidPipeBlock> FLUID_PIPE = CONTEXT.block("fluid_pipe", FluidPipeBlock::new);
    public static final DeferredItem<BlockItem> FLUID_PIPE_ITEM = CONTEXT.item("fluid_pipe", () -> new BlockItem(FLUID_PIPE.get(), new Item.Properties()));
    public static final Map<DyeColor, DeferredBlock<ColoredFluidPipeBlock>> COLORED_FLUID_PIPES = new EnumMap<>(DyeColor.class);
    public static final List<DeferredItem<BlockItem>> COLORED_FLUID_PIPE_ITEMS = new ArrayList<>();

    static {
        for (DyeColor color : DyeColor.values()) {
            String path = "fluid_pipe_" + color.getName();
            DeferredBlock<ColoredFluidPipeBlock> block = CONTEXT.block(path, () -> new ColoredFluidPipeBlock(color));
            COLORED_FLUID_PIPES.put(color, block);
            COLORED_FLUID_PIPE_ITEMS.add(CONTEXT.item(path, () -> new BlockItem(block.get(), new Item.Properties())));
        }
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE_BE = BLOCK_ENTITIES.register("fluid_pipe", () -> {
        Block[] extra = COLORED_FLUID_PIPES.values().stream().map(DeferredBlock::get).toArray(Block[]::new);
        Block[] blocks = new Block[extra.length + 1];
        blocks[0] = FLUID_PIPE.get();
        System.arraycopy(extra, 0, blocks, 1, extra.length);
        return BlockEntityType.Builder.of(FluidPipeBlockEntity::new, blocks).build(null);
    });

    public static final DeferredBlock<PumpBlock> PUMP = CONTEXT.block("pump", PumpBlock::new);
    public static final DeferredItem<BlockItem> PUMP_ITEM = CONTEXT.item("pump", () -> new BlockItem(PUMP.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PumpBlockEntity>> PUMP_BE = BLOCK_ENTITIES.register("pump",
            () -> BlockEntityType.Builder.of(PumpBlockEntity::new, PUMP.get()).build(null));

    public static final DeferredBlock<FluidTankBlock> FLUID_TANK = CONTEXT.block("fluid_tank", FluidTankBlock::new);
    public static final DeferredItem<BlockItem> FLUID_TANK_ITEM = CONTEXT.item("fluid_tank", () -> new BlockItem(FLUID_TANK.get(), new Item.Properties()));
    public static final DeferredBlock<BasicTankBlock> BASIC_TANK = CONTEXT.block("basic_tank", BasicTankBlock::new);
    public static final DeferredItem<BlockItem> BASIC_TANK_ITEM = CONTEXT.item("basic_tank", () -> new BlockItem(BASIC_TANK.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidTankBlockEntity>> FLUID_TANK_BE = BLOCK_ENTITIES.register("fluid_tank",
            () -> BlockEntityType.Builder.of(FluidTankBlockEntity::new, FLUID_TANK.get(), BASIC_TANK.get()).build(null));

    public static final DeferredHolder<net.minecraft.world.item.crafting.RecipeType<?>, net.minecraft.world.item.crafting.RecipeType<AlloyKilnRecipe>> ALLOY_TYPE = NeepRecipes.ALLOY;

    private NeepMachines() {}

    public static void register(IEventBus bus) {
        CONTEXT.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENUS.register(bus);
        NeepRecipes.TYPES.register(bus);
        NeepRecipes.SERIALIZERS.register(bus);
        bus.addListener(NeepMachines::capabilities);
        bus.addListener(NeepMachines::creative);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        MeatlibCapabilities.itemHandler(event, ALLOY_KILN_BE.get(), (kiln, side) -> new net.neoforged.neoforge.items.wrapper.InvWrapper(kiln));
        MeatlibCapabilities.fluidHandler(event, FLUID_TANK_BE.get(), (tank, side) -> tank.tank());
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, ITEM_REQUESTER_BE.get(), (requester, side) -> requester);
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, NETWORK_CRAFTER_BE.get(), (crafter, side) -> crafter);
    }

    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == BuildingBlocks.TAB.getKey()) {
            event.accept(ALLOY_KILN_ITEM.get());
            event.accept(ROBOTIC_ARM_ITEM.get());
            event.accept(REDSTONE_INTERFACE_ITEM.get());
            event.accept(PLC_ITEM.get());
            event.accept(EXECUTOR_ITEM.get());
            event.accept(BUILDER_ITEM.get());
            event.accept(ITEM_PIPE_ITEM.get());
            event.accept(FILTER_ITEM_PIPE_ITEM.get());
            event.accept(MERGE_PIPE_ITEM.get());
            event.accept(PIPE_DRIVER_ITEM.get());
            event.accept(STORAGE_BUS_ITEM.get());
            event.accept(ITEM_REQUESTER_ITEM.get());
            event.accept(FLUID_PIPE_ITEM.get());
            for (DeferredItem<BlockItem> colored : COLORED_FLUID_PIPE_ITEMS) {
                event.accept(colored.get());
            }
            event.accept(PUMP_ITEM.get());
            event.accept(FLUID_TANK_ITEM.get());
            event.accept(BASIC_TANK_ITEM.get());
        }
    }
}
