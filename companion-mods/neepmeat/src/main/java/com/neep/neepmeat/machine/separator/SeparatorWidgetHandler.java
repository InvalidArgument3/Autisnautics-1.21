package com.neep.neepmeat.machine.separator;

import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.RecordParamCodec;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import net.minecraft.world.entity.player.Player;

public class SeparatorWidgetHandler extends ConfigWidgetHandler<SeparatorWidgetHandler.Values>
{
    private final SeparatorBlockEntity be;

    protected SeparatorWidgetHandler(Player player, SeparatorBlockEntity be)
    {
        super(player, Values.PARAM_CODEC);
        this.be = be;
    }

    @Override
    protected Values getValues()
    {
        return be.createValues();
    }

    @Override
    protected void setValues(Values values)
    {
        be.setValues(values);
    }

    public record Values(int radius, boolean takeBabies, int remainder)
    {
        public static final ParamCodec<Values> PARAM_CODEC = RecordParamCodec.builder(Values.class)
                .param(ParamCodec.INT)
                .param(ParamCodec.BOOLEAN)
                .param(ParamCodec.INT)
                .build();
    }
}
