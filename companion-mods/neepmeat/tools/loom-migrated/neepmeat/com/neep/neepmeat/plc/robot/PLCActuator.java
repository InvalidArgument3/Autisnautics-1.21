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

package com.neep.neepmeat.plc.robot;

import com.neep.neepmeat.api.plc.PLC;
import com.neep.meatlib.neepasm.NeepASM;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public interface PLCActuator
{
    BlockPos getBasePos();

    void spawnItem(@Nullable ResourceAmount<ItemVariant> stored);

    @Nullable
    default ResourceAmount<ItemVariant> getStored(PLC plc)
    {
        error(plc);
        return null;
    }

    default void setStored(PLC plc, @Nullable ResourceAmount<ItemVariant> stored)
    {
        error(plc);
    }

    default void dumpStored(PLC plc)
    {
        spawnItem(getStored(plc));
    }

    default boolean reachedTarget(PLC plc)
    {
        error(plc);
        return true;
    }

    default void setTarget(PLC plc, @Nullable BlockPos target) throws NeepASM.RuntimeException
    {
        error(plc);
    }

    private void error(PLC plc)
    {
        if (plc != null)
            plc.raiseError(new PLC.Error("Actuator does not support action"));
    }

    default double getX() { return 0; }
    default double getY() { return 0; }
    default double getZ() { return 0; }

    PLCActuator EMPTY  = new PLCRobot()
    {
        @Override
        public void setImplantTarget(PLC plc, Vec3 implantSite, Entity target)
        {

        }

        @Override
        public void setTarget(PLC plc, @Nullable BlockPos target)
        {

        }

        @Override
        public boolean reachedTarget(PLC plc)
        {
            return false;
        }

        @Override
        public void spawnItem(@Nullable ResourceAmount<ItemVariant> stored)
        {

        }

        @Override
        public void dumpStored(PLC plc)
        {

        }

        @Override
        public void setStored(PLC plc, @Nullable ResourceAmount<ItemVariant> stored)
        {

        }

        @Override
        public BlockPos getBasePos()
        {
            return BlockPos.ZERO;
        }

        @Override
        public @Nullable ResourceAmount<ItemVariant> getStored(PLC plc)
        {
            return null;
        }

        @Override
        public double getX()
        {
            return 0;
        }

        @Override
        public double getY()
        {
            return 0;
        }

        @Override
        public double getZ()
        {
            return 0;
        }

        @Override
        public boolean actuatorRemoved()
        {
            return true;
        }
    };

    boolean actuatorRemoved();

    default EnumSet<Capability> capabilities()
    {
        return EnumSet.noneOf(Capability.class);
    }

    interface Provider
    {
        PLCActuator getPlcActuator();
    }

    enum Capability
    {
        MOVE_ROBOT,
        ROUTE_ITEM,
    }
}
