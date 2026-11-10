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

package com.neep.neepmeat.machine.pylon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.neep.meatlib.client.flw.MeatModels;
import com.neep.neepmeat.client.NMExtraModels;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Environment(value = EnvType.CLIENT)
public class PylonVisual extends AbstractBlockEntityVisual<PylonBlockEntity> implements SimpleDynamicVisual
{
    private final PoseStack matrices = new PoseStack();
    private final TransformedInstance rotor;
    private final TransformedInstance activeRotor;

    public PylonVisual(VisualizationContext ctx, PylonBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);

        matrices.translate(getVisualPosition().getX(), getVisualPosition().getY(), getVisualPosition().getZ());

        rotor = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.P_PYLON_ROTOR)).createInstance();
        activeRotor = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.P_PYLON_ACTIVE_ROTOR)).createInstance();
        rotor.translate(getVisualPosition()).setChanged();
    }

    public void beginFrame(Context context)
    {
        matrices.pushPose();
        float renderTimt = Minecraft.getInstance().getFrameTimeNs();
        float xOff = blockEntity.isUnstable() ? 0.5f + 0.05f * Mth.sin(renderTimt) : 0.5f;
        matrices.translate(xOff, 0.5, 0.5);
        float delta = !Minecraft.getInstance().isPaused() ? Minecraft.getInstance().getDeltaFrameTime() : 0;
        blockEntity.angle = Mth.wrapDegrees(blockEntity.angle + delta * blockEntity.getSpeed());
        matrices.mulPose(Axis.YP.rotationDegrees(blockEntity.angle));
        matrices.translate(-xOff, -0.5, -0.5);

        if (blockEntity.isRunning())
        {
            rotor.setIdentityTransform().scale(0, 0, 0);
            activeRotor.setTransform(matrices);
        }
        else
        {
            activeRotor.setIdentityTransform().scale(0, 0, 0);
            rotor.setTransform(matrices);
        }

        rotor.setChanged();
        activeRotor.setChanged();

        matrices.popPose();
    }

    @Override
    public void updateLight(float v)
    {
//        int blight = blockEntity.getWorld().getLightLevel(LightType.BLOCK, getVisualPosition());
//        int slight = blockEntity.getWorld().getLightLevel(LightType.SKY, getVisualPosition());
        relight(getVisualPosition(), rotor, activeRotor);
//        relight(getVisualPosition(), rotor);
//        relight(getVisualPosition(), activeRotor);
    }

    @Override
    public void _delete()
    {
        rotor.delete();
        activeRotor.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

   
}
