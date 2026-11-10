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

package com.neep.neepmeat.client.implant;

import com.neep.meatlib.client.screen.auto.*;
import com.neep.meatlib.client.screen.widget.ColourSelectWidget;
import com.neep.meatlib.client.screen.widget.LabelWidget;
import com.neep.neepmeat.client.screen.ImplantManagerScreen;
import com.neep.neepmeat.client.screen.button.TextToggleButtonWidget;
import com.neep.neepmeat.implant.config.CatEarsConfigHandler;
import com.neep.neepmeat.implant.entity.CatEarsImplant;
import com.neep.neepmeat.screen_handler.ImplantManagerScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

public class CatEarsConfigPanel extends ImplantConfigPanel<CatEarsConfigHandler>
{
    private final ColourSelectWidget outerColSelect;
    private final ColourSelectWidget innerColSelect;
    private final PreviewWidget preview;

    public CatEarsConfigPanel(ImplantManagerScreenHandler screenHandler, ImplantManagerScreen screen)
    {
        super(screenHandler, CatEarsConfigHandler.class);

        outerColSelect = new ColourSelectWidget(screenHandler.getImplantManager().<CatEarsImplant>getImplant(CatEarsImplant.ID).getCol(), c -> configHandler.updateColC2S.emitter().apply(c))
                .preview(this::updatePreview);
        innerColSelect = new ColourSelectWidget(screenHandler.getImplantManager().<CatEarsImplant>getImplant(CatEarsImplant.ID).getInnerCol(), c -> configHandler.updateInnerColC2S.emitter().apply(c))
                .preview(this::updatePreview);
        this.preview = new PreviewWidget();
    }

    @Override
    protected void onInit()
    {
        int entryHeight = 18;
        ScrollVStackPanel buttons = new ScrollVStackPanel(1, 1, true, false);

        buttons.addChild(new TextToggleButtonWidget(0, 0, 100, entryHeight, () -> false,
                Component.nullToEmpty("Use player skin"),
                (button, toggled) ->
                {
                    setSkin(CatEarsImplant.CAT_EARS_PLAYER_SKIN);
                    outerColSelect.set(0xffffffff);
                    innerColSelect.set(CatEarsImplant.INNER_PINK);
                    configHandler.updateColC2S.emitter().apply(0xffffffff);
                    configHandler.updateInnerColC2S.emitter().apply(CatEarsImplant.INNER_PINK);
                })
                .showBackground(false)
        );

        buttons.addChild(new TextToggleButtonWidget(0, 0, 100, entryHeight, () -> false,
                Component.nullToEmpty("White (configurable colour)"),
                (button, toggled) ->
                        setSkin(CatEarsImplant.CAT_EARS_COLOUR))
                .showBackground(false)
        );

        buttons.addChild(new LabelWidget(0, 0, 100, 18, false, Component.nullToEmpty("Outer colour: ")));

        // oof
        buttons.addChild(outerColSelect);

        buttons.addChild(new LabelWidget(0, 0, 100, 18, false, Component.nullToEmpty("Inner colour: ")));

        buttons.addChild(innerColSelect);

        SplitPanel split = new SplitPanel(SplitPanel.Split.HORIZONTAL, 0);
        split.setLeft(buttons);
        split.setRight(new BorderPanel(new PreviewWidget()));
        split.setDivide(new Divide.Rel(0.75f));

        main = split;
    }

    private void setSkin(ResourceLocation skin)
    {
        configHandler.updateAppearanceC2S.emitter().apply(skin);

        CatEarsImplant implant = configHandler.getImplant();
        implant.setAppearance(skin, implant.getCol(), implant.getInnerCol());
    }

    private void updatePreview(ColourSelectWidget widget)
    {
        CatEarsImplant implant = configHandler.getImplant();
        implant.setAppearance(implant.getSkin(), outerColSelect.get(), innerColSelect.get());
    }

    public static class PreviewWidget extends EndpointPanel
    {
        private final Minecraft client;

        public PreviewWidget()
        {
            super();
            client = Minecraft.getInstance();
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            LocalPlayer entity = client.player;
            float copyPitch = entity.getXRot();
            float copyHeadYaw = entity.getYHeadRot();
            float copyYaw = entity.getYRot();
            float copyBodyYaw = entity.yBodyRot;

            context.enableScissor(x, y, x + w, y + h);
            entity.setXRot(0);
            entity.setYHeadRot(0);
            entity.setYBodyRot(0);
            entity.setYRot(0);

            int size = 80;
            Quaternionf quaternion = new Quaternionf().rotateZ(Mth.PI).rotateY(Mth.PI);
            InventoryScreen.renderEntityInInventory(context, x + (w) / 2, y + h / 2 + 120, size,
                    quaternion,
                    null,
                    entity);
            context.disableScissor();

//            renderer.render(context.getMatrices(),
//                    context.getVertexConsumers(),
//                    LightmapTextureManager.MAX_LIGHT_COORDINATE,
//                    client.player,
//                    0, 0,
//                    delta,
//                    0, 0, 0);

            entity.setXRot(copyPitch);
            entity.setYHeadRot(copyHeadYaw);
            entity.setYRot(copyYaw);
            entity.yBodyRot = copyBodyYaw;
        }
    }
}
