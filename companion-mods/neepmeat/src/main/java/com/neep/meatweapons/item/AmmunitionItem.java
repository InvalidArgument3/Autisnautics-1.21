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

package com.neep.meatweapons.item;

import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatweapons.implant.MagazineOrganImplant;
import com.neep.meatweapons.meatgun.AmmunitionProvider;
import com.neep.neepmeat.implant.entity.PlayerImplantManager;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.init.NMSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AmmunitionItem extends BaseItem
{
    public AmmunitionItem(TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(tooltipSupplier, settings);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.EAT;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        PlayerImplantManager manager = (PlayerImplantManager) NMComponents.IMPLANT_MANAGER.get(user);
        ItemStack itemStack = user.getItemInHand(hand);

        @Nullable MagazineOrganImplant implant = manager.getImplant(MagazineOrganImplant.ID);

        if (implant != null)
        {
            if (implant.canConsume(this, itemStack))
            {
                user.startUsingItem(hand);
                return InteractionResultHolder.consume(itemStack);
            }
            else
            {
                return InteractionResultHolder.fail(itemStack);
            }
        }
        else
        {
            return InteractionResultHolder.pass(user.getItemInHand(hand));
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity)
    {
        return 10;
    }

    @Override
    public SoundEvent getEatingSound()
    {
        return NMSounds.ROCK_DRILL.get();
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user)
    {
        if (user instanceof Player player
                && NMComponents.IMPLANT_MANAGER.getNullable(user) instanceof PlayerImplantManager manager)
        {
            Inventory inventory = player.getInventory();
            AmmunitionProvider.Context context = new AmmunitionProvider.InventoryContext(player.getInventory(), inventory.selected);
            @Nullable AmmunitionProvider provider = AmmunitionProvider.LOOKUP.find(stack, context);
            @Nullable MagazineOrganImplant implant = manager.getImplant(MagazineOrganImplant.ID);

            if (provider != null && implant != null)
            {
                return implant.consume(world, stack, provider);
            }
        }

        return super.finishUsingItem(stack, world, user);
    }
}
