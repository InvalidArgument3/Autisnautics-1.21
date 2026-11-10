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

package com.neep.neepmeat.machine.fabricator;

import com.neep.meatlib.client.screen.auto.*;
import com.neep.meatlib.client.screen.auto.Divide.Abs;
import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.slot.VanillaInventoryWidget;
import com.neep.meatlib.client.screen.widget.AbstractClickableWidget;
import com.neep.meatlib.client.screen.widget.CheckboxWidget;
import com.neep.meatlib.client.screen.widget.Label;
import com.neep.meatlib.client.screen.widget.TextureWidget;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.client.screen.util.BeigeSlotBlockWidget;
import com.neep.neepmeat.client.screen.util.BorderSlotBlockWidget;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.jetbrains.annotations.Nullable;

import static com.neep.meatlib.client.screen.auto.Divide.*;

@OnlyIn(Dist.CLIENT)
public class FabricatorScreen extends BetterHandledScreen<FabricatorScreenHandler>
{
    private ItemStack result = ItemStack.EMPTY;
    private ItemStack buffered = ItemStack.EMPTY;

    private boolean useExternal;
    private boolean useInternal;

    public FabricatorScreen(FabricatorScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        handler.syncRecipe.receiver(this::receive);

        ScreenReady.ready();
    }

    void receive(@Nullable ResourceLocation id, ItemStack buffered, boolean useExternal, boolean useInternal)
    {
        updateRecipe(id);
        this.buffered = buffered;
        this.useExternal = useExternal;
        this.useInternal = useInternal;
    }

    void updateRecipe(ResourceLocation id)
    {
        if (id == null)
            result = ItemStack.EMPTY;

        if (minecraft == null || minecraft.level == null)
            return;

        if (minecraft.level.getRecipeManager().byKey(id).orElse(null) instanceof CraftingRecipe recipe)
            result = recipe.getResultItem(minecraft.level.registryAccess());
        else
            result = ItemStack.EMPTY;
    }

    @Override
    protected void init()
    {
        this.imageWidth = PLCCols.INV_W;
        this.imageHeight = 224;
        super.init();

        var patternBlock = addSlotBlock(new BorderSlotBlockWidget(0, 0, 3, 3, menu.patternSlots, this));
        var inputBlock = addSlotBlock(new BeigeSlotBlockWidget(0, 0, 2, 9, menu.inputSlots, this));
        var playerInv = addSlotBlock(new VanillaInventoryWidget(0, 0, menu.playerSlots, this));

        var resultWidget = new ResultWidget();
        var bufferedWidget = new BufferedWidget();

        var arrow = new TextureWidget(GUIUtil.SCREEN_BORDER, 48, 241, 22, 15, PLCCols.BORDER.col);

        var useExternal = new CheckboxWidget(60, font.lineHeight, () -> this.useExternal, Component.nullToEmpty("Use External"), (w, v) -> {
            menu.useExternal.emitter().accept(v);
        }).tooltip(NeepMeat.translationKey("screen", "fabricator.use_external.tooltip"));
        var useInternal = new CheckboxWidget(60, font.lineHeight, () -> this.useInternal, Component.nullToEmpty("Request internal"), (w, v) -> {
            menu.disableInternal.emitter().accept(v);
        }).tooltip(NeepMeat.translationKey("screen", "fabricator.use_internal.tooltip"));

        var background = new HashedBackgroundPanel(
                new SplitPanel(SplitPanel.Split.VERTICAL, 0) // Recipe at the top and input slots at the bottom
                        .setLeft(new BlackBackgroundPanel(
                                new LabeledPanel(Component.nullToEmpty("Enter Pattern"), new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                                    .setFit(SplitPanel.Fit.FIRST)
                                    .setLeft(patternBlock)
                                    .setRight(new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                                            .setLeft(new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                                                    .setLeft(new SnapPanel<>(arrow))
                                                    .setRight(new SplitPanel(SplitPanel.Split.HORIZONTAL, 4)
                                                            .setLeft(resultWidget)
                                                            .setRight(bufferedWidget)
                                                            .centre(true)
                                                            .setFit(SplitPanel.Fit.FIRST))
                                            )
                                            .setRight(new VStackPanel(0, 1, false, true)
                                                    .addChild(useExternal)
                                                    .addChild(useInternal))
                                    )).col(PLCCols.TEXT.col)))
                        .setRight(
                                    new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                                        .setLeft(new GridPanel(1, 2)
                                                .set(0, 0, new Label(Component.nullToEmpty("Input")))
                                        )
                                        .setRight(inputBlock)
                                        .setDivide(new Abs(Anchor.START, font.lineHeight + 2))
                                )
                        .setDivide(new Abs(Anchor.START, 3 * 18 + 19))
                        .separation()
        );

        SplitPanel mainSplit = new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                .setLeft(background)
                .setRight(playerInv)
                .setFit(SplitPanel.Fit.SECOND)
                .centre(true)
                ;

        mainSplit.setDims(leftPos, topPos, imageWidth, imageHeight);
        addRenderableWidget(mainSplit);

//        useExternal.setPos(patternBlock.x1() + 5, patternBlock.y1() - textRenderer.fontHeight - 2);
//        addDrawableChild(useExternal);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);

        this.renderTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics matrices, float delta, int mouseX, int mouseY)
    {
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
//        RenderSystem.setShaderTexture(0, TEXTURE);
    }

    @Override
    protected void renderLabels(GuiGraphics matrices, int mouseX, int mouseY)
    {
        matrices.drawString(font, this.title, this.inventoryLabelX, this.titleLabelY, 0x404040, false);
    }

    @Override
    public void onClose()
    {
        super.onClose();
    }

    @Override
    public void removed()
    {
        super.removed();
    }

    private class ResultWidget extends AbstractClickableWidget
    {
        private ResultWidget()
        {
            this.width = 24;
            this.height = 24;
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            GUIUtil.renderBorderInner(context, x0(), y0(), nw(), nh(), PLCCols.BORDER.col, 0);

            context.renderItem(result, x0() + 4, y0() + 4);
            context.renderItemDecorations(font, result, x0() + 4, y0() + 4);
            if (!result.isEmpty() && mouseX > x0() && mouseX < x0() + 18 && mouseY > y0() && mouseY < y0() + 18)
            {
                context.renderComponentTooltip(font, getTooltipFromContainerItem(result), mouseX, mouseY);
            }
        }

        @Override
        protected void renderButton(GuiGraphics context, int mouseX, int mouseY, float delta)
        {

        }
    }

    private class BufferedWidget extends AbstractClickableWidget
    {
        private BufferedWidget()
        {
            this.width = 20;
            this.height = 20;
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            GUIUtil.renderBorderInner(context, x0(), y0(), nw(), nh(), PLCCols.BORDER.col, 0);

            context.renderItem(buffered, x0() + 2, y0() + 2);
            context.renderItemDecorations(font, buffered, x0() + 2, y0() + 2);
            if (!buffered.isEmpty() && mouseX > x0() && mouseX < x0() + 18 && mouseY > y0() && mouseY < y0() + 18)
            {
                var tooltip = getTooltipFromContainerItem(buffered);
                tooltip.add(1, Component.nullToEmpty("(Buffered)").copy().withStyle(ChatFormatting.GOLD));
                context.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            }
        }

        @Override
        protected void renderButton(GuiGraphics context, int mouseX, int mouseY, float delta)
        {

        }
    }
}
