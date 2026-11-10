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

package com.neep.meatweapons.implant;

import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.item.AmmunitionItem;
import com.neep.meatweapons.meatgun.AmmunitionProvider;
import com.neep.meatweapons.meatgun.AmmunitionType;
import com.neep.neepmeat.implant.entity.EntityImplant;
import com.neep.neepmeat.init.NMSounds;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class MagazineOrganImplant implements EntityImplant
{
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "magazine_organ");

    private final Entity entity;
    private int maxSize = 8;

//    private final AmmunitionProvider[] inventory = new AmmunitionProvider[6];
    private final List<AmmunitionProvider> inventory = new ArrayList<>();

    public MagazineOrganImplant(Entity entity)
    {
        this.entity = entity;
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        ListTag list = new ListTag();
        for (AmmunitionProvider provider : inventory)
        {
            list.add(provider.getStack().save(new CompoundTag()));
        }
        nbt.put("items", list);
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {
        inventory.clear();
        ListTag list = nbt.getList("items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            ItemStack stack = ItemStack.of(list.getCompound(i));
            var provider = AmmunitionProvider.LOOKUP.find(stack, s -> {});
            if (provider != null)
            {
                inventory.add(provider);
            }
        }
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void tick(Runnable remove)
    {

    }

    public boolean canConsume(AmmunitionItem ammunitionItem, ItemStack itemStack)
    {
        return inventory.size() < maxSize;
    }

    private void insert(AmmunitionProvider provider)
    {
        if (inventory.size() < maxSize)
        {
            inventory.add(provider);
        }
    }

    public ItemStack consume(Level world, ItemStack itemStack, AmmunitionProvider provider)
    {
        world.playSound(
                null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                SoundEvents.GENERIC_EAT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.4F
        );

//        if (!(entity instanceof PlayerEntity player) || !player.getAbilities().creativeMode)
//        {
//            itemStack.decrement(1);
//        }

        entity.gameEvent(GameEvent.EAT);

        insert(provider);
        provider.consume();

        return itemStack;
    }

    public AmmunitionProvider provide(AmmunitionType type, int amount)
    {
        inventory.sort(Comparator.comparingInt(AmmunitionProvider::getAmount));
        var it = inventory.iterator();
        while (it.hasNext())
        {
            AmmunitionProvider provider = it.next();
            if (provider.ammoType() == type && provider.getAmount() >= amount)
            {
                it.remove();
                if (entity.level() instanceof ServerLevel serverWorld)
                {
                    serverWorld.playSound(null, entity, SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1, 1);
                    serverWorld.playSound(null, entity, NMSounds.MEATGUN_MODULE_REMOVE.get(), SoundSource.PLAYERS, 1, 1);
                }
                return provider;
            }
        }
        return null;
    }
}
