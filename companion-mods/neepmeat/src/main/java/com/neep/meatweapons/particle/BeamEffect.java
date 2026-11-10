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

package com.neep.meatweapons.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.client.BeamRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.render.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.Random;
import java.util.function.Function;

public class BeamEffect extends BeamGraphicsEffect
{
    public BeamEffect(Level world, Vec3 start, Vec3 end, Vec3 col, float scale, int maxTime, FriendlyByteBuf buf)
    {
        super(world, start, end, col, scale, maxTime, buf);
    }

    @OnlyIn(Dist.CLIENT)
    public void tick()
    {
        super.tick();

        Random random = new Random(world.getGameTime());
        double d = 0.1;
        random.nextFloat();
//        world.addParticle(ParticleTypes.ENCHANTED_HIT, end.x, end.y, end.z, random.nextFloat() - 0.5, random.nextFloat() - 0.5 , random.nextFloat() - 0.5);
        world.addParticle(MWParticles.PLASMA_PARTICLE, end.x, end.y, end.z,
                d * (random.nextFloat() - 0.75),
//                0,
                d * (random.nextFloat() - 0.5),
                d * (random.nextFloat() - 0.5));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(Camera camera, PoseStack matrices, MultiBufferSource consumers, float tickDelta)
    {
        matrices.pushPose();
        VertexConsumer consumer = consumers.getBuffer(Client.BEAM_FUNC.apply(Client.BEAM_TEXTURE));
//        Vec3d beam = (end.subtract(start));
        float x = Math.max(0, maxTime - time - tickDelta) / (float) maxTime;
//        float x = 1;
        BeamRenderer.renderBeam(matrices, consumer, camera.getPosition(),
//                startPos.add(norm.multiply(beam.length() * (1 - x))), endPos, 123, 171, 254,
                start, end, 123, 171, 254,
                maxTime > 0 ? (int) (255f * x) : 255, scale, 255);
        matrices.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Client
    {
        public static void init()
        {

        }


        // All these things come from RenderPhase. They may appear public, but that is due to Architectury accessWideners.
        public static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("translucent_transparency", () ->
        {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }, () -> {
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        });

        public static final RenderStateShard.CullStateShard DISABLE_CULLING = new RenderStateShard.CullStateShard(false);

        public static final RenderStateShard.WriteMaskStateShard COLOR_MASK = new RenderStateShard.WriteMaskStateShard(true, false);

        public static final Function<ResourceLocation, RenderType> BEAM_FUNC = Util.memoize((texture) ->
        {
            RenderType.CompositeState multiPhaseParameters = RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeBeaconBeamShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(DISABLE_CULLING)
                    .setWriteMaskState(COLOR_MASK)
                    .createCompositeState(false);
            return RenderType.create("beam", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, true, multiPhaseParameters);
        });

        public static final ResourceLocation BEAM_TEXTURE = ResourceLocation.fromNamespaceAndPath(MeatWeapons.NAMESPACE, "textures/misc/beam.png");

    }
}
