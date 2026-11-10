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

package com.neep.neepmeat.entity.bovine_horror;

import com.neep.meatweapons.particle.MWGraphicsEffects;
import com.neep.neepmeat.entity.goal.AnimatedGoal;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.init.NMSounds;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BHPhaseActionGoal extends AnimatedGoal<BHPhaseActionGoal>
{
    protected final BovineHorrorEntity mob;

    protected final Sequence<BHPhaseActionGoal> orient = (action, counter) ->
    {
        var mob = action.mob;
        mob.setVisibility(1);
        mob.setInvulnerable(true);
        if (counter > 20)
        {
            getMob().setVisibility(1);
            PlayerLookup.tracking(getMob()).forEach(p ->
            {
                MWGraphicsEffects.syncBeamEffect(p, MWGraphicsEffects.BEAM, getMob().level(),
                        getMob().position().add(0, 40, 0), getMob().position(), Vec3.ZERO, 1.8f, 60);
            });
            mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(), NMSounds.BH_PHASE2, SoundSource.PLAYERS, 5, 0.7f);
            setSequence(action.particles);
        }
    };

    protected final Sequence<BHPhaseActionGoal> particles = (action, counter) ->
    {
        getMob().setVisibility(1);
        if (counter > 60)
        {
            if (getMob().level() instanceof ServerLevel serverWorld)
            {
                Vec3 mobPos = getMob().position();
                serverWorld.sendParticles(NMParticles.MEAT_BIT, mobPos.x, mobPos.y + 2, mobPos.z, 50, 3, 3, 3, 0.1);

                serverWorld.explode(action.mob, serverWorld.damageSources().mobAttack(action.mob), new Behaviour(),
                        mobPos.x, mobPos.y, mobPos.z, 3, false, Level.ExplosionInteraction.MOB);
            }
            markFinished();
            getMob().setInvulnerable(false);
            getMob().updateGoals = true;
        }
    };

    public BHPhaseActionGoal(BovineHorrorEntity mob)
    {
        this.mob = mob;
    }

    @Override
    public void start()
    {
        super.start();
        setSequence(orient);
    }

    @Override
    public boolean canUse()
    {
        return !finished;
    }

    protected BovineHorrorEntity getMob()
    {
        return mob;
    }

    private static class Behaviour extends ExplosionDamageCalculator
    {
        @Override
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter world, BlockPos pos, BlockState state, float power)
        {
            return false;
        }
    }
}
