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

import com.neep.neepmeat.api.big_block.BigBlock;
import com.neep.neepmeat.api.big_block.BlockPlacementPreview;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import org.jetbrains.annotations.Nullable;

/** Draws the multiblock volume while the player holds a {@link BlockPlacementPreview} block. */
public final class BlockPlacementHelper {
    private static final int PLACE_COLOUR = 0x55FF55;
    private static final int BLOCKED_COLOUR = 0xFF5555;
    @Nullable
    private static Preview preview;

    private BlockPlacementHelper() {}

    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft client = Minecraft.getInstance();
        preview = null;
        if (client.level == null || client.player == null || !(client.hitResult instanceof BlockHitResult hit)) {
            return;
        }
        ItemStack held = client.player.getMainHandItem();
        if (!(held.getItem() instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof BlockPlacementPreview volume)) {
            return;
        }
        hit = volume.modifyHitPos(hit);
        BlockPlaceContext context = new BlockPlaceContext(client.player, client.player.getUsedItemHand(), held, hit);
        BlockState state = volume.getPlacementStatePreview(context);
        if (state == null) {
            return;
        }
        BlockPos pos = context.getClickedPos();
        boolean canPlace = context.canPlace() && (volume instanceof BigBlock<?> big
                ? big.survivesPreview(state, client.level, pos)
                : volume.canPlaceAtPreview(state, client.level, pos));
        preview = new Preview(volume.getVolume(state).toVoxelShape(), pos, canPlace ? PLACE_COLOUR : BLOCKED_COLOUR);
        if (client.options.keyAttack.isDown() && client.hitResult instanceof BlockHitResult breaking && client.player.tickCount % 4 == 0) {
            BlockState breakingState = client.level.getBlockState(breaking.getBlockPos());
            if (breakingState.getBlock() instanceof BlockPlacementPreview block) {
                block.addBlockBreakParticles(client.level, breaking.getBlockPos(), breakingState);
            }
        }
    }

    public static void onHighlight(RenderHighlightEvent.Block event) {
        if (preview == null) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        var pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(preview.pos.getX() - camera.x, preview.pos.getY() - camera.y, preview.pos.getZ() - camera.z);
        float red = ((preview.colour >> 16) & 0xff) / 255f;
        float green = ((preview.colour >> 8) & 0xff) / 255f;
        float blue = (preview.colour & 0xff) / 255f;
        var buffer = event.getMultiBufferSource().getBuffer(RenderType.lines());
        preview.shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                LevelRenderer.renderLineBox(pose, buffer, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, 1f));
        pose.popPose();
    }

    private record Preview(VoxelShape shape, BlockPos pos, int colour) {}
}
