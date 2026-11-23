/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge rewrite of meatlib. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file.
 * This port does not relicense the work.
 */

package com.neep.meatlib;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.component.MeatlibComponents;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.registry.MeatlibProbe;
import com.neep.meatlib.transfer.MultiFluidBuffer;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import com.neep.neepmeat.NeepMeat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.concurrent.atomic.AtomicInteger;

@GameTestHolder(NeepMeat.ID)
@PrefixGameTestTemplate(false)
public class MeatlibGameTests {
    @GameTest(template = "empty")
    public static void libraryRegisters(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(ResourceLocation.fromNamespaceAndPath(MeatLib.ID, "probe")), "probe block");
        helper.assertTrue(MeatlibProbe.PROBE.get() != null, "probe instance");
        helper.assertTrue(BuiltInRegistries.SOUND_EVENT.containsKey(MeatlibSounds.WRENCH_CLICK.getId()), "wrench sound");
        helper.assertTrue(BuiltInRegistries.DATA_COMPONENT_TYPE.containsKey(MeatlibComponents.UNIQUE_ID.getId()), "unique id component");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fluidBufferUsesMilliBuckets(GameTestHelper helper) {
        MultiFluidBuffer buffer = new MultiFluidBuffer(2, 4000);
        FluidStack water = new FluidStack(Fluids.WATER, 1500);
        int filled = buffer.fill(water, IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(filled == 1500, "filled " + filled);
        FluidStack drained = buffer.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(drained.getAmount() == 1000, "drained " + drained.getAmount());
        helper.assertTrue(buffer.getFluidInTank(0).getAmount() == 500, "left " + buffer.getFluidInTank(0).getAmount());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void channelFormatRoundTrips(GameTestHelper helper) {
        AtomicInteger received = new AtomicInteger();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ChannelFormat.INT.emitter(outgoing -> buf.writeBytes(outgoing)).accept(42);
        buf.readerIndex(0);
        ChannelFormat.INT.receive(received::set, buf, Runnable::run);
        helper.assertTrue(received.get() == 42, "channel " + received.get());
        helper.succeed();
    }
}
