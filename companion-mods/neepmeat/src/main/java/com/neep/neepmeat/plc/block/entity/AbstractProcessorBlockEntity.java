package com.neep.neepmeat.plc.block.entity;

import net.minecraft.core.HolderLookup;

import com.google.common.collect.Queues;
import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.plc.interrupt.InterruptEmitter;
import com.neep.neepmeat.api.plc.interrupt.InterruptReceiver;
import com.neep.neepmeat.api.plc.program.PLCProgram;
import com.neep.neepmeat.api.plc.robot.RobotAction;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.instruction.PlcInstruction;
import com.neep.neepmeat.plc.robot.InterruptManager;
import com.neep.neepmeat.plc.robot.PLCActuator;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractProcessorBlockEntity extends SyncableBlockEntity implements InterruptReceiver
{
    protected final InterruptManager interrupts = new InterruptManager(this::getLevel, this, this::setChanged);

    @Nullable protected PLCProgram currentProgram;
    @Nullable protected PlcInstruction currentInstruction;

    protected final Queue<Pair<RobotAction, PLCConsumer>> robotActions = Queues.newArrayDeque();
    @Nullable protected Pair<RobotAction, PLCConsumer> currentAction;

    @Nullable protected PLCActuator selectedActuator = null;

    // Allows manufacture instructions to retain a complex state.
    // Clear this whenever the program counter changes.
    protected CompoundTag volatileStorage = new CompoundTag();

    // If a RET branches to the same RET there is no way of knowing that the instruction needs to be executed again without this variable.
    protected boolean counterChanged;
    protected int counter;

    public AbstractProcessorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    // --- Interrupts ---

    @Override
    public void interrupt(BlockPos pos, InterruptEmitter emitter)
    {
        if (currentProgram != null)
        {
            interrupts.interrupt(pos, emitter);
        }
    }

    @Override
    public InterruptManager getInterrupts()
    {
        return interrupts;
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        interrupts.setEnabled(enabled);
    }

    // ---  Actuators ---

    protected @Nullable PLCActuator findActuator(@Nullable BlockPos pos)
    {
        if (pos == null)
            return defaultActuator();
        else if (level.getBlockEntity(pos) instanceof PLCActuator.Provider actuator)
            return actuator.getPlcActuator();
        else
            return null;
    }

    @Override
    public void selectActuator(@Nullable BlockPos pos) throws NeepASM.RuntimeException
    {
        var actuator = findActuator(pos);

        if (actuator == null)
            throw new NeepASM.RuntimeException("Block at " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " is not an actuator.");

        selectedActuator = actuator;
        setChanged();
    }

    @Nullable PLCActuator defaultActuator() { return null; }

    // --- Counter things ---

    @Override
    public int counter()
    {
        return counter;
    }

    @Override
    public void setCounter(int counter)
    {
        if (counter == -1)
        {
            stop();
        }
        else
        {
            this.counter = counter;
            counterChanged = true;
        }

        setChanged();
    }

    @Override
    public void advanceCounter()
    {
        counter++;
        counterChanged = true;
        setChanged();
    }

    public void execute(Instruction instruction)
    {
        // Normal instructions do not use robot actions.
        if (instruction instanceof PlcInstruction plcInstruction)
        {
            if (plcInstruction.canStart(this))
            {
                robotActions.clear();
                if (currentAction != null)
                {
                    currentAction = null;
                }

                currentInstruction = plcInstruction;
                try
                {
                    instruction.start(this);
                }
                catch (NeepASM.RuntimeException e)
                {
                    raiseError(e);
                }
            }
        }
        else
        {
            try
            {
                instruction.start(this);
            }
            catch (NeepASM.RuntimeException e)
            {
                raiseError(e);
            }
        }
    }

    protected void raiseError(NeepASM.RuntimeException e)
    {
        raiseError(new Error(e));
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);

        nbt.put("volatile_storage", volatileStorage.copy());
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);

        volatileStorage = nbt.getCompound("volatile_storage");
    }

    @Override
    public CompoundTag volatileStorage()
    {
        return volatileStorage;
    }

    public void stop()
    {
        currentProgram = null;
        counter = -1;
        interrupts.setEnabled(true);
        interrupts.clear();
        volatileStorage = new CompoundTag();
    }
}
