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

package com.neep.neepmeat.machine.fabricator;

import com.neep.meatlib.client.flw.MeatModels;
import com.neep.neepmeat.client.NMExtraModels;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class FabricatorVisual extends AbstractBlockEntityVisual<FabricatorBlockEntity> implements SimpleDynamicVisual
{
    private final TransformedInstance rotor;
    private final List<TransformedInstance> segments;

    private float speed;
    private float mainAngle;

    private final Minecraft client;

    public FabricatorVisual(VisualizationContext ctx, FabricatorBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
        this.client = Minecraft.getInstance();
        rotor = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.FABRICATOR_ROTOR, true)).createInstance();
        segments = IntStream.range(0, 8).mapToObj(i ->
                ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.FABRICATOR_SEGMENT, true)).createInstance()).toList();

        rotor.setIdentityTransform()
                .translate(getVisualPosition())
                .translate(0, 1, 0)
                .rotateCentered(mainAngle, Direction.UP.step())
                .setChanged();

        updateBits(true);
    }

    @Override
    public void _delete()
    {
        rotor.delete();
        for (var segment : segments)
        {
            segment.delete();
        }
    }

    private void updateBits(boolean changed)
    {
        for (int i = 0; i < segments.size(); ++i)
        {
            float angle = Mth.DEG_TO_RAD * (((i / 8f) * 360) % 360) + mainAngle;

            float yOffset = 0.1f * ((Mth.sin(mainAngle * (i - 8 * Mth.sin(mainAngle / 100f) - 4))));

            var segment = segments.get(i);
            segment.setIdentityTransform()
                    .translate(getVisualPosition())
                    .translate(0, 1 - yOffset , 0)
                    .rotateCentered(angle, Direction.UP.step())
            ;

            if (changed)
                segment.setChanged();
        }

    }

    @Override
    public void beginFrame(DynamicVisual.Context context)
    {
        boolean paused = client.isPaused();
        speed = Mth.lerp(paused ? 0 : 0.1f, speed, blockEntity.animation ? 10 : 0);

        if (blockEntity.animation)
            blockEntity.animation = false;

        float oldMainAngle = mainAngle;
        mainAngle += paused ? 0 : client.getTimer().getGameTimeDeltaPartialTick(true) * speed;
        boolean changed = mainAngle != oldMainAngle;

        rotor.setIdentityTransform()
                .translate(getVisualPosition())
                .translate(0, 1, 0)
                .rotateCentered(mainAngle, Direction.UP.step())
        ;

        updateBits(changed);

        if (changed)
            rotor.setChanged();
    }

    @Override
    public void updateLight(float v)
    {
        relight(pos, (Iterator<FlatLit>) (Iterator<?>) segments.iterator());
        relight(pos, rotor);
    }

    // I put this here because it's the most convenient place.
    public static void initNetwork()
    {
        // I wish I knew how chests sync their animations. This is horrible.
        ClientPlayNetworking.registerGlobalReceiver(FabricatorBlockEntity.CHANNEL_ID, (client, handler, buf, responseSender) ->
        {
            BlockPos pos = buf.readBlockPos();
            client.execute(() ->
            {
                if (client.level.getBlockEntity(pos) instanceof FabricatorBlockEntity fabricator)
                {
                    fabricator.animation = true;
                }
            });
        });
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

   
}
