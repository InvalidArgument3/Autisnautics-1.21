package com.neep.neepmeat.neepasm.vm;

import com.neep.neepmeat.plc.instruction.Instruction;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;

import java.util.Collection;
import java.util.stream.Stream;

public interface ProgramMemoryModule
{
    Collection<Segment> segments();

    void addListener(Listener listener);

    /// Receiver for callbacks when the module's available segments change.
    interface Listener
    {
        void notify(Reason reason);

        boolean isRemoved();
    }

    enum Reason
    {
        SEGMENT_CHANGED,
        REMOVED
    }

    interface Segment
    {
        int segmentOffset();
        Instruction getInstruction(int offset);

        default Stream<ObjectIntPair<String>> getRelocatedSymbols() { return Stream.empty(); }
    }
}
