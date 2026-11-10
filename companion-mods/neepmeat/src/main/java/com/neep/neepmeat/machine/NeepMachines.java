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
import com.neep.neepmeat.machine.alloy.AlloyKilnBlockEntity;
import com.neep.neepmeat.machine.alloy.AlloyKilnMenu;
import com.neep.neepmeat.machine.transport.ColoredFluidPipeBlock;
import com.neep.neepmeat.machine.transport.FilterItemPipeBlock;
import com.neep.neepmeat.machine.transport.FluidPipeBlock;
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
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemPipeBlockEntity>> ITEM_PIPE_BE = BLOCK_ENTITIES.register("item_pipe",
            () -> BlockEntityType.Builder.of(ItemPipeBlockEntity::new, ITEM_PIPE.get(), FILTER_ITEM_PIPE.get(), MERGE_PIPE.get()).build(null));

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
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidTankBlockEntity>> FLUID_TANK_BE = BLOCK_ENTITIES.register("fluid_tank",
            () -> BlockEntityType.Builder.of(FluidTankBlockEntity::new, FLUID_TANK.get()).build(null));

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
    }

    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == BuildingBlocks.TAB.getKey()) {
            event.accept(ALLOY_KILN_ITEM.get());
            event.accept(ITEM_PIPE_ITEM.get());
            event.accept(FILTER_ITEM_PIPE_ITEM.get());
            event.accept(MERGE_PIPE_ITEM.get());
            event.accept(PIPE_DRIVER_ITEM.get());
            event.accept(FLUID_PIPE_ITEM.get());
            for (DeferredItem<BlockItem> colored : COLORED_FLUID_PIPE_ITEMS) {
                event.accept(colored.get());
            }
            event.accept(PUMP_ITEM.get());
            event.accept(FLUID_TANK_ITEM.get());
        }
    }
}
