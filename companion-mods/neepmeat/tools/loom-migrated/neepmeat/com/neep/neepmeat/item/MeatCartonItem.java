package com.neep.neepmeat.item;

import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class MeatCartonItem extends BaseItem
{
    public MeatCartonItem(TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(tooltipSupplier, settings.food(new FoodProperties.Builder().nutrition(5).build()));
    }

    @Override
    public SoundEvent getEatingSound()
    {
        return SoundEvents.HONEYCOMB_WAX_ON;
    }

    @Override
    public SoundEvent getDrinkingSound()
    {
        return SoundEvents.HONEYCOMB_WAX_ON;
    }

    @Override
    public int getUseDuration(ItemStack stack)
    {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.EAT;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        return ItemUtils.startUsingInstantly(world, user, hand);
    }
}
