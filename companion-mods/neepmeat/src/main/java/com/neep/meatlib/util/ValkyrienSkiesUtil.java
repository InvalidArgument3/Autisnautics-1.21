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
 *
 * Plain Java. Valkyrien Skies is not a compile dependency, so ship transforms are identity
 * and "no ship" until that mod is on the classpath.
 */

package com.neep.meatlib.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import org.joml.Matrix4dc;
import org.joml.Quaterniondc;
import org.joml.Vector3d;

import java.util.function.Supplier;

public class ValkyrienSkiesUtil {

    @OnlyIn(Dist.CLIENT)
    public Client CLIENT;

    public ValkyrienSkiesUtil() {
        if (!ModList.get().isLoaded("valkyrienskies")) {
            throw new IllegalStateException("This class should never be initialized if VS2 is not present.");
        }
    }

    public Matrix4dc getShipToWorldMatrix(Vector3d pos, Level level) {
        return null;
    }

    public Matrix4dc getShipToWorldMatrix(BlockPos pos, Level level) {
        return null;
    }

    public Matrix4dc getWorldToShipMatrix(Vector3d pos, Level level) {
        return null;
    }

    public Matrix4dc getWorldToShipMatrix(BlockPos pos, Level level) {
        return null;
    }

    public boolean hasShipAtPosition(Vector3d pos, Level level) {
        return false;
    }

    public boolean hasShipAtPosition(Vec3 pos, Level level) {
        return false;
    }

    public boolean hasShipAtPosition(BlockPos pos, Level level) {
        return false;
    }

    public Quaterniondc getShipToWorldRotation(Vector3d pos, Level level) {
        return null;
    }

    public Quaterniondc getShipToWorldRotation(Vec3 pos, Level level) {
        return null;
    }

    public Quaterniondc getShipToWorldRotation(BlockPos pos, Level level) {
        return null;
    }

    public <T> T transformPlayerTemporarily(Player player, Level world, BlockPos blockInShip, Supplier<T> inside) {
        return inside.get();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client {
        public void transformRenderIfOnShip(PoseStack matrixStack, Vector3d renderOffset) {
        }
    }
}
