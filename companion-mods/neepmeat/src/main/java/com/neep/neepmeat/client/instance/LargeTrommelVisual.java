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
import com.neep.neepmeat.machine.live_machine.block.LargeTrommelBlock;
import com.neep.neepmeat.machine.live_machine.block.entity.LargeTrommelBlockEntity;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class LargeTrommelVisual extends AbstractBlockEntityVisual<LargeTrommelBlockEntity> implements SimpleDynamicVisual
{
    private final TransformedInstance mesh;
    private float rotSpeedRadians;
    private float rotation;

    public LargeTrommelVisual(VisualizationContext ctx, LargeTrommelBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
        this.mesh = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.cutout(NMExtraModels.LARGE_TROMMEL_MESH, true))
                .createInstance()
                .translate(getVisualPosition());
        mesh.setChanged();

    }

    @Override
    public void beginFrame(DynamicVisual.Context context)
    {
        if (!Minecraft.getInstance().isPaused())
        {
            rotSpeedRadians = Mth.lerp(0.1f, rotSpeedRadians, blockEntity.progressIncrement() * 0.1f);
            rotation += rotSpeedRadians;
        }

        float rot = blockEntity.getBlockState().getValue(LargeTrommelBlock.FACING).toYRot() + 180;
        mesh.setIdentityTransform().translate(getVisualPosition())
                .rotateCentered(rot * Mth.PI / 180, Direction.DOWN.step())
                .translate(1, 1, 0)
                .rotate(rotation, Direction.NORTH.step())
                .translate(-1, -1, 0)
        ;

        mesh.setChanged();
    }

    @Override
    public void updateLight(float v)
    {
        relight(pos, mesh);
    }

    @Override
    public void _delete()
    {
        this.mesh.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

   
}
