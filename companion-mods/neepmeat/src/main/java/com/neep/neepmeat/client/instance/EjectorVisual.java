package com.neep.neepmeat.client.instance;

import com.mojang.math.Axis;
import com.neep.meatlib.client.flw.MeatModels;
import com.neep.neepmeat.client.NMExtraModels;
import com.neep.neepmeat.transport.block.item_transport.machine.EjectorBlockEntity;
import com.neep.neepmeat.transport.block.item_transport.machine.ItemPumpBlock;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class EjectorVisual<T extends EjectorBlockEntity> extends AbstractBlockEntityVisual<T> implements SimpleDynamicVisual
{
    private final TransformedInstance shuttle;

    public EjectorVisual(VisualizationContext ctx, T blockEntity, float partialTick)
    {
        super(ctx, blockEntity, partialTick);
        this.shuttle = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED,
                MeatModels.solid(NMExtraModels.P_ITEM_PUMP_SHUTTLE, true)).createInstance();

        this.shuttle.setIdentityTransform().translate(getVisualPosition()).setChanged();

    }

    @Override
    public void beginFrame(Context context)
    {
        Direction facing = blockEntity.getBlockState().getValue(ItemPumpBlock.FACING);
        var vec = blockEntity.getBlockState().getValue(ItemPumpBlock.FACING).step();

        shuttle.setIdentityTransform()
                .translate(getVisualPosition()).center();

        blockEntity.offset = (float) Mth.lerp(0.3, blockEntity.offset, blockEntity.shuttle > 0 ? (float) 0.2 : 0);

        vec.mul((float) blockEntity.offset, (float) blockEntity.offset, (float) blockEntity.offset);
        shuttle.translate(vec.x, vec.y, vec.z);

        shuttle.rotate(Axis.YN.rotationDegrees(facing.toYRot()));
        if (facing == Direction.DOWN)
        {
            shuttle.rotate(Axis.YP.rotationDegrees(90));
            shuttle.rotate(Axis.XN.rotationDegrees(-90));
        }
        else if (facing == Direction.UP)
        {
            shuttle.rotate(Axis.YP.rotationDegrees(90));
            shuttle.rotate(Axis.XN.rotationDegrees(90));
        }

        shuttle.uncenter();
        shuttle.setChanged();
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer)
    {
        consumer.accept(shuttle);
    }

    @Override
    public void updateLight(float v)
    {
        relight(getVisualPosition(), shuttle);
    }

    @Override
    protected void _delete()
    {
        shuttle.delete();
    }
}
