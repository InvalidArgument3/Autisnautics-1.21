package com.neep.neepmeat.screen_handler;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.api.network.RecordParamCodec;
import com.neep.meatlib.network.ChannelManager;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.neepbus.screen.NeepBusConfigScreenHandler;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMScreenHandlers;
import com.neep.neepmeat.machine.phage_ray.PhageRayBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class PhageRayScreenHandler extends BasicScreenHandler
{
    private final PhageRayBlockEntity be;
    public final WidgetHandler widgetHandler;
    public final ChannelManager<Runnable> openNeepBusC2S;

    public PhageRayScreenHandler(int syncId, Inventory playerInventory)
    {
        this(playerInventory, syncId, null);
    }

    public PhageRayScreenHandler(Inventory playerInventory, int syncId, PhageRayBlockEntity be)
    {
        super(NMScreenHandlers.PHAGE_RAY, playerInventory, null, syncId, null);
        this.be = be;
        this.widgetHandler = addWidgetHandler(new WidgetHandler(playerInventory.player));
        this.openNeepBusC2S = addClosable(ChannelManager.create(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "phage_ray/open_neepbus"),
                ChannelFormat.RUNNABLE, playerInventory.player));

        if (!MeatLib.isClient())
        {
            openNeepBusC2S.receiver(() ->
            {
                playerInventory.player.openMenu(NeepBusConfigScreenHandler.getFactory(be.getConfig()));
            });
        }
    }

    public class WidgetHandler extends ConfigWidgetHandler<WidgetHandler.Values>
    {
        protected WidgetHandler(Player player)
        {
            super(player, RecordParamCodec.builder(Values.class).param(ParamCodec.INT).build());
        }

        @Override
        protected Values getValues()
        {
            return new Values(be.getRange());
        }

        @Override
        protected void setValues(Values values)
        {
            be.setRange(values.range);
        }

        public record Values(int range) {}
    }
}
