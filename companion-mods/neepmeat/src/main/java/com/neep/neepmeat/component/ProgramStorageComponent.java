package com.neep.neepmeat.component;

import com.neep.neepmeat.api.plc.program.PLCProgram;
import com.neep.neepmeat.plc.program.PLCProgramImpl;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.function.Supplier;

public class ProgramStorageComponent extends ItemComponent
{
    public ProgramStorageComponent(ItemStack stack)
    {
        super(stack);
    }

    public void setSource(String source, PLCProgram program)
    {
        String prevName = getName();

        putString("source", source);
        putCompound("program", program.writeNbt(new CompoundTag()));

        if (!prevName.isEmpty())
            setName(prevName);
    }

    public String getSource()
    {
        return getString("source");
    }

    public PLCProgram getProgram(Supplier<Level> worldSupplier)
    {
        var program = new PLCProgramImpl(worldSupplier);
        program.readNbt(getCompound("program"));
        return program;
    }

    public CompoundTag getProgramNbt()
    {
        return getCompound("program").copy();
    }

    public String getName()
    {
        return PLCProgramImpl.getName(getCompound("program"));
    }

    public void setName(String name)
    {
        PLCProgramImpl program = new PLCProgramImpl(() -> null);
        program.readNbt(getProgramNbt());
        program.setName(name);
        putCompound("program", program.writeNbt(new CompoundTag()));
    }

    public boolean isEmpty()
    {
        return getCompound("program").isEmpty();
    }
}
