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

package com.neep.meatlib.mixin;

import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AnvilMenu.class)
public class AnvilScreenHandlerMixin
{
//    @ModifyVariable(method = "updateResult",
//            name = "bl4",
//            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/enchantment/Enchantment;isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z")
//    )
//    private boolean allowCustomEnchantments(boolean value, @Local(name = "enchantment") Enchantment enchantment, @Local(name = "itemStack2") ItemStack itemStack)
//    {
//        if (itemStack.getItem() instanceof CustomEnchantable customEnchantable)
//        {
//            if (customEnchantable.injectVanilla().contains(enchantment))
//                return true;
//        }
//        return value;
//    }
}
