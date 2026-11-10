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

import com.neep.meatlib.MeatLib;
import com.neep.neepmeat.api.big_block.BlockPlacementPreview;
import com.neep.neepmeat.api.block.PlacementPreview;
import com.neep.meatlib.theme.PLCCols;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import static com.neep.neepmeat.client.plc.PLCHudRenderer.drawCuboidShapeOutline;

import com.mojang.blaze3d.vertex.PoseStack;

public class BlockPlacementHelper
{
    @Nullable
    private static Result RESULT;

    public static void init()
    {
        WorldRenderEvents.BLOCK_OUTLINE.register(BlockPlacementHelper::onRender);
        ClientTickEvents.START_CLIENT_TICK.register(client ->
        {
            if (client.level != null && client.player != null)
                tick(client);
        });
    }

    private static void tick(Minecraft client)
    {
        ItemStack mainStack = client.player.getMainHandItem();
        if (client.hitResult instanceof BlockHitResult hitResult)
        {
            if (mainStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof BlockPlacementPreview bigBlock)
            {
                BlockState placementState;

                hitResult = bigBlock.modifyHitPos(hitResult);
                BlockPlaceContext itemPlacementContext = new BlockPlaceContext(client.player, client.player.swingingArm, mainStack, hitResult);
                BlockPos pos = itemPlacementContext.getClickedPos();
                boolean place = itemPlacementContext.canPlace();

                if (MeatLib.vsUtil != null && MeatLib.vsUtil.hasShipAtPosition(hitResult.getBlockPos(), client.level))
                {
                    placementState = MeatLib.vsUtil.transformPlayerTemporarily(client.player, client.level, hitResult.getBlockPos(), () ->
                            bigBlock.getPlacementStatePreview(itemPlacementContext));
                }
                else
                {
                    placementState = bigBlock.getPlacementStatePreview(itemPlacementContext);
                }

                if (placementState != null)
                {
                    place = place && bigBlock.canPlaceAtPreview(placementState, client.level, pos);

                    RESULT = new ResultImpl(
                            null,
                            bigBlock.getVolume(placementState).toVoxelShape(),
                            pos,
                            itemPlacementContext.getClickedFace(),
                            place ? PLCCols.BORDER.col : PLCCols.ERROR_LINE.col,
                            itemPlacementContext);
                    return;
                }
            }
            else if (mainStack.getItem() instanceof PlacementPreview preview)
            {
                BlockPlaceContext itemPlacementContext = new BlockPlaceContext(client.player, client.player.swingingArm, mainStack, hitResult);
                BlockPos pos = itemPlacementContext.getClickedPos();
                boolean canPlace = itemPlacementContext.canPlace();

                RESULT = PlacementPreviewRegistry.get(preview).tick(client, pos, canPlace, hitResult, itemPlacementContext);
                return;
            }
        }
        RESULT = null;
    }

    private static boolean onRender(WorldRenderContext wrctx, WorldRenderContext.BlockOutlineContext blockOutlineContext)
    {
        if (RESULT != null)
        {
            Minecraft client = Minecraft.getInstance();
            Vec3 camPos = client.gameRenderer.getMainCamera().getPosition();
            BlockPos pos = RESULT.pos();
            PoseStack matrices = wrctx.matrixStack();

            if (RESULT.handler() != null)
            {
                matrices.pushPose();

                matrices.translate(-camPos.x, -camPos.y, -camPos.z);
                boolean ret = RESULT.handler().render(client, RESULT, pos, matrices, wrctx.consumers(), wrctx.tickDelta());
                matrices.popPose();

                return ret;
            }
            else
            {
                // Draw a simple preview
                // Mainly for BigBlock

                matrices.pushPose();

                matrices.translate(-camPos.x, -camPos.y, -camPos.z);

                if (MeatLib.vsUtil != null)
                    MeatLib.vsUtil.CLIENT.transformRenderIfOnShip(matrices, new Vector3d(pos.getX(), pos.getY(), pos.getZ()));
                else
                    matrices.translate(pos.getX(), pos.getY(), pos.getZ());

                int col = RESULT.col();

                drawCuboidShapeOutline(
                        wrctx.matrixStack(),
                        wrctx.consumers().getBuffer(RenderType.lines()),
                        RESULT.shape(),
                        0,
                        0,
                        0,
                        ((col >> 16) & 0xff) / 255f, ((col >> 8) & 0xff) / 255f, ((col >> 0) & 0xff) / 255f, 1
                );

                matrices.popPose();
                return false;
            }
        }
        return true;
    }

    public interface Result
    {
        @Nullable PlacementPreviewRegistry.Handler handler();
        VoxelShape shape();
        BlockPos pos();
        Direction face();
        int col();
        BlockPlaceContext context();
    }

    public record ResultImpl(@Nullable PlacementPreviewRegistry.Handler handler, VoxelShape shape, BlockPos pos, Direction face, int col, BlockPlaceContext context) implements Result
    {

    }
}
