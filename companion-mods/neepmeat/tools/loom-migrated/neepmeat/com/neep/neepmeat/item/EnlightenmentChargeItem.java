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

package com.neep.neepmeat.item;

import com.neep.neepmeat.enlightenment.EnlightenmentManager;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.init.NMSounds;
import java.util.List;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

public class EnlightenmentChargeItem extends Item
{
    public EnlightenmentChargeItem(Properties settings)
    {
        super(settings);
        DispenserBlock.registerBehavior(this, EnlightenmentChargeItem::dispense);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.NONE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        ItemStack itemStack = user.getItemInHand(hand);
        if (world instanceof ServerLevel serverWorld)
        {
            expose(user, serverWorld);
        }

        itemStack.shrink(1);

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        if (context.getPlayer() != null)
        {
            var result = use(context.getLevel(), context.getPlayer(), context.getHand());

            if (result.getResult().consumesAction())
                return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    public static void expose(Player player, ServerLevel world)
    {
        EnlightenmentManager manager = NMComponents.ENLIGHTENMENT_MANAGER.get(player);

        manager.addChronic(20f);

        world.sendParticles(NMParticles.BLOOD_SHOWER, player.getX(), player.getY(), player.getZ(), 100, 1, 1, 1, 0.01f);

        world.playSeededSound(null, player.getX(), player.getY(), player.getZ(), NMSounds.VIVISECTION_COMPLETE, SoundSource.PLAYERS, 0.5f, 1, 0);
    }

    private static ItemStack dispense(BlockSource pointer, ItemStack stack)
    {
        Level world = pointer.getLevel();
        if (world instanceof ServerLevel serverWorld)
        {
            Direction direction = pointer.getBlockState().getValue(DispenserBlock.FACING);

            var unit = direction.getNormal();
            AABB box = new AABB(pointer.getPos()).inflate(1, 1, 1).expandTowards(unit.getX() * 9, unit.getY() * 9, unit.getZ() * 9);

            List<Player> playerEntities = world.getEntitiesOfClass(Player.class, box, p -> true);

            playerEntities.forEach(player -> expose(player, serverWorld));
        }

        stack.shrink(1);
        return stack;
    }
}
