package com.neep.neepmeat.plc.block.entity;

import com.neep.neepbus.block.entity.ConfigProvider;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepbus.util.SimpleEntry;
import com.neep.neepbus.util.WritePort;
import com.neep.neepmeat.api.plc.memory.MemoryManager;
import com.neep.neepmeat.api.plc.robot.RobotAction;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.vm.DataStack;
import com.neep.neepmeat.neepasm.vm.ProgramResolver;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.instruction.PlcInstruction;
import com.neep.neepmeat.plc.processor.PLCMemory;
import com.neep.neepmeat.plc.processor.PLCProgramResolver;
import com.neep.neepmeat.plc.processor.VariableStack;
import com.neep.neepmeat.plc.robot.InterruptManager;
import com.neep.neepmeat.plc.robot.PLCActuator;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ProcessorBlockEntity extends AbstractProcessorBlockEntity implements ConfigProvider
{
    protected final PLCProgramResolver programResolver = new PLCProgramResolver(this, () -> this.currentProgram);

    @Nullable private BlockPos actuatorPos = null; // Workaround for loading selected actuator from NBT

    private final int maxStackSize = 32;
    private final VariableStack returnStack = new VariableStack(maxStackSize, "return stack");
    private final VariableStack dataStack = new VariableStack(maxStackSize, "data stack");
    private final PLCMemory memory = new PLCMemory(dataStack);

    private final WritePort setCounter = data ->
    {
        counter = data.intValue();
        paused = false;
        error = null;
    };

    private final WritePort reset = data ->
    {
        counter = -1;
        error = null;
    };

    private final NeepBusConfig config = NeepBusConfig.builder(this::setChanged)
            .applyChanges(this)
            .input(new SimpleEntry("SET COUNTER"), setCounter)
            .input(new SimpleEntry("RESET"), reset)
            .build();

    private boolean paused;

    @Nullable private Error error;

    public ProcessorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void addRobotAction(RobotAction action, PLCConsumer callback)
    {

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
            selectedActuator = null;
            actuatorPos = null;
        }

        return selectedActuator;
    }

    @Override
    public MemoryManager getMemory() { return memory; }

    @Override
    public void raiseError(Error error)
    {
        say(error.what().getString());
        this.error = error;

        paused = true;

        robotActions.clear();
        if (currentAction != null)
            currentAction = null;

        if (currentInstruction != null)
            currentInstruction.cancel(this);

        returnStack.clear();

        if (level instanceof ServerLevel serverWorld)
        {
            serverWorld.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, 20, 0.25, 0.25, 0.25, 0.1);
        }
    }

    public void serverTick()
    {
        interrupts.update();

        if (!paused && counter != -1 && error == null)
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

        if (currentInstruction != null)
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
            if (interrupts.hasQueued())
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

    public void stop()
    {
        super.stop();
        paused = true;
        returnStack.clear();
    }

    @Override
    public void say(String message)
    {
        PlayerLookup.around((ServerLevel) level, worldPosition, 20).forEach(p -> p.sendSystemMessage(
                Component.nullToEmpty("[Processor at " + worldPosition.getX() + " " + worldPosition.getY() + " " + worldPosition.getZ() + "] " + message)));
    }

    @Override
    public ProgramResolver getProgramResolver()
    {
        return programResolver;
    }

    @Override
    public DataStack dataStack()
    {
        return dataStack;
    }

    @Override
    public DataStack returnStack()
    {
        return returnStack;
    }

    public void invalidateNeepBus()
    {

    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.put("config", config.writeNbt(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        config.readNbt(nbt.getCompound("config"));
    }
}
