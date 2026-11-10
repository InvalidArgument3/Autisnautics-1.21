/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 */

package com.neep.neepmeat.plc.block.entity;

import com.neep.meatlib.neepasm.NeepASM;
import com.neep.meatlib.network.MeatlibPayloads;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.plc.program.PLCProgram;
import com.neep.neepmeat.machine.NeepMachines;
import com.neep.neepmeat.plc.block.PLCBlock;
import com.neep.neepmeat.plc.compiler.NeepAsmCompilePipeline;
import com.neep.neepmeat.plc.robot.PLCActuator;
import com.neep.neepmeat.plc.runtime.InstructionPlc;
import com.neep.neepmeat.plc.screen.PLCMenu;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class PLCBlockEntity extends BlockEntity implements MenuProvider {
    public static final ResourceLocation CHANNEL = ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "plc_source");

    private String source = "";
    private String error = "";
    private boolean running;
    private @Nullable InstructionPlc machine;

    public PLCBlockEntity(BlockPos pos, BlockState state) {
        super(NeepMachines.PLC_BE.get(), pos, state);
    }

    public static void registerNetwork() {
        MeatlibPayloads.listen(CHANNEL, (buf, player) -> {
            BlockPos pos = buf.readBlockPos();
            String text = buf.readUtf();
            boolean run = buf.readBoolean();
            if (!(player.level().getBlockEntity(pos) instanceof PLCBlockEntity plc)) {
                return;
            }
            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64) {
                return;
            }
            plc.setSource(text);
            if (run) {
                plc.start();
            } else {
                plc.stop();
            }
        });
    }

    public static void sendSource(BlockPos pos, String source, boolean run) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeUtf(source);
        buf.writeBoolean(run);
        MeatlibPayloads.sendToServer(CHANNEL, buf);
    }

    public String source() {
        return source;
    }

    public void setSource(String source) {
        this.source = source == null ? "" : source;
        setChanged();
    }

    public String error() {
        return machine == null ? error : (machine.error() == null ? error : machine.error());
    }

    public boolean running() {
        return running;
    }

    public @Nullable InstructionPlc machine() {
        return machine;
    }

    public void start() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        error = "";
        try {
            PLCProgram program = new NeepAsmCompilePipeline(() -> server).compile(
                    new com.neep.neepmeat.neepasm.compiler.NeepAsmParser(com.neep.neepmeat.plc.Instructions.REGISTRY).parse(source));
            PLCActuator actuator = findActuator();
            if (actuator == null) {
                error = "No actuator in front";
                running = false;
                machine = null;
                setChanged();
                return;
            }
            machine = new InstructionPlc(program, actuator);
            running = true;
        } catch (NeepASM.ProgramBuildException exception) {
            error = exception.getMessage();
            running = false;
            machine = null;
        }
        setChanged();
    }

    public void stop() {
        running = false;
        setChanged();
    }

    public void serverTick() {
        if (!running || machine == null) {
            return;
        }
        PLCActuator actuator = machine.getActuator();
        if (actuator instanceof BuilderBlockEntity builder) {
            builder.serverTick();
        }
        machine.tick();
        if (machine.error() != null) {
            error = machine.error();
            running = false;
        } else if (machine.counter() < 0) {
            running = false;
        }
        setChanged();
    }

    public @Nullable PLCActuator findActuator() {
        if (level == null) {
            return null;
        }
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockEntity entity = level.getBlockEntity(worldPosition.relative(facing));
        if (entity instanceof PLCActuator.Provider provider) {
            return provider.getPlcActuator();
        }
        if (entity instanceof PLCActuator actuator) {
            return actuator;
        }
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.neepmeat.plc");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new PLCMenu(id, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("source", source);
        tag.putString("error", error);
        tag.putBoolean("running", running);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        source = tag.getString("source");
        error = tag.getString("error");
        running = false;
    }
}
