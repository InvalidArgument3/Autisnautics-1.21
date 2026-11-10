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

package com.neep.neepmeat.meatgun.module;

import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.meatweapons.meatgun.MeatgunCore;
import com.neep.meatweapons.meatgun.MeatgunModuleCapability;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.meatgun.NMMeatgunModules;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ReciprocatingDrillHeadModule extends AbstractMiningMeatgunModule
{
    public static final int METAB_PER_BLOCK = 1;
    private long lastTime;

    public ReciprocatingDrillHeadModule(RootModuleHolder.Listener listener)
    {
        super(listener, List.of());
    }

    public static ReciprocatingDrillHeadModule fromNbt(RootModuleHolder.Listener listener, CompoundTag nbt)
    {
        return new ReciprocatingDrillHeadModule(listener);
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return NMMeatgunModules.ROCK_DRILL_HEAD;
    }

    @Override
    public Set<MeatgunModuleCapability> getCapabilities()
    {
        return MeatgunModuleCapability.NONE;
    }

    @Override
    public float getMiningSpeedMultiplier(BlockState state)
    {
        if (canRun())
        {
            return state.is(NMTags.ROCK_DRILL_MINEABLE) ? 50 : 1;
        }

        return 1;
    }

    public boolean canRun()
    {
        if (previousOwner != null)
        {
            var core = new MeatgunCore(listener.getHolder());
            return core.canRun(previousOwner, METAB_PER_BLOCK);
        }
        return false;
    }

    @Override
    public boolean isSuitableFor(BlockState state)
    {
        return canRun() && state.is(NMTags.ROCK_DRILL_MINEABLE);
    }

    @Override
    public void onBlockBroken(Level world, BlockPos pos, BlockState state, LivingEntity miner, InteractionHand hand)
    {
        long time = world.getGameTime();
        if (time - lastTime >= 2)
        {
            world.playSound(null, miner, NMSounds.ROCK_DRILL.get(), SoundSource.PLAYERS,
                    (float) miner.getRandom().triangle(1, 0.1),
                    (float) miner.getRandom().triangle(1, 0.04));

            lastTime = time;
        }

        if (miner instanceof Player playerEntity && new MeatgunCore(listener.getHolder()).getResource() == AmmunitionType.METABOLIC)
        {
            listener.getHolder().getAmmoOrReload(AmmunitionType.METABOLIC, METAB_PER_BLOCK, playerEntity.inventory, playerEntity, hand).commit();
        }
    }
}
