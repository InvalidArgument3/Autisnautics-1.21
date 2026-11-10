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

package com.neep.neepmeat.implant.entity;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class ExplosiveImplant implements EntityImplant
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "explosive");

    private final Entity entity;
    private int fuse = 0;
    private int fuseTime = 0;

    private boolean exploded = false;

    public ExplosiveImplant(Entity entity)
    {
        this.entity = entity;
    }

    public static boolean onInteract(Player player, InteractionHand hand, Entity entity, ItemStack stack, RandomSource random)
    {
        ImplantManager manager = NMComponents.IMPLANT_MANAGER.getNullable(entity);
        if (manager != null)
        {
            ExplosiveImplant implant = manager.getImplant(ExplosiveImplant.ID);
            if (implant != null && stack.is(ItemTags.CREEPER_IGNITERS))
            {
                SoundEvent soundEvent = stack.is(Items.FIRE_CHARGE) ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE;
                entity.level().playSound(player, entity.getX(), entity.getY(), entity.getZ(), soundEvent, entity.getSoundSource(), 1.0F, random.nextFloat() * 0.4F + 0.8F);

                implant.setFuseTime(30);

                if (!stack.isDamageableItem())
                {
                    stack.shrink(1);
                }
                else
                {
                    stack.hurtAndBreak(1, player, playerx -> playerx.broadcastBreakEvent(hand));
                }

                return true;
            }
        }

        return false;
    }

    public void setFuseTime(int time)
    {
        if (time != 0)
        {
            entity.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
            entity.gameEvent(GameEvent.PRIME_FUSE);
        }
        this.fuseTime = time;
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void tick(Runnable remove)
    {
        if (fuseTime > 0)
        {
            fuse++;
            if (fuse >= fuseTime)
            {
                exploded = true;
                explode();
                remove.run();
            }
        }
    }

    @Override
    public void onDeath(Runnable remove)
    {
        if (!exploded)
        {
            exploded = true;
            explode();
            remove.run();
        }
    }

    private void explode()
    {
        entity.level().explode(null, entity.getX(), entity.getY(), entity.getZ(), 3, Level.ExplosionInteraction.MOB);
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {

    }
}