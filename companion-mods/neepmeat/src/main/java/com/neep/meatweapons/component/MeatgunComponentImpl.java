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

package com.neep.meatweapons.component;

import com.neep.meatlib.MeatLib;
import com.neep.meatweapons.client.meatgun.RecoilManager;
import com.neep.meatweapons.item.meatgun.Meatgun;
import com.neep.meatweapons.item.meatgun.MeatgunAnimationManager;
import com.neep.meatweapons.meatgun.MeatgunCore;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.neepmeat.implant.item.ItemImplantManager;
import com.neep.neepmeat.init.NMComponents;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.UUID;

public class MeatgunComponentImpl extends ItemComponent implements MeatgunComponent
{
    @Nullable private RecoilManager recoil;
    private RootModuleHolder holder;

    private boolean dirty = true;
    private boolean invalidated = false;

    // Eeek! A bit unsafe, but normally fine.
    @OnlyIn(Dist.CLIENT)
    private MeatgunAnimationManager animationManager;

    public MeatgunComponentImpl(ItemStack stack, ComponentKey<MeatgunComponent> key)
    {
        super(stack, key);

        holder = initUUID();
        holder.setComponent(this); // this line here is the source of many problems

        if (MeatLib.isClient() || holder.isNew())
            holder.readNbt(getOrCreateRootTag().getCompound("root"));

//        int formatVersion = getInt("version");
//        if (MeatgunMigration.LATEST_VERSION > formatVersion)
//        {
//            MeatgunMigration.apply(this, formatVersion);
//            putInt("version", MeatgunMigration.LATEST_VERSION);
//        }
    }

    @Override
    public RootModuleHolder getRootHolder()
    {
        return holder;
    }

    @Override
    public RootModuleHolder initUUID()
    {
        Meatgun meatgun = (Meatgun) getStack().getItem();
        boolean isServer = !MeatLib.isClient();
        UUID foundUUID = getUuid("meatgun_uuid");
        if (foundUUID == null || foundUUID.equals(RootModuleHolder.NULL_UUID))
        {
            // Completely new item
            // Use the null UUID. This will be replaced when the item is copied and given to a player.
            foundUUID = RootModuleHolder.NULL_UUID;
            putUuid("meatgun_uuid", foundUUID);

            return new RootModuleHolder(meatgun);
        }

        return meatgun.getCache().getOrCreate(foundUUID, meatgun, isServer);
    }

    @Override
    public void trigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        holder.root.trigger(world, player, stack, id, pitch, yaw, handType, hand);
    }

    @Override
    public void release(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        holder.root.release(world, player, stack, id, pitch, yaw, handType, hand);
    }

    @Override
    public void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        holder.root.tickTrigger(world, player, stack, id, pitch, yaw, handType, hand);
    }

    @Override
    public RecoilManager getRecoil()
    {
        if (recoil == null)
            recoil = RecoilManager.getOrCreate(getUuid());

        return recoil;
    }

    @Override
    public @Nullable MeatgunAnimationManager getAnimationManager()
    {
        return animationManager;
    }

    @Override
    public ItemStack getStack()
    {
        return stack;
    }

    @Override
    public void tickUUIDResolution(Player player)
    {
        if (getUuid().equals(RootModuleHolder.NULL_UUID))
        {
            UUID newUUID = UUID.randomUUID();
            putUuid("meatgun_uuid", newUUID);
            Meatgun meatgun = (Meatgun) stack.getItem();
            holder = meatgun.getCache().getOrCreate(newUUID, meatgun, !player.level().isClientSide());
            holder.setComponent(this); //

            // Initialise with defaults
            ItemStack defaultStack = stack.getItem().getDefaultInstance();
            meatgun.createDefaultTree(holder.root, holder.getListener());

            // Default implants
            ItemImplantManager defaultImplants = (ItemImplantManager) NMComponents.IMPLANT_MANAGER.get(defaultStack);
            defaultImplants.copyTo(NMComponents.IMPLANT_MANAGER.get(stack));
        }
    }

    @Override
    public void commonTick(Player player)
    {
        holder.root.tick(player);

        if (!player.level().isClientSide())
        {
            if (dirty)
            {
                getOrCreateRootTag().put("root", holder.root.writeNbt(new CompoundTag()));
                dirty = false;
            }

            if (invalidated)
            {
                // Migration from old format
                if (!getOrCreateRootTag().contains("root"))
                    holder.root.readNbt(getOrCreateRootTag(), new Matrix4f());
                else
                    holder.root.readNbt(getOrCreateRootTag().getCompound("root"), new Matrix4f());
                invalidated = false;
            }
        }
    }

    @Override
    public void clientTick(Player player)
    {
        if (animationManager == null)
            this.animationManager = MeatgunAnimationManager.getOrCreate(getUuid(), (Meatgun) stack.getItem());

        animationManager.tick(this);
    }

    @Override
    public void markDirty()
    {
        getOrCreateRootTag().put("root", holder.root.writeNbt(new CompoundTag()));
    }


    @Override
    public void onTagInvalidated()
    {
        super.onTagInvalidated();
        dirty = true;
        invalidated = true;
    }

    @Override
    public UUID getUuid()
    {
        return getUuid("meatgun_uuid");
    }

    @Override
    public MeatgunCore getCore()
    {
        return getRootHolder().getCore();
    }
}
