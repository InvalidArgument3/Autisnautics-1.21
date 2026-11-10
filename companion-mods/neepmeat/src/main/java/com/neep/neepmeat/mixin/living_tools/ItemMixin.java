package com.neep.neepmeat.mixin.living_tools;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.neep.neepmeat.item.util.LivingTools;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public class ItemMixin
{
    @ModifyReturnValue(method = "getItemBarColor", at = @At("RETURN"))
    protected int onGetItemBarColor(int original, @Local(argsOnly = true) ItemStack stack)
    {
        Integer col = LivingTools.getBarCol(stack);
        if (col != null)
        {
            return col;
        }
        return original;
    }

    @ModifyReturnValue(method = "isItemBarVisible", at = @At("RETURN"))
    protected boolean onGetItemBarVisible(boolean original, @Local(argsOnly = true) ItemStack stack)
    {
        return original || LivingTools.isBarVisible(stack);
    }
}
