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

package com.neep.meatweapons.meatgun.module;

import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.item.TriggerReceiver;
import com.neep.meatweapons.item.meatgun.MeatgunModuleItem;
import com.neep.meatweapons.meatgun.MeatgunModuleCapability;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface MeatgunModule extends TriggerReceiver
{
    UUID getUuid();

    List<ModuleSlot> getChildren();

    Type<? extends MeatgunModule> getType();

    default void receivePacket(FriendlyByteBuf buf)
    {
    }

    default void tick(Player player)
    {
        getChildren().forEach(s -> s.get().tick(player));
    }

    default void trigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        getChildren().forEach(c -> c.get().trigger(world, player, stack, id, pitch, yaw, handType, hand));
    }

    default void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        getChildren().forEach(c -> c.get().tickTrigger(world, player, stack, id, pitch, yaw, handType, hand));
    }

    default void release(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        getChildren().forEach(c -> c.get().release(world, player, stack, id, pitch, yaw, handType, hand));
    }

    void setTransform(Matrix4f transform);

    CompoundTag writeNbt(CompoundTag nbt);

    /// @param global Parent's full transform. null if unavailable.
    void readNbt(CompoundTag nbt, @Nullable Matrix4f global);

    static CompoundTag toNbt(MeatgunModule module)
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", MeatgunModules.REGISTRY.getKey(module.getType()).toString());
        module.writeNbt(nbt);
        return nbt;
    }

    static Type<?> readType(CompoundTag nbt)
    {
        ResourceLocation id = new ResourceLocation(nbt.getString("id"));
        return MeatgunModules.REGISTRY.get(id);
    }

    MeatgunModule DEFAULT = new MeatgunModule()
    {
        @Override
        public UUID getUuid()
        {
            return UUID.fromString("");
        }

        @Override
        public List<ModuleSlot> getChildren()
        {
            return List.of();
        }

        @Override
        public Type<? extends MeatgunModule> getType()
        {
            return DEFAULT_TYPE;
        }

        @Override
        public void setTransform(Matrix4f transform)
        {

        }

        @Override
        public CompoundTag writeNbt(CompoundTag nbt)
        {
            return nbt;
        }

        @Override
        public void readNbt(CompoundTag nbt, @Nullable Matrix4f global)
        {

        }

        @Override
        public Set<MeatgunModuleCapability> getCapabilities()
        {
            return Collections.emptySet();
        }

        //        @Override
//        public EnumSet<ChildProperties> getChildProperties()
//        {
//             TODO
//            return null;
//        }
    };

    ResourceLocation DEFAULT_ID = new ResourceLocation(MeatWeapons.NAMESPACE, "default");
    Type<?> DEFAULT_TYPE = new MeatgunModule.Type<>((l, p, stack) -> DEFAULT, (l, n) -> DEFAULT);

    default ItemStack createStack(MeatgunModuleItem item)
    {
        return item.getDefaultInstance();
    }

    /// Redirects all triggers by default
    default Set<MeatgunModuleCapability> getCapabilities()
    {
        return MeatgunModuleCapability.REDIRECT_ALL;
    }

    @FunctionalInterface
    interface Factory<T extends MeatgunModule>
    {
        T create(RootModuleHolder.Listener listener, MeatgunModule parent, ItemStack stack);
    }

    @FunctionalInterface
    interface NbtFactory<T extends MeatgunModule>
    {
        T create(RootModuleHolder.Listener listener, CompoundTag nbt);
    }

    class Type<T extends MeatgunModule>
    {
        private final Factory<T> factory;
        private final NbtFactory<T> nbtFactory;
        private final AttributeContainer attributes;

        public Type(AttributeContainer attributes, Factory<T> factory, NbtFactory<T> nbtFactory)
        {
            this.attributes = attributes;
            this.factory = factory;
            this.nbtFactory = nbtFactory;
        }

        public Type(Factory<T> factory, NbtFactory<T> nbtFactory)
        {
            this.attributes = new AttributeContainer();
            this.factory = factory;
            this.nbtFactory = nbtFactory;
        }

        public T create(RootModuleHolder.Listener listener, MeatgunModule parent, ItemStack stack)
        {
            return factory.create(listener, parent, stack);
        }

        public T create(RootModuleHolder.Listener listener, CompoundTag nbt)
        {
            return nbtFactory.create(listener, nbt);
        }

        public int complexity()
        {
            return (int) attributes.get(Attribute.COMPLEXITY);
        }

        public AttributeContainer attributes()
        {
            return attributes;
        }

        public int capacity()
        {
            return (int) attributes.get(Attribute.CAPACITY);
        }
    }

    enum Attribute
    {
        ATTACK_DAMAGE,
        COOLDOWN,
        COMPLEXITY,
        AMOUNT_PER_USE,
        CAPACITY,
    }
}