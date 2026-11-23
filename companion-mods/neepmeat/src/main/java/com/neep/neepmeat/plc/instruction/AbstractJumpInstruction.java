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

package com.neep.neepmeat.plc.instruction;

import net.minecraft.nbt.CompoundTag;

public abstract class AbstractJumpInstruction implements Instruction
{
    protected final int offset;

    public AbstractJumpInstruction(int offset)
    {
        this.offset = offset;
    }

    public AbstractJumpInstruction(CompoundTag nbt)
    {
        this(nbt.getInt("offset"));
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putInt("offset", offset);
        return nbt;
    }
}
