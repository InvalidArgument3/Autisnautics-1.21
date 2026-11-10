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

package com.neep.neepmeat.machine.hydraulic_press;

import com.neep.meatlib.client.flw.MeatModels;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.client.NMExtraModels;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class HydraulicPressVisual extends AbstractBlockEntityVisual<HydraulicPressBlockEntity> implements SimpleDynamicVisual
{
    public static final float MAX_DISPLACEMENT = 8 / 16f + 0.04f;

    private final TransformedInstance arm;

    public HydraulicPressVisual(VisualizationContext ctx, HydraulicPressBlockEntity blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);

        this.arm = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MeatModels.solid(NMExtraModels.HYDRAULIC_PRESS_ARM)).createInstance();
        arm.setIdentityTransform().translate(getVisualPosition()).translate(0, -MAX_DISPLACEMENT * blockEntity.renderExtension, 0)
                .setChanged();
    }

    @Override
    public void _delete()
    {
        arm.delete();
    }

    @Override
    public void beginFrame(DynamicVisual.Context context)
    {
        WritableSingleFluidStorage storage = blockEntity.getStorage(null);
        float extension = storage.getAmount() / (float) HydraulicPressBlockEntity.EXTEND_AMOUNT;
        float prevRenderExtension = blockEntity.renderExtension;
        blockEntity.renderExtension = Mth.lerp(0.1f, blockEntity.renderExtension, extension);

        arm.setIdentityTransform().translate(getVisualPosition()).translate(0, - MAX_DISPLACEMENT * blockEntity.renderExtension, 0);

        if (blockEntity.renderExtension != prevRenderExtension)
            arm.setChanged();
    }

    @Override
    public void updateLight(float v)
    {
        relight(pos, arm);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {

    }

   
}
