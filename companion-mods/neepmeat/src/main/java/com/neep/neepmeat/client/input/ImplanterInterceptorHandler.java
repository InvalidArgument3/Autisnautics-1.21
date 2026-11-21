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

package com.neep.neepmeat.client.input;

import com.neep.neepbus.client.input.InputInterceptorHandler;
import com.neep.neepmeat.client.plc.PLCHudRenderer;
import com.neep.neepmeat.client.screen.button.ImplanterScreen;
import com.neep.neepmeat.mixin.client.CameraAccessor;
import com.neep.neepmeat.plc.block.entity.ImplanterBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@OnlyIn(Dist.CLIENT)
public class ImplanterInterceptorHandler implements InputInterceptorHandler
{
    private final Minecraft client;

    public ImplanterInterceptorHandler()
    {
        this.client = Minecraft.getInstance();
    }

    @Override
    public boolean acceptScreen(Screen screen)
    {
        return screen instanceof ImplanterScreen;
    }

    @Override
    public boolean requiresScreen()
    {
        return true;
    }

    @Override
    public boolean hideHud()
    {
        return true;
    }

    @Override
    public void render(GuiGraphics matrices, float tickDelta)
    {

    }

    @Override
    public boolean onRenderOutline(WorldRenderContext wrctx, WorldRenderContext.BlockOutlineContext ctx)
    {
        if (client.screen instanceof ImplanterScreen screen && screen.targetEntity != null)
        {
            var camPos = wrctx.camera().getPosition();
            VoxelShape shape = Shapes.create(screen.targetEntity.getDimensions(
                    Pose.STANDING).makeBoundingBox(0, 0, 0));

//            var be = screen.getScreenHandler().getImplanter();
//            BlockPos bePos = be.getPos().up().north(3);
            Vec3 entityPos = screen.targetEntity.position();

            wrctx.matrixStack().pushPose();
            wrctx.matrixStack().translate(entityPos.x, entityPos.y, entityPos.z);
            PLCHudRenderer.drawCuboidShapeOutline(
                    wrctx.matrixStack(),
                    wrctx.consumers().getBuffer(RenderType.lines()),
                    shape,
                    -camPos.x,
                    -camPos.y,
                    -camPos.z,
                    1, 0.36f, 0.13f, 0.8f
            );
            wrctx.matrixStack().popPose();
        }

        return false;
    }

    @Override
    public void overrideCamera(Minecraft client, Camera camera, float tickDelta)
    {
        if (client.screen instanceof ImplanterScreen screen)
        {
            CameraAccessor accessor = (CameraAccessor) camera;
            ImplanterBlockEntity be = screen.getMenu().getImplanter();
            BlockPos pos = be.getBlockPos();
            be.cameraYaw = Mth.lerp(0.2f, be.cameraYaw, be.getYaw());
            be.cameraX = Mth.lerp(0.2f, be.cameraX, be.getPolX());
            be.cameraY = Mth.lerp(0.2f, be.cameraY, be.getPolY());
//            float t = client.getLastFrameDuration();
//            be.cameraYaw = MathHelper.lerp(0.9f * f, be.cameraYaw, be.getYaw());
//            be.cameraX = MathHelper.lerp(0.9f * f, be.cameraX, be.getPolX());
//            be.cameraY = MathHelper.lerp(0.9f * f, be.cameraY, be.getPolY());

            double x = pos.getX() + 0.5 + Mth.sin(be.cameraYaw) * be.cameraX;
            double z = pos.getZ() + 0.5 + Mth.cos(be.cameraYaw) * be.cameraX;
            double y = pos.getY() + be.cameraY;
            accessor.callSetPos(x, y, z);
            accessor.callSetRotation(-be.cameraYaw * Mth.RAD_TO_DEG, 0);
            accessor.setThirdPerson(true);
        }
    }


}
