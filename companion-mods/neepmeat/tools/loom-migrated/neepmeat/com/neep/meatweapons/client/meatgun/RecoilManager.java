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

package com.neep.meatweapons.client.meatgun;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatlib.client.AnimationTicks;
import com.neep.meatweapons.network.MeatgunNetwork;
import org.apache.commons.collections4.map.AbstractReferenceMap;
import org.apache.commons.collections4.map.ReferenceMap;

import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class RecoilManager
{
    private static final Map<UUID, RecoilManager> INSTANCES = new ReferenceMap<>(AbstractReferenceMap.ReferenceStrength.HARD, AbstractReferenceMap.ReferenceStrength.SOFT, true);

    public MeatgunNetwork.RecoilDirection direction;

    public float amount;
    public float horAmount;
    public float returnSpeed;
    public float horReturnSpeed;

    public float shakeMagnitude;
    public float shakeReturnSpeedTicks; // reciprocal ticks

    public static RecoilManager getOrCreate(UUID uuid)
    {
        return INSTANCES.computeIfAbsent(uuid, u -> new RecoilManager());
    }

    public void set(MeatgunNetwork.RecoilDirection direction, float amount, float horAmount, float returnSpeed, float horReturnSpeed)
    {
        this.direction = direction;
        this.amount = amount;
        this.horAmount = horAmount;
        this.returnSpeed = returnSpeed;
        this.horReturnSpeed = horReturnSpeed;
    }

    public void step(Minecraft client)
    {
        // Recoil
        float lastFrame = !client.isPaused() ? client.getDeltaFrameTime() : 0;

        float prevHorAmount = horAmount;
        horAmount = horAmount - Math.signum(horAmount) * horReturnSpeed * lastFrame;
        if (Math.signum(prevHorAmount) != Math.signum(horAmount))
            horReturnSpeed = 0;

        float prevAmount = amount;
        amount = amount - Math.signum(amount) * returnSpeed * lastFrame;
        if (Math.signum(prevAmount) != Math.signum(amount))
        {
            direction = MeatgunNetwork.RecoilDirection.UP;
            returnSpeed = 0;
        }

        // Shake
        shakeMagnitude = Mth.clamp(shakeMagnitude - (lastFrame) * shakeReturnSpeedTicks, 0f, 1f);
    }

    public void shakeTranslate(PoseStack matrices)
    {
        if (!Minecraft.getInstance().isPaused() && shakeMagnitude > 0)
        {
            float t = AnimationTicks.getRenderTime();
            float f1 = (float) (shakeMagnitude * Math.sin(t * 1.2f) * Math.cos(t * 1.8f));
            float f2 = (float) (shakeMagnitude * Math.cos(t * 2) * Math.sin(t * 4f));

            matrices.translate(f1, f2, 0);
        }
    }

    public void setShake(float magnitude, float returnSpeed)
    {
        shakeMagnitude = magnitude;
        shakeReturnSpeedTicks = returnSpeed;
    }
}
