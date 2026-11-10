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

package com.neep.neepmeat.plc.editor;

//public class CompiledProgramEditorState implements PLCState
//{
//    private final PLCBlockEntity parent;
//    private MutableProgram program;
//    @Nullable private InstructionBuilder instructionBuilder;
//    @Nullable private InstructionProvider provider;
//    private final List<Listener> listeners = Lists.newArrayList();
//    private int selected;
//
//    public CompiledProgramEditorState(PLCBlockEntity parent)
//    {
//        this.parent = parent;
//        program = new PLCProgramImpl(parent::getWorld);
//    }
//
//    @Override
//    public void setInstructionBuilder(InstructionProvider provider)
//    {
//        this.provider = provider;
//        instructionBuilder = provider.start((ServerWorld) parent.getWorld(), this::emitInstruction);
//    }
//
//    private void emitInstruction(Instruction instruction)
//    {
//        if (program != null)
//        {
//            program.add(selected, instruction);
////            program.addBack(instruction);
//            selected = Math.min(program.size() - 1, selected + 1);
//            sendProgram();
//        }
//    }
//
//    @Override
//    public void argument(Argument argument)
//    {
//        if (program != null && instructionBuilder != null)
//        {
//            try
//            {
//                instructionBuilder.argument(argument);
//            }
//            catch (InstructionException e)
//            {
//                PLCErrorMessageS2C.send((ServerPlayerEntity) parent.getSurgeryRobot().getController(), e.getMessage());
//            }
//        }
//    }
//
//    @Override
//    public RecordMode getMode()
//    {
//        return RecordMode.EDIT;
//    }
//
//    @Override
//    public int getArgumentCount()
//    {
//        if (instructionBuilder == null)
//            return 0;
//
//        return instructionBuilder.argumentCount();
//    }
//
//    @Override
//    public int getMaxArguments()
//    {
//        if (provider == null)
//            return 0;
//
//        return provider.maxArguments();
//    }
//
//    public void delete(int index)
//    {
//        if (program != null)
//        {
//            program.remove(index);
//            if (selected >= program.size())
//            {
//                selected = program.size() - 1;
//            }
//            sendProgram();
//        }
//    }
//
//    private void sendProgram()
//    {
//        if (!parent.getWorld().isClient() && parent.getSurgeryRobot().getController() != null)
//        {
////            PLCSyncThings.sendProgram((ServerPlayerEntity) parent.getSurgeryRobot().getController(), parent, program);
//        }
//    }
//
//    public void receiveProgram(NbtCompound nbt)
//    {
//        if (program == null)
//        {
//            program = new PLCProgramImpl(parent::getWorld);
//        }
//        program.readNbt(nbt);
//        updateListeners();
//    }
//
//    public PLCProgram getProgram()
//    {
//        return program;
//    }
//
//    public void addListener(Listener listener)
//    {
//        this.listeners.add(listener);
//    }
//
//    private void updateListeners()
//    {
//        for (var listener : listeners)
//        {
//            listener.update(program);
//        }
//    }
//
//    @Override
//    public NbtCompound writeNbt(NbtCompound nbt)
//    {
//        program.writeNbt(nbt);
//        return nbt;
//    }
//
//    @Override
//    public void readNbt(NbtCompound nbt)
//    {
//        program.readNbt(nbt);
//    }
//
//    public int getSelected()
//    {
//        return selected;
//    }
//
//    public void setSelected(int selected)
//    {
//        this.selected = selected;
//    }
//
//    @FunctionalInterface
//    public interface Listener
//    {
//        void update(PLCProgram program);
//    }
//}
