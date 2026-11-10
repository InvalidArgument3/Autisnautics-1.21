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

import com.neep.meatlib.util.Completions;
import com.neep.meatlib.util.NbtSerialisable;
import com.neep.neepmeat.api.plc.program.PLCProgram;
import com.neep.neepmeat.api.storage.WorldSupplier;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.LabelLookup;
import com.neep.neepmeat.neepasm.compiler.ParsedInstructionList;
import com.neep.neepmeat.network.plc.PLCSyncAction;
import com.neep.neepmeat.plc.block.entity.PLCBlockEntity;
import com.neep.neepmeat.plc.compiler.PLCCompilePipeline;
import com.neep.neepmeat.plc.program.PLCProgramImpl;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.*;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class ProgramEditor implements NbtSerialisable
{
    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private String programSource = "";

    // *sigh* This has to be lazy as the parent BE needs to load NBT after construction
    @Nullable private PLCCompilePipeline<?> pipeline = null;
    private final PLCBlockEntity plc;

    private PLCProgram program = PLCProgram.EMPTY;
    private boolean needsCompile = true;

    @Nullable private ProgramEditor.Misery compileFuture;

    public ProgramEditor(PLCBlockEntity plc)
    {
        this.program = new PLCProgramImpl(plc::getLevel);
        this.plc = plc;
    }

    public void setProgramSource(String text)
    {
        this.programSource = text;
        needsCompile = true;
    }

    public String getSource()
    {
        return programSource;
    }

    public void updatePipeline()
    {
        pipeline = null;
    }

    private static <T extends ParsedInstructionList & LabelLookup> CompletableFuture<PLCProgram> thing(PLCCompilePipeline<T> pipeline, String source, Executor server)
    {
        CompletionStage<PLCProgram> stage = Completions.supplyAsync(() -> pipeline.parse(source), ProgramEditor.EXECUTOR)
                .thenApplyAsync(Completions.checkedFunction(pipeline::compile), server);

        return stage.toCompletableFuture()
                .orTimeout(100, TimeUnit.MILLISECONDS);
    }

    public void compile(Consumer<PLCProgram> completeAction)
    {
        if (pipeline == null)
            pipeline = plc.getLanguage().apply(WorldSupplier.of(plc));

        if (compileFuture != null)
            return;

        Executor server = plc.getLevel().getServer();


        // Prevent users from spamming compile
        if (!needsCompile)
        {
            CompletableFuture<PLCProgram> future = new CompletableFuture<>();
            future.complete(program);
            this.compileFuture = new Misery(future, completeAction);
            return;
        }

        needsCompile = false;

        CompletableFuture<PLCProgram> future = thing(pipeline, programSource, server);

        this.compileFuture = new Misery(future, completeAction);
    }

    public void tick()
    {
        if (compileFuture != null && compileFuture.isDone())
        {
            try
            {
                PLCProgram result = compileFuture.get();
                if (result != program)
                {
                    this.program = result;

                    if (plc.getSurgeryRobot().getController() != null)
                        PLCSyncAction.sendCompileStatus((ServerPlayer) plc.getSurgeryRobot().getController(), "Compiled successfully", true, -1);
                }
            }
            catch (ExecutionException e)
            {
                if (e.getCause() instanceof NeepASM.ProgramBuildException e1)
                {
                    if (plc.getSurgeryRobot().getController() != null)
                        PLCSyncAction.sendCompileStatus((ServerPlayer) plc.getSurgeryRobot().getController(), e1.getMessage(), false, e1.line());
                }
                else if (e.getCause() instanceof TimeoutException timeout)
                {
                    if (plc.getSurgeryRobot().getController() != null)
                        PLCSyncAction.sendCompileStatus((ServerPlayer) plc.getSurgeryRobot().getController(), "Compilation timeout. Program is too complex.", false, -1);
                }

                program = PLCProgram.EMPTY;
            }
            catch (InterruptedException ignored) { }

            compileFuture = null;
        }
    }

    public PLCProgram getProgram()
    {
        return program;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putString("source", programSource);
        nbt.put("program", program.writeNbt(new CompoundTag()));

        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        this.programSource = nbt.getString("source");
        this.program.readNbt(nbt.getCompound("program"));
    }

    public void setProgram(PLCProgram program)
    {
        this.program = program;
    }

    private record Misery(Future<PLCProgram> future, Consumer<PLCProgram> completeAction)
    {
        public PLCProgram get() throws InterruptedException, ExecutionException
        {
            PLCProgram program = future.get();
            completeAction.accept(program);
            return program;
        }

        public boolean isDone()
        {
            return future.isDone();
        }
    }
}
