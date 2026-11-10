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

package com.neep.neepmeat.transport;

import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.MeatlibItemSettings;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.ItemRegistry;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.registry.annotation.Path;
import com.neep.meatlib.registry.annotation.RegisterMe;
import com.neep.neepmeat.NMItemGroups;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.item.NMItemSettings;
import com.neep.neepmeat.machine.dumper.DumperBlock;
import com.neep.neepmeat.plc.block.ProcessInterfaceBlock;
import com.neep.neepmeat.plc.block.entity.ProcessInterfaceBlockEntity;
import com.neep.neepmeat.transport.api.item_network.RoutablePipe;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.api.pipe.ItemPipe;
import com.neep.neepmeat.transport.block.item_transport.*;
import com.neep.neepmeat.transport.block.item_transport.entity.ManualRequesterBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.entity.PipeDriverBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.entity.RequesterBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.entity.StorageDriverBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.machine.*;
import com.neep.neepmeat.transport.item.ProcessCardItem;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;

import static net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE;

@RegisterMe(NeepMeat.NAMESPACE)
public class ItemTransport
{
    public static final RegistrationContext C = new RegistrationContext(NeepMeat.NAMESPACE, () -> NMItemGroups.GENERAL);

    public static final int BFS_MAX_DEPTH = 800;
    public static final Block PROCESS_INTERFACE = new ProcessInterfaceBlock(C, NMBlocks.block().plcInterrupt(), MeatlibBlockSettings.copyOf(NMBlocks.MACHINE_SETTINGS));

    public static BlockApiLookup<ItemPipe, Direction> ITEM_PIPE_LOOKUP = BlockApiLookup.get(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "item_pipe"), ItemPipe.class, Direction.class);
    public static BlockEntityType<PipeDriverBlockEntity> PIPE_DRIVER_BE;
    public static BlockEntityType<StorageDriverBlockEntity> STORAGE_BUS_BE;
    public static BlockEntityType<ManualRequesterBlockEntity> ITEM_REQUESTER_BE;
    public static BlockEntityType<RequesterBlockEntity> REQUESTER_BE;

    public static final Block WOODEN_HOPPER = new WoodenHopperBlock(C, NMItemSettings.block(), MeatlibBlockSettings.of().simpleDrop(ItemRegistry::getMatchingItem).strength(1f).tags(MINEABLE_WITH_AXE).sound(SoundType.WOOD));
    @Path("item_duct") public static Block HOPPER_EXTENDER = new HopperExtenderBlock(C, NMBlocks.block(), MeatlibBlockSettings.copyOf(WOODEN_HOPPER).strength(0.5f));

    public static final Block PIPE_DRIVER = new PipeDriverBlock(C, NMItemSettings.block().plcActuator().tooltip(TooltipSupplier.hidden(2)), MeatlibBlockSettings.of().strength(1f).sound(SoundType.METAL));
    public static final Block STORAGE_BUS = new StorageDriverBlock(C, NMItemSettings.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.of().strength(0.3f).sound(SoundType.METAL));
    public static final Block ITEM_REQUESTER = new ManualRequesterBlock(C, NMItemSettings.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.of().strength(0.3f).sound(SoundType.METAL));
    public static final Block ITEM_PIPE = new ItemPipeBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(NMBlocks.ITEM_PIPE_SETTINGS));
    public static final Block FILTER_ITEM_PIPE = new FilterItemPipeBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(NMBlocks.ITEM_PIPE_SETTINGS));
    public static final Block SPEED_ITEM_PIPE = new SpeedItemPipeBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(NMBlocks.ITEM_PIPE_SETTINGS));
    public static final Block OPAQUE_ITEM_PIPE = new OpaqueItemPipeBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(ITEM_PIPE));
    public static final Block ENCASED_ITEM_PIPE = new EncasedItemPipeBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(NMBlocks.ITEM_PIPE_SETTINGS));
    public static final Block MERGE_ITEM_PIPE = new MergePipeBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(NMBlocks.ITEM_PIPE_SETTINGS));
    public static final Block ITEM_PUMP = new ItemPumpBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(NMBlocks.MACHINE_SETTINGS));
    public static final Block EJECTOR = new EjectorBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(NMBlocks.MACHINE_SETTINGS));
    public static final Block FILTERED_EJECTOR = new FilteredEjectorBlock(C, NMItemSettings.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(EJECTOR));
    public static final Block ADVANCED_EJECTOR = new AdvancedEjectorBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(NMBlocks.MACHINE_SETTINGS));
    public static final Block TRANSPOSER = new TransposerBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(NMBlocks.MACHINE_SETTINGS));
    public static final Block ITEM_INTERFACE = new ItemInterfaceBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(NMBlocks.MACHINE_SETTINGS));
    public static final Block ROUTER = new RouterBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(NMBlocks.MACHINE_SETTINGS));
    public static final Block ADVANCED_ROUTER = new AdvancedRouterBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(ROUTER));
    public static final Block BUFFER = new BufferBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(Blocks.CHEST).tags(MINEABLE_WITH_AXE));
    public static final Block DUMPER = new DumperBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), MeatlibBlockSettings.copyOf(Blocks.OAK_WOOD).tags(MINEABLE_WITH_AXE));
    public static final Block REQUESTER = new RequesterBlock(C, NMBlocks.block().neepbus().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(EJECTOR));

    public static final FlexSiloBlock FLEX_SILO = new FlexSiloBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), FluidTransport.FLEX_TANK_SETTINGS, () -> NMBlockEntities.FLEX_SILO);

    public static final Item PROCESS_CARD = new ProcessCardItem(TooltipSupplier.hidden(1), new MeatlibItemSettings().group(NMItemGroups.GENERAL));

    public static BlockEntityType<FilteredEjectorBlockEntity> FILTERED_EJECTOR_BE;
    public static BlockEntityType<ProcessInterfaceBlockEntity> PROCESS_INTERFACE_BE;

    public static void init()
    {
        PIPE_DRIVER_BE = NMBlockEntities.register("pipe_driver", PipeDriverBlockEntity::new, PIPE_DRIVER);
        RoutingNetwork.LOOKUP.registerForBlockEntity(PipeDriverBlockEntity::getNetwork, PIPE_DRIVER_BE);
        STORAGE_BUS_BE = NMBlockEntities.register("storage_bus", StorageDriverBlockEntity::new, STORAGE_BUS);
        RoutablePipe.LOOKUP.registerSelf(STORAGE_BUS_BE);
        ITEM_REQUESTER_BE = NMBlockEntities.register("item_requester", ManualRequesterBlockEntity::new, ITEM_REQUESTER);
        RoutablePipe.LOOKUP.registerSelf(ITEM_REQUESTER_BE);
        FILTERED_EJECTOR_BE = NMBlockEntities.register("filtered_ejector", (p, s) -> new FilteredEjectorBlockEntity(ItemTransport.FILTERED_EJECTOR_BE, p, s), FILTERED_EJECTOR);
        REQUESTER_BE = NMBlockEntities.register("requester", (p, s) -> new RequesterBlockEntity(ItemTransport.REQUESTER_BE, p, s), REQUESTER);
        PROCESS_INTERFACE_BE = NMBlockEntities.register("process_interface", (pos, state) -> new ProcessInterfaceBlockEntity(ItemTransport.PROCESS_INTERFACE_BE, pos, state), PROCESS_INTERFACE);
        ItemStorage.SIDED.registerForBlockEntity(ProcessInterfaceBlockEntity::getStorage, ItemTransport.PROCESS_INTERFACE_BE);
        RoutablePipe.LOOKUP.registerSelf(ItemTransport.PROCESS_INTERFACE_BE);

        ITEM_PIPE_LOOKUP.registerFallback((world, pos, state, blockEntity, context) -> state.getBlock() instanceof ItemPipe pipe ? pipe : null);

    }
}
