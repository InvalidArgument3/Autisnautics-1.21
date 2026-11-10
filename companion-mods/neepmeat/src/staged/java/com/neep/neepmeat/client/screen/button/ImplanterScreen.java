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

package com.neep.neepmeat.client.screen.button;

import com.neep.meatlib.client.screen.AbstractSlotBlockWidget;
import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.auto.VStackPanel;
import com.neep.meatlib.client.screen.button.NMButtonWidget;
import com.neep.meatlib.client.screen.slot.MobileSlot;
import com.neep.meatlib.client.screen.slot.OutlineInventoryWidget;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.screen.MeatlibScreenHandlers;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.client.plc.PLCHudRenderer;
import com.neep.neepmeat.client.screen.plc.PLCProgramScreen;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.plc.block.entity.ImplanterBlockEntity;
import com.neep.neepmeat.plc.instruction.ImplantInstruction;
import com.neep.neepmeat.screen_handler.ImplanterScreenHandler;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector4d;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ImplanterScreen extends BetterHandledScreen<ImplanterScreenHandler>
{
    @Nullable public Entity targetEntity;
    private float targetError;
    private final ImplantSlotWidget implantSlot;

    public ImplanterScreen(ImplanterScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        this.implantSlot = new ImplantSlotWidget(handler.slots.subList(36, 37), this);
    }

    @Override
    protected void init()
    {
        imageWidth = minecraft.getWindow().getGuiScaledWidth();
        imageHeight = minecraft.getWindow().getGuiScaledHeight();

        super.init();

        var playerInv = addSlotBlock(new OutlineInventoryWidget(menu.slots.subList(0, 36), this, true));
        addSlotBlock(implantSlot);

        var main = addRenderableWidget(new VStackPanel(0, 1, false, true)
                .addChild(playerInv)
                .addChild(new ApplyButton(1, 18, Component.nullToEmpty("APPLY"), button -> menu.applyC2S.emitter().apply()).showBackground(false)));
        addRenderableWidget(implantSlot);
        main.setPos(imageWidth - playerInv.nw(), 0);
        implantSlot.setPos(leftPos + (width - implantSlot.nw()) / 2, topPos + (height - implantSlot.nh()) / 2);
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        BlockState wallState = PLCProgramScreen.getWallState(minecraft.level, minecraft.gameRenderer.getMainCamera());
        if (wallState != null)
        {
            // Fill screen with the block's particle sprite to prevent xray vision
            TextureAtlasSprite sprite = minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(wallState);
            PLCProgramScreen.renderInWallOverlay(sprite, matrices.pose(), width, height);
        }
        else
        {
            // Red fleshy vignette
            PLCProgramScreen.drawScreenTexture(matrices.pose(), PLCProgramScreen.VIGNETTE, 0, 0, 1, 1, 0.9f, width, height);
        }

        super.render(matrices, mouseX, mouseY, delta);
        renderTooltip(matrices, mouseX, mouseY);

        renderTargetCrosshairs(matrices);
    }

    private void renderTargetCrosshairs(GuiGraphics matrices)
    {
        if (targetEntity != null)
        {
            var window = minecraft.getWindow();
            Camera camera = minecraft.gameRenderer.getMainCamera();
            Vec3 camPos = camera.getPosition();

            Vec3 targetPos = targetPos(targetEntity).subtract(camPos);

            Vector3d worldPos = new Vector3d(targetPos.x, targetPos.y, targetPos.z);

            var m = new Matrix4d(PLCHudRenderer.PROJECTION).mul(PLCHudRenderer.MODEL_VIEW);
            int[] viewport = new int[]{0, window.getGuiScaledHeight(), window.getGuiScaledWidth(), -window.getGuiScaledHeight()};

            Vector4d result = m.project(worldPos, viewport, new Vector4d());

            int x = (int) result.x;
            int y = (int) result.y;
            int l = 10;
            GUIUtil.drawHorizontalLine1(matrices, x - l / 2, x + l / 2, y, PLCCols.BORDER.col);
            GUIUtil.drawVerticalLine1(matrices, x, y - l / 2 - 1, y + l / 2 + 1, PLCCols.BORDER.col);
        }
    }

    @Override
    protected void containerTick()
    {
        super.containerTick();
        ImplanterBlockEntity be = menu.getImplanter();
        this.targetEntity = be.raycast();
        if (targetEntity != null)
        {
            Vec3 origin = menu.getImplanter().endOrigin();
            Vec3 centre = targetPos(targetEntity);
            Vec3 toCentre = centre.subtract(origin);

            var direction = be.direction();

            this.targetError = (float) (Mth.RAD_TO_DEG * Math.acos(
                                toCentre.dot(direction) / (toCentre.length() * direction.length())));
        }
    }

    private Vec3 targetPos(Entity entity)
    {
        return new Vec3(entity.getX(), ImplantInstruction.getTargetY(entity), entity.getZ());
    }

    protected boolean canApply()
    {
        if (implantSlot.getSlot().getItem().isEmpty())
            return false;

        float margin = 0.3f;
        float f = Mth.clamp(targetError / 40f, 0, 1f);
        return targetEntity != null && f < margin;
    }

    @Override
    public void renderBackground(GuiGraphics context)
    {
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        if (button == GLFW.GLFW_MOUSE_BUTTON_2 || button == GLFW.GLFW_MOUSE_BUTTON_3)
        {
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private class ApplyButton extends NMButtonWidget
    {
        public ApplyButton(int width, int height, Component message, OnPress onPress)
        {
            super(0, 0, width, height, message, onPress);
        }

        @Override
        public void onPress()
        {
            if (canApply())
                super.onPress();
        }

        @Override
        public void playDownSound(SoundManager soundManager)
        {
            if (canApply() && !menu.getSlot(0).getItem().isEmpty())
                soundManager.play(SimpleSoundInstance.forUI(MeatlibSounds.UI_BEEP, 1.0F));
            else
                soundManager.play(SimpleSoundInstance.forUI(NMSounds.ERROR.get(), 1.0F));
        }

        @Override
        protected int borderCol()
        {
            return !canApply() ? PLCCols.INVALID.col : super.borderCol();
        }

        @Override
        public void renderWidget(GuiGraphics matrices, int mouseX, int mouseY, float delta)
        {
            if (canApply())
                matrices.fill(this.x0(), this.y0(), this.x0() + nw(), this.y0() + nh(), 0xffbb0000);
            else
                matrices.fill(this.x0(), this.y0(), this.x0() + nw(), this.y0() + nh(), 0x90000000);

            super.renderWidget(matrices, mouseX, mouseY, delta);
        }
    }

    private class ImplantSlotWidget extends AbstractSlotBlockWidget implements ClickableWidget, NarratableEntry
    {
        public ImplantSlotWidget(List<Slot> slots, BetterHandledScreen<?> handler)
        {
            super(0, 0, slots, handler);
        }

        public Slot getSlot()
        {
            return slots.get(0).slot();
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            MobileSlot slot = slots.get(0);
            int slotW = slot.nw();

            setSlotPositions(x + (nw() - slotW) / 2, y + (nh() - slotW) / 2, slots, 1, 1);

            Component text = Component.translatable("block.neepmeat.implanter.no_implant");

            if (!slot.slot().getItem().isEmpty())
            {
                text = Component.translatable("block.neepmeat.implanter.no_target");

                if (targetEntity != null)
                {
                    float f = Mth.clamp(targetError / 40f, 0, 1f);
                    int col = PLCCols.BORDER.col;
                    int d = Math.round(20 * f);

                    text = Component.translatable("block.neepmeat.implanter.off_target");

                    if (canApply())
                    {
                        col = 0xffff0000;
                        text = Component.translatable("block.neepmeat.implanter.implant_ready");
                    }

                    GUIUtil.renderBorderInner(context,
                            x + Math.round((nw() - 20) / 2f), y + Math.round((nh() - 20) / 2f),
                            20, 20, col, d);
                }
            }


            GUIUtil.renderBorderInner(context,
                    x + Math.round((nw() - 20) / 2f), y + Math.round((nh() - 20) / 2f),
                    20, 20, PLCCols.TRANSPARENT.col, 0);

            GUIUtil.drawCenteredText(context, font, text, x + nw() / 2f, y + nw() / 2f + 20, PLCCols.TEXT.col, true);

            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public int nw()
        {
            return MeatlibScreenHandlers.playerSlotsW();
        }

        @Override
        public int nh()
        {
            return MeatlibScreenHandlers.playerInvH();
        }

        @Override
        public void setFocused(boolean focused)
        {

        }

        @Override
        public boolean isFocused()
        {
            return false;
        }

        @Override
        public NarrationPriority narrationPriority()
        {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput builder)
        {

        }
    }
}
