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

package com.neep.neepmeat.client.screen.tablet;

import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.theme.GuideCols;
import com.neep.neepmeat.guide.GuideNode;
import com.neep.neepmeat.guide.GuideReloadListener;
import com.neep.neepmeat.guide.article.Article;
import com.neep.neepmeat.guide.article.ImageContent;
import com.neep.neepmeat.guide.article.TextContent;
import com.neep.neepmeat.screen_handler.DummyScreenHandler;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.registry.EmiExclusionAreas;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

@Environment(value = Dist.CLIENT)
// Inherits from HandledScreen so that EMI allows you to return to the guide from a recipe view
public class GuideMainScreen extends AbstractContainerScreen<DummyScreenHandler> implements GuideScreen
{
    public static final ResourceLocation LOGO_TEXTURE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/gui/tablet/neep.png");
    protected final ContentPane leftPane;
    // Current menu location
    protected final Deque<GuideNode> path;
    protected int contentWidth = 340;
    protected int contentHeight = 280;

    protected int animationTicks;
    protected boolean start;
    protected ContentPane rightPane;
    private int x;

//    private final GuideScreenHandler handler;
    private int y;


    public GuideMainScreen()
    {
        super(new DummyScreenHandler(), Minecraft.getInstance().player.inventory, Component.empty());

        this.leftPane = new GuideListPane(this);
        this.rightPane = new GuideArticlePane(this, Article.EMPTY);
        this.start = true;

        GuideReloadListener reloadListener = GuideReloadListener.getInstance();
        @Nullable GuideNode root = reloadListener.getRootNode();

        if (root != null)
        {
            path = reloadListener.getPersistentPath();

            if (path.isEmpty())
            {
                push(root);
            }
        }
        else
        {
            // This is an invalid state caused when the guide is empty.
            // Some logic elsewhere should prevent this...
            path = new LinkedList<>();
        }
    }

    @Override
    public void push(GuideNode node)
    {
        path.push(node);
        leftPane.init();
    }

    @Override
    public GuideNode pop()
    {
        GuideNode ret = null;
        if (path.size() > 1)
            ret = path.pop();
        leftPane.init();
        return ret;
    }

    @Override
    public Deque<GuideNode> getPath()
    {
        return path;
    }

    @Override
    public int getAnimationTicks()
    {
        return animationTicks;
    }

    @Override
    public ArticleTextWidget openArticle(Article article)
    {
        if (article == null)
        {
            article = new Article.Simple(List.of(
                    new TextContent(Component.literal("This article failed to load. Something has gone horribly wrong.")),
                    new ImageContent(200, 200, 1, ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "textures/gui/misc/potato_cat.png"))
            ));
        }

        if (rightPane instanceof GuideArticlePane pane
                && article.equals(pane.getWidget().getArticle()))
        {
            return pane.getWidget();
        }
        else
        {
            var pane = new GuideArticlePane(this, article);
            setRightPane(pane);
            return pane.getWidget();
        }
    }

    @Override
    public void setArticleScroll(int scroll)
    {
        if (rightPane instanceof GuideArticlePane pane)
        {
            pane.setScroll(scroll);
        }
    }

    @Override
    public ContentPane getRightPane()
    {
        return rightPane;
    }

    @Override
    public void setRightPane(ContentPane element)
    {
        removeWidget(rightPane);
        this.rightPane = element;
        init();
    }

    @Override
    public void renderBackground(GuiGraphics context)
    {
        int c = GuideCols.backgroundCol();
        context.fillGradient(0, 0, this.width, this.height, c, c);
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        this.renderBackground(matrices);

        // Initial animation
        matrices.pose().pushPose();
        float scale = Mth.clampedLerp(0.01f, 1f, (animationTicks + delta) / 10f);
        matrices.pose().translate(0, height / 2f * (1 - scale), 0);
        matrices.pose().scale(1, scale, 1);
        if (animationTicks > 10)
        {
            super.render(matrices, mouseX, mouseY, delta);
//            this.drawMouseoverTooltip(matrices, mouseX, mouseY);
            this.drawLogo(matrices, delta);
        }

        int borderCol = GuideCols.borderCol();
        int offset = 3;

        if (animationTicks != 0 && animationTicks != 2)
            GUIUtil.renderBorder(matrices, leftPos, topPos, contentWidth, contentHeight, borderCol, offset);
        matrices.pose().popPose();
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) { }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount)
    {
        return this.getChildAt(mouseX, mouseY).filter(element ->
                element.mouseScrolled(mouseX, mouseY, amount)).isPresent();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (leftPane.keyPressed(keyCode, scanCode, modifiers))
            return true;

        if (rightPane.keyPressed(keyCode, scanCode, modifiers))
            return true;

        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc())
        {
            this.onClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        if (leftPane.charTyped(chr, modifiers))
            return true;

        return rightPane.charTyped(chr, modifiers);
    }

    @Override
    protected void init()
    {
        super.init();
        addRenderableWidget(leftPane);
        addRenderableWidget(rightPane);

        float windowWidth = minecraft.getWindow().getGuiScaledWidth();
        float windowHeight = minecraft.getWindow().getGuiScaledHeight();
        float aspectRatio = windowWidth / windowHeight;

        contentHeight = (int) (windowHeight - 40);
        contentWidth = (int) (contentHeight * aspectRatio);


        this.leftPos = (this.width - contentWidth) / 2;
        this.topPos = (this.height - contentHeight) / 2;

        float ratio = 0.4f;
        int leftWidth = (int) (ratio * contentWidth);
        int rightWidth = (int) ((1 - ratio) * contentWidth);
        int rightStart = this.leftPos + leftWidth;

        if (leftPane != null)
        {
            leftPane.setDims(leftPos, topPos, leftWidth, contentHeight - 26);
        }
        if (rightPane != null)
        {
            rightPane.setDims(rightStart, topPos, rightWidth, contentHeight);
        }
    }

    protected void drawLogo(GuiGraphics matrices, float delta)
    {
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
//        RenderSystem.setShaderTexture(0, LOGO_TEXTURE);
        int logoHeight = 24;
        int logoWidth = 60;
        matrices.blit(LOGO_TEXTURE, leftPos, topPos + contentHeight - logoHeight + 1, 0, 0, logoWidth, logoHeight, logoWidth, 26);

        String version = FabricLoader.getInstance().getModContainer(NeepMeat.NAMESPACE)
                .map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("");
        GUIUtil.drawText(
                matrices, font, Component.nullToEmpty(version),
                leftPos + logoWidth + 2, topPos + contentHeight - font.lineHeight + 2,
                GuideCols.darkCol(), false);
    }

//    @Override
//    protected void renderTooltip(DrawContext context, ItemStack stack, int x, int y)
//    {
//        this.renderTooltip(matrices, this.getTooltipFromItem(stack), stack.getTooltipData(), x, y);
//    }

//    @Override
//    public void renderTooltip(MatrixStack matrices, List<Text> lines, Optional<TooltipData> data2, int x, int y)
//    {
//        List<TooltipComponent> list = lines.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList());
//        data2.ifPresent(data -> list.add(1, TooltipComponent.of(data)));
//        this.renderTooltipFromComponents(matrices, list, x, y);
//    }


    @Override
    protected void containerTick()
    {
        ++animationTicks;
        leftPane.tick();
        rightPane.tick();
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY)
    {
    }

    @Override
    public void removed()
    {
//        if (client.player == null)
//            return;

//        handler.close(this.client.player);
    }

    @Override
    public void onClose()
    {
        super.onClose();
    }
}
