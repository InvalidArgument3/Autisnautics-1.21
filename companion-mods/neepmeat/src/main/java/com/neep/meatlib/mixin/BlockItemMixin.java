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

package com.neep.meatlib.mixin;

import com.neep.meatlib.api.block.NormalPlacementSoundBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin
{
    @Shadow
    public abstract Block getBlock();

    // Placement sound pitch is multiplied by 0.8 for some mysterious reason, which makes the sound design process even more painful.
    @ModifyConstant(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", constant = @Constant(floatValue = 0.8F))
    float modifyPlacementSoundPitch(float constant)
    {
        if (getBlock() instanceof NormalPlacementSoundBlock)
            return 1;

        return constant;
    }
}
