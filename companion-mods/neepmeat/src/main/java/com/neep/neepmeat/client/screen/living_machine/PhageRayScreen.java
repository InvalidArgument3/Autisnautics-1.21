package com.neep.neepmeat.client.screen.living_machine;

import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.auto.BlackBackgroundPanel;
import com.neep.meatlib.client.screen.auto.HashedBackgroundPanel;
import com.neep.meatlib.client.screen.auto.SnapPanel;
import com.neep.meatlib.client.screen.auto.SplitPanel;
import com.neep.meatlib.client.screen.widget.IntNumberTextField;
import com.neep.meatlib.client.screen.widget.config.ConfigPanel;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepbus.client.screen.OpenNEEPBusConfigButton;
import com.neep.neepmeat.screen_handler.PhageRayScreenHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PhageRayScreen extends BetterHandledScreen<PhageRayScreenHandler>
{
    private final ConfigWidget configWidget;

    public PhageRayScreen(PhageRayScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        this.configWidget = new ConfigWidget(handler.widgetHandler);
    }

    @Override
    protected void init()
    {
        imageWidth = PLCCols.INV_W;
        imageHeight = PLCCols.INV_H;

        super.init();

        var main = new HashedBackgroundPanel(new BlackBackgroundPanel(
                new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                        .setLeft(configWidget)
                        .setRight(new SnapPanel<>(SnapPanel.Snap.BOTTOM_RIGHT, new OpenNEEPBusConfigButton(menu.openNeepBusC2S.emitter())))
                        .setFit(SplitPanel.Fit.SECOND)
        ));

        addRenderableWidget(main);
        main.setDims(leftPos, topPos, imageWidth, imageHeight);

        ScreenReady.ready();
    }

    private class ConfigWidget extends ConfigPanel<PhageRayScreenHandler.WidgetHandler.Values>
    {
        private final IntNumberTextField range;

        public ConfigWidget(ConfigWidgetHandler<PhageRayScreenHandler.WidgetHandler.Values> handler)
        {
            super(handler);
            range = addTrackedField(new IntNumberTextField(textRenderer, 0, 0, 100, PLCCols.ENTRY_H, Component.literal("Range"), f -> update(), 16, 128));
        }

        @Override
        protected PhageRayScreenHandler.WidgetHandler.Values createValues()
        {
            return new PhageRayScreenHandler.WidgetHandler.Values(range.parse());
        }

        @Override
        protected void updateParams(PhageRayScreenHandler.WidgetHandler.Values values)
        {
            range.setNumberText(values.range());
        }
    }
}
