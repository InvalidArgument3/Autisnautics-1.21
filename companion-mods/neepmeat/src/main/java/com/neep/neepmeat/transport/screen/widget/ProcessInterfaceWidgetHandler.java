package com.neep.neepmeat.transport.screen.widget;

import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.RecordParamCodec;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.neepmeat.plc.block.entity.ProcessInterfaceBlockEntity;
import net.minecraft.world.entity.player.Player;

public class ProcessInterfaceWidgetHandler extends ConfigWidgetHandler<ProcessInterfaceWidgetHandler.Values>
{
    private final ProcessInterfaceBlockEntity be;

    public ProcessInterfaceWidgetHandler(Player player, ProcessInterfaceBlockEntity be)
    {
        super(player, Values.PARAM_CODEC);
        this.be = be;
    }

    @Override
    protected Values getValues()
    {
        return new Values(be.useInterrupts, be.delayInterrupts, be.recordQueue);
    }

    @Override
    protected void setValues(Values values)
    {
        be.setUseInterrupts(values.useInterrupts, values.delayInterrupts, values.recordQueue);
    }

    public record Values(boolean useInterrupts, boolean delayInterrupts, boolean recordQueue)
    {
        public static final ParamCodec<Values> PARAM_CODEC = RecordParamCodec.builder(Values.class)
                .param(ParamCodec.BOOLEAN).param(ParamCodec.BOOLEAN).param(ParamCodec.BOOLEAN).build();
    }
}
