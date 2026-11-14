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

package com.neep.neepmeat.client.hud;

import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.item.ScutterItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static com.neep.neepmeat.client.plc.PLCHudRenderer.drawCuboidShapeOutline;

@OnlyIn(Dist.CLIENT)
public class FarmingScutterHudRenderer
{
    public static void init()
    {
        WorldRenderEvents.BLOCK_OUTLINE.register((wrctx, blockOutlineContext) ->
        {
            Minecraft client = Minecraft.getInstance();
            if (client.player.getMainHandItem().getItem() instanceof ScutterItem<?> scutter && client.hitResult instanceof BlockHitResult hitResult)
            {
                BlockPos pos = hitResult.getBlockPos();

                Vec3 camPos = client.gameRenderer.getMainCamera().getPosition();
                BlockState targetState = client.level.getBlockState(pos);
                VoxelShape shape = targetState.getShape(client.level, pos, CollisionContext.of(client.player));

//                int col = FarmingScutter.hasMoisture(targetState
                int col = PLCCols.BORDER.col;

                drawCuboidShapeOutline(
                        wrctx.matrixStack(),
                        wrctx.consumers().getBuffer(RenderType.lines()),
                        shape,
                        pos.getX() - camPos.x,
                        pos.getY() - camPos.y,
                        pos.getZ() - camPos.z,
                    ((col >> 16) & 0xff) / 255f, ((col >> 8) & 0xff) / 255f, ((col >> 0) & 0xff) / 255f, 1
                );
                return false;
            }
            return true;
        });
    }
}
