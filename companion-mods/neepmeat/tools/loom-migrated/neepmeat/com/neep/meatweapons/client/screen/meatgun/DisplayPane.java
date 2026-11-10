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
import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatweapons.MWItems;
import com.neep.meatweapons.component.MeatgunComponent;
import com.neep.meatweapons.init.MWComponents;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.neep.meatlib.theme.PLCCols;
import com.neep.meatlib.client.screen.GUIUtil;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

class DisplayPane extends TinkerTableScreen.PaneWidget
{
    private final Font textRenderer = Minecraft.getInstance().font;
    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    private final Slot slot;

    private float offsetX = 0;
    private float offsetY = 0;
    private float rotY = Mth.HALF_PI + Mth.PI / 4;
    private float rotX = -Mth.PI / 4;
    private float scale = 5;

    private ItemStack stack = ItemStack.EMPTY;

    public DisplayPane(Slot slot)
    {
        this.slot = slot;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);

        float cx = x0() + nw() / 2f + offsetX;
        float cy = y0() + nh() / 2f + offsetY;

        int zOffset = stack.is(MWItems.MEATGUN_PISTOL) ? 4 : 0;
        Matrix4f modelTransform = new Matrix4f()
                .translate(0, 8, zOffset)
                .scale(scale)
                .rotateX(rotX)
                .rotateY(rotY)
                .translate(0, -8, -zOffset)
                ;

        PoseStack matrices = context.pose();
        matrices.pushPose();
        matrices.translate(cx, cy, 150);
        matrices.mulPoseMatrix(modelTransform);
        matrices.mulPoseMatrix((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
        matrices.scale(16.0F, 16.0F, 16.0F);
        context.enableScissor(x0() + 2, y0() + 2, x0() + nw() - 2, y0() + nh() - 2);
        itemRenderer.renderStatic(stack, ItemDisplayContext.NONE,
                15728880, OverlayTexture.NO_OVERLAY,
                context.pose(), context.bufferSource(), null, 0);
        context.flush(); // Draw now so that the scissor works consistently (?)
        context.disableScissor();
        matrices.popPose();

        MeatgunComponent component = MWComponents.MEATGUN.getNullable(stack);
        if (component != null)
        {
            RootModuleHolder holder = component.getRootHolder();
            int maxComplexity = holder.getMaxComplexity(stack);
            int remaining = holder.getRemainingComplexity();
            matrices.pushPose();
//            RenderSystem.disableDepthTest();
            matrices.translate(0, 0, 500);
            GUIUtil.drawText(context, textRenderer,
                    Component.translatable("tooltip.meatweapons.meatgun_module.remaining_complexity", maxComplexity - remaining, maxComplexity),
                    x0() + 3, y1() - textRenderer.lineHeight - 1, PLCCols.TEXT.col, true);
//            RenderSystem.enableDepthTest();
            matrices.popPose();
        }
    }

    @Override
    protected void playDownSound(SoundManager soundManager)
    {
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1)
        {
            offsetX += deltaX;
            offsetY += deltaY;
        }
        else
        {
            rotY += deltaX / 40;
            rotX -= deltaY / 40;
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount)
    {
        scale += amount / 10;
        return true;
    }

    public void tick()
    {
        stack = slot.getItem();
//        meatgun = MWComponents.MEATGUN.getNullable(slot.getStack());
    }
}
