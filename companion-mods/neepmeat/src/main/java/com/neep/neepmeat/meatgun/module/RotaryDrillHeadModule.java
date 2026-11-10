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
import com.neep.meatweapons.meatgun.module.MeatgunModules;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RotaryDrillHeadModule extends AbstractMiningMeatgunModule
{
    private final MiningMeatgunModuleType<?> type;
    public float angle;
    public float currentSpeed;

    public RotaryDrillHeadModule(MiningMeatgunModuleType<?> type, RootModuleHolder.Listener listener)
    {
        super(listener, List.of());
        this.type = type;
    }

    public static RotaryDrillHeadModule fromNbt(MiningMeatgunModuleType<?> type, RootModuleHolder.Listener listener, CompoundTag nbt)
    {
        return new RotaryDrillHeadModule(type, listener);
    }

    @Override
    public MiningMeatgunModuleType<? extends MeatgunModule> getType()
    {
        return type;
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
            int i = listener.getHolder().moduleCache().modulesOfType(MeatgunModules.PHAGE_RAY_SPEED_MODIFIER).size();
            var s1 = (state.is(BlockTags.MINEABLE_WITH_PICKAXE) ? getType().miningSpeed() : 1)
                    + ((i > 0) ? (i * i + 1) : 0);
            return s1;

        }

        return 1;
    }

    public boolean canRun()
    {
        if (previousOwner != null)
        {
            var core = new MeatgunCore(listener.getHolder());
            return core.canRun(previousOwner, 1);
        }
        return false;
    }

    @Override
    public boolean isSuitableFor(BlockState state)
    {
        return canRun()
                && state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                && getType().harvestable(state);
    }

    @Override
    public void onBlockBroken(Level world, BlockPos pos, BlockState state, LivingEntity miner, InteractionHand hand)
    {
        if (miner instanceof Player playerEntity && new MeatgunCore(listener.getHolder()).getResource() == AmmunitionType.METABOLIC)
        {
            listener.getHolder().getAmmoOrReload(AmmunitionType.METABOLIC, 1, playerEntity.getInventory(), playerEntity, hand).commit();
        }
    }
}
