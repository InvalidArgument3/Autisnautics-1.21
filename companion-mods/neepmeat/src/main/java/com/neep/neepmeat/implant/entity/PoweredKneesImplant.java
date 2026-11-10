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

package com.neep.neepmeat.implant.entity;

import com.neep.neepmeat.NeepMeat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class PoweredKneesImplant implements EntityImplant
{
    public static final float ACTIVE_STEP_HEIGHT = 1.101f;

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "extra_knee");

    protected final Player player;
    private float jumpPower;

    public PoweredKneesImplant(Entity player)
    {
        this.player = (Player) player;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putFloat("jump_power", jumpPower);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        this.jumpPower = nbt.getFloat("jump_power");
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void tick(Runnable remove)
    {
        if (player.level().getGameTime() % 40 == 0)
        {
            if (player.maxUpStep() < ACTIVE_STEP_HEIGHT)
            {
                player.setMaxUpStep(ACTIVE_STEP_HEIGHT);
            }
        }
    }

    @Override
    public void clientTick()
    {
        if (player.level().getGameTime() % 40 == 0)
        {
            if (player.maxUpStep() < ACTIVE_STEP_HEIGHT)
            {
                player.setMaxUpStep(ACTIVE_STEP_HEIGHT);
            }
        }
    }

    @Override
    public void onInstall()
    {
        if (player.maxUpStep() < ACTIVE_STEP_HEIGHT)
            player.setMaxUpStep(ACTIVE_STEP_HEIGHT);
    }

    @Override
    public void onUninstall()
    {
        if (player.maxUpStep() == ACTIVE_STEP_HEIGHT)
            player.setMaxUpStep(0.6f);
    }

    public float getJumpPower()
    {
        return jumpPower;
    }

    public void setJumpPower(float jumpPower)
    {
        this.jumpPower = Mth.clamp(jumpPower, 0, 0.2f);
    }

    public float modifyJumpBoost(float original)
    {
        return original + jumpPower;
    }
}
