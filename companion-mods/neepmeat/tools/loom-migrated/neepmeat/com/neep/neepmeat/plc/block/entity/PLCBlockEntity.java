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

package com.neep.neepmeat.plc.block.entity;

import com.neep.neepbus.util.CachingSender;
import com.neep.neepmeat.api.plc.program.PLCProgram;
import com.neep.neepmeat.api.plc.robot.RobotAction;
import com.neep.neepmeat.client.screen.plc.LanguageMode;
import com.neep.neepmeat.client.screen.plc.RecordMode;
import com.neep.neepmeat.machine.surgical_controller.SurgicalRobot;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.vm.DataStack;
import com.neep.neepmeat.neepasm.vm.ProgramResolver;
import com.neep.neepmeat.network.plc.PLCRobotEnterS2C;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.PLCState;
import com.neep.neepmeat.plc.editor.ProgramEditor;
import com.neep.neepmeat.plc.editor.ShellState;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.instruction.PlcInstruction;
import com.neep.neepmeat.plc.processor.PLCMemory;
import com.neep.neepmeat.plc.processor.PLCProgramResolver;
import com.neep.neepmeat.plc.processor.VariableStack;
import com.neep.neepmeat.plc.robot.InterruptManager;
import com.neep.neepmeat.plc.robot.PLCActuator;
import com.neep.neepmeat.plc.screen.PLCScreenHandler;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PLCBlockEntity extends AbstractProcessorBlockEntity implements ExtendedScreenHandlerFactory, PLCActuator.Provider
{
    private boolean paused;

    protected final PLCProgramResolver programResolver = new PLCProgramResolver(this, () -> this.currentProgram);
    protected final SurgicalRobot robot = new SurgicalRobot(this);
    protected boolean overrideController;

    protected final ShellState shell = new ShellState(this);

    private final ProgramEditor editor = new ProgramEditor(this);
    
    @Nullable private Error error;

    private final PLCPropertyDelegate delegate = new PLCPropertyDelegate();

    private final int maxStackSize = 32;
    private final VariableStack returnStack = new VariableStack(maxStackSize, "return stack");
    private final VariableStack dataStack = new VariableStack(maxStackSize, "data stack");
    private final PLCMemory memory = new PLCMemory(dataStack);

    @Nullable private BlockPos actuatorPos = null; // Workaround for loading selected actuator from NBT

    private RecordMode mode = RecordMode.IMMEDIATE;
    private LanguageMode language = LanguageMode.THORD;

    private final CachingSender neepBusSender = new CachingSender(this::getLevel, getBlockPos());

    public PLCBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);

        this.currentProgram = null;
        this.selectedActuator = robot;
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
    }

    public void tick()
    {
        editor.tick();

        interrupts.update();

        PLCProgram program = currentProgram;

        if (!paused && counter != -1 && program != null && error == null)
        {
            nextInstruction(currentInstruction, interrupts);

            // Execute CALL and RET immediately to lower time penalty for using lots of words
            // JAAAAAAAAAAAANK
            if (currentInstruction != null && (currentInstruction.getOpcode() == Instructions.CALL || currentInstruction.getOpcode() == Instructions.RET))
            {
                if (counter != -1 && error == null)
                    nextInstruction(currentInstruction, interrupts);
            }
        }

        if (program != null || currentInstruction != null)
        {
            if (currentAction == null || currentAction.first().finished(this))
            {
                if (currentAction != null)
                {
                    // Finish current action
                    try
                    {
                        currentAction.second().accept(this);
                    }
                    catch (NeepASM.RuntimeException e)
                    {
                        raiseError(e);
                    }

                    if (currentAction != null) // Action may have been made null by an error
                        currentAction.first().end(this);
                }

                if (robotActions.peek() != null)
                {
                    currentAction = robotActions.poll();
                    try
                    {
                        currentAction.first().start(this);
                    }
                    catch (NeepASM.RuntimeException e)
                    {
                        raiseError(e);
                    }

                    sync();
                }
                else
                {
                    if (currentAction != null)
                    {
                        robot.stay();
                        currentAction = null;
                        sync();
                    }
                }
            }
            else
            {
                try
                {
                    currentAction.first().tick(this);
                }
                catch (NeepASM.RuntimeException e)
                {
                    raiseError(e);
                }
            }
        }

        // Set override
        boolean prevOverride = overrideController;
        if (currentAction == null || currentAction.first().finished(this))
        {
            overrideController = false;
        }
        else
        {
            overrideController = currentAction.first().blocksController();
        }
        if (overrideController != prevOverride)
        {
            sync();
        }

        if (level instanceof ServerLevel serverWorld)
        {
            if (robot.shouldUpdatePosition(serverWorld))
            {
                robot.syncPosition(serverWorld);
            }
        }

        robot.tick();
    }

    @Override
    public void addRobotAction(RobotAction action, PLCConsumer callback)
    {
        robotActions.add(Pair.of(action, callback));
    }

    @Override
    public PLCActuator getActuator()
    {
        // Cache the current actuator
        if (actuatorPos != null && selectedActuator == null)
        {
            selectedActuator = findActuator(actuatorPos);
        }

        // Handle removal of the actuator
        if (selectedActuator == null || selectedActuator.actuatorRemoved())
        {
            selectedActuator = robot;
            actuatorPos = getBlockPos();
        }

        return selectedActuator;
    }

    public SurgicalRobot getSurgeryRobot()
    {
        return robot;
    }

    public void enter(Player player)
    {
        if (robot.getController() == null)
        {
            robot.setController(player);
            if (!level.isClientSide())
            {
                PLCRobotEnterS2C.send(player, this);
            }
            player.openMenu(this);
        }
    }

    @Override
    public VariableStack dataStack()
    {
        setChanged();
        return dataStack;
    }

    @Override
    public DataStack returnStack()
    {
        setChanged();
        return returnStack;
    }

    public VariableStack getVariableStack()
    {
        return dataStack;
    }

    @Override
    public PLCMemory getMemory()
    {
        return memory;
    }

    @Override
    public ProgramResolver getProgramResolver()
    {
        return programResolver;
    }

    private void say(Component what)
    {
        PlayerLookup.around((ServerLevel) getLevel(), getBlockPos(), 20).forEach(p -> p.sendSystemMessage(
                Component.nullToEmpty("[PLC at " + getBlockPos().getX() + " " + getBlockPos().getY() + " " + getBlockPos().getZ() + "] ").copy().append(what)));
    }

    @Override
    public void raiseError(Error error)
    {
        say(error.what());
        this.error = error;

        paused = true;

        robotActions.clear();
        if (currentAction != null)
        {
            currentAction = null;
        }

        if (currentInstruction != null)
        {
            currentInstruction.cancel(this);
        }

        robot.stay();

        returnStack.clear();

        if (level instanceof ServerLevel serverWorld)
        {
            serverWorld.sendParticles(ParticleTypes.SMOKE, robot.getX(), robot.getY(), robot.getZ(), 20, 0.25, 0.25, 0.25, 0.1);
        }
    }

    public void resetError()
    {
        error = null;
    }

    public @Nullable Error getError()
    {
        return error;
    }

    private void nextInstruction(@Nullable PlcInstruction current, InterruptManager interrupts)
    {
        Instruction instruction = programResolver.get(counter);

        // Detect whether the counter has been moved by the current instruction
        if (counterChanged || instruction != current)
        {
            if (counterChanged)
                volatileStorage = new CompoundTag();

            counterChanged = false;
            if (interrupts.isEnabled() && interrupts.hasQueued())
            {
                // Save the current counter in the call stack
                var entry = interrupts.poll();
                if (!entry.name().equals("_"))
                {
                    // Ugly try catch
                    try
                    {
                        // Use counter (not counter + 1) because this instruction has not yet been executed.
                        // It will be executed when RET pops from the call stack.
                        returnStack.push(counter);
                    }
                    catch (NeepASM.RuntimeException e)
                    {
                        raiseError(e);
                    }
                    setCounter(entry.index());

                    // Execute the instruction at the called position
                    instruction = programResolver.get(counter);
                }
            }

            execute(instruction);

            sync();
        }
    }

    @Override
    public void sync()
    {
        super.sync();
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        if (currentInstruction != null)
            currentInstruction.writeVolatileState(volatileStorage);

        super.saveAdditional(nbt);

        robot.writeNbt(nbt);
        nbt.putBoolean("override_controller", overrideController);
        nbt.putInt("counter", counter);
        nbt.putBoolean("paused", paused);
        nbt.putShort("mode", (short) mode.ordinal());
        nbt.putShort("language", (short) language.ordinal());

        nbt.put("editor", editor.writeNbt(new CompoundTag()));

        nbt.putBoolean("has_program", currentProgram != null);

        nbt.put("call_stack", returnStack.writeNbt(new CompoundTag()));
        nbt.put("variable_stack", dataStack.writeNbt(new CompoundTag()));

        nbt.put("interrupts", interrupts.writeNbt(new CompoundTag()));

        nbt.put("memory", memory.writeNbt(new CompoundTag()));

        if (selectedActuator != robot && selectedActuator != null)
            nbt.put("actuator", NbtUtils.writeBlockPos(selectedActuator.getBasePos()));
        else if (selectedActuator == null)
            nbt.put("actuator", NbtUtils.writeBlockPos(actuatorPos));

    }

    @Override
    public void toClientTag(CompoundTag nbt)
    {
        robot.writeNbt(nbt);
        nbt.putBoolean("override_controller", overrideController);
        nbt.putInt("counter", counter);
        nbt.putBoolean("paused", paused);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        robot.readNbt(nbt);
        this.overrideController = nbt.getBoolean("override_controller");
        this.counter = nbt.getInt("counter");
        this.paused = nbt.getBoolean("paused");
        this.mode = RecordMode.values()[nbt.getShort("mode")];
        this.language = LanguageMode.values()[nbt.getShort("language")];

        editor.readNbt(nbt.getCompound("editor"));

        returnStack.readNbt(nbt.getCompound("call_stack"));
        dataStack.readNbt(nbt.getCompound("variable_stack"));

        this.interrupts.readNbt(nbt.getCompound("interrupts"));
        this.memory.readNbt(nbt.getCompound("memory"));

        if (nbt.getBoolean("has_program"))
            currentProgram = editor.getProgram();

        if (nbt.contains("actuator"))
        {
            actuatorPos = NbtUtils.readBlockPos(nbt.getCompound("actuator"));
            selectedActuator = null;
        }
        else
        {
            actuatorPos = null;
            selectedActuator = robot;
        }
    }

    @Override
    public void saveToItem(ItemStack stack)
    {
        CompoundTag createdNbt = saveWithoutMetadata();
        createdNbt.remove("robot"); // Slightly janky as this key is internal to SurgicalRobot
        BlockItem.setBlockEntityData(stack, getType(), createdNbt);
    }

    public boolean notExecuting()
    {
        return currentAction == null || currentAction.first().finished(this);
    }

    public boolean overrideController()
    {
        return overrideController;
    }

    public void exit()
    {
        robot.setController(null);
    }

    public PLCState getState()
    {
        return shell;
    }

    public boolean actionBlocksController()
    {
        return currentAction != null && currentAction.first().blocksController();
    }

    public void setMode(RecordMode value)
    {
        this.mode = value;
        setChanged();
    }

    @Override
    public Component getDisplayName()
    {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player)
    {
        return new PLCScreenHandler(syncId, player, this, delegate, editor.getSource());
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf)
    {
        buf.writeBlockPos(worldPosition);
        buf.writeUtf(editor.getSource());
        buf.writeInt(mode.ordinal());
    }

    private void clear()
    {
        resetError();
        dataStack.clear();
        memory.clear();
        interrupts.setEnabled(true);
        interrupts.clear();
        currentInstruction = null;
    }

    public void runProgram(@Nullable PLCProgram program)
    {
        // If the program is currently running, simply unpause it.
        if (currentProgram != program)
        {
            clear();
            this.currentProgram = program;
        }

        paused = false;

        if (counter == -1)
            counter = 0;
    }

    public void hardStop()
    {
        if (currentInstruction != null)
        {
            currentInstruction.cancel(this);
            currentInstruction = null;
        }
        if (currentAction != null)
        {
            currentAction = null;
            robotActions.clear();
        }
        stop();
    }

    public void stop()
    {
        super.stop();
        paused = true;
        returnStack.clear();
    }

    public void pause()
    {
        paused = true;
    }

    public void step()
    {
        // Schedule compile if no program set
        if (currentProgram == null)
        {
            editor.compile(p -> currentProgram = p);
            return;
        }

        if (currentProgram != null)
        {
            // Start program if stopped
            if (counter == -1)
            {
                clear();
                counter = 0;
            }

            // Prevent execution of next instruction
            paused = true;

            // Counter is controlled by instructions and should not be advanced manually
            nextInstruction(currentInstruction, interrupts);
        }
    }

    public ProgramEditor getProgramEditor()
    {
        return editor;
    }

    @Override
    public PLCActuator getPlcActuator()
    {
        return robot;
    }
    // For receiving the stack on the client when a screen handler is in use

    public void updateVariableStack(CompoundTag nbt)
    {
        dataStack.readNbt(nbt);
    }

    public void setLanguage(LanguageMode languageMode)
    {
        this.language = languageMode;
        editor.updatePipeline();
        setChanged();
    }

    public LanguageMode getLanguage()
    {
        return language;
    }

    public void resetRobot()
    {
        robot.returnToBase();
    }

    public void updateMemory(ByteBuf bb)
    {
        memory.read(bb);
    }

    public class PLCPropertyDelegate implements ContainerData
    {
        public static final int SIZE = Names.values().length;

        @Override
        public int get(int index)
        {
            return switch (Names.values()[index])
            {
                case PROGRAM_COUNTER -> counter;
                case HAS_PROGRAM -> currentProgram != null || (currentInstruction != null && currentInstruction != Instruction.EMPTY) ? 1 : 0; // TODO: change to CAN_STOP
                case EDIT_MODE -> mode.ordinal();
                case LANGUAGE -> language.ordinal();
                case RUNNING -> (currentProgram != null && !paused) ? 1 : 0;
                case ARGUMENT -> shell.getArgumentCount();
                case MAX_ARGUMENTS -> shell.getMaxArguments();
                case DEBUG_LINE -> getDebugLine();
                case SELECTED_INSTRUCTION -> 0;
            };
        }

        @Override
        public void set(int index, int value)
        {
            switch (Names.values()[index])
            {
                case PROGRAM_COUNTER -> counter = value;
                case EDIT_MODE -> setMode(RecordMode.values()[Mth.clamp(value, 0, RecordMode.values().length)]);
                case SELECTED_INSTRUCTION -> {}
            }
        }

        @Override
        public int getCount()
        {
            return SIZE;
        }

        public enum Names
        {
            PROGRAM_COUNTER,
            HAS_PROGRAM,
            EDIT_MODE,
            LANGUAGE,
            RUNNING,
            ARGUMENT,
            MAX_ARGUMENTS,
            SELECTED_INSTRUCTION,
            DEBUG_LINE,
        }
    }

    private int getDebugLine()
    {
        if (currentProgram != null)
        {
            return currentProgram.getDebugLine(counter);
        }
        return -1;
    }

    public CachingSender getSender()
    {
        return neepBusSender;
    }

    @Override
    @Nullable PLCActuator defaultActuator()
    {
        return robot;
    }
}
