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

import com.google.common.collect.Maps;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMComponents;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerImplantManager implements EntityImplantManager, Component, ServerTickingComponent, AutoSyncedComponent, ClientTickingComponent
{
    public static final String ID = "neepmeat:upgrades";
    public static final String TRANSLATION_PREFIX = "implant";

    protected final Player player;
    protected Map<ResourceLocation, EntityImplant> implants = Maps.newHashMap();

    public PlayerImplantManager(Player player)
    {
        this.player = player;
    }

    public static net.minecraft.network.chat.Component getImplantName(ResourceLocation id)
    {
        return net.minecraft.network.chat.Component.translatable(id.toLanguageKey(TRANSLATION_PREFIX));
    }

    @Override
    public boolean installImplant(ResourceLocation id)
    {
        ImplantRegistry.Constructor constructor = ImplantRegistry.REGISTRY.get(id);
        if (constructor != null)
        {
            if (!player.level().isClientSide())
            {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message." + NeepMeat.NAMESPACE + ".implant.install", getImplantName(id)), true);
            }

            addImplant(constructor);
            return true;
        }
        else
        {
            NeepMeat.LOGGER.error("Tried to add an unregistered implant to player {}", player.getScoreboardName());
        }
        return false;
    }

    protected EntityImplant addImplant(ImplantRegistry.Constructor constructor)
    {
        EntityImplant implant = constructor.create(player);
        implants.put(implant.getId(), implant);
        var modifiers = implant.getModifiers();
        implant.onInstall();

        if (!modifiers.isEmpty())
            player.getAttributes().addTransientAttributeModifiers(modifiers);

        sync();
        return implant;
    }

    @Override
    public boolean removeImplant(ResourceLocation id)
    {
        EntityImplant upgrade = implants.get(id);
        if (upgrade != null)
        {
            upgrade.onUninstall();
            var modifiers = upgrade.getModifiers();

            if (!modifiers.isEmpty())
                player.getAttributes().removeAttributeModifiers(modifiers);

            implants.remove(id);

            sync();

            return true;
        }

        return false;
    }

    @Override
    public Set<ResourceLocation> getInstalled()
    {
        return implants.keySet();
    }

    public float getProtectionAmount(DamageSource source, float amount)
    {
        return (float) implants.values().stream().mapToDouble(u -> u.getProtectionAmount(source, amount)).sum();
    }

    public static PlayerImplantManager get(Player player)
    {
        return (PlayerImplantManager) player.getComponent(NMComponents.IMPLANT_MANAGER);
    }

    @Override
    @Nullable
    public <T extends EntityImplant> T getImplant(ResourceLocation id)
    {
        if (implants.isEmpty())
            return null;

        // There is no generic 'ImplantType' class, so whoever calls this will need to cast it anyway.
        return (T) implants.get(id);
    }

    @Override
    public void serverTick()
    {
        var it = implants.values().iterator();
        while (it.hasNext())
        {
            it.next().tick(it::remove);
        }
    }

    @Override
    public void clientTick()
    {
        implants.values().forEach(EntityImplant::clientTick);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag nbt)
    {
        ListTag list = nbt.getList("upgrades", Tag.TAG_COMPOUND);
        Map<ResourceLocation, EntityImplant> prevInstalled = new HashMap<>(implants);
        implants.clear();
        list.forEach(nbt1 ->
        {
            CompoundTag implantNbt = (CompoundTag) nbt1;
            ResourceLocation id = ResourceLocation.tryParse(implantNbt.getString("id"));
            ImplantRegistry.Constructor constructor = ImplantRegistry.REGISTRY.get(id);
            if (constructor != null)
            {
                EntityImplant implant = addImplant(constructor);
                implant.readNbt(implantNbt);
            }
        });

        prevInstalled.forEach((id, implant) ->
        {
            if (!implants.containsKey(id))
            {
                implant.onUninstall();
            }
        });
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag nbt)
    {
        ListTag list = new ListTag();
        implants.forEach((i, u) ->
        {
            CompoundTag nbt1 = new CompoundTag();
            nbt1.putString("id", i.toString());
            u.writeNbt(nbt1);
            list.add(nbt1);
        });
        nbt.put("upgrades", list);
    }

    @Override
    public void sync()
    {
        NMComponents.IMPLANT_MANAGER.sync(player);
    }

    public static void init()
    {
        ServerPlayConnectionEvents.INIT.register((handler, server) ->
        {
            get(handler.getPlayer()).implants.values().forEach(EntityImplant::onPlayerInit);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
        {
            PlayerImplantManager manager = get(handler.getPlayer());
            manager.implants.values().forEach(EntityImplant::onPlayerRemove);
        });
    }

    @Override
    public void onDeath()
    {
        if (!player.level().isClientSide())
        {
            var it = implants.values().iterator();
            while (it.hasNext())
            {
                it.next().onDeath(it::remove);
            }
        }
    }
}
