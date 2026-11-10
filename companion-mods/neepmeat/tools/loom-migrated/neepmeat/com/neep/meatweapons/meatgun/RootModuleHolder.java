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

package com.neep.meatweapons.meatgun;

import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.implant.BloodBulletProviderImplant;
import com.neep.meatweapons.implant.MagazineOrganImplant;
import com.neep.meatweapons.item.meatgun.Meatgun;
import com.neep.meatweapons.meatgun.module.AmmunitionStoringModule;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.meatweapons.network.MeatgunNetwork;
import com.neep.neepmeat.implant.entity.ImplantManager;
import com.neep.neepmeat.implant.entity.PlayerImplantManager;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.init.NMDamageSources;
import com.neep.neepmeat.init.NMSounds;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Set;
import java.util.UUID;

// Instances of this persist when the parent ItemStack is refreshed.
// Newly created MeatgunComponents take ownership of the RootModuleHolder that corresponds to the UUID stored in NBT.
public class RootModuleHolder
{
    public static final UUID NULL_UUID = new UUID(0, 0);

    public final MeatgunModule root;

    private final Meatgun meatgun;

    @Nullable private MeatgunComponent component;
    private int remainingCapacity = -1;

    private final Listener listener = new ListenerImpl();

    private final ModuleCache cache;

    private boolean isNew = true;

    private final MeatgunCore core = new MeatgunCore(this);

    public RootModuleHolder(Meatgun meatgun)
    {
        this.root = meatgun.createBase(listener);
        this.meatgun = meatgun;
        cache = new ModuleCache(root);
    }

    public void setComponent(@Nullable MeatgunComponent component)
    {
        this.component = component;
    }

    public void markDirty(Reason reason)
    {
        if (component != null)
            component.markDirty();

        if (reason == Reason.MODULE_SWAPPED)
        {
            // This null check is necessary as this method can be called due to the constructor before the root module is initialised.
            // This could probably be fixed with better design.
            if (cache != null)
                cache.invalidate();

            remainingCapacity = -1;
        }
    }

    public UUID getUuid()
    {
        return component != null ? component.getUuid() : NULL_UUID;
    }

    public Meatgun getMeatgun()
    {
        return meatgun;
    }

    public boolean containsType(MeatgunModule.Type<?> type)
    {
        return cache.moduleTypes().contains(type);
    }

    // TODO: Slim this down and preferably remove
    private void cacheModules()
    {
        if (remainingCapacity == -1 && component != null)
        {
            remainingCapacity = meatgun.getMaxComplexity(component.getStack());
            for (var module : cache.getModules())
            {
                remainingCapacity -= module.getType().complexity();
            }
        }
    }

    public void readNbt(CompoundTag rootTag)
    {
        root.readNbt(rootTag, new Matrix4f());
    }

    public Listener getListener()
    {
        return listener;
    }

    public int getRemainingComplexity()
    {
        cacheModules();
        return remainingCapacity;
    }

    public int getMaxComplexity(ItemStack stack)
    {
        return meatgun.getMaxComplexity(stack);
    }

    public boolean canSupport(MeatgunModule.Type<?> type)
    {
        cacheModules();
        return remainingCapacity >= type.complexity();
    }

    /**
     * @return non-empty if the module can fire and false if it cannot. Return value is independent of whether a reload has taken place.
     */
    public AmmunitionTransaction getAmmoOrReload(AmmunitionType type, int amount, Container inventory, Player player, @Nullable InteractionHand hand)
    {
        // Switched off for debug porpoises
//        if (player.isCreative())
//            return true;

        PlayerImplantManager manager = (PlayerImplantManager) NMComponents.IMPLANT_MANAGER.get(player);
        boolean hasBloodBullets = manager.isInstalled(BloodBulletProviderImplant.ID);

        cacheModules();
        int available = cache.getAmmo(type);

        boolean satisfied = available >= amount;

        // Reload from player
        if (!satisfied)
        {
            boolean reloaded = false;

            @Nullable MagazineOrganImplant implant = manager.getImplant(MagazineOrganImplant.ID);
            if (implant != null)
            {
                @Nullable AmmunitionProvider provider = implant.provide(type, amount);
                if (provider != null)
                {
                    reloaded = reload(provider, player, false, hand);

                    if (reloaded)
                        return AmmunitionTransaction.EMPTY;
                }
            }

            for (int i = 0; i < inventory.getContainerSize(); ++i)
            {
                ItemStack stack = inventory.getItem(i);
                @Nullable AmmunitionProvider provider = AmmunitionProvider.LOOKUP.find(stack, new AmmunitionProvider.InventoryContext(inventory, i));
                if (provider != null && provider.ammoType() == type)
                {
                    reloaded = reload(provider, player, true, hand);

                    if (reloaded)
                        return AmmunitionTransaction.EMPTY;
                }
            }
        }

        AmmunitionTransaction transaction = () -> { };

        // Provide ammunition from storage
        int required = amount;
        if (satisfied || hasBloodBullets)
        {
            for (var otherModule : cache.getModules())
            {
                if (otherModule instanceof AmmunitionStoringModule storage
                        && storage.ammoType() == type)
                {
                    if (storage.amount() > 0)
                    {
                        int canExtract = Math.min(storage.amount(), required);
                        transaction = transaction.chain(consumeFromModule(storage, canExtract));
                        required -= canExtract;
                    }

                    if (required == 0)
                        return transaction.chain(() -> listener.markDirty(Reason.SAVE_DATA));
                }
            }
        }

        // Use blood!
        if (required > 0 && hasBloodBullets && type.healthPerUnit > 0)
        {
            return transaction
                    .chain(consumeBlood(type, required, player))
                    .chain(() -> listener.markDirty(Reason.SAVE_DATA))
                    ;
        }

        return AmmunitionTransaction.EMPTY;
    }

    private AmmunitionTransaction consumeFromModule(AmmunitionStoringModule module, int amount)
    {
        return () ->
        {
            module.extract(amount);
        };
    }

    private AmmunitionTransaction consumeBlood(AmmunitionType type, int required, Player player)
    {
        return () ->
        {
            float healthPerUnit = type.healthPerUnit;
            float fractionalHearts = healthPerUnit * required;
            player.hurt(player.level().damageSources().source(NMDamageSources.BLOOD_BULLETS), fractionalHearts);
        };
    }

    /// Does not care about leftovers, consumes the entire provider or doesn't.
    /// @param hand Leave null to send no animation
    public boolean reload(AmmunitionProvider provider, Player player, boolean consume, @Nullable InteractionHand hand)
    {
        cacheModules();
        int current = cache.getAmmo(provider.ammoType());
        if (current >= provider.getAmount())
            return false;

        int remaining = provider.getAmount() - current;
        for (var otherModule : cache.getModules())
        {
            if (otherModule instanceof AmmunitionStoringModule storage
                    && storage.ammoType() == provider.ammoType())
            {
                remaining -= storage.insert(remaining);
                if (remaining <= 0)
                    break;
            }
        }

        if (remaining < provider.getAmount())
        {
            if (consume)
                provider.consume();

            listener.markDirty(Reason.SAVE_DATA);
            player.playNotifySound(NMSounds.RELOAD_MECHANICAL_HISS, SoundSource.PLAYERS, 1, 1);

            if (hand != null)
                MeatgunNetwork.SEND_ANIMATION.emitter(player).apply("reload", hand, PacketByteBufs.create().writeVarInt(10));

            return true;
        }
        return false;
    }

    /// Inserts a specific amount, accounting for capacity.
    public int insertAmmo(AmmunitionType type, int amount)
    {
        cacheModules();
        int current = cache.getAmmo(type);
        int capacity = cache.getCapacity(type);

        int remaining = Math.min(amount, capacity - current);

        if (remaining == 0)
            return 0;

        for (var otherModule : cache.getModules())
        {
            if (otherModule instanceof AmmunitionStoringModule storage
                    && storage.ammoType() == type)
            {
                remaining -= storage.insert(remaining);
                if (remaining <= 0)
                    break;
            }
        }

        if (remaining < amount)
        {
            return amount - remaining;
        }

        return 0;
    }

    public Set<AmmunitionType> getSupportedAmmo()
    {
        return cache.getAmmo().keySet();
    }

    public ModuleCache moduleCache()
    {
        return cache;
    }

    public ItemStack getOwningItemStack()
    {
        return component != null ? component.getStack() : ItemStack.EMPTY;
    }

    public ImplantManager coreImplants()
    {
        return NMComponents.IMPLANT_MANAGER.maybeGet(getOwningItemStack()).orElse(ImplantManager.EMPTY);
    }

    public boolean isNew()
    {
        if (isNew)
        {
            isNew = false;
            return true;
        }

        return false;
    }

    public MeatgunCore getCore()
    {
        return core;
    }

    private class ListenerImpl implements Listener
    {
        @Override
        public RootModuleHolder getHolder()
        {
            return RootModuleHolder.this;
        }

        @Override
        public void markDirty(Reason reason)
        {
            RootModuleHolder.this.markDirty(reason);
        }
    }

    public interface Listener
    {
        RootModuleHolder getHolder();

        void markDirty(Reason reason);
    }

    public enum Reason
    {
        MODULE_SWAPPED,
        SAVE_DATA
    }
}
