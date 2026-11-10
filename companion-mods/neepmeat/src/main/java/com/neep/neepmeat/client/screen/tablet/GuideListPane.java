/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
import net.minecraft.client.resources.model.ModelResourceLocation; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.client.screen.tablet;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.widget.AbstractClickableWidget;
import com.neep.neepmeat.api.theme.GuideCols;
import com.neep.neepmeat.guide.GuideNode;
import com.neep.neepmeat.guide.GuideReloadListener;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.util.NMMaths;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.render.*;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public class GuideListPane extends ContentPane implements Renderable, GuiEventListener, NarratableEntry
{
    // Currently available entries
    protected final List<EntryWidget> entries = new ArrayList<>();

    private GuideNode lastNode;
    private int selected;
    protected int entryHeight = 11;
    protected int contentHeight;

    private int animationTicks;

    private boolean searchMode;

    private final GuideSearchWidget searchString;

    private int scroll;

    public GuideListPane(GuideScreen parent)
    {
        super(parent);
        this.searchString = new GuideSearchWidget("/", () -> this.searchMode, textRenderer);
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        if (parent.getAnimationTicks() < 12)
            return;

        searchString.render(matrices, mouseX, mouseY, delta);
        matrices.enableScissor(x0(), y0(), x1(), y1() - searchString.nh());
        super.render(matrices, mouseX, mouseY, delta);

        if (selected >= 0 && selected < entries.size())
        {
            var se = entries.get(selected);
            if (!se.section)
                GUIUtil.renderBorderL(matrices, se.x0(), se.y0(), se.nw(), se.nh(), 0xFFAAAA00, 0);
        }

        matrices.disableScissor();

        GUIUtil.renderBorderL(matrices, x0(), this.y0(), nw(), nh(), 0xFF888800, 0);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (searchMode)
        {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE)
            {
                searchString.clear();
                searchMode = false;
                init();
                return true;
            }
            else if (keyCode == GLFW.GLFW_KEY_TAB)
            {
                setSelected(getSelected() + (modifiers == GLFW.GLFW_MOD_SHIFT ? -1 : 1));
//                setSelected(getSelected() + 1);
            }
            else if (keyCode == GLFW.GLFW_KEY_BACKSPACE)
            {
                searchString.erase(-1);
                init();
            }

            super.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_SLASH)
            super.keyPressed(keyCode, scanCode, modifiers);

        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        if (!(chr == GLFW.GLFW_KEY_ESCAPE))
        {
            if (searchMode)
            {
                searchString.write(chr, modifiers);
                init();
                return true;
            }

            if (chr == GLFW.GLFW_KEY_SLASH)
            {
                searchMode = true;
                init();
            }
        }

        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double amount)
    {
        int prevScroll = scroll;
        scroll = (int) Mth.clamp(scroll + amount * 10,
                Math.min(0, -(entries.size() * entryHeight) + contentHeight - 30),
                0);

        int moveAmount = scroll - prevScroll;
        for (EntryWidget entry : entries)
        {
            entry.setPos(entry.x0(), entry.y0() + moveAmount);
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void tick()
    {
        ++animationTicks;
        super.tick();
    }

    @Override
    public void init()
    {
        // Subtract the y offset and the height of the search widget
        this.contentHeight = nh() - screenOffsetY - entryHeight;

        clearChildren();

        if (!searchMode)
            generateMenu();
        else
            updateSearch();

//        addChild(searchString);
        searchString.setDims(x0(), this.y0() + nh() - entryHeight, nw(), entryHeight);

        if (lastNode != parent.getPath().peek())
        {
            entryAnimationStart = parent.getAnimationTicks();
            selected = -1;
            lastNode = parent.getPath().peek();
        }
    }

    protected void updateSearch()
    {
        entries.clear();

        // Create a de-duplicated set of matching entries.
        // GuideNode.GuideNodeImpl::equals() only checks the ID string since there is no reason for multiple entries to share an ID but have different contents.
        Iterator<GuideNode.ArticleNode> filtered = GuideReloadListener.getInstance().getArticleNodes().stream().distinct().filter(
                a -> a.getText().toString().toLowerCase(Locale.ROOT).contains(searchString.string)).iterator();

        for (int i = 0; filtered.hasNext() && (i + 1) * entryHeight < contentHeight; ++i)
        {
            GuideNode node = filtered.next();
            ItemStack icon = new ItemStack(BuiltInRegistries.ITEM.get(node.getIcon()));
            entries.add(new EntryWidget(i,
                    screenOffsetX + this.x,
                    screenOffsetY + this.y + (i) * entryHeight,
                    nw() - 2 * screenOffsetX,
                    entryHeight,
                    icon,
                    node.getText(),
                    node, node.section()));
        }

        entries.forEach(this::addChild);
    }

    private int entryAnimationStart;

    private final Component backArrow = Component.nullToEmpty("«").copy().setStyle(Style.EMPTY.withFont(GuideCols.FONT));
    private final Component forwardArrow = Component.nullToEmpty("»").copy().setStyle(Style.EMPTY.withFont(GuideCols.FONT));

    protected void generateMenu()
    {
        entries.clear();

        // Back button
        entries.add(new EntryWidget(0, screenOffsetX + this.x, screenOffsetY + this.y, nw() - 2 * screenOffsetX, entryHeight, ItemStack.EMPTY, backArrow, GuideNode.BACK, false));

        List<GuideNode> nodes = parent.getPath().peek().getChildren();

        for (int i = 0; i < nodes.size(); ++i)
        {
            GuideNode node = nodes.get(i);
            ItemStack icon = new ItemStack(BuiltInRegistries.ITEM.get(node.getIcon()));
            entries.add(new EntryWidget(i + 1,
                    screenOffsetX + this.x,
                    screenOffsetY + this.y + (i + 1) * entryHeight,
                    nw() - 2 * screenOffsetX,
                    entryHeight,
                    icon,
                    node.getText(),
                    node, node.section()));
        }

        entries.forEach(this::addChild);
    }

    public void setSelected(int sel)
    {
        this.selected = NMMaths.wrap(sel, 0, entries.size() - 1);
        entries.get(this.selected).onPress();
    }

    public int getSelected()
    {
        return selected;
    }

    @Override
    public void updateNarration(NarrationElementOutput builder)
    {

    }

    @Override
    public NarrationPriority narrationPriority()
    {
        return NarrationPriority.NONE;
    }

    public class EntryWidget extends AbstractClickableWidget
    {
        private final GuideNode node;
        private final ItemStack icon;
        private final int index;
        private final boolean section; // Whether to treat this entry as a section separator

        public EntryWidget(int index, int x, int y, int w, int h, ItemStack icon, Component text, GuideNode node, boolean section)
        {
            super(x, y, w, h, text);
            this.index = index;
            this.node = node;
            this.icon = icon;
            this.section = section;
        }

        @Override
        public void playDownSound(SoundManager soundManager)
        {
            soundManager.play(SimpleSoundInstance.forUI(NMSounds.MARATHON_BEEP.get(), 1.0f));
        }

        @Override
        public void onClick(double mouseX, double mouseY)
        {
            this.onPress();
        }

        protected void onPress()
        {
            selected = index;
            node.visitScreen(parent);
        }

        @Override
        public void renderButton(GuiGraphics matrices, int mouseX, int mouseY, float delta)
        {
            // Delay animation by 16 ticks if the screen has been opened for fewer than 32 ticks.
            if (parent.getAnimationTicks() < (animationTicks <= 32 ? 16 : 0) + entryAnimationStart + index) return;

            MultiBufferSource vertexConsumers = Minecraft.getInstance().renderBuffers().bufferSource();
//            this.renderBackground(matrices, MinecraftClient.getInstance(), mouseX, mouseY);
            int borderCol = GuideCols.textCol();
            int x = x0();
            int y = y0();

            int ty = y0() + Math.round((height - 9) / 2f);
            if (!section)
            {
                GUIUtil.drawHorizontalLine1(matrices, x, x + width, y + height, borderCol);
                GUIUtil.drawHorizontalLine1(matrices, x, x + width, y, borderCol);
                GUIUtil.drawVerticalLine1(matrices, x, y, y + height, borderCol);
                GUIUtil.drawVerticalLine1(matrices, x0() + width, y0(), y0() + height, borderCol);

                GUIUtil.drawText(matrices, textRenderer, getMessage(), x0() + 2, ty, GuideCols.textCol(), true);
            }
            else
            {
                GUIUtil.drawCenteredText(matrices, textRenderer, getMessage(), x0() + (float) width / 2, ty + 1, GuideCols.textCol(), true);
            }

            if (!node.getChildren().isEmpty())
            {
                GUIUtil.drawText(matrices, textRenderer, forwardArrow, x0() + width - 9, ty, GuideCols.textCol(), true);
            }
            else if (!section)
            {
                renderItemIcon(x0() + width - 16, y0() - 1, client.getItemRenderer(), 0, icon, matrices, vertexConsumers, 15, 0);
            }

        }
    }

    public static void renderItemIcon(int x, int y, ItemRenderer renderer, int zOffset, ItemStack stack, GuiGraphics context, MultiBufferSource vertexConsumers, int light, int overlay)
    {
        PoseStack matrices = context.pose();
        if (stack.isEmpty())
        {
            return;
        }

        ItemModelShaper models = renderer.getItemModelShaper();
        BakedModel bakedModel = models.getItemModel(stack);

        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        PoseStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushPose();
        matrixStack.translate(x, y, 100.0f + zOffset);
        matrixStack.translate(8.0, 7.0, 0.0); // Positive y is downwards on the screen
        matrixStack.scale(1.0f, -1.0f, 1.0f);
        matrixStack.scale(10.0f, 10.0f, 10.0f);
        RenderSystem.applyModelViewMatrix();
        MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
//        renderer.renderItem(stack, ModelTransformation.Mode.GUI, false, matrixStack2, immediate, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, bakedModel);
        renderItem(stack, matrices, vertexConsumers, light, overlay, bakedModel);
        immediate.endBatch();
        RenderSystem.enableDepthTest();
        matrixStack.popPose();
        RenderSystem.applyModelViewMatrix();

//        renderer.zOffset = bakedModel.hasDepth() ? zOffset + 50.0f + 0 : zOffset + 50.0f;
//
//        BakedModel model = renderer.getModels().getModel(ModelResourceLocation.standalone(stack));
//        renderItem(stack, matrices, vertexConsumers, light, overlay, model);
    }

    public static void renderItem(ItemStack stack, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model)
    {
        ItemDisplayContext renderMode = ItemDisplayContext.GUI;
        matrices.pushPose();
        model.getTransforms().getTransform(renderMode).apply(false, matrices);
        matrices.translate(-0.5, -0.5, -0.5);
        RenderType renderLayer = ItemBlockRenderTypes.getRenderType(stack, true);
        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(vertexConsumers, renderLayer, true, stack.hasFoil());
        renderBakedItemModel(model, stack, light, overlay, matrices, vertexConsumer);
        matrices.popPose();
    }

    private static void renderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, PoseStack matrices, VertexConsumer vertices)
    {
        var random = RandomSource.create();

        for (Direction direction : Direction.values())
        {
            random.setSeed(42L);
            renderGreenQuads(matrices, vertices, model.getQuads(null, direction, random), stack, light, overlay);
        }
        random.setSeed(42L);
        renderGreenQuads(matrices, vertices, model.getQuads(null, null, random), stack, light, overlay);
    }

    private static void renderGreenQuads(PoseStack matrices, VertexConsumer vertices, List<BakedQuad> quads, ItemStack stack, int light, int overlay)
    {
        PoseStack.Pose entry = matrices.last();
        for (BakedQuad bakedQuad : quads)
        {

            int i = 0xFFFFFF;
            float r = (float)(i >> 16 & 0xFF) / 255.0f;
            float g = (float)(i >> 8 & 0xFF) / 255.0f;
            float b = (float)(i & 0xFF) / 255.0f;

            vertices.putBulkData(entry, bakedQuad, r, g, b, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }
    }
}