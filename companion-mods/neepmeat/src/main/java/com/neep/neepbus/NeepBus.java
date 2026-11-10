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

package com.neep.neepbus;

import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.item.MeatlibItemSettings;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.registry.annotation.Path;
import com.neep.meatlib.registry.annotation.RegisterMe;
import com.neep.neepbus.block.*;
import com.neep.neepbus.block.entity.*;
import com.neep.neepbus.input.InputInterceptors;
import com.neep.neepbus.item.NetworkingToolItem;
import com.neep.neepbus.network.NeepBusNetwork;
import com.neep.neepbus.screen.NeepBusConfigScreenHandler;
import com.neep.neepbus.util.ConfigEntry;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepmeat.NMItemGroups;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.item.NMItemSettings;
import net.neoforged.api.distmarker.Dist;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@RegisterMe(NeepBus.REGISTRY_NAMESPACE)
public class NeepBus implements ModInitializer
{
    // Use the same namespace so that the content will appear when @neepmeat is used in REI/EMI
    public static final String REGISTRY_NAMESPACE = "neepmeat";

    public static final RegistrationContext C = new RegistrationContext(REGISTRY_NAMESPACE, () -> NMItemGroups.GENERAL);

    public static final BlockBehaviour.Properties SETTINGS = NMBlocks.MACHINE_SETTINGS;
    public static final MeatlibBlockSettings GAUGE_SETTINGS = MeatlibBlockSettings.copyOf(SETTINGS).destroyTime(0.3f);

    public static final Block SLIDER = new SliderBlock(C, NMItemSettings.block().neepbus().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(GAUGE_SETTINGS));
    public static final Block SLANTED_SLIDER = new SlantedSliderBlock(C, NMItemSettings.block().neepbus().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(GAUGE_SETTINGS));
    public static final Block VERTICAL_GAUGE = new VerticalGaugeBlock(C, NMItemSettings.block().neepbus().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(GAUGE_SETTINGS));
    @Path("single_button") public static final Block SINGLE_BUTTON = new LargeButtonBlock(C, NMItemSettings.block().neepbus().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(GAUGE_SETTINGS));
    public static final Block REDSTONE_TRANSDUCER = new RedstoneTransducerBlock(C, NMItemSettings.block().neepbus().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(SETTINGS));
    public static final Block PID_CONTROLLER = new PIDControllerBlock(C, NMItemSettings.block().neepbus().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(SETTINGS));
    public static final Block ADDRESS_DECODER = new AddressDecoderBlock(C, NMItemSettings.block().neepbus().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(SETTINGS));
    public static final Block DELAY = new UnitDelayBlock(C, NMItemSettings.block().neepbus().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(SETTINGS));
    public static final Block NEEPBUS_BUFFER = new NeepBusBufferBlock(C, NMItemSettings.block().neepbus().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(SETTINGS));
    public static final Block ARITHMETIC = new ArithmeticBlock(C, NMItemSettings.block().neepbus().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(SETTINGS));
    public static final Block INTEGRATOR = new IntegratorBlock(C, NMItemSettings.block().neepbus().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(SETTINGS));
    public static final Block SAMPLE_AND_HOLD = new SampleAndHoldBlock(C, NMItemSettings.block().neepbus().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(SETTINGS));
    public static final Block CLOCK = new ClockBlock(C, NMItemSettings.block().neepbus().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(SETTINGS));
    public static final Block MOVEMENT_CONTROL = new MovementControlBlock(C, NMItemSettings.block().neepbus().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(SETTINGS));
    public static final Block AUTOMATIC_MOUTH = new MouthBlock(C, NMItemSettings.block().retainsInv().neepbus().tooltip(TooltipSupplier.hidden(1)), MeatlibBlockSettings.copyOf(SETTINGS));

    public static BlockEntityType<VerticalGaugeBlockEntity> VERTICAL_GAUGE_BE;
    public static BlockEntityType<SliderBlockEntity> LINEAR_LEVER_BE;
    public static BlockEntityType<LargeButtonBlockEntity> LARGE_BUTTON_BE;
//    public static BlockEntityType<SenderTestBlock.BlockEntity> SENDER_TEST_BE;
//    public static BlockEntityType<PortTestBlock.PortTestBlockEntity> PORT_TEST_BE;
    public static BlockEntityType<RedstoneTransducerBlockEntity> REDSTONE_TRANSDUCER_BE;
    public static BlockEntityType<PIDControllerBlockEntity> PID_CONTROLLER_BE;
    public static BlockEntityType<AddressDecoderBlockEntity> ADDRESS_DECODER_BE;
    public static BlockEntityType<UnitDelayBlockEntity> DELAY_BE;
    public static BlockEntityType<NeepBusBufferBlockEntity> BUFFER_BE;
    public static BlockEntityType<ArithmeticBlockEntity> ARITHMETIC_BE;
    public static BlockEntityType<IntegratorBlockEntity> INTEGRATOR_BE;
    public static BlockEntityType<SampleAndHoldBlockEntity> SAMPLE_AND_HOLD_BE;
    public static BlockEntityType<ClockBlockEntity> CLOCK_BE;
    public static BlockEntityType<MovementControlBlockEntity> MOVEMENT_CONTROL_BE;
    public static BlockEntityType<MouthBlockEntity> MOUTH_BE;

    public static Item NETWORKING_TOOL = new NetworkingToolItem(NMItems.C, TooltipSupplier.hidden(3), new MeatlibItemSettings().group(NMItemGroups.GENERAL));

    public void onInitialize()
    {
        VERTICAL_GAUGE_BE = NMBlockEntities.register("vertical_gauge", (p, s) -> new VerticalGaugeBlockEntity(VERTICAL_GAUGE_BE, p, s), VERTICAL_GAUGE);
        LINEAR_LEVER_BE = NMBlockEntities.register("slider", (p, s) -> new SliderBlockEntity(LINEAR_LEVER_BE, p, s), SLIDER, SLANTED_SLIDER);
        LARGE_BUTTON_BE = NMBlockEntities.register("large_button", (p, s) -> new LargeButtonBlockEntity(LARGE_BUTTON_BE, p, s), SINGLE_BUTTON);
//        SENDER_TEST_BE = NMBlockEntities.register("sender_test", (p, s) -> new SenderTestBlock.BlockEntity(SENDER_TEST_BE, p, s), SENDER_TEST);
//        PORT_TEST_BE = NMBlockEntities.register("port_test", (p, s) -> new PortTestBlock.PortTestBlockEntity(PORT_TEST_BE, p, s), PORT_TEST);
        REDSTONE_TRANSDUCER_BE = NMBlockEntities.register("redstone_transducer", (p, s) -> new RedstoneTransducerBlockEntity(REDSTONE_TRANSDUCER_BE, p, s), REDSTONE_TRANSDUCER);
        PID_CONTROLLER_BE = NMBlockEntities.register("pid_controller", (p, s) -> new PIDControllerBlockEntity(PID_CONTROLLER_BE, p, s), PID_CONTROLLER);
        ADDRESS_DECODER_BE = NMBlockEntities.register("address_decoder", (p, s) -> new AddressDecoderBlockEntity(ADDRESS_DECODER_BE, p, s), ADDRESS_DECODER);
        DELAY_BE = NMBlockEntities.register("delay", (p, s) -> new UnitDelayBlockEntity(DELAY_BE, p, s), DELAY);
        BUFFER_BE = NMBlockEntities.register("neepbus_buffer", (p, s) -> new NeepBusBufferBlockEntity(BUFFER_BE, p, s), NEEPBUS_BUFFER);
        ARITHMETIC_BE = NMBlockEntities.register("arithmetic", (p, s) -> new ArithmeticBlockEntity(ARITHMETIC_BE, p, s), ARITHMETIC);
        INTEGRATOR_BE = NMBlockEntities.register("integrator", (p, s) -> new IntegratorBlockEntity(INTEGRATOR_BE, p, s), INTEGRATOR);
        SAMPLE_AND_HOLD_BE = NMBlockEntities.register("sample_and_hold", (p, s) -> new SampleAndHoldBlockEntity(SAMPLE_AND_HOLD_BE, p, s), SAMPLE_AND_HOLD);
        CLOCK_BE = NMBlockEntities.register("clock", (p, s) -> new ClockBlockEntity(CLOCK_BE, p, s), CLOCK);
        MOVEMENT_CONTROL_BE = NMBlockEntities.register("movement_control", (p, s) -> new MovementControlBlockEntity(MOVEMENT_CONTROL_BE, p, s), MOVEMENT_CONTROL);
        MOUTH_BE = NMBlockEntities.register("mouth", (p, s) -> new MouthBlockEntity(MOUTH_BE, p, s), AUTOMATIC_MOUTH);

        NeepBusScreenHandlers.init();
        InputInterceptors.init();

        NeepBusNetwork.NT_CONNECT.receiverHandler(Dist.DEDICATED_SERVER, (player, buf, responseSender) -> (pos, isOutput, entryIndex, address) ->
        {
            Level world = player.level();
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof NeepBusProvider provider)
            {
                @Nullable NeepBusConfig config = provider.getConfig(world, pos, state, null);
                if (config != null)
                {
                    List<? extends ConfigEntry> entries = isOutput ? config.getOutputs() : config.getInputs();

                    if (entryIndex < entries.size())
                    {
                        entries.get(entryIndex).setAddress(address);
                        config.applyChanges();
                    }
                }
            }
        });

        NeepBusNetwork.OPEN_CONFIG_SCREEN.receiverHandler(Dist.DEDICATED_SERVER, (player, buf, responseSender) -> (pos) ->
        {
            Level world = player.level();
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof NeepBusProvider provider)
            {
                @Nullable NeepBusConfig config = provider.getConfig(world, pos, state, null);
                if (config != null)
                {
                    player.openMenu(NeepBusConfigScreenHandler.getFactory(config));
                }
            }
        });
    }
}
