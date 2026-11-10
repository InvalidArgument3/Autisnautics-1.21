/*
 * Copyright (c) MeatWheeze 2026.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.mixin.control_drops;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.neep.neepmeat.interfaces.ILivingEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements ILivingEntity
{
    @Unique
    public boolean neepmeat$dropsLoot = true;

    @Override
    public void setDropsLoot(boolean bl)
    {
        this.neepmeat$dropsLoot = bl;
    }

    @ModifyReturnValue(method = "shouldDropLoot", at = @At(value = "TAIL"))
    boolean modifyDropsLoot(boolean original)
    {
        return original && neepmeat$dropsLoot;
    }
}
