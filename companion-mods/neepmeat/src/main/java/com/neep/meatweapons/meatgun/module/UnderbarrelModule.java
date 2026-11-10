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

package com.neep.meatweapons.meatgun.module;

import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import org.joml.Matrix4f;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class UnderbarrelModule extends AbstractMeatgunModule
{
    private final ModuleSlot upSlot;
    private final ModuleSlot downSlot;

    public UnderbarrelModule(RootModuleHolder.Listener listener)
    {
        super(listener);
        upSlot = new SimpleModuleSlot(this.listener, new Matrix4f().translate(0, 0, -3 / 16f));
        downSlot = new SimpleModuleSlot(this.listener, new Matrix4f()
                .scale(0.999f, 1f, 1f)
                .translate(0, 0, -0.001f)
                .rotateZ(Mth.PI)
                .translate(0, 4 / 16f, -3 / 16f));
        setSlots(List.of(upSlot, downSlot));
    }

    public UnderbarrelModule(RootModuleHolder.Listener listener, CompoundTag nbt)
    {
        this(listener);
        readNbt(nbt, null);
    }

    @Override
    public void trigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        if (id == 1)
            upSlot.get().trigger(world, player, stack, 1, pitch, yaw, handType, hand);

        if (id == 2)
            downSlot.get().trigger(world, player, stack, 1, pitch, yaw, handType, hand);
    }

    @Override
    public void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        if (id == 1)
            upSlot.get().tickTrigger(world, player, stack, 1, pitch, yaw, handType, hand);

        if (id == 2)
            downSlot.get().tickTrigger(world, player, stack, 1, pitch, yaw, handType, hand);
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.UNDERBARREL;
    }
}
