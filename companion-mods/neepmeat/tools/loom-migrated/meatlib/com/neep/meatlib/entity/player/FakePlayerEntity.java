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

package com.neep.meatlib.entity.player;

import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stat;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;

public class FakePlayerEntity extends ServerPlayer
{
    public static final UUID DEFAULT_UUID = UUID.fromString("41C82C87-7AfB-4024-BA57-13D2C99CAE77");
    private static final GameProfile DEFAULT_PROFILE = new GameProfile(DEFAULT_UUID, "[NEEPMeat]");

    private static final Map<FakePlayerKey, FakePlayerEntity> FAKE_PLAYER_MAP = new MapMaker().weakValues().makeMap();

    private FakePlayerInventory fakeInventory = new FakePlayerInventory(this);

    public int lootingLevel = 0; // Jank

    public static FakePlayerEntity get(ServerLevel world)
    {
        return FAKE_PLAYER_MAP.computeIfAbsent(new FakePlayerKey(world, DEFAULT_PROFILE), k ->
                new FakePlayerEntity(world.getServer(), world, k.profile));
    }
    // Can't use the FAPI fake player as it's not in 1.19

    private FakePlayerEntity(MinecraftServer server, ServerLevel world, GameProfile profile)
    {
        super(server, world, profile);

        this.connection = new FakePlayerEntityNetworkHandler(server, this);

        // Hehehehhe get access widened n00b
        this.inventory = fakeInventory;
    }

    @Override
    public FakePlayerInventory getInventory()
    {
        return fakeInventory;
    }

    public void delegateInventory(Container inventory, Runnable markDirty)
    {
        fakeInventory = new DelegatePlayerInventory(this, inventory, markDirty);
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack)
    {
        if (fakeInventory instanceof DelegatePlayerInventory delegate)
        {
            delegate.equipStack(slot, stack);
        }
        else
        {
            super.setItemSlot(slot, stack);
        }
    }

    @Override
    public SlotAccess getSlot(int mappedIndex)
    {
        if (fakeInventory instanceof DelegatePlayerInventory delegate)
            return delegate.getStackReference(mappedIndex);

        return super.getSlot(mappedIndex);
    }

    public void setSelectedSlot(int slot)
    {
    }

// Copied from net.fabricmc.fabric.api.entity.FakePlayer

    @Override
    public void tick() { }

    @Override
    public void updateOptions(ServerboundClientInformationPacket packet) { }

    @Override
    public void awardStat(Stat<?> stat, int amount) { }

    @Override
    public void resetStat(Stat<?> stat) { }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource)
    {
        return true;
    }

    @Nullable
    @Override
    public Team getTeam() {
        // Scoreboard team is checked using the gameprofile name by default, which we don't want.
        return null;
    }

    @Override
    public void startSleeping(BlockPos pos) {
        // Don't lock bed forever.
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        return false;
    }

    @Override
    public void openTextEdit(SignBlockEntity sign, boolean front) { }

    @Override
    public OptionalInt openMenu(@Nullable MenuProvider factory) {
        return OptionalInt.empty();
    }

    @Override
    public void openHorseInventory(AbstractHorse horse, Container inventory) { }

    public int getLooting()
    {
        return lootingLevel;
    }

    private record FakePlayerKey(ServerLevel world, GameProfile profile) { }

    private static class FakePlayerEntityNetworkHandler extends ServerGamePacketListenerImpl
    {
        private static final Connection FAKE_CONNECTION = new Connection(PacketFlow.CLIENTBOUND);

        public FakePlayerEntityNetworkHandler(MinecraftServer server, ServerPlayer player)
        {
            super(server, FAKE_CONNECTION, player);
        }

        @Override
        public void send(Packet<?> packet) { }
    }

    public static class FakePlayerInventory extends Inventory
    {
        public FakePlayerInventory(Player player)
        {
            super(player);
        }

        public void setSelectedSlot(int slot)
        {
            if (Inventory.isHotbarSlot(slot))
                selected = slot;
        }
    }

}
