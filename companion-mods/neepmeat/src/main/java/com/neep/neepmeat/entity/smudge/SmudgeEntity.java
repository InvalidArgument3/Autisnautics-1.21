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

package com.neep.neepmeat.entity.smudge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class SmudgeEntity extends Entity
{
    protected float angle;
    protected float scale = 1;

    private final RandomSource random = RandomSource.create();

    public SmudgeEntity(EntityType<?> type, Level world)
    {
        super(type, world);

        this.angle = random.nextFloat() * Mth.TWO_PI;
        this.scale = 5 + random.nextFloat() * 10;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder)
    {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {

    }

    public int getCol()
    {
        return 0xFFFFFFFF;
    }

    public float getAngle()
    {
        return angle;
    }

    public float getSize(float tickDelta)
    {
        return scale;
    }
}
