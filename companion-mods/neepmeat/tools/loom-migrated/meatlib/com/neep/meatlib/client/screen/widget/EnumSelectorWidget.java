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

package com.neep.meatlib.client.screen.widget;

import com.neep.meatlib.client.screen.TooltipHelper;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.theme.PLCCols;
import com.neep.meatlib.client.screen.button.NMButtonWidget;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class EnumSelectorWidget<T extends Enum<T>> extends NMButtonWidget implements TooltipRenderer
{
    private final TooltipHelper tooltipUser;
    private final Component name;
    private final Supplier<T> current;
    private final EnumPressAction<T> enumOnPress;

    private final T[] constants;

    private Tooltip<T> tooltip;

    public EnumSelectorWidget(int x, int y, int width, int height, T[] constants, Component name, Supplier<T> current, EnumPressAction<T> onPress)
    {
        super(x, y, width, height, Component.empty(), button -> {});
        this.tooltipUser = new TooltipHelper(false, PLCCols.TEXT.col, false);
        this.name = name;
        this.current = current;
        this.enumOnPress = onPress;
        this.constants = constants;
        showBackground(false);

        tooltip = t -> TooltipSupplier.wrapLines(new ObjectArrayList<>(), this.name);
    }

    public EnumSelectorWidget<T> tooltip(Tooltip<T> tooltip)
    {
        this.tooltip = tooltip;
        return this;
    }

    @Override
    public void onPress()
    {
        super.onPress();
        T newValue = constants[(current.get().ordinal() + 1) % constants.length];
        enumOnPress.onPress(this, newValue);
    }

    public T getCurrent()
    {
        return current.get();
    }

    @Override
    public Component getMessage()
    {
        return Component.nullToEmpty(current.get().name());
    }

    @Override
    public void renderWidget(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        super.renderWidget(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void renderTooltipLayer(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        if (isWithin(mouseX, mouseY))
        {
            List<Component> tooltipText = tooltip.get(current.get());
            tooltipUser.draw(context, this.x0(), this.y0() + nh(), tooltipText);
        }
    }

    public interface EnumPressAction<T extends Enum<T>>
    {
        void onPress(EnumSelectorWidget<T> button, T newValue);
    }

    public interface Tooltip<T>
    {
        List<Component> get(T value);
    }
}
