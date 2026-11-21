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
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TripleCarouselModule extends AbstractMeatgunModule
{
    public float lerpAngle;

    private int rotateTicks = 5;
    private int selected = 0;
    private long rotateStartTime = 0;

    public TripleCarouselModule(RootModuleHolder.Listener listener)
    {
        super(listener);
        var slot0 = new TCSlot(this.listener, 0);
        var slot1 = new TCSlot(this.listener, 1);
        var slot2 = new TCSlot(this.listener, 2);

        setSlots(List.of(slot0, slot1, slot2));
    }

    public TripleCarouselModule(RootModuleHolder.Listener listener, CompoundTag nbt)
    {
        this(listener);
    }

    @Override
    public void trigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        if (!isRotating(world.getGameTime()))
        {
            if (id == 2)
            {
                rotate(player);
            }
            else
            {
                slots.get(selected).get().trigger(world, player, stack, id, pitch, yaw, handType, hand);
                rotate(player);
            }
        }
    }

    @Override
    public void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        if (!isRotating(world.getGameTime()))
        {
            if (id == 2)
            {
                rotate(player);
            }
            else
            {
                slots.get(selected).get().tickTrigger(world, player, stack, id, pitch, yaw, handType, hand);
                rotate(player);
            }
        }
    }

    private void rotate(Player player)
    {
        rotateStartTime = player.level().getGameTime();
        sync(player);
    }

    public boolean isRotating(long time)
    {
        return time < rotateStartTime + rotateTicks;
    }

    @Override
    public void tick(Player player)
    {
        super.tick(player);
        long time = player.level().getGameTime();

        if (!player.level().isClientSide())
        {
            if (isRotating(time))
            {
            }
            else if (time == rotateStartTime + rotateTicks)
            {
                rotateStartTime = 0;
                selected = (selected + 1) % 3;
                slots.forEach(s -> s.get().setTransform(s.transform()));
                sync(player);
            }
        }
        else if (time == rotateStartTime + rotateTicks)
        {
            selected = (selected + 1) % 3;
        }
    }

    void sync(Player player)
    {

//        PacketByteBuf buf = listener.getBuf(this);
//        buf.writeLong(rotateStartTime);
//        buf.writeInt(selected);
//        listener.send(player, buf);
        listener.markDirty(RootModuleHolder.Reason.SAVE_DATA);
    }

    @Override
    public void receivePacket(FriendlyByteBuf buf)
    {
        this.rotateStartTime = buf.readLong();
        this.selected = buf.readInt();
    }

    public float getRotation(long time, float tickDelta)
    {
        float thing = rotateTicks - (rotateStartTime + rotateTicks - time);
        if (thing > rotateTicks)
            return 0;

        return Mth.clamp((thing + tickDelta) / rotateTicks, 0, 1);
    }

    public int getSelected()
    {
        return selected;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        super.writeNbt(nbt);
//        nbt.putInt("rotate_ticks", rotateTicks);
        nbt.putInt("selected", selected);
        nbt.putLong("rotate_start_time", rotateStartTime);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt, @Nullable Matrix4f global)
    {
        super.readNbt(nbt, global);
//        this.rotateTicks = nbt.getInt("rotate_ticks");
        this.selected = nbt.getInt("selected");
        this.rotateStartTime = nbt.getLong("rotate_start_time");
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.TRIPLE_CAROUSEL;
    }

    private class TCSlot extends SimpleModuleSlot
    {
        private final int index;
        public TCSlot(RootModuleHolder.Listener listener, int index)
        {
            super(listener,
                new Matrix4f().rotateZ(index * 2f / 3f * Mth.PI).translate(0, 4 / 16f, -2 / 16f));
            this.index = index;
        }

        @Override
        public Matrix4f transform()
        {
            return new Matrix4f()
                    .rotateZ((index - selected) * 2f / 3f * Mth.PI)
                    .translate(0, 4 / 16f, -2 / 16f);
        }

        @Override
        public Matrix4f transform(float tickDelta)
        {
            return super.transform(tickDelta);
        }
    }
}
