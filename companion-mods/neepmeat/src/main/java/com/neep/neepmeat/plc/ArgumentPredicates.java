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

package com.neep.neepmeat.plc;

import com.neep.neepmeat.api.plc.instruction.ArgumentPredicate;
import com.neep.neepmeat.api.plc.instruction.InstructionException;
import com.neep.neepmeat.plc.component.MutateInPlace;
import com.neep.neepmeat.plc.robot.PLCActuator;
import com.neep.meatlib.transfer.FluidStorage;
import com.neep.meatlib.transfer.ItemStorage;

public class ArgumentPredicates
{
    public static final ArgumentPredicate ANY = ((world, argument) -> true);

    public static final ArgumentPredicate IS_ITEM_STORAGE = (world, argument) ->
    {
        var be = world.getBlockEntity(argument.pos());
        if (ItemStorage.SIDED.find(world, argument.pos(), argument.face()) != null)
            return true;

        throw new InstructionException("No item storage at " + argument.pos().getX() + ", " + argument.pos().getY() + ", " + argument.pos().getZ() + ", " + argument.face().name());
    };

    public static final ArgumentPredicate IS_FLUID_STORAGE = (world, argument) ->
    {
        if (FluidStorage.SIDED.find(world, argument.pos(), argument.face()) != null)
            return true;

        throw new InstructionException("No fluid storage at " + argument.pos().getX() + ", " + argument.pos().getY() + ", " + argument.pos().getZ() + ", " + argument.face().name());
    };

    public static final ArgumentPredicate IS_ITEM_MIP = (world, argument) ->
    {
        if (MutateInPlace.ITEM.find(world, argument.pos(), null) != null)
            return true;

        throw new InstructionException("No item workbench at " + argument.pos().getX() + ", " + argument.pos().getY() + ", " + argument.pos().getZ());
    };

    public static final ArgumentPredicate IS_ENTITY_MIP = (world, argument) ->
    {
        if (MutateInPlace.ENTITY.find(world, argument.pos(), null) != null)
            return true;

        throw new InstructionException("No entity workbench at " + argument.pos().getX() + ", " + argument.pos().getY() + ", " + argument.pos().getZ());
    };

    public static final ArgumentPredicate IS_ITEM_ENTITY_MIP = (world, argument) ->
    {
        if (
                MutateInPlace.ENTITY.find(world, argument.pos(), null) != null
                || MutateInPlace.ITEM.find(world, argument.pos(), null) != null)
            return true;

        throw new InstructionException("No entity workbench at " + argument.pos().getX() + ", " + argument.pos().getY() + ", " + argument.pos().getZ());
    };

    public static ArgumentPredicate IS_ACTUATOR = (world, argument) ->
    {
        if (world.getBlockEntity(argument.pos()) instanceof PLCActuator.Provider)
            return true;

        throw new InstructionException("No PLC actuator at " + argument.pos().getX() + ", " + argument.pos().getY() + ", " + argument.pos().getZ());
    };
}
