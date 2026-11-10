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

package com.neep.meatlib.util;

import com.mojang.blaze3d.vertex.PoseStack;
import kotlin.jvm.functions.Function0;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4dc;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.valkyrienskies.mod.common.PlayerUtil;
import org.valkyrienskies.mod.common.VSClientGameUtils;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

/**
 * This class should never be initialized if VS2 is not present. It exists to serve as a buffer such that things don't explode if VS2 isn't present, that
 * way it is not a hard dependency.
 */
public class ValkyrienSkiesUtil {

    @OnlyIn(Dist.CLIENT)
    public Client CLIENT;

    public ValkyrienSkiesUtil() {
        if (!FabricLoader.getInstance().isModLoaded("valkyrienskies")) {
            throw new IllegalStateException("This class should never be initialized if VS2 is not present.");
        }
    }

    /**
     * Gets the transformation matrix from Shipspace to Worldspace. Returns null if there is no ship at that position.
     * @param pos Position
     * @param level Clientlevel or Serverlevel
     * @return The Matrix, or Null
     * @see ValkyrienSkiesUtil#getWorldToShipMatrix(Vector3d, Level)
     */
    public Matrix4dc getShipToWorldMatrix(Vector3d pos, Level level) {
        if (!hasShipAtPosition(pos, level)) {
            return null;
        }
        return VSGameUtilsKt.getShipObjectManagingPos(level, pos).getShipToWorld();
    }

    public Matrix4dc getShipToWorldMatrix(BlockPos pos, Level level) {
        if (!hasShipAtPosition(pos, level)) {
            return null;
        }
        return VSGameUtilsKt.getShipObjectManagingPos(level, pos).getShipToWorld();
    }

    /**
     * Inverse of [getShipToWorldMatrix]
     * @param pos Position
     * @param level Clientlevel or Serverlevel
     * @return The Matrix, or Null
     * @see ValkyrienSkiesUtil#getShipToWorldMatrix(Vector3d, Level)
     */
    public Matrix4dc getWorldToShipMatrix(Vector3d pos, Level level) {
        if (!hasShipAtPosition(pos, level)) {
            return null;
        }
        return VSGameUtilsKt.getShipObjectManagingPos(level, pos).getWorldToShip();
    }

    public Matrix4dc getWorldToShipMatrix(BlockPos pos, Level level) {
        if (!hasShipAtPosition(pos, level)) {
            return null;
        }
        return VSGameUtilsKt.getShipObjectManagingPos(level, pos).getWorldToShip();
    }

    /**
     * Checks if a ship exists at a given position.
     * @param pos Position
     * @param level Clientlevel or Serverlevel
     * @return True if a ship exists at the position.
     */
    public boolean hasShipAtPosition(Vector3d pos, Level level) {
        return VSGameUtilsKt.getShipObjectManagingPos(level, pos) != null;
    }

    public boolean hasShipAtPosition(Vec3 pos, Level level) {
        Vector3d realPos = new Vector3d(pos.x, pos.y, pos.z);
        return VSGameUtilsKt.getShipObjectManagingPos(level, realPos) != null;
    }

    public boolean hasShipAtPosition(BlockPos pos, Level level) {
        return VSGameUtilsKt.getShipObjectManagingPos(level, pos) != null;
    }

    public Quaterniondc getShipToWorldRotation(Vector3d pos, Level level) {
        if (!hasShipAtPosition(pos, level)) {
            return null;
        }
        return VSGameUtilsKt.getShipObjectManagingPos(level, pos).getTransform().getShipToWorldRotation();
    }

    public Quaterniondc getShipToWorldRotation(Vec3 pos, Level level) {
        if (!hasShipAtPosition(pos, level)) {
            return null;
        }
        Vector3d realPos = new Vector3d(pos.x, pos.y, pos.z);
        return VSGameUtilsKt.getShipObjectManagingPos(level, realPos).getTransform().getShipToWorldRotation();
    }

    public Quaterniondc getShipToWorldRotation(BlockPos pos, Level level) {
        if (!hasShipAtPosition(pos, level)) {
            return null;
        }
        return VSGameUtilsKt.getShipObjectManagingPos(level, pos).getTransform().getShipToWorldRotation();
    }

    public <T> T transformPlayerTemporarily(Player player, Level world, BlockPos blockInShip, Function0<T> inside)
    {
        return PlayerUtil.transformPlayerTemporarily(player, world, blockInShip, inside);
    }

    @Environment(net.neoforged.api.distmarker.Dist.CLIENT)
    public static class Client {
        public void transformRenderIfOnShip(PoseStack matrixStack, Vector3d renderOffset) {
            VSClientGameUtils.transformRenderIfInShipyard(matrixStack, renderOffset.x, renderOffset.y, renderOffset.z);
        }
    }
}
