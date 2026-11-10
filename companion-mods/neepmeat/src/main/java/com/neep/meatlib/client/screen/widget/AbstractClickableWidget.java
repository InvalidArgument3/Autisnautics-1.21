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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * An implementation of ClickableWidget that should allow consistency between Minecraft versions.
 */
public abstract class AbstractClickableWidget implements ClickableWidget, NarratableEntry, TooltipRenderer
{
    protected final Minecraft client = Minecraft.getInstance();

    public boolean active = true;
    public boolean visible = true;
    protected int width;
    protected int height;
    protected boolean hovered;
    protected float alpha = 1.0F;
    private int x;
    private int y;
    private Component message = Component.empty();
    private boolean focused;

    @Nullable private Supplier<List<Component>> tooltip;
    protected int tooltipDelayMs = 500;
    private long lastHoveredTimeMs;

    public AbstractClickableWidget(int x, int y, int width, int height, Component message)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.message = message;
    }

    /**
     * For widgets that use auto-layout
     */
    public AbstractClickableWidget()
    {
    }

    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        if (this.visible)
        {
            if (this.hovered != isWithin(mouseX, mouseY))
            {
                this.hovered = !this.hovered;

                if (hovered)
                    lastHoveredTimeMs = Util.getMillis();
            }
            this.renderWidget(context, mouseX, mouseY, delta);
        }
    }

    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {}

    @Override
    public void renderTooltipLayer(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        if (tooltip != null && isWithin(mouseX, mouseY))
        {
            if (hovered && Util.getMillis() >= lastHoveredTimeMs + tooltipDelayMs)
            {
                // Draw tooltip
                List<Component> wrapped = new ObjectArrayList<>();
                tooltip.get().forEach(t -> TooltipHelper.wrapLines(wrapped, t));
                TooltipHelper.get().draw(context, mouseX, mouseY, wrapped);
            }
        }
    }

    public void onClick(double mouseX, double mouseY)
    {
    }

    public void onRelease(double mouseX, double mouseY)
    {
    }

    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY)
    {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (this.active && this.visible)
        {
            if (this.isValidClickButton(button))
            {
                boolean bl = this.clicked(mouseX, mouseY);
                if (bl)
                {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onClick(mouseX, mouseY);
                    return true;
                }
            }

        }
        return false;
    }

    protected void playDownSound(SoundManager soundManager)
    {
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (this.isValidClickButton(button))
        {
            this.onRelease(mouseX, mouseY);
            return true;
        }
        else
        {
            return false;
        }
    }

    protected boolean isValidClickButton(int button)
    {
        return button == 0;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        if (this.isValidClickButton(button))
        {
            this.onDrag(mouseX, mouseY, deltaX, deltaY);
            return true;
        }
        else
        {
            return false;
        }
    }

    protected boolean clicked(double mouseX, double mouseY)
    {
        return active
                && visible
                && isWithin(mouseX, mouseY);
    }

    public boolean isHovered()
    {
        return hovered;
    }

    @Override
    public boolean isActive()
    {
        return visible && active;
    }

    public Component getMessage()
    {
        return this.message;
    }

    @Override
    public final void updateNarration(NarrationElementOutput builder)
    {
//        this.appendClickableNarrations(builder); // ???
        if (this.tooltip != null)
        {
            // Argh
//            this.tooltip.get().updateNarration(builder);
        }
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority()
    {
        if (this.isFocused())
        {
            return NarratableEntry.NarrationPriority.FOCUSED;
        }
        else
        {
            return this.hovered ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
        }
    }

    @Override
    public boolean isFocused()
    {
        return focused;
    }

    @Override
    public void setFocused(boolean focused)
    {
        this.focused = focused;
    }

    public boolean isSelected()
    {
        return hovered || focused;
    }

    @Override
    public int x0()
    {
        return x;
    }

    @Override
    public int y0()
    {
        return y;
    }

    @Override
    public int nw()
    {
        return width;
    }

    @Override
    public int nh()
    {
        return height;
    }

    @Override
    public void setPos(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public AbstractClickableWidget tooltip(List<Component> toot)
    {
        // toot
        this.tooltip = () -> toot;
        return this;
    }

    public AbstractClickableWidget tooltip(Component tootText)
    {
        this.tooltip = () -> List.of(tootText);
        return this;
    }

    public AbstractClickableWidget tooltipSingle(Supplier<Component> tootText)
    {
        this.tooltip = () -> List.of(tootText.get());
        return this;
    }

    public AbstractClickableWidget tooltip(Supplier<List<Component>> tootText)
    {
        this.tooltip = tootText;
        return this;
    }
}
