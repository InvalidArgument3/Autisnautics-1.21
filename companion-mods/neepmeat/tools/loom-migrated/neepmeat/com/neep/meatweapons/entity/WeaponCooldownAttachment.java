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

package com.neep.meatweapons.entity;

import com.neep.meatlib.attachment.player.MeatPlayerEntity;
import com.neep.meatlib.attachment.player.PlayerAttachment;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;

// A minimal implementation of ItemCooldownManager that works for individual stacks
public class WeaponCooldownAttachment implements PlayerAttachment
{
    public static String ID = "meatweapons:weapon_cooldown";
    private final Player player;

    protected Long2ObjectOpenHashMap<Entry> map = new Long2ObjectOpenHashMap<>();
    protected int time;

    public static WeaponCooldownAttachment get(Player player)
    {
        return ((MeatPlayerEntity) player).meatlib$getAttachmentManager().getAttachment(ID);
    }

    public WeaponCooldownAttachment(Player player)
    {

        this.player = player;
    }

    @Override
    public void tickAttachment()
    {
        ++time;
        ObjectIterator<Long2ObjectMap.Entry<Entry>> it = map.long2ObjectEntrySet().fastIterator();
        while (it.hasNext())
        {
            if (it.next().getValue().endTime <= time) it.remove();
        }
    }

    public boolean isCoolingDown(ItemStack stack, int trigger)
    {
        long id = getStackId(stack, trigger, player.level());
        Entry entry = map.get(getStackId(stack, trigger, player.level()));
        return entry != null && entry.endTime > time;
    }

    // Returns a unique ID for each stack (hopefully).
    public static long getStackId(ItemStack stack, int trigger, Level world)
    {
        if (world instanceof ServerLevel serverWorld)
        {
            return GeoItem.getOrAssignId(stack, serverWorld);
        }
        return GeoItem.getId(stack) + trigger;
//        return Objects.hash(stack.getItem().getTranslationKey(), stack.getNbt(), stack.getCount(), trigger);
    }

    public void set(ItemStack stack, int trigger, int cooldown)
    {
        map.put(getStackId(stack, trigger, player.level()), new Entry(time, time + cooldown));
    }

    protected static class Entry
    {
        final int startTime;
        final int endTime;

        Entry(int start, int end)
        {
            this.startTime = start;
            this.endTime = end;
        }

    }
}