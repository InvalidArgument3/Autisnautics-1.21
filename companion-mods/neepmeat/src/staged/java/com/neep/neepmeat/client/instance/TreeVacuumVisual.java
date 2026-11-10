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

package com.neep.neepmeat.client.instance;

import com.neep.meatlib.client.flw.MeatModels;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.machine.live_machine.block.TreeVacuumBlock;
import com.neep.neepmeat.machine.live_machine.block.entity.TreeVacuumBlockEntity;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class TreeVacuumVisual extends AbstractBlockEntityVisual<TreeVacuumBlockEntity> implements SimpleDynamicVisual
{
    private final TransformedInstance lungL;
    private final TransformedInstance lungR;
    private final TransformedInstance plunger;

    public TreeVacuumVisual(VisualizationContext ctx, TreeVacuumBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
        lungL = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.TREE_VACUUM_LUNG)).createInstance();
        lungR = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.TREE_VACUUM_LUNG)).createInstance();
        plunger = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.TREE_VACUUM_PLUNGER)).createInstance();

        lungL.translate(getVisualPosition()).setChanged();
        lungR.translate(getVisualPosition()).setChanged();
        plunger.translate(getVisualPosition()).setChanged();
    }

    @Override
    public void _delete()
    {
        lungL.delete();
        lungR.delete();
        plunger.delete();
    }

    private float easeOutSine(float x)
    {
        return Mth.sin(x * Mth.PI / 2f);
    }

    public void beginFrame(SimpleDynamicVisual.Context context)
    {
        Direction facing = blockEntity.getBlockState().getValue(TreeVacuumBlock.FACING);

        int animationTicks = blockEntity.animationTicks;
//        float t = MathHelper.lerp(AnimationTickHolder.getPartialTicks(), animationTicks, animationTicks - 1);
        float t = animationTicks - (animationTicks > 0 ? context.partialTick() : 0);

        float plungerOffset;
//        if (animationTicks < 3)
//            plungerOffset = easeOutSine(1 - t / 3);
//        else
//            plungerOffset = easeOutSine( t / 7);
        if (t > 7)
            plungerOffset = easeOutSine(1 - (t - 7) / 3);
        else
            plungerOffset = easeOutSine((t) / 7);

        float tRad = 2 * Mth.PI * t / 10 + Mth.PI;
        float sf = 1 + 0.2f * (Mth.cos(tRad) + 1) / 2;

        plunger.setIdentityTransform()
                .translate(getVisualPosition())
                .center()
                .rotateToFace(facing)
                .uncenter()
//                .translate(0, 0, 10 / 16f * (MathHelper.cos(tRad) + 1) / 2)
                .translate(0, 0, 7 / 16f * plungerOffset)
        ;

        lungL.setIdentityTransform()
                .translate(getVisualPosition())
                .center()
                .rotateToFace(facing)
                .uncenter()
                .translate(-2 / 16f, 0.5, 0.5)
                .scale(sf, 1f, sf)
                .translate(2 / 16f, -0.5, -0.5)
        ;

        // Look at all these numbers that I arrived at through trial and error!
        lungR.setIdentityTransform().translate(getVisualPosition())
                .center()
                .rotateToFace(facing)
                .uncenter()
                .translate(13 / 16f, 0, 4 / 16f)
                .rotate(Mth.PI, Direction.UP.step())
                .translate(-13 / 16f, 0, -4 / 16f)
                .translate(10 / 16f, 0, 0)
                .translate(-2 / 16f, 0.5, 0 / 16f)
                .scale(sf, 1f, sf)
                .translate(2 / 16f, -0.5, -0 / 16f)
        ;

        plunger.setChanged();
        lungL.setChanged();
        lungR.setChanged();
    }

    @Override
    public void updateLight(float v)
    {
        relight(pos, lungL, lungR, plunger);
    }

    // I put this here because it's the most convenient place.
    public static void initNetwork()
    {
        // I wish I knew how chests sync their animations. This is horrible.
        ClientPlayNetworking.registerGlobalReceiver(TreeVacuumBlockEntity.CHANNEL_ID, (client, handler, buf, responseSender) ->
        {
            BlockPos pos = buf.readBlockPos();
            boolean playSound = buf.readBoolean();
            client.execute(() ->
            {
                if (client.level.getBlockEntity(pos) instanceof TreeVacuumBlockEntity be)
                {
                    be.startAnimation(playSound, client.player);
                }
            });
        });
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

   
}
