package com.neep.neepmeat.compat.emi.recipe;

import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.api.plc.recipe.ManufactureStep;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.neepmeat.compat.emi.NMEmiPlugin;
import com.neep.neepmeat.plc.recipe.CombineStep;
import com.neep.neepmeat.plc.recipe.ImplantStep;
import com.neep.neepmeat.plc.recipe.InjectStep;
import com.neep.neepmeat.plc.recipe.TransformingToolRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ToolManufactureEmiRecipe implements EmiRecipe
{
    private static final ResourceLocation GHOST_AXE = new ResourceLocation(NeepMeat.NAMESPACE, "textures/gui/ghost_axe.png");
    private static final ResourceLocation GHOST_SWORD = new ResourceLocation(NeepMeat.NAMESPACE, "textures/gui/ghost_sword.png");

    private final ResourceLocation id;
    private final Item base;
    private final ManufactureStep<?> finalStep;

    public ToolManufactureEmiRecipe(TransformingToolRecipe recipe)
    {
        this.base = recipe.getBase();
        this.finalStep = recipe.getSteps().get(0);

        this.id = new ResourceLocation(recipe.getId().getNamespace(), "/" + recipe.getId().getPath());
    }

    public static int borderCol()
    {
        return PLCCols.BORDER.col;
    }

    static void appendStepIngredients(List<ManufactureStep<?>> steps, List<EmiIngredient> ingredients)
    {
        for (var step : steps)
        {
            if (step instanceof CombineStep combineStep)
            {
                ingredients.add(EmiStack.of(combineStep.getItem()));
            }
            else if (step instanceof InjectStep injectStep)
            {
                ingredients.add(EmiStack.of(injectStep.fluid().getObject()));
            }
            else if (step instanceof ImplantStep implantStep)
            {
                ingredients.add(EmiStack.of(implantStep.getItem()));
            }
        }
    }

    static void drawThing(int x, int y, ManufactureStep<?> step, WidgetHolder widgets)
    {
        if (step instanceof CombineStep combineStep)
        {
            widgets.addSlot(EmiStack.of(combineStep.getItem()), x - 1, y + 1).drawBack(false);
        }
        else if (step instanceof InjectStep injectStep)
        {
            widgets.addSlot(EmiStack.of(injectStep.fluid().getFluid()), x - 1, y + 1).drawBack(false);
        }
        else if (step instanceof ImplantStep implantStep)
        {
            widgets.addSlot(EmiStack.of(implantStep.getItem()), x - 1, y + 1).drawBack(false);
        }
        else
            widgets.addSlot(EmiStack.EMPTY, x, y + 2).drawBack(false);
    }

    @Override
    public EmiRecipeCategory getCategory()
    {
        return NMEmiPlugin.SURGERY;
    }

    @Override
    public @Nullable ResourceLocation getId()
    {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs()
    {
        return List.of(EmiStack.of(base));
    }

    @Override
    public List<EmiStack> getOutputs()
    {
        return List.of();
    }

    @Override
    public int getDisplayWidth()
    {
        return 160;
    }

    @Override
    public int getDisplayHeight()
    {
        return 150;
    }

    @Override
    public void addWidgets(WidgetHolder widgets)
    {
        int startX = 5;
        int startY = 5;

        widgets.add(new OutlineWidget(new Bounds(0, 0, getDisplayWidth(), getDisplayHeight())));

        var widgetBase = new ItemManufactureEmiRecipe.LabelledSlot(startX, startY, Component.nullToEmpty("Base: "), EmiStack.of(base), widgets);
        widgets.add(widgetBase);

        int entryX = startX + 1;
        int entryY = startY + 22;


        ToolWidget toolWidget;
        toolWidget = new ToolWidget(entryX, entryY, getDisplayWidth() - 20, widgets, GHOST_AXE);
        widgets.add(toolWidget);
        entryY += toolWidget.height() + 2;

        toolWidget = new ToolWidget(entryX, entryY, getDisplayWidth() - 20, widgets, GHOST_SWORD);
        widgets.add(toolWidget);
        entryY += toolWidget.height() + 2;

        ItemManufactureEmiRecipe.EntryWidget widget;
        widget = new ItemManufactureEmiRecipe.EntryWidget(entryX, entryY, finalStep, getDisplayWidth() - 20, widgets);
        widgets.add(widget);
    }

    public static class ToolWidget extends Widget
    {
        private final int originX;
        private final int originY;
        private final ResourceLocation texture;
        private final ManufactureStep<?> step;
        private final Component name;
        private final Font textRenderer = Minecraft.getInstance().font;
        private final int width;

        public ToolWidget(int originX, int originY, int width, WidgetHolder widgets, ResourceLocation texture)
        {
            this.originX = originX;
            this.originY = originY;
            this.texture = texture;
            this.step = new CombineStep(ItemVariant.blank());
            this.name = step.getName();
            this.width = width;

            widgets.addTexture(new EmiTexture(texture, 0, 0, 16, 16, 16, 16, 16, 16),
                    originX + width() - 14, originY);
        }

        public int height()
        {
            return Math.max(textRenderer.lineHeight + 3, 19);
        }

        public int width()
        {
            return width;
        }

        @Override
        public Bounds getBounds()
        {
            return new Bounds(originX, originY, width(), height());
        }

        @Override
        public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
        {
            int x = originX + 2;
            int y = originY + 2;

            matrices.drawString(textRenderer, name, x, y, borderCol(), true);
            GUIUtil.renderBorder(matrices, originX, originY, width() + 3, height(), borderCol(), 0);
        }
    }

    static class OutlineWidget extends Widget
    {
        private final Bounds bounds;

        public OutlineWidget(Bounds bounds)
        {
            this.bounds = bounds;
        }

        @Override
        public Bounds getBounds()
        {
            return this.bounds;
        }

        @Override
        public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
        {
            matrices.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(), 0xFF000000);
            GUIUtil.renderBorder(matrices, bounds.x(), bounds.y(), bounds.width(), bounds.height(), borderCol(), 0);
            GUIUtil.renderBorder(matrices, bounds.x() + 1, bounds.y() + 1, bounds.width() - 2, bounds.height() - 2, PLCCols.TRANSPARENT.col, 0);
        }
    }
}
