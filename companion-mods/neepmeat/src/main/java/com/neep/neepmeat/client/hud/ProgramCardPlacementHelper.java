package com.neep.neepmeat.client.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.neepmeat.plc.block.entity.PLCBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class ProgramCardPlacementHelper implements PlacementPreviewRegistry.Handler
{
    @Override
    public BlockPlacementHelper.@Nullable Result tick(Minecraft client, BlockPos pos, boolean canPlace, BlockHitResult hitResult, BlockPlaceContext hitPos)
    {
        if (client.level.getBlockEntity(hitResult.getBlockPos()) instanceof PLCBlockEntity plc)
        {
            if (client.level.getGameTime() % 5 == 0)
            {
                Component message;
                if (client.player.isShiftKeyDown())
                    message = Component.nullToEmpty("Use to overwrite the PLC's program.");
                else
                    message = Component.nullToEmpty("Use to save program to disk.");

                client.player.displayClientMessage(message, true);
            }
        }

        return null;
    }

    @Override
    public boolean render(Minecraft client, BlockPlacementHelper.Result result, BlockPos pos, PoseStack matrices, MultiBufferSource consumers, float tickDelta)
    {
        return false;
    }
}
