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

import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.client.screen.BaseHandledScreen;
import com.neep.neepmeat.client.screen.plc.PLCScreenButton;
import com.neep.neepmeat.client.screen.util.Border;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.neepmeat.screen_handler.AdvancedRouterScreenHandler;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public class AdvancedRouterScreen extends BaseHandledScreen<AdvancedRouterScreenHandler>
{
    private Border border = new Border(0, 0, 0, 0, 0, () -> 0);

    public AdvancedRouterScreen(AdvancedRouterScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
    }

    @Override
    protected void init()
    {
        this.imageWidth = 17 * 6 + 5;
        this.imageHeight = 50;

        super.init();
        this.border = addRenderableOnly(new Border(leftPos, topPos, imageWidth, imageHeight, 3, () -> PLCCols.BORDER.col));
        Rectangle withoutPadding = border.withoutPadding();

        for (int i = 0; i < Direction.values().length; ++i)
        {
            Direction direction = Direction.values()[i];
            addRenderableWidget(new OpenFilterWidget(withoutPadding.x0() + i * 17, withoutPadding.y1() - 16,
                    Component.empty(), direction,
                    this::openFilter, () -> menu.notEmpty(direction)));
        }
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY)
    {
        renderBackground(context);
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY)
    {
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        Rectangle withoutPadding = border.withoutPadding();
        GUIUtil.drawCenteredText(context, font,
                NeepMeat.translationKey("screen", "advanced_router.choose_side"),
                withoutPadding.x0() + withoutPadding.nw() / 2f, withoutPadding.y0(),
                PLCCols.TEXT.col, true);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int button, ClickType actionType)
    {
        super.slotClicked(slot, slotId, button, actionType);
    }

    private void openFilter(Direction direction)
    {
        menu.openFilterC2S.emitter().apply(direction.ordinal());
    }

    private static class OpenFilterWidget extends PLCScreenButton
    {
        private final Direction direction;
        private final Consumer<Direction> onClick;
        private final BooleanSupplier notEmpty;

        public OpenFilterWidget(int x, int y, Component message, Direction direction, Consumer<Direction> onClick, BooleanSupplier notEmpty)
        {
            super(x, y, message);
            this.direction = direction;
            this.onClick = onClick;
            this.notEmpty = notEmpty;
        }

        @Override
        protected int getU()
        {
            return direction.ordinal() * 16;
        }

        @Override
        protected int getYImage(boolean hovered)
        {
            if (hovered)
                return 2;

            if (!notEmpty.getAsBoolean())
                return 0;

            return 1;
        }

        @Override
        protected int getV()
        {
            return 48;
        }

        @Override
        public void onClick(double mouseX, double mouseY)
        {
            super.onClick(mouseX, mouseY);
            onClick.accept(direction);
        }
    }
}
