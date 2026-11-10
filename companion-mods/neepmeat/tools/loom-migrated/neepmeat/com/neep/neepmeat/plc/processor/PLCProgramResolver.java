package com.neep.neepmeat.plc.processor;

import com.neep.neepmeat.api.plc.program.PLCProgram;
import com.neep.neepmeat.neepasm.vm.ProgramMemoryModule;
import com.neep.neepmeat.neepasm.vm.ProgramResolver;
import com.neep.neepmeat.plc.block.ComputerBinder;
import com.neep.neepmeat.plc.block.entity.AbstractProcessorBlockEntity;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.util.BFSGroupFinder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public class PLCProgramResolver implements ProgramResolver
{
    public static final int SEGMENT_BITS = 13;
    //    public static final int SEGMENT_SIZE = 1 << SEGMENT_BITS;
    public static final int SEGMENT_SIZE = 1 << 32 - SEGMENT_BITS;

    private final Supplier<PLCProgram> mainProgram;

    private final AbstractProcessorBlockEntity parent;
    private final ObjectArrayList<ProgramMemoryModule.Segment> segments = new ObjectArrayList<>();
    private final ObjectArrayList<ProgramMemoryModule> modules = new ObjectArrayList<>();
    private final Object2IntMap<String> symbolLookup = new Object2IntOpenHashMap<>();

    private boolean segmentsDirty = true;

    public PLCProgramResolver(AbstractProcessorBlockEntity parent, Supplier<PLCProgram> mainProgram)
    {
        this.parent = parent;
        this.mainProgram = mainProgram;
    }

    private PLCProgram getMainProgram()
    {
        return Objects.requireNonNullElse(mainProgram.get(), PLCProgram.EMPTY);
    }

    public int lookupSymbol(String symbol)
    {
        // We need to get all the programs together
        var result = getMainProgram().exportedSymbols().getOrDefault(symbol, -1);
        if (result == -1)
        {
            if (segmentsDirty)
                organiseSegments(searchForModules());

            result = symbolLookup.getOrDefault(symbol, -1);
        }
        return result;
    }

    public Instruction get(int address)
    {
        int offsetBits = 32 - SEGMENT_BITS;

        int segment = address >> offsetBits;
        int offset = address & (-1 >>> offsetBits);
        // TODO: extract cabinet address

        if (segment == 0)
        {
            return getMainProgram().get(offset);
        }
        else
        {
            if (segmentsDirty)
                organiseSegments(searchForModules());

            // We need to find the cabinets somehow
            var element = (segment < segments.size() && segment > 0)
                    ? segments.get(segment) : null;
            if (element == null)
                return Instruction.EMPTY;

            return element.getInstruction(offset);
        }
    }

    @Override
    public void memoryAdded(ProgramMemoryModule module)
    {
        segmentsDirty = true;
    }

    private void organiseSegments(Collection<ProgramMemoryModule> newModules)
    {
        segmentsDirty = false;

        modules.clear();
        symbolLookup.clear();

        // Create a new listener and invalidate the old one so that it will no longer receive updates.
        // This will prevent modules that still exist but have been disconnected from sending updates to this PLC.
        if (listener != null)
            listener.remove();
        this.listener = new ModuleListener();

        for (var module : newModules)
        {
            modules.add(module);
            module.addListener(listener);
        }

        segments.clear();
        var allSegments = newModules.stream()
                .flatMap(m -> m.segments().stream())
                .collect(ObjectArrayList<ProgramMemoryModule.Segment>::new, ObjectArrayList::add, ObjectArrayList::addAll);

        allSegments.sort(Comparator.comparingInt(ProgramMemoryModule.Segment::segmentOffset));

        for (var segment : allSegments)
        {
            segments.size(segment.segmentOffset() + 1);
            if (segments.get(segment.segmentOffset()) != null)
                parent.say("Invalid memory layout: overlapping modules at segment " + segment.segmentOffset());

            segments.add(segment.segmentOffset(), segment);
            segment.getRelocatedSymbols().forEach(p -> symbolLookup.put(p.key(), p.valueInt()));
        }
    }

    private Collection<ProgramMemoryModule> searchForModules()
    {
        BFSGroupFinder<ProgramMemoryModule> finder = new BFSGroupFinder<>()
        {
            private final Level world = parent.getLevel();

            @Override
            protected State processPos(BlockPos pos)
            {
                BlockPos.MutableBlockPos mutable = pos.mutable();

                for (Direction direction : Direction.values())
                {
                    mutable.setWithOffset(pos, direction);
                    if (world.getBlockState(mutable).getBlock() instanceof ComputerBinder)
                    {
                        queueBlock(mutable.immutable());
                        visited.add(mutable.asLong());

                        if (world.getBlockEntity(mutable) instanceof ProgramMemoryModule module)
                            addResult(mutable.immutable(), module);
                    }
                }
                return State.CONTINUE;
            }
        };

        finder.queueBlock(parent.getBlockPos());
        finder.loop(4);
        return finder.getResult().values();
    }

    private class ModuleListener implements ProgramMemoryModule.Listener
    {
        // True if this listener has been invalidated
        private boolean removed = false;

        @Override
        public void notify(ProgramMemoryModule.Reason reason)
        {
            segmentsDirty = true;
        }

        @Override
        public boolean isRemoved()
        {
            // Listener may not be invalidated if the parent PLC is destroyed
            return removed || parent.isRemoved();
        }

        public void remove()
        {
            this.removed = true;
        }
    }

    @Nullable private ModuleListener listener;
}
