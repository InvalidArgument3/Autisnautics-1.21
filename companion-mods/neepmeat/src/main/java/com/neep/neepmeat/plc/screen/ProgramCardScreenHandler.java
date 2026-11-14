package com.neep.neepmeat.plc.screen;

import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.RecordParamCodec;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.neepmeat.component.ProgramStorageComponent;
import com.neep.neepmeat.init.NMScreenHandlers;
import com.neep.neepmeat.screen_handler.BasicScreenHandler;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class ProgramCardScreenHandler extends BasicScreenHandler
{
    public final CardWidgetHandler handler;

    public ProgramCardScreenHandler(Inventory playerInventory, int syncId, ProgramStorageComponent component)
    {
        super(NMScreenHandlers.PROGRAM_CARD, playerInventory, syncId);
        this.handler = addWidgetHandler(new CardWidgetHandler(playerInventory.player, component));
    }

    public ProgramCardScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, syncId, null);
    }

    public static class CardWidgetHandler extends ConfigWidgetHandler<CardWidgetHandler.Values>
    {
        private final ProgramStorageComponent component;

        protected CardWidgetHandler(Player player, ProgramStorageComponent component)
        {
            super(player, Values.PARAM_CODEC);
            this.component = component;
        }

        @Override
        protected Values getValues()
        {
            return new Values(component.getName());
        }

        @Override
        protected void setValues(Values values)
        {
            component.setName(values.name);
        }

        public record Values(String name)
        {
            public static final ParamCodec<Values> PARAM_CODEC = RecordParamCodec.builder(Values.class)
                    .param(ParamCodec.STRING)
                    .build();
        }
    }
}
