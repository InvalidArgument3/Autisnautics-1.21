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

/*
 * Decompiled with CFR 0.1.1 (FabricMC 57d88659).
 */
package com.neep.neepmeat.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.neepmeat.entity.keeper.KeeperEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;

@Environment(value=Dist.CLIENT)
public class KeeperEntityModel
extends HumanoidModel<com.neep.neepmeat.entity.keeper.KeeperEntity>
{
    public KeeperEntityModel(ModelPart modelPart)
    {
        super(modelPart);
    }

    public void setAngles(KeeperEntity hostileEntity, float f, float g, float h, float i, float j)
    {
        super.setupAnim(hostileEntity, f, g, h, i, j);
//        this.leftArm.
//        this.leftArm.pitch = -0;
//        CrossbowPosing.meleeAttack(this.leftArm, this.rightArm, this.isAttacking(hostileEntity), this.handSwingProgress, h);
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack matrices)
    {
        super.translateToHand(arm, matrices);
    }

    public boolean isAttacking(KeeperEntity entity)
    {
        return (entity).isAggressive();
    }
}

