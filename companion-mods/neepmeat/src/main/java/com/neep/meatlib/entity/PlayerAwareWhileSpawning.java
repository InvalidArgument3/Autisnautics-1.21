package com.neep.meatlib.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public interface PlayerAwareWhileSpawning
{
    boolean isValidSpawn(ServerLevel world, Player nearestPlayer, double squaredDistance);
}
