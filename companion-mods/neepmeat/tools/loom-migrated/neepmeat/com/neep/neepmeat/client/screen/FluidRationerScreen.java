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

package com.neep.neepmeat.client.screen;

import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.auto.*;
import com.neep.meatlib.client.screen.slot.VanillaInventoryWidget;
import com.neep.meatlib.client.screen.widget.IntNumberTextField;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepbus.client.screen.OpenNEEPBusConfigButton;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.client.screen.button.NMButtonWidget;
import com.neep.neepmeat.client.screen.util.BeigeSlotBlockWidget;
import com.neep.neepmeat.guide.larkdown.ColumnsLarkdownContent;
import com.neep.neepmeat.network.ScreenPropertyC2SPacket;
import com.neep.neepmeat.screen_handler.FluidRationerScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@Environment(value= EnvType.CLIENT)
public class FluidRationerScreen extends BetterHandledScreen<FluidRationerScreenHandler>
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(NeepMeat.NAMESPACE, "textures/gui/fluid_rationer.png");
    protected com.neep.meatlib.client.screen.NMTextField textField;

    protected static final Component BUCKET = Component.translatable("screen." + NeepMeat.NAMESPACE + ".fluid_rationer.text.bucket");
    protected static final Component INGOT = Component.translatable("screen." + NeepMeat.NAMESPACE + ".fluid_rationer.text.ingot");
    protected static final Component BOTTLE = Component.translatable("screen." + NeepMeat.NAMESPACE + ".fluid_rationer.text.bottle");

    protected int slotX;
    protected int amountX;
    protected int amountY;
    protected Component amountText = Component.translatable("screen." + NeepMeat.NAMESPACE + ".fluid_rationer.text.amount");

    public FluidRationerScreen(FluidRationerScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 178;
    }

    @Override
    protected void init()
    {
        super.init();

        this.slotX = leftPos + 80;
        this.amountX = slotX + 18 + 3;
        this.amountY = topPos + 33;

        textField = new IntNumberTextField(this.font, amountX, amountY, 3 * 18, 17, Component.empty(), (c) -> {},
            0, Integer.MAX_VALUE, 10, 100);
        textField.setText(Integer.toString(menu.getProperty(FluidRationerScreenHandler.PROP_TARGET_AMOUNT)));
        textField.tooltip(NeepMeat.translationKey("screen", "fluid_rationer.amount.tooltip"));

        var filterSlot = addSlotBlock(new BeigeSlotBlockWidget(1,  1, menu.filterSlots, this)
                .overlay(GUIUtil.SCREEN_BORDER, 20, 210));

        int h = 20;
        var main = new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                .setFit(SplitPanel.Fit.SECOND)
                .setLeft(new HashedBackgroundPanel(
                        new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                                .setFit(SplitPanel.Fit.FIRST)
                                .setLeft(new VStackPanel(0, 3, false, true)
                                        .addChild(new NMButtonWidget(0, 0, 50, h, INGOT,
                                                button -> textField.setText(String.valueOf(FluidConstants.INGOT))))
                                        .addChild(new NMButtonWidget(0, 0, 50, h, BOTTLE,
                                button -> textField.setText(String.valueOf(FluidConstants.BOTTLE))))
                                        .addChild(new NMButtonWidget(0, 0, 50, h, BUCKET,
                                                button -> textField.setText(String.valueOf(FluidConstants.BUCKET)))))
                                .setRight(new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                                        .setDivide(new Divide.Rel(0.5f))
                                        .setLeft(new GridPanel(2, 1)
                                            .set(0, 0, new SnapPanel<>(filterSlot)))
                                        .setRight(new BlackBackgroundPanel(new GridPanel(2, 1)
                                                .set(0, 0, textField)
                                                .set(0, 1, new SnapPanel<>(SnapPanel.Snap.BOTTOM_RIGHT, new OpenNEEPBusConfigButton(menu.openConfigC2S.emitter())))
                                        ))
                                )
                ))
                .setRight(addSlotBlock(new VanillaInventoryWidget(menu.playerSlots, this)))
                ;

        textField.setChangedListener(s ->
        {
            int parsed;
            try
            {
                parsed =!s.isEmpty() && s.matches("[0-9]*") ? Integer.parseInt(s) : 0;
            }
            catch (NumberFormatException ignored)
            {
                parsed = 0;
            }

            ClientPlayNetworking.send(ScreenPropertyC2SPacket.ID, ScreenPropertyC2SPacket.Client.create(FluidRationerScreenHandler.PROP_TARGET_AMOUNT, parsed));
        });

        addRenderableWidget(main);
        main.setDims(leftPos, topPos, imageWidth, imageHeight);

        this.titleLabelX = 29;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }
}