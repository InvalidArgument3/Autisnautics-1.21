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

package com.neep.neepmeat.client.plc;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.meatlib.MeatLib;
import com.neep.meatlib.client.event.KeyboardEvents;
import com.neep.neepmeat.client.screen.plc.PLCProgramScreen;
import com.neep.neepmeat.machine.surgical_controller.SurgicalRobot;
import com.neep.neepmeat.mixin.client.CameraAccessor;
import com.neep.neepmeat.network.plc.PLCRobotEnterS2C;
import com.neep.neepmeat.plc.block.entity.PLCBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4dc;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class PLCHudRenderer
{
    @Nullable
    public static BlockHitResult HIT_RESULT;
    public static Matrix4f PROJECTION;
    public static Matrix4f MODEL_VIEW;
    @Nullable
    private static PLCHudRenderer INSTANCE;
    private final PLCBlockEntity be;
    private final SurgicalRobot.Client robotClient;
    private final Minecraft client;
    private final PLCMotionController controller;
    private final Camera camera;

    private PLCHudRenderer(PLCBlockEntity be)
    {
        this.client = Minecraft.getInstance();
        this.be = be;
        this.robotClient = new SurgicalRobot.Client(be.getSurgeryRobot(), be);
        this.be.getSurgeryRobot().setController(client.player);
        this.controller = new PLCMotionController(be.getSurgeryRobot());
        this.camera = client.gameRenderer.getMainCamera();

        robotClient.resetKeys();
    }

    @Nullable
    public static PLCHudRenderer getInstance()
    {
        return INSTANCE;
    }

    public static void enter(PLCBlockEntity be)
    {
        INSTANCE = new PLCHudRenderer(be);
    }

    public static boolean active()
    {
        return INSTANCE != null;
    }

    public static void leave()
    {
        if (INSTANCE != null)
        {
            INSTANCE.exit();
        }
        INSTANCE = null;
    }

    public static void drawCuboidShapeOutline(PoseStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ,
                                              float r, float g, float b, float a)
    {
        PoseStack.Pose entry = matrices.last();
        shape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) ->
        {
            line((float) (minX + offsetX), (float) (minY + offsetY), (float) (minZ + offsetZ),
                    (float) (maxX + offsetX), (float) (maxY + offsetY), (float) (maxZ + offsetZ), r, g, b, a, vertexConsumer, entry);
        });
    }

    public static void line(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a, VertexConsumer consumer, PoseStack.Pose entry)
    {
        float k = (maxX - minX);
        float l = (maxY - minY);
        float m = (maxZ - minZ);
        float n = Mth.sqrt(k * k + l * l + m * m);
        k /= n;
        l /= n;
        m /= n;
        consumer.addVertex(entry.pose(), minX, minY, minZ).setColor(r, g, b, a).setNormal(entry.normal(), k, l, m);
        consumer.addVertex(entry.pose(), maxX, maxY, maxZ).setColor(r, g, b, a).setNormal(entry.normal(), k, l, m);
    }

    public static void init()
    {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context ->
        {
            PROJECTION = new Matrix4f(context.projectionMatrix());
            MODEL_VIEW = new Matrix4f(context.matrixStack().last().pose());
        });

        ClientTickEvents.START_CLIENT_TICK.register(client ->
        {
            PLCHudRenderer instance = getInstance();
            if (instance != null)
            {
                instance.clientTick();
            }
        });

        // Rendere the PLC's orange block outline
        WorldRenderEvents.BLOCK_OUTLINE.register((worldRenderContext, blockOutlineContext) ->
        {
            PLCHudRenderer instance = getInstance();
            if (instance != null)
            {
                instance.onOutlineRender(worldRenderContext, blockOutlineContext);
                return false;
            }
            return true;

        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client1) ->
        {
            leave();
        });

        // Screen.passEvents was removed before 1.20, so input events must be processed here while in the scree.
        KeyboardEvents.PRE_INPUT.register((window, key, scancode, action, modifiers) ->
        {
            Minecraft client = Minecraft.getInstance();

            PLCHudRenderer instance = getInstance();
            if (instance != null
                    && client.screen instanceof PLCProgramScreen ps
                    && ps.passEvents())
            {
                // Handle repeats as presses
                instance.handleMovementKey(client, key, action != GLFW.GLFW_RELEASE);
            }

            return false;
        });
    }

    private void handleMovementKey(Minecraft client, int key, boolean pressed)
    {
        InputConstants.Key forward = KeyBindingHelper.getBoundKeyOf(client.options.keyUp);
        InputConstants.Key back = KeyBindingHelper.getBoundKeyOf(client.options.keyDown);
        InputConstants.Key left = KeyBindingHelper.getBoundKeyOf(client.options.keyLeft);
        InputConstants.Key right = KeyBindingHelper.getBoundKeyOf(client.options.keyRight);
        InputConstants.Key up = KeyBindingHelper.getBoundKeyOf(client.options.keyJump);
        InputConstants.Key down = KeyBindingHelper.getBoundKeyOf(client.options.keyShift);

        if (key == forward.getValue())
            robotClient.forwardKey = pressed;
        else if (key == back.getValue())
            robotClient.backKey = pressed;
        else if (key == left.getValue())
            robotClient.leftKey = pressed;
        else if (key == right.getValue())
            robotClient.rightKey = pressed;
        else if (key == up.getValue())
            robotClient.upKey = pressed;
        else if (key == down.getValue())
            robotClient.downKey = pressed;

    }

    public void exit()
    {
        PLCRobotEnterS2C.Client.send(be);
        this.be.exit();
    }

    public boolean onRender()
    {
        return true;
    }

    public void onCameraUpdate(float tickDelta)
    {
        controller.update();

        var robot = be.getSurgeryRobot();

        be.getSurgeryRobot().cameraX = Mth.lerp(0.1d, be.getSurgeryRobot().cameraX, be.getSurgeryRobot().getX());
        be.getSurgeryRobot().cameraY = Mth.lerp(0.1d, be.getSurgeryRobot().cameraY, be.getSurgeryRobot().getY());
        be.getSurgeryRobot().cameraZ = Mth.lerp(0.1d, be.getSurgeryRobot().cameraZ, be.getSurgeryRobot().getZ());

        double realCameraX = be.getSurgeryRobot().cameraX;
        double realCameraY = be.getSurgeryRobot().cameraY;
        double realCameraZ = be.getSurgeryRobot().cameraZ;

        if (MeatLib.vsUtil != null)
        {
            Vector3d pos = new Vector3d(realCameraX, realCameraY, realCameraZ);
            if (MeatLib.vsUtil.hasShipAtPosition(be.getSurgeryRobot().getBasePos(), client.level))
            {
                Matrix4dc shipToWorld = MeatLib.vsUtil.getShipToWorldMatrix(be.getSurgeryRobot().getBasePos(), client.level);
                if (shipToWorld != null)
                {
                    shipToWorld.transformPosition(pos);
                    realCameraX = pos.x;
                    realCameraY = pos.y;
                    realCameraZ = pos.z;
                }
            }
        }

        ((CameraAccessor) camera).callSetPos(realCameraX, realCameraY, realCameraZ);

        ((CameraAccessor) camera).callSetRotation(controller.lerpYaw, controller.lerpPitch);

        ((CameraAccessor) camera).setThirdPerson(true);
    }

    private void onOutlineRender(WorldRenderContext wrctx, WorldRenderContext.BlockOutlineContext boctx)
    {
        if (HIT_RESULT == null)
            return;

        wrctx.matrixStack().pushPose();

        BlockPos pos = HIT_RESULT.getBlockPos();
        Vec3 camPos = camera.getPosition();
        BlockState targetState = client.level.getBlockState(pos);
        VoxelShape shape = targetState.getShape(client.level, pos, CollisionContext.of(client.player));

        Vector3d realPos = new Vector3d(pos.getX(), pos.getY(), pos.getZ());

        if (MeatLib.vsUtil != null)
        {
            MeatLib.vsUtil.CLIENT.transformRenderIfOnShip(wrctx.matrixStack(), new Vector3d(pos.getX(), pos.getY(), pos.getZ()));
            if (MeatLib.vsUtil.hasShipAtPosition(pos, client.level))
            {
                Matrix4dc matrix = MeatLib.vsUtil.getShipToWorldMatrix(pos, client.level);
                if (matrix != null)
                {
                    matrix.transformPosition(realPos);
                }
            }
        }
        else
        {
            wrctx.matrixStack().translate(realPos.x, realPos.y, realPos.z);
        }

        drawCuboidShapeOutline(
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

    private void clientTick()
    {
        if (!client.isPaused())
        {
            robotClient.tick();

//            ((MinecraftClientAccessor) client).callHandleInputEvents();
        }
    }

    public PLCBlockEntity getBlockEntity()
    {
        return be;
    }

    public PLCMotionController getController()
    {
        return controller;
    }
}
