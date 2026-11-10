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

package com.neep.neepmeat.entity.scutter;

import com.neep.neepmeat.entity.compressor_scutter.FollowingScutter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class ScutterEntity extends PathfinderMob implements FollowingScutter
{
    public ScutterEntity(EntityType<? extends ScutterEntity> type, Level world)
    {
        super(type, world);
    }

    @Override
    protected boolean shouldStayCloseToLeashHolder()
    {
        return false;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state)
    {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return super.getAddEntityPacket();
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet)
    {
        super.recreateFromPacket(packet);
    }

    @Override
    public boolean requiresCustomPersistence()
    {
        return true;
    }

    @Override
    public boolean canBeSeenAsEnemy()
    {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount)
    {
        // Respond to /kill
        if (amount == Float.MAX_VALUE)
        {
            remove(RemovalReason.KILLED);
            return true;
        }

        return false;
    }

    public void pickUp()
    {
        playSound(SoundEvents.ITEM_PICKUP, 1, 1);

        ItemStack stack = getPickResult();
        if (stack != null)
        {
            CompoundTag nbt = new CompoundTag();
            writeStackNbt(nbt);

            if (!nbt.isEmpty())
                stack.setTag(nbt);

            if (getCustomName() != null)
                stack.setHoverName(getCustomName());

            spawnAtLocation(stack);
        }

        remove(RemovalReason.DISCARDED);
    }

    protected void writeStackNbt(CompoundTag nbt) { }

    public void readStackNbt(CompoundTag nbt) { }

    @Override
    public @Nullable ItemStack getPickResult()
    {
        return null;
    }

    public boolean isStatic()
    {
        return false;
    }

    @Override
    public @Nullable LivingEntity getOwner()
    {
        return null;
    }

    @Override
    public boolean shouldShowName()
    {
        return true;
    }
}
