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

package com.neep.neepmeat.transport.client.screen;

import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.auto.BlackBackgroundPanel;
import com.neep.meatlib.client.screen.auto.SplitPanel;
import com.neep.meatlib.client.screen.button.NMButtonWidget;
import com.neep.meatlib.client.screen.widget.IntNumberTextField;
import com.neep.meatlib.client.screen.widget.config.ConfigPanel;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.transport.block.fluid_transport.entity.FluidUnit;
import com.neep.neepmeat.transport.screen.LimiterValveScreenHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class LimiterValveScreen extends AbstractContainerScreen<LimiterValveScreenHandler>
{
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/gui/limiter_valve.png");
    protected static final Component RATE = Component.translatable("screen." + NeepMeat.NAMESPACE + ".limiter_valve.text.rate");
    protected static final Component UNIT = Component.translatable("screen." + NeepMeat.NAMESPACE + ".limiter_valve.text.unit");
    protected static final Component DROPLET_MODE = Component.translatable("screen." + NeepMeat.NAMESPACE + ".limiter_valve.text.droplet");
    protected static final MutableComponent DROPLET_MODE_INFO = Component.translatable("screen." + NeepMeat.NAMESPACE + ".limiter_valve.text.droplet_info").withStyle(ChatFormatting.GRAY);
    protected static final Component MB_MODE = Component.translatable("screen." + NeepMeat.NAMESPACE + ".limiter_valve.text.mb");
    protected static final MutableComponent MB_MODE_INFO = Component.translatable("screen." + NeepMeat.NAMESPACE + ".limiter_valve.text.mb_info").withStyle(ChatFormatting.GRAY);
    protected static final Component EJ_MODE = Component.translatable("screen." + NeepMeat.NAMESPACE + ".limiter_valve.text.ej_t");
    protected static final MutableComponent EJ_MODE_INFO = Component.translatable("screen." + NeepMeat.NAMESPACE + ".limiter_valve.text.ej_t_info").withStyle(ChatFormatting.GRAY);

    private final ConfigFields configFields;

    public LimiterValveScreen(LimiterValveScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 87;
        configFields = new ConfigFields(handler.widgetHandler);
        ScreenReady.ready();
    }

    @Override
    protected void init()
    {
        super.init();

        var main = new BlackBackgroundPanel(
                new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                    .setLeft(configFields.modeWidget)
                    .setRight(configFields.numberField)
        );

        addRenderableWidget(main);
        main.setDims(leftPos + 4, topPos + 4, imageWidth - 8, imageHeight / 2);
    }

    public static int toDroplets(int value, FluidUnit unit)
    {
        return switch (unit)
        {
            case DROPLETS -> value;
            case MILLIBUCKETS -> value * 81;
            case EJ_T -> value / 2;
        };
    }

    public static int toUnit(int droplets, FluidUnit unit)
    {
        return switch (unit)
        {
            case DROPLETS -> droplets;
            case MILLIBUCKETS -> droplets / 81;
            case EJ_T -> droplets * 2;
        };
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics matrices, float delta, int mouseX, int mouseY)
    {
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
//        RenderSystem.setShaderTexture(0, TEXTURE);
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        matrices.blit(TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int button, ClickType actionType)
    {
        super.slotClicked(slot, slotId, button, actionType);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount)
    {
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    protected void renderLabels(GuiGraphics matrices, int mouseX, int mouseY)
    {
        GUIUtil.drawText(matrices, font, this.title, this.inventoryLabelX, this.titleLabelY, 0x404040,false);
    }

    private static class ConfigFields extends ConfigPanel<LimiterValveScreenHandler.Values>
    {
        private final NMButtonWidget modeWidget;
        private final IntNumberTextField numberField;
        private FluidUnit currentMode = FluidUnit.DROPLETS;

        public ConfigFields(ConfigWidgetHandler<LimiterValveScreenHandler.Values> handler)
        {
            super(handler);

            numberField = (IntNumberTextField) addTrackedField(new IntNumberTextField(this.textRenderer, 0, 0, 100, PLCCols.ENTRY_H, Component.empty(), c -> update(),
                    0, Integer.MAX_VALUE, 10, 100).tooltip(RATE));

            modeWidget = addTrackedField(new NMButtonWidget(0, 0, 100, PLCCols.ENTRY_H, getButtonText(), button -> {})
            {
                @Override
                public void onPress()
                {
                    super.onPress();
                    FluidUnit oldMode = currentMode;
                    currentMode = FluidUnit.values()[(oldMode.ordinal() + 1) % FluidUnit.values().length];

                    update();
                }
            }.showBackground(false));
        }

        @Override
        protected LimiterValveScreenHandler.Values createValues()
        {
            return new LimiterValveScreenHandler.Values(numberField.parse(), currentMode);
        }

        @Override
        protected void updateParams(LimiterValveScreenHandler.Values values)
        {
            numberField.setNumberText(values.maxAmount());
//            updateText(currentMode, values.mode());
            currentMode = values.mode();
            modeWidget.setMessage(getButtonText());
        }

        // Doesn't work anymore
//        protected void updateText(FluidUnit oldMode, FluidUnit newMode)
//        {
//            if (oldMode != newMode)
//            {
//                String oldText = numberField.text();
//                int parsed = !oldText.isEmpty() && oldText.matches("[0-9]*") ? Integer.parseInt(oldText) : 0;
//
//                int newValue = toUnit(toDroplets(parsed, oldMode), newMode);
//
//                numberField.setText(String.valueOf(newValue));
//            }
//        }

        protected List<Component> getButtonTooltip()
        {
            return List.of(
                    UNIT,
                    switch (currentMode)
                    {
                        case DROPLETS -> DROPLET_MODE_INFO;
                        case MILLIBUCKETS -> MB_MODE_INFO;
                        case EJ_T -> EJ_MODE_INFO;
                    }
            );
        }

        protected Component getButtonText()
        {
            return switch (currentMode)
            {
                case DROPLETS -> DROPLET_MODE;
                case MILLIBUCKETS -> MB_MODE;
                case EJ_T -> EJ_MODE;
            };
        }
    }
}