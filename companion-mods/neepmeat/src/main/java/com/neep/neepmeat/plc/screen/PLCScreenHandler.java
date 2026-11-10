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

package com.neep.neepmeat.plc.screen;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.network.ChannelManager;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.screen.plc.LanguageMode;
import com.neep.neepmeat.client.screen.plc.RecordMode;
import com.neep.neepmeat.init.NMScreenHandlers;
import com.neep.neepmeat.network.ScreenPropertyC2SPacket;
import com.neep.neepmeat.network.plc.PLCSyncAction;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.block.entity.PLCBlockEntity;
import com.neep.neepmeat.plc.instruction.Argument;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class PLCScreenHandler extends AbstractContainerMenu
{
    private final ContainerData delegate;
    private final PLCBlockEntity plc;
    private final String initialText;

    public final ChannelManager<ApplyAction> channel;
    public final ChannelManager<ChangeOperation> changeOperation;
    public final ChannelManager<ApplyArgument> applyArgument;
    public final ChannelManager<Consumer<RecordMode>> changeMode;
    public final ChannelManager<Consumer<LanguageMode>> changeLanguage;
    public final ChannelManager<Consumer<String>> updateText;

    public final ChannelManager<ApplyCompileMessage> compileMessageS2C;
    public final ChannelManager<Consumer<CompoundTag>> updateStackS2C;
    public final ChannelManager<Consumer<ByteBuf>> updateMemoryS2C;

    // Client
    public PLCScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf)
    {
        this(syncId,
                playerInventory.player, (PLCBlockEntity) playerInventory.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(PLCBlockEntity.PLCPropertyDelegate.SIZE),
                buf.readUtf()
        );
        this.delegate.set(PLCBlockEntity.PLCPropertyDelegate.Names.EDIT_MODE.ordinal(), buf.readInt());
    }

    // Server
    public PLCScreenHandler(int syncId, Player player, PLCBlockEntity plc, ContainerData delegate, String source)
    {
        super(NMScreenHandlers.PLC, syncId);
        this.plc = plc;
        this.delegate = delegate;
        this.initialText = source;
        addDataSlots(delegate);

        this.channel = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "plc_sync_program"),
                ChannelFormat.builder(ApplyAction.class).param(PLCSyncAction.PARAM_CODEC).build(), player);
        this.changeOperation = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "change_operation"),
                ChannelFormat.builder(ChangeOperation.class).param(ParamCodec.STRING).build(), player);
        this.applyArgument = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "apply_argument"),
                ChannelFormat.builder(ApplyArgument.class).param(Argument.PARAM_CODEC).build(), player);
        this.changeMode = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "change_mode"),
                ChannelFormat.builder(ThingConsumer.<RecordMode>as()).param(RecordMode.PARAM_CODEC).build(), player);
        this.changeLanguage = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "change_language"),
                ChannelFormat.builder(ThingConsumer.<LanguageMode>as()).param(LanguageMode.PARAM_CODEC).build(), player);
        this.updateText = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "update_text"),
                ChannelFormat.builder(ThingConsumer.<String>as()).param(ParamCodec.STRING).build(), player);

        this.compileMessageS2C = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "compile_message"),
                ChannelFormat.builder(ApplyCompileMessage.class).param(ParamCodec.STRING).param(ParamCodec.BOOLEAN).param(ParamCodec.INT).build(), player);
        this.updateStackS2C = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "update_stack"),
                ChannelFormat.builder(ThingConsumer.<CompoundTag>as()).param(ParamCodec.NBT).build(), player);
        this.updateMemoryS2C = ChannelManager.create(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "update_memory"),
                ChannelFormat.builder(ThingConsumer.<ByteBuf>as()).param(ParamCodec.BYTE_BUF).build(), player);

        channel.receiver(this::onApply);
        changeOperation.receiver(this::changeOperation);
        applyArgument.receiver(this::applyArgument);
        changeMode.receiver(plc::setMode);
        changeLanguage.receiver(plc::setLanguage);
        updateText.receiver(s -> plc.getProgramEditor().setProgramSource(s));

    }

    @Override
    public void broadcastFullState()
    {
        super.broadcastFullState();
    }

    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();
        updateStackS2C.emitter().accept(plc.getVariableStack().writeNbt(new CompoundTag()));
        ByteBuf bb = Unpooled.buffer();
        plc.getMemory().write(bb);
        updateMemoryS2C.emitter().accept(bb);
//        PLCSyncThings.sendStack((ServerPlayerEntity) player, plc.getVariableStack());
    }

    @Override
    public void sendAllDataToRemote()
    {
        super.sendAllDataToRemote();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player)
    {
        return true;
    }

    public PLCBlockEntity getPlc()
    {
        return plc;
    }

    public RecordMode getMode()
    {
        return RecordMode.values()[
                delegate.get(PLCBlockEntity.PLCPropertyDelegate.Names.EDIT_MODE.ordinal())
                ];
    }

    public boolean isRunning()
    {
        return delegate.get(PLCBlockEntity.PLCPropertyDelegate.Names.RUNNING.ordinal()) > 0;
    }

    public int getCounter()
    {
        return delegate.get(PLCBlockEntity.PLCPropertyDelegate.Names.PROGRAM_COUNTER.ordinal());
    }

    public int getMaxArguments()
    {
        return delegate.get(PLCBlockEntity.PLCPropertyDelegate.Names.MAX_ARGUMENTS.ordinal());
    }

    public LanguageMode getLanguage()
    {
        return LanguageMode.values()[delegate.get(PLCBlockEntity.PLCPropertyDelegate.Names.LANGUAGE.ordinal())];
    }

    public int getArguments()
    {
        return delegate.get(PLCBlockEntity.PLCPropertyDelegate.Names.ARGUMENT.ordinal());
    }

    public void setSelectedInstruction(int index)
    {
        ScreenPropertyC2SPacket.Client.send(PLCBlockEntity.PLCPropertyDelegate.Names.SELECTED_INSTRUCTION.ordinal(), index);
    }

    public int getSelectedInstruction()
    {
        return delegate.get(PLCBlockEntity.PLCPropertyDelegate.Names.SELECTED_INSTRUCTION.ordinal());
    }

    public String getInitialText()
    {
        return initialText;
    }

    public int hasProgram()
    {
        return delegate.get(PLCBlockEntity.PLCPropertyDelegate.Names.HAS_PROGRAM.ordinal());
    }

    public int debugLine()
    {
        return delegate.get(PLCBlockEntity.PLCPropertyDelegate.Names.DEBUG_LINE.ordinal());
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        channel.close();
        changeOperation.close();
        applyArgument.close();
        changeMode.close();
        changeLanguage.close();
        updateText.close();

        compileMessageS2C.close();
        updateStackS2C.close();
        updateMemoryS2C.close();
    }

    private void onApply(PLCSyncAction action)
    {
        switch (action)
        {
            case RUN -> plc.runProgram(plc.getProgramEditor().getProgram());
            case STOP -> plc.hardStop();
            case PAUSE -> plc.pause();
            case COMPILE -> plc.getProgramEditor().compile(p -> {});
            case STEP -> plc.step();
            case COMPILE_RUN ->
            {
                plc.getProgramEditor().compile(program ->
                {
                    plc.hardStop();
                    plc.runProgram(program);
                });
            }
        }
    }

    private void changeOperation(String id)
    {
        InstructionProvider provider = Instructions.REGISTRY.get(id);
        if (provider != null)
        {
            plc.getState().setInstructionBuilder(provider);
        }
    }

    private void applyArgument(Argument argument)
    {
        plc.getState().argument(argument);
    }

    @FunctionalInterface
    public interface ApplyAction
    {
        void apply(PLCSyncAction action);
    }

    @FunctionalInterface
    public interface ChangeOperation
    {
        void apply(String id);
    }

    @FunctionalInterface
    public interface ApplyArgument
    {
        void apply(Argument argument);
    }

    @FunctionalInterface
    public interface ApplyCompileMessage
    {
        void apply(String message, boolean success, int line);
    }
}
