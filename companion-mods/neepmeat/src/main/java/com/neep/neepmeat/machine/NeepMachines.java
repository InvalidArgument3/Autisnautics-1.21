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
import com.neep.neepmeat.machine.transport.FluidPipeBlock;
import com.neep.neepmeat.machine.transport.FluidPipeBlockEntity;
import com.neep.neepmeat.machine.transport.FluidTankBlock;
import com.neep.neepmeat.machine.transport.FluidTankBlockEntity;
import com.neep.neepmeat.machine.transport.ItemPipeBlock;
import com.neep.neepmeat.machine.transport.ItemPipeBlockEntity;
import com.neep.neepmeat.recipe.AlloyKilnRecipe;
import com.neep.neepmeat.recipe.NeepRecipes;
import net.minecraft.core.BlockPos;
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
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemPipeBlockEntity>> ITEM_PIPE_BE = BLOCK_ENTITIES.register("item_pipe",
            () -> BlockEntityType.Builder.of(ItemPipeBlockEntity::new, ITEM_PIPE.get()).build(null));

    public static final DeferredBlock<FluidPipeBlock> FLUID_PIPE = CONTEXT.block("fluid_pipe", FluidPipeBlock::new);
    public static final DeferredItem<BlockItem> FLUID_PIPE_ITEM = CONTEXT.item("fluid_pipe", () -> new BlockItem(FLUID_PIPE.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE_BE = BLOCK_ENTITIES.register("fluid_pipe",
            () -> BlockEntityType.Builder.of(FluidPipeBlockEntity::new, FLUID_PIPE.get()).build(null));

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
            event.accept(FLUID_PIPE_ITEM.get());
            event.accept(FLUID_TANK_ITEM.get());
        }
    }
}
