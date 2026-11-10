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

import com.google.common.collect.Maps;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.registry.annotation.Ignore;
import com.neep.meatlib.registry.annotation.Path;
import com.neep.meatlib.registry.annotation.RegisterMe;
import com.neep.neepmeat.NMItemGroups;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.NMSoundGroups;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.item.FluidComponentItem;
import com.neep.neepmeat.item.TankItem;
import com.neep.neepmeat.machine.fluid_rationer.FluidRationerBlock;
import com.neep.neepmeat.machine.multitank.MultiTankBlock;
import com.neep.neepmeat.machine.reservoir.ReservoirBlock;
import com.neep.neepmeat.transport.api.FluidTransportConstants;
import com.neep.neepmeat.transport.api.pipe.FluidPipe;
import com.neep.neepmeat.transport.block.fluid_transport.*;
import com.neep.neepmeat.transport.block.fluid_transport.machine.FluidPipeBlockEntity;
import com.neep.neepmeat.transport.block.fluid_transport.machine.TankType;
import com.neep.neepmeat.transport.fluid_network.FluidPipeVertex;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import com.neep.meatlib.resource.FabricResources.ResourceManagerHelper;
import com.neep.meatlib.transfer.FluidConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RegisterMe(NeepMeat.NAMESPACE)
public class FluidTransport
{
    public static final RegistrationContext C = new RegistrationContext(NeepMeat.NAMESPACE, () -> NMItemGroups.GENERAL);

    public static final Map<FluidPipe.PipeCol, FluidPipeBlock> COLOURED_FLUID_PIPES = Maps.newEnumMap(FluidPipe.PipeCol.class);
    public static final List<FluidPipeBlock> FLUID_PIPES = new ObjectArrayList<>();

    public static final BlockBehaviour.Properties TANK = MeatlibBlockSettings.create(BlockTags.MINEABLE_WITH_PICKAXE).forceSolidOn().strength(0.8f).sound(NMSoundGroups.TANK);
    public static final BlockBehaviour.Properties PORTABLE_TANK = MeatlibBlockSettings.copyOf(TANK).noOcclusion().properties();
    public static final BlockBehaviour.Properties FLEX_TANK_SETTINGS = MeatlibBlockSettings.copyOf(TANK).strength(1.4f).properties();

    // --- Fluid Pipes ---
    public static Block FLUID_PIPE = new FluidPipeBlock(C, FluidPipe.PipeCol.ANY, NMBlocks.block().factory(FluidComponentItem::new), NMBlocks.FLUID_PIPE_SETTINGS);
    public static Block ENCASED_FLUID_PIPE = new EncasedFluidPipeBlock(C, FluidPipe.PipeCol.ANY, NMBlocks.block().factory(FluidComponentItem::new).tooltip(TooltipSupplier.simple(1)), NMBlocks.FLUID_PIPE_SETTINGS);

    @Ignore public static Block WHITE_FLUID_PIPE = makePipe(FluidPipe.PipeCol.WHITE);
    @Ignore public static Block ORANGE_FLUID_PIPE = makePipe(FluidPipe.PipeCol.ORANGE);
    @Ignore public static Block MAGENTA_FLUID_PIPE = makePipe(FluidPipe.PipeCol.MAGENTA);
    @Ignore public static Block LIGHT_BLUE_FLUID_PIPE = makePipe(FluidPipe.PipeCol.LIGHT_BLUE);
    @Ignore public static Block YELLOW_FLUID_PIPE = makePipe(FluidPipe.PipeCol.YELLOW);
    @Ignore public static Block LIME_FLUID_PIPE = makePipe(FluidPipe.PipeCol.LIME);
    @Ignore public static Block PINK_FLUID_PIPE = makePipe(FluidPipe.PipeCol.PINK);
    @Ignore public static Block GRAY_FLUID_PIPE = makePipe(FluidPipe.PipeCol.GRAY);
    @Ignore public static Block LIGHT_GRAY_FLUID_PIPE = makePipe(FluidPipe.PipeCol.LIGHT_GRAY);
    @Ignore public static Block CYAN_FLUID_PIPE = makePipe(FluidPipe.PipeCol.CYAN);
    @Ignore public static Block PURPLE_FLUID_PIPE = makePipe(FluidPipe.PipeCol.PURPLE);
    @Ignore public static Block BLUE_FLUID_PIPE = makePipe(FluidPipe.PipeCol.BLUE);
    @Ignore public static Block BROWN_FLUID_PIPE = makePipe(FluidPipe.PipeCol.BROWN);
    @Ignore public static Block GREEN_FLUID_PIPE = makePipe(FluidPipe.PipeCol.GREEN);
    @Ignore public static Block RED_FLUID_PIPE = makePipe(FluidPipe.PipeCol.RED);
    @Ignore public static Block BLACK_FLUID_PIPE = makePipe(FluidPipe.PipeCol.BLACK);

    public static Block FILTER_PIPE = new FilterPipeBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(2)), NMBlocks.FLUID_PIPE_SETTINGS);
    public static Block STOP_VALVE = new StopValveBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), NMBlocks.FLUID_PIPE_SETTINGS);
    public static Block CHECK_VALVE = new CheckValveBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), NMBlocks.FLUID_PIPE_SETTINGS);
    public static Block LIMITER_VALVE = new LimiterValveBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), NMBlocks.FLUID_PIPE_SETTINGS);
//    public static Block ADVANCED_LIMITER_VALVE = new AdvancedLimiterValve(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), NMBlocks.FLUID_PIPE_SETTINGS);
    @Path("window_fluid_pipe") public static Block WINDOW_PIPE = new WindowPipeBlock(C, NMBlocks.block().tooltip(TooltipSupplier.blank()), NMBlocks.FLUID_PIPE_SETTINGS);
//    public static Block COPPER_PIPE = new CapillaryFluidPipeBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), NMBlocks.FLUID_PIPE_SETTINGS);
    @Path("pump") public static Block PUMP = new PumpBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), NMBlocks.FLUID_MACHINE_SETTINGS);

    public static Block BASIC_TANK = new TankBlock(C, NMBlocks.block().factory(TankItem.factory(TankType.BASIC)).tooltip(TooltipSupplier.hidden(2)), PORTABLE_TANK);
    public static Block BASIC_GLASS_TANK = new GlassTankBlock(C, NMBlocks.block().factory(TankItem.factory(TankType.BASIC_GLASS)).tooltip(TooltipSupplier.hidden(2)), PORTABLE_TANK);
    public static Block ADVANCED_TANK = new AdvancedTankBlock(C, NMBlocks.block().factory(TankItem.factory(TankType.ADVANCED)).tooltip(TooltipSupplier.hidden(2)), PORTABLE_TANK);

    public static Block MULTI_TANK = new MultiTankBlock(C, NMBlocks.block(), NMBlocks.FLUID_MACHINE_SETTINGS);
    public static Block FLUID_BUFFER = new FluidBufferBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), TANK);
    public static Block FLUID_INTERFACE = new FluidInterfaceBlock(C, NMBlocks.block().tooltip((TooltipSupplier.simple(1))).factory(FluidComponentItem::new), NMBlocks.FLUID_MACHINE_SETTINGS);
    public static Block FLUID_DRAIN = new FluidDrainBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), NMBlocks.FLUID_MACHINE_SETTINGS);

    public static Block RESERVOIR = new ReservoirBlock(C, NMBlocks.block().tooltip(TooltipSupplier.simple(1)), NMBlocks.FLUID_MACHINE_SETTINGS);

    public static FlexTankBlock FLEX_TANK = new FlexTankBlock(C, 8 * FluidConstants.BUCKET, () -> NMBlockEntities.FLEX_TANK,
            NMBlocks.block().tooltip(TooltipSupplier.simple(2)), FLEX_TANK_SETTINGS);
    public static FlexTankBlock ADVANCED_FLEX_TANK = new FlexTankBlock(C, 16 * FluidConstants.BUCKET, () -> NMBlockEntities.ADVANCED_FLEX_TANK,
            NMBlocks.block().tooltip(TooltipSupplier.simple(2)), FLEX_TANK_SETTINGS);

    public static Block FLUID_GAUGE = new FluidGaugeBlock<>(C, () -> NMBlockEntities.FLUID_GAUGE,
            NMBlocks.block().tooltip(TooltipSupplier.simple(1)), NMBlocks.FLUID_MACHINE_SETTINGS);
    public static Block ITEM_GAUGE = new FluidGaugeBlock<>(C, () -> NMBlockEntities.ITEM_GAUGE,
            NMBlocks.block().tooltip(TooltipSupplier.simple(1)), NMBlocks.FLUID_MACHINE_SETTINGS);
    public static Block FLUID_RATIONER = new FluidRationerBlock(C, NMBlocks.block().tooltip(TooltipSupplier.hidden(2)).neepbus(), MeatlibBlockSettings.copyOf(NMBlocks.FLUID_MACHINE_SETTINGS).noOcclusion().properties());

    public static void init()
    {
        // I can't remember why we can't use makePipe to create the uncoloured pipe, but it might be an important one.
        FLUID_PIPES.add((FluidPipeBlock) FLUID_PIPE);

        FluidPipeVertex.LOOKUP.registerFallback(FluidTransport::getVertex);

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(FluidTransportConstants.INSTANCE);
    }

    private static FluidPipeBlock makePipe(FluidPipe.PipeCol col)
    {
        var pipe = C.addParent(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "fluid_pipe_" + col.name().toLowerCase(Locale.ROOT)), new FluidPipeBlock(C, col, NMBlocks.block().factory(FluidComponentItem::new), NMBlocks.FLUID_PIPE_SETTINGS));
        COLOURED_FLUID_PIPES.put(col, pipe);
        FLUID_PIPES.add(pipe);
        return pipe;
    }

    private static FluidPipeVertex getVertex(Level world, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, Void unused)
    {
        if (blockEntity instanceof FluidPipeBlockEntity<?> be)
        {
            return be.getPipeVertex();
        }
        return null;
    }
}
