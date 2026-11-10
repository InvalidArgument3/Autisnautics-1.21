package com.neep.neepmeat.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DummyBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T>
{
    public DummyBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {

    }
                                    @Override
    public void render(T entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
    {

    }
}
