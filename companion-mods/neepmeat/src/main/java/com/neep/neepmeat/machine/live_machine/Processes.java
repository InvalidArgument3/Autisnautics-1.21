/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * A process matches when every required component bit is set. The first match wins.
 */

package com.neep.neepmeat.machine.live_machine;

import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.machine.live_machine.process.BallMillProcess;
import com.neep.neepmeat.machine.live_machine.process.CrusherProcess;
import com.neep.neepmeat.machine.live_machine.process.LargeTrommelProcess;
import com.neep.neepmeat.machine.live_machine.process.LargeCompressorProcess;
import com.neep.neepmeat.machine.live_machine.process.MetaboliserProcess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public final class Processes {
    private static final Processes INSTANCE = new Processes();
    private final List<Entry> entries = new ArrayList<>();
    private boolean initialised;

    private Processes() {}

    public static Processes getInstance() {
        return INSTANCE;
    }

    public static void init() {
        if (INSTANCE.initialised) {
            return;
        }
        INSTANCE.initialised = true;
        INSTANCE.register(new MetaboliserProcess());
        INSTANCE.register(new LargeCompressorProcess());
        INSTANCE.register(new CrusherProcess());
        INSTANCE.register(new LargeTrommelProcess());
        INSTANCE.register(new BallMillProcess());
    }

    public void register(Process process) {
        BitSet bits = new BitSet();
        for (ComponentType<?> type : process.getRequired()) {
            bits.set(type.getBitIdx());
        }
        entries.add(new Entry(bits, process));
    }

    public @Nullable Process getRecipeFor(BitSet bits) {
        for (Entry entry : entries) {
            BitSet copy = (BitSet) entry.bits().clone();
            copy.and(bits);
            if (copy.cardinality() == entry.bits().cardinality()) {
                return entry.process();
            }
        }
        return null;
    }

    private record Entry(BitSet bits, Process process) {}
}
