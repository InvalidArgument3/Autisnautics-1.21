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

package com.neep.meatlib.entity;

import com.neep.meatlib.entity.player.FakePlayerEntity;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class MobKillUtil
{
    //    private static final ThreadLocal<PlayerEntity> killPlayer = ThreadLocal.withInitial(() -> new FakePlayerEntity());

    public static void dropLoot(ServerLevel world, LivingEntity entity, DamageSource damageSource, List<ItemStack> resultStacks, boolean player, int looting)
    {
        FakePlayerEntity fakePlayer = null;
        if (player)
        {
            fakePlayer = FakePlayerEntity.get(world);
            fakePlayer.lootingLevel = looting; // Jank
        }

        ResourceLocation identifier = entity.getLootTable();
        LootTable lootTable = world.getServer().getLootData().getLootTable(identifier);
        LootParams.Builder builder = (new LootParams.Builder(world))
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, fakePlayer)
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, fakePlayer)
                .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, fakePlayer)
                ;

        LootParams lootContextParameterSet = builder.create(LootContextParamSets.ENTITY);
        lootTable.getRandomItems(lootContextParameterSet, entity.getLootTableSeed(), resultStacks::add);
    }
}
