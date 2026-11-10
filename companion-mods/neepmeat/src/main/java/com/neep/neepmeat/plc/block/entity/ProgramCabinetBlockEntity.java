package com.neep.neepmeat.plc.block.entity;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.neepmeat.api.plc.program.PLCProgram;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.neepasm.vm.ProgramMemoryModule;
import com.neep.neepmeat.plc.instruction.Instruction;
import com.neep.neepmeat.plc.processor.PLCProgramResolver;
import com.neep.neepmeat.plc.program.PLCProgramImpl;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ProgramCabinetBlockEntity extends SyncableBlockEntity implements ProgramMemoryModule
{
    public static final int SLOTS = 5 * 6;
    private final ImplementedInventory inventory = ImplementedInventory.ofSize(SLOTS, this::onProgramsChanged);

    private final List<PLCProgram> programs = Arrays.asList(new PLCProgram[SLOTS]);
    private final Map<String, PLCProgram> programMap = new HashMap<>();
    private final Object2IntMap<String> symbolToAddress = new Object2IntOpenHashMap<>();

    private int segmentOffset = 1;

    private boolean programsDirty = true;

    private final Set<WeakReference<Listener>> listeners = new HashSet<>();

    public ProgramCabinetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public static Stream<ItemStack> getItems(CompoundTag blockNbt)
    {
        ImplementedInventory inventory = ImplementedInventory.ofSize(SLOTS);
        inventory.readNbt(blockNbt.getCompound("inventory"));
        return inventory.getItems().stream().filter(s -> !s.isEmpty());
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.put("inventory", inventory.writeNbt(new CompoundTag()));

        ListTag l = new ListTag();
        for (PLCProgram t : programs)
        {
            if (t != null)
                l.add(t.writeNbt(new CompoundTag()));
            else
                l.add(new CompoundTag());
        }
        nbt.put("programs", l);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        inventory.readNbt(nbt.getCompound("inventory"));
        symbolToAddress.clear();
        var list = nbt.getList("programs", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            var c = list.getCompound(i);
            if (c.isEmpty())
            {
                programs.set(i, null);
            }
            else
            {
                var program = new PLCProgramImpl(this::getLevel, c);
                programs.set(i, program);
                symbolToAddress.putAll(program.exportedSymbols());
            }
        }
    }

    public ImplementedInventory getInventory()
    {
        return inventory;
    }

    private void onProgramsChanged()
    {
        setChanged();
        programsDirty = true;
        notifyListeners(Reason.SEGMENT_CHANGED);
    }

    public void updatePrograms()
    {
        if (!programsDirty)
            return;

        programsDirty = false;

        programMap.clear();
        for (int i = 0; i < SLOTS; i++)
        {
            ItemStack slot = inventory.getItem(i);
            var component = NMComponents.PROGRAM_STORAGE.getNullable(slot);
            if (component != null) // Component will be null if the slot is empty
            {
                var program = component.getProgram(this::getLevel);
                programs.set(i, program);
                programMap.put(component.getName(), program);

                int finalI = i;
                program.exportedSymbols().forEach((name, offset) ->
                {
                    // Relocate symbol to its real position in memory spacee
                    int addr = (segmentOffset + finalI) << (32 - PLCProgramResolver.SEGMENT_BITS);
                    addr |= offset;
                    symbolToAddress.put(name, addr);
                });
            }
            else
            {
                programs.set(i, null);
            }
        }
    }

    public PLCProgram getProgram(String name)
    {
        return programMap.getOrDefault(name, PLCProgram.EMPTY);
    }

    public Instruction getInstruction(int segment, int address)
    {
        updatePrograms();
        var program = programs.get(segment - segmentOffset);
        if (program == null)
            return Instruction.EMPTY;

        return program.get(address);
    }

    /// Gives the real address of the symbol
    public int findSymbol(String symbol)
    {
        updatePrograms();
       return symbolToAddress.getOrDefault(symbol, -1);
    }

    public void saveToItem(ItemStack stack)
    {
        CompoundTag createdNbt = saveWithoutMetadata();
        createdNbt.remove("programs");
        BlockItem.setBlockEntityData(stack, getType(), createdNbt);
    }

    public int segmentOffset()
    {
        return segmentOffset;
    }

    public Collection<Segment> segments()
    {
        updatePrograms();
        List<Segment> segments = new ObjectArrayList<>();
        for (int i = 0; i < programs.size(); i++)
        {
            var program = programs.get(i);
            if (program == null)
                continue;

            var segment = new ProgramMemorySegment(program, segmentOffset + i);
            segments.add(segment);
        }
        return segments;
    }

    @Override
    public void addListener(Listener listener)
    {
        listeners.add(new WeakReference<>(listener));
    }

    private void notifyListeners(Reason reason)
    {
        listeners.forEach(r ->
        {
            var listener = r.get();
            if (listener != null)
            {
                if (!listener.isRemoved())
                    listener.notify(reason);
                else
                    r.clear();
            }
        });

        listeners.removeIf(r -> r.get() == null);
    }

    public boolean isEmpty()
    {
        return inventory.isEmpty();
    }

    public void onRemoved()
    {
        notifyListeners(Reason.REMOVED);
    }

    public void setMemoryOffset(int offset)
    {
        this.segmentOffset = Math.max(1, offset);
        onProgramsChanged();
    }

    record ProgramMemorySegment(PLCProgram program, int segmentOffset) implements ProgramMemoryModule.Segment
    {
        @Override
        public Instruction getInstruction(int offset)
        {
            return program.get(offset);
        }

        @Override
        public Stream<ObjectIntPair<String>> getRelocatedSymbols()
        {
            int msb = (segmentOffset) << (32 - PLCProgramResolver.SEGMENT_BITS);

            return program.exportedSymbols().entrySet().stream().map(e ->
            {
                int offset = e.getValue();
                return ObjectIntPair.of(e.getKey(), msb | offset);
            });
        }
    }
}
