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

package com.neep.neepmeat.entity.skyworm;

import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.init.NMSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class SkywormDropEggAction extends SkywormAction<SkywormDropEggAction>
{
    public SkywormDropEggAction(SkywormEntity mob, double targetY)
    {
        super(mob);
        this.targetY = targetY;
        setSequence(delay);
    }

    private final Sequence<SkywormDropEggAction> delay = (t, counter) ->
    {
        if (counter == 0)
            getWorld().playSound(null, getX(), targetY, getZ(), NMSounds.SKYWORM_DESCEND.get(), SoundSource.NEUTRAL, 16, 1);

        if (counter > 40)
        {
            setSequence(t.descend);
        }
    };

    private final Sequence<SkywormDropEggAction> descend = (t, counter) ->
    {
        double error = targetY - getY();

        float speed = (float) (Mth.clamp(Math.abs(error) / 10, 0.04f, 3f));

        double newY = getY() + Mth.sign(error) * speed;
        if (Math.abs(error) <= speed)
            newY = targetY;

        mob.setPosRaw(getX(), newY, getZ());

        if (getY() == targetY)
        {
            mob.setBulge(mob.getLength() - 1);
            setSequence(t.bulge);
        }
    };

    private final Sequence<SkywormDropEggAction> bulge = (t, counter) ->
    {
        mob.setBulgeDirection(-1);
        if (counter == 0)
            mob.setBulge(mob.getLength());

        if (getWorld() instanceof ServerLevel serverWorld)
        {
            float twist = (float) (getY() / 10f * Mth.PI / 5f);
            float wobbleX =
//                        MathHelper.sin(twist) * 0.4f
                    +Mth.sin(mob.tickCount / 10f) * 0.3f;
            float wobbleZ =
//                        MathHelper.cos(twist) * 0.4f
                    +Mth.cos(mob.tickCount / 15f) * 0.3f;
            serverWorld.sendParticles(NMParticles.BLOOD_SHOWER, getX() + wobbleX, getY(), getZ() + wobbleZ, 10, 0.1, 0.1, 0.1, 00);
        }

        if (mob.getBulge() <= -1)
        {
            setSequence(t.dropEgg);
            mob.setBulge(-1);
        }
    };

    private final Sequence<SkywormDropEggAction> dropEgg = (t, counter) ->
    {
        if (counter == 0)
        {
            getWorld().playSound(null, getX(), targetY, getZ(), NMSounds.SKYWORM_DISPENSE.get(), SoundSource.NEUTRAL, 16, 1);

            getWorld().addFreshEntity(
                    new ItemEntity(
                            getWorld(), getX(), getY(), getZ(), new ItemStack(NMBlocks.INTEGRATOR_EGG), 0, -0.1, 0));

            if (getWorld() instanceof ServerLevel serverWorld)
            {
                serverWorld.sendParticles(NMParticles.MEAT_BIT, getX(), getY(), getZ(), 20, 1, 1, 1, 0.1);
                serverWorld.sendParticles(NMParticles.BLOOD_SHOWER, getX(), getY(), getZ(), 50, 0.1, 0.1, 0.1, 0.1);
            }
        }

        if (counter >= 40)
        {
            getWorld().playSound(null, getX(), targetY, getZ(), NMSounds.SKYWORM_ASCEND.get(), SoundSource.NEUTRAL, 16, 1);

            targetY = getY() + 200;
            setSequence(t.ascend);
        }
    };

    private final Sequence<SkywormDropEggAction> ascend = (t, counter) ->
    {
        float speed = 3;

        double error = targetY - getY();
        double newY = getY() + Mth.sign(error) * speed;
        if (Math.abs(error) <= speed)
            newY = targetY;

        mob.setPosRaw(getX(), newY, getZ());

        if (getY() == targetY)
        {
            mob.discard();
        }
    };
}
