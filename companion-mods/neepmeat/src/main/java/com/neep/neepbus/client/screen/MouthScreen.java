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

package com.neep.neepbus.client.screen;

import com.neep.meatlib.client.screen.auto.*;
import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.widget.CheckboxWidget;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.meatlib.client.screen.widget.IntNumberTextField;
import com.neep.meatlib.client.screen.widget.config.ConfigPanel;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepbus.block.entity.MouthBlockEntity;
import com.neep.neepmeat.client.screen.widget.MultiLineConfigTextField;
import com.neep.neepbus.screen.MouthScreenHandler;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.theme.PLCCols;
import com.neep.meatlib.client.screen.button.NMButtonWidget;
import com.neep.neepmeat.client.screen.button.OpenGuideButton;
import com.neep.neepmeat.client.sound.NEEPSpeakSoundInstance;
import com.neep.neepspeak.NEEPSpeakException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MouthScreen extends BetterHandledScreen<MouthScreenHandler>
{
    private final HashedBackgroundPanel main;
    private final MouthConfigPanel config;

    public MouthScreen(MouthScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        config = new MouthConfigPanel(handler.handler);

        var neepBusButton = new OpenNEEPBusConfigButton(() -> handler.openConfigC2S.emitter().run());
        var guideButton = new OpenGuideButton("automatic_mouth");

        this.main = new HashedBackgroundPanel(new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                .separation()
                .setLeft(new BlackBackgroundPanel(
                        new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                                .setDivide(new Divide.Abs(Anchor.END, 16))
                                .setLeft(config.textField)
                                .setRight(new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                                        .setDivide(new Divide.Abs(Anchor.START, 32))
                                        .setLeft(new NMButtonWidget(0, 0, 100, 16,
                                                NeepMeat.translationKey("screen", "mouth.trigger"),
                                                b ->
                                                {
                                                    config.update();
                                                    handler.handler.sayC2S.emitter().run();
                                                }).showBackground(false))
                                        .setRight(new BorderPanel(config.errorField))
                                        .separation()
                                )
                ))
                .setRight(new BlackBackgroundPanel(new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                        .setDivide(new Divide.Abs(Anchor.END, 16))
                        .setLeft(config)
                        .setRight(new HStackPanel(0, 1).addChild(guideButton).addChild(neepBusButton))
                )
        ));
    }

    @Override
    protected void init()
    {
        imageWidth = 200;
        imageHeight = 166;

        super.init();

        addRenderableWidget(main);
        main.setDims(leftPos, topPos, imageWidth, imageHeight);

        ScreenReady.ready();
    }

    public static class MouthConfigPanel extends ConfigPanel<MouthScreenHandler.Values>
    {
        public final MultiLineConfigTextField textField;
        private final ErrorField errorField = new ErrorField();

        private final IntNumberTextField radius;

        private boolean setAutoSay;
        private boolean setPhonetic;

        public MouthConfigPanel(MouthScreenHandler.MouthConfigPanelHandler handler)
        {
            super(handler);
            this.textField = new MultiLineConfigTextField(0, 0, 100, 18, w -> update());
//            textField.drawFancyBackground(false);

            CheckboxWidget autoSay = addTrackedField(new CheckboxWidget(
                    100, 12, () -> setAutoSay,
                    NeepMeat.translationKey("screen", "mouth.auto_say"),
                    (button, toggled) ->
                    {
                        setAutoSay = toggled;
                        update();
                    }));

            CheckboxWidget phonetic = addTrackedField(new CheckboxWidget(
                    100, 12, () -> setPhonetic,
                    NeepMeat.translationKey("screen", "mouth.phonetic"),
                    (button, toggled) ->
                    {
                        setPhonetic = toggled;
                        update();
                    }));

            this.radius = addTrackedField(new IntNumberTextField(textRenderer, 0 , 0, 100, 12, NeepMeat.translationKey("screen", "mouth.radius"),
                    f -> update(), 0, 50));

            handler.errorS2C.receiver(this::setError);
        }

        private void setError(String error)
        {
            errorField.error = error;
        }

        public void play()
        {
            Minecraft client = Minecraft.getInstance();
            try
            {
                client.getSoundManager().play(
                        new NEEPSpeakSoundInstance(
                                MouthBlockEntity.parse(textField.parse(), setPhonetic),
                                client.player.getEyePosition(), false));
            }
            catch (NEEPSpeakException e)
            {
                setError(e.getMessage());
            }
        }

        @Override
        protected MouthScreenHandler.Values createValues()
        {
            return new MouthScreenHandler.Values(
                    textField.parse(),
                    setAutoSay,
                    setPhonetic,
                    radius.parse()
            );
        }

        @Override
        protected void updateParams(MouthScreenHandler.Values values)
        {
            textField.setTextSilent(values.message());
            setAutoSay = values.autoSay();
            setPhonetic = values.phonetic();
            radius.setNumberText(values.radius());
        }

        private class ErrorField extends SimplePanel
        {
            private String error = "";

            @Override
            protected ClickableWidget initMainElement()
            {
                return EndpointPanel.INSTANCE;
            }

            @Override
            public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
            {
                super.render(context, mouseX, mouseY, delta);

                context.enableScissor(this.x0(), this.y0(), x1(), y1());
                Component text = error.isEmpty() ? NeepMeat.translationKey("screen", "mouth.no_error") : Component.nullToEmpty(error);
                GUIUtil.drawText(context, textRenderer, text, x + 1, y + (nh() - textRenderer.lineHeight) / 2f, PLCCols.TEXT.col, false);
                context.disableScissor();
            }
        }
    }
}
