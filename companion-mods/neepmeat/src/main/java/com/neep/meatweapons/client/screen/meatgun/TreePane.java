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

package com.neep.meatweapons.client.screen.meatgun;

import com.mojang.blaze3d.systems.RenderSystem;
import com.neep.meatlib.client.RotationAxis;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.TooltipHelper;
import com.neep.meatlib.client.screen.primitive.MutableRectangle;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.init.MWComponents;
import com.neep.meatweapons.item.meatgun.MeatgunModuleItem;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.meatweapons.meatgun.module.MeatgunModules;
import com.neep.meatweapons.meatgun.module.ModuleSlot;
import com.neep.meatweapons.meatgun.module.SimpleModuleSlot;
import com.neep.meatweapons.screen.TinkerTableScreenHandler;
import com.neep.meatlib.theme.PLCCols;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

class TreePane extends TinkerTableScreen.PaneWidget
{
    private final Font textRenderer = Minecraft.getInstance().font;
    private final List<ModuleWidget> moduleWidgets = new ArrayList<>();
    private final TinkerTableScreenHandler handler;
    private final Slot slot;
    @Nullable private MeatgunComponent meatgun;
    private boolean scissor;

    private float ox, oy;

//    private final Sender<TinkerTableScreenHandler.SlotClick> sender;

    public TreePane(TinkerTableScreenHandler handler, Slot itemSlot)
    {
        this.handler = handler;
        this.slot = itemSlot;
//        this.sender = new ClientChannelSender<>(TinkerTableScreenHandler.CHANNEL_ID, TinkerTableScreenHandler.CHANNEL_FORMAT);
    }

    @Override
    public void init()
    {
        super.init();
        ox = x0() + (float) nw() / 2;
        oy = y0() + 10;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        enableScissor(context);
        for (var moduleWidget : moduleWidgets)
        {
            moduleWidget.render(context, mouseX, mouseY, delta);
        }
        disableScissor(context);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
//        if (button == GLFW.GLFW_MOUSE_BUTTON_2 || button == GLFW.GLFW_MOUSE_BUTTON_3)
        {
            double thing = 1;
            ox += (float) (deltaX * thing);
            oy += (float) (deltaY * thing);

            updateWidgetOrigins();
            return true;
        }
//        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private void updateWidgetOrigins()
    {
        for (var widget : moduleWidgets)
        {
            widget.updateOrigin(Math.round(ox), Math.round(oy));
        }
    }

    private void reorganise()
    {
        moduleWidgets.clear();
        if (meatgun == null)
            return;

        List<List<ModuleWidget>> levels = new ArrayList<>();
        MeatgunModule root = meatgun.getRootHolder().root;
        process(levels, 0, root, null);

        int thingHeight = ModuleWidget.height() + 5;
        int offsetY = 0;

        for (var level : levels)
        {
            int totalWidth = 0;
            int padding = 4;
            for (var widget : level)
            {
                widget.updateSize();
                totalWidth += widget.nw() + padding;
            }

            int offsetX = -totalWidth / 2;
            for (var widget : level)
            {
                widget.init(offsetX, offsetY);
                offsetX += widget.nw() + padding;

                moduleWidgets.add(widget);
            }

            offsetY += thingHeight;
        }
        updateWidgetOrigins();
    }

    private void process(List<List<ModuleWidget>> levels, int level, MeatgunModule module, @Nullable ModuleSlotWidget parentSlotWidget)
    {
        if (levels.size() == level)
        {
            levels.add(new ArrayList<>());
        }

        List<ModuleSlotWidget> slotWidgets = new ObjectArrayList<>();
        for (int i = 0; i < module.getChildren().size(); i++)
        {
            var slot = module.getChildren().get(i);
            ModuleSlotWidget slotWidget = new ModuleSlotWidget(textRenderer, module, i, slot);
            slotWidgets.add(slotWidget);

            if (slot.get() == MeatgunModule.DEFAULT)
                continue;

            process(levels, level + 1, slot.get(), slotWidget);
        }

        ModuleWidget widget = new ModuleWidget(textRenderer, module, slotWidgets, parentSlotWidget);
        levels.get(level).add(widget);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (isWithin(mouseX, mouseY))
        {
            for (var widget : moduleWidgets)
            {
                if (widget.mouseClicked(mouseX, mouseY, button))
                    return true;
            }
        }
        return false;
    }

    private void enableScissor(GuiGraphics context)
    {
        context.enableScissor(x0() + 2, y0() + 2, x0() + nw() - 2, y0() + nh() - 2);
        scissor = true;
    }

    private void disableScissor(GuiGraphics context)
    {
        context.disableScissor();
        scissor = false;
    }

    public void tick()
    {
        meatgun = MWComponents.MEATGUN.getNullable(slot.getItem());
//        if (foundComponent != meatgun)
//        {
//            foundComponent = meatgun;
//        }
        reorganise();
    }

    private class ModuleSlotWidget implements Renderable, Rectangle
    {
        private static final Rectangle size = new ImmutableRect(0, 0, 18, 18);
        private final Font textRenderer;
        private final MeatgunModule module;
        private final int slotIdx;
        private final ModuleSlot slot1;
        private Rectangle local = new ImmutableRect(size);
        private Rectangle bounds = new ImmutableRect(size);

        private final ItemStack defaultStack;

        public ModuleSlotWidget(Font textRenderer, MeatgunModule module, int slotIdx, ModuleSlot slot)
        {
            this.textRenderer = textRenderer;
            this.module = module;
            this.slotIdx = slotIdx;
            this.slot1 = slot;

            MeatgunModule module1 = slot1.get();
            if (module1.getType() != MeatgunModule.DEFAULT_TYPE)
                defaultStack = MeatgunModuleItem.get(module1);
            else
                defaultStack = ItemStack.EMPTY;
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            int bordereCol = PLCCols.BORDER.col;
            if (bounds.isWithin(mouseX, mouseY))
            {
                bordereCol = PLCCols.SELECTED.col;
                renderTooltip(context, mouseX, mouseY, delta);
            }
            GUIUtil.drawTexture(TinkerTableScreen.WIDGETS_TEXTURE, context, bounds.x0(), bounds.y0(), 0, 0, 18, 19, bordereCol);
            context.renderItem(defaultStack, bounds.x0() + 1, bounds.y0() + 1);
        }

        private void renderTooltip(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            if (scissor)
            {
                disableScissor(context);
                if (defaultStack.isEmpty())
                {
                    if (slot1 instanceof SimpleModuleSlot simpleSlot)
                    {
//                        context.drawTooltip(textRenderer, Text.of(simpleSlot.getSlotType().toString()), mouseX, mouseY);
                        RenderSystem.enableDepthTest();
                        List<Component> types = simpleSlot.getApplicable().stream().<Component>map(t -> Component.translatable(t.translationKey())).toList();
                        TooltipHelper.get().draw(context, mouseX, mouseY, types);
                        RenderSystem.disableDepthTest();
                    }
                }
                else
                {
                    context.renderTooltip(textRenderer, defaultStack, mouseX, mouseY);
                }
                context.flush();
                enableScissor(context);
            }
            else
            {
                context.renderTooltip(textRenderer, defaultStack, mouseX, mouseY);
            }

            // There must be an easier way
        }

        public void updateOrigin(int ox, int oy)
        {
            bounds = local.offset(ox, oy);
        }

        public static int width()
        {
            return size.nw();
        }

        public static int height()
        {
            return size.nh();
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            if (bounds.isWithin(mouseX, mouseY))
            {
//                sender.emitter().apply(module.getUuid(), slotIdx);
                handler.slotClick.emitter().apply(module.getUuid(), slotIdx);

                return true;
            }
            return false;
        }

        public void init(int x, int y)
        {
            local = size.offset(x, y);
        }

        @Override
        public int x0()
        {
            return bounds.x0();
        }

        @Override
        public int y0()
        {
            return bounds.y0();
        }

        @Override
        public int nw()
        {
            return bounds.nw();
        }

        @Override
        public int nh()
        {
            return bounds.nh();
        }
    }

    private class ModuleWidget extends MutableRectangle implements Renderable
    {
        private final MeatgunModule module;
        private int ox, oy;
        private int absX, absY;

        private final Font textRenderer;

        private final List<ModuleSlotWidget> slots;
        @Nullable private final TreePane.ModuleSlotWidget parentSlot;

        public ModuleWidget(Font textRenderer, MeatgunModule module, List<ModuleSlotWidget> slots, @Nullable ModuleSlotWidget parentSlot)
        {
            this.textRenderer = textRenderer;
            this.module = module;
            this.parentSlot = parentSlot;
            this.slots = slots;
        }

        public void init(int x, int y)
        {
            this.absX = x;
            this.absY = y;
            updateBounds();

            if (!slots.isEmpty())
            {
                float spacing = (float) nw() / slots.size();
                int offsetX = (int) (nw() - (18 * slots.size() + (slots.size() - 1) * (spacing - 18))) / 2 + 1;
                for (var slot : slots)
                {
                    slot.init(x0() + offsetX, y0() + nh() - ModuleSlotWidget.height() - 2);
                    offsetX += spacing;
                }
            }
        }

        public void updateOrigin(int ox, int oy)
        {
            this.ox = ox;
            this.oy = oy;

            for (var slot : slots)
            {
                slot.updateOrigin(ox, oy);
            }

            updateBounds();
        }

        private void updateBounds()
        {
            setPos(ox + absX, oy + absY);
        }

        public void updateSize()
        {
            int maxW = Math.max(textRenderer.width(getName()) + 4, slots.size() * (18 + 3));
            setSize(maxW, height());
        }

        public static int height()
        {
            return ModuleSlotWidget.height() + 9 + 5;
        }

        public Component getName()
        {
            ResourceLocation id = MeatgunModules.REGISTRY.getKey(module.getType());
            return Component.translatable(id.toLanguageKey("meatgun_module"));
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            GUIUtil.drawText(context, textRenderer, getName(), x0() + 3, y0() + 2, PLCCols.TEXT.col, false);
            GUIUtil.renderBorder(context, x0(), y0(), nw(), nh(), PLCCols.BORDER.col, 0);

            for (var slot : slots)
            {
                slot.render(context, mouseX, mouseY, delta);
            }

            if (parentSlot != null)
            {
                var c = parentSlot.bounds;
                float dx = (c.x0() + c.nw() / 2f) - (x0() + nw() / 2f);
                float dy = c.y1() + 4 - y0();
                double len = Math.sqrt(dx * dx + dy * dy);
                double angle = Math.atan2(dy, dx);
                context.pose().pushPose();
                context.pose().translate(x0() + nw() / 2f, y0(), 0);
                context.pose().mulPose(RotationAxis.POSITIVE_Z.rotation((float) angle));
                context.hLine(0, (int) len, 0, PLCCols.BORDER.col);
                context.pose().popPose();
            }
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button)
        {
            if (isWithin(mouseX, mouseY))
            {
                for (var slot : slots)
                {
                    if (slot.mouseClicked(mouseX, mouseY, button))
                        return true;
                }
            }
            return false;
        }
    }
}
