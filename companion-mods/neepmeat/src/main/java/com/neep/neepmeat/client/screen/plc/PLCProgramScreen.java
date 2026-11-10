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

package com.neep.neepmeat.client.screen.plc;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.neep.meatlib.client.screen.TooltipHelper;
import com.neep.meatlib.client.screen.widget.TooltipRenderer;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.api.plc.recipe.Workpiece;
import com.neep.neepmeat.client.plc.PLCHudRenderer;
import com.neep.neepmeat.client.plc.PLCMotionController;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.neepmeat.guide.GuideNode;
import com.neep.neepmeat.guide.GuideReloadListener;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.item.ProjectorItem;
import com.neep.neepmeat.network.plc.PLCSyncAction;
import com.neep.neepmeat.plc.component.MutateInPlace;
import com.neep.neepmeat.plc.instruction.Argument;
import com.neep.neepmeat.plc.screen.PLCScreenHandler;
import com.neep.meatlib.util.MiscUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;

public class PLCProgramScreen extends Screen implements MenuAccess<PLCScreenHandler>
{
    public static final ResourceLocation WIDGETS = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/gui/widget/plc_widgets.png");
    public static final ResourceLocation VIGNETTE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/gui/plc_robot_vignette.png");
    protected final PLCScreenEditorState editor;
    protected final PLCScreenShellState shell;
    private final PLCScreenHandler handler;

    // Text relating to the block that the mouse is currently over
    private final List<Component> tooltipText = Lists.newArrayList();
    protected PLCScreenState state;
    private double mouseX;
    private double mouseY;
    private RecordMode mode;

    public PLCProgramScreen(PLCScreenHandler handler, Inventory playerInventory, Component unused)
    {
        super(unused);
        this.handler = handler;

        this.editor = new PLCScreenEditorState(this);
        this.shell = new PLCScreenShellState(this);
        this.state = shell;
        this.mode = handler.getMode();

        handler.compileMessageS2C.receiver(editor::setCompileMessage);
        handler.updateStackS2C.receiver(handler.getPlc()::updateVariableStack);
        handler.updateMemoryS2C.receiver(handler.getPlc()::updateMemory);
    }

    @Nullable
    public static BlockState getWallState(Level world, Camera camera)
    {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        float width = 0.8f;
        for (int i = 0; i < 8; ++i)
        {
            Vec3 camPos = camera.getPosition();
            double d = camPos.x + (((i >> 0) % 2) - 0.5f) * width * 0.8f;
            double e = camPos.y + (((i >> 1) % 2) - 0.5f) * 0.1f;
            double f = camPos.z + (((i >> 2) % 2) - 0.5f) * width * 0.8f;
            mutable.set(d, e, f);

            BlockState blockState = world.getBlockState(mutable);
            if (blockState.getRenderShape() == RenderShape.INVISIBLE || !blockState.isViewBlocking(world, mutable))
                continue;
            return blockState;
        }
        return null;
    }

    private static void makeEntityTooltip(@Nullable Entity entity, List<Component> tooltip)
    {
        if (entity == null)
            return;

        tooltip.add(entity.getDisplayName());
        Workpiece workpiece = NMComponents.WORKPIECE.getNullable(entity);
        if (workpiece != null)
        {
            for (var step : workpiece.getSteps())
            {
                step.appendText(tooltip);
            }
        }
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    protected void init()
    {
        super.init();

        editor.init(width, height);
        shell.init(width, height);
        if (mode == RecordMode.EDIT)
        {
            addRenderableWidget(editor);
            editor.setDimensions(width, height);

            addRenderableWidget(new ModeSwitchButton(width - 17, 1));
            addRenderableWidget(new StopButton(width - 2 * 17, 1, Component.nullToEmpty("Stop (CTRL+T)")));
            addRenderableWidget(new StepButton(width - 3 * 17, 1, Component.nullToEmpty("Step (CTRL+F)")));
            addRenderableWidget(new RunButton(width - 4 * 17, 1, Component.nullToEmpty("Run/Pause (CTRL+R)")));
            addRenderableWidget(new CompileButton(width - 5 * 17, 1, Component.nullToEmpty("Compile (CTRL+S)")));
            addRenderableWidget(new LanguageButton(width - 6 * 17 - 32, 1, Component.empty()));
            state = editor;
        }
        else
        {
            addRenderableWidget(shell);
            addRenderableWidget(new ModeSwitchButton(width - 17, 1));
            addRenderableWidget(new StopButton(width - 2 * 17, 1, Component.nullToEmpty("Stop")));
            state = shell;
        }

    }

    @Override
    public void tick()
    {
        super.tick();
        tickTooltip(mouseX, mouseY);

        editor.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (!state.isSelected() || keyCode == GLFW.GLFW_KEY_ESCAPE)
            return super.keyPressed(keyCode, scanCode, modifiers);

        state.onKeyPressed(keyCode, scanCode, modifiers);
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta)
    {
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        BlockState wallState = getWallState(minecraft.level, minecraft.gameRenderer.getMainCamera());
        if (wallState != null)
        {
            // Fill screen with the block's particle sprite to prevent xray vision
            TextureAtlasSprite sprite = minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(wallState);
            renderInWallOverlay(sprite, matrices.pose(), width, height);
        }
        else
        {
            // Red fleshy vignette
            drawScreenTexture(matrices.pose(), VIGNETTE, 0, 0, 1, 1, 0.9f, width, height);
        }

        if (!tooltipText.isEmpty())
        {
            TooltipHelper.get().draw(matrices, mouseX, mouseY, tooltipText);
        }

        // Handle TooltipRenderers. Done automatically by BetterHandledScreen.
        for (var child : children())
        {
            if (child instanceof TooltipRenderer toot)
                toot.renderTooltipLayer(matrices, mouseX, mouseY, delta);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    public static void drawScreenTexture(PoseStack matrices, ResourceLocation texture, float u0, float v0, float u1, float v1, float light, int x1, int y1)
    {
        float x0 = 0;
        float y0 = 0;
        float z = 0;
        var matrix = matrices.last().pose();
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex(matrix, x0, y1, z).setColor(light, light, light, 1).setUv(u0, v1);
        bufferBuilder.addVertex(matrix, x1, y1, z).setColor(light, light, light, 1).setUv(u1, v1);
        bufferBuilder.addVertex(matrix, x1, y0, z).setColor(light, light, light, 1).setUv(u1, v0);
        bufferBuilder.addVertex(matrix, x0, y0, z).setColor(light, light, light, 1).setUv(u0, v0);
        BufferUploader.drawWithShader(bufferBuilder.build());
    }

    public static void renderInWallOverlay(TextureAtlasSprite sprite, PoseStack matrices, int width, int height)
    {
        float x0 = 0;
        float y0 = 0;
        float z = 0;
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        float l = sprite.getU0();
        float m = sprite.getU1();
        float n = sprite.getV0();
        float o = sprite.getV1();
        Matrix4f matrix4f = matrices.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferBuilder.addVertex(matrix4f, x0, (float) height, z).setColor(0.1f, 0.1f, 0.1f, 1.0f).setUv(m, o);
        bufferBuilder.addVertex(matrix4f, (float) width, (float) height, z).setColor(0.1f, 0.1f, 0.1f, 1.0f).setUv(l, o);
        bufferBuilder.addVertex(matrix4f, (float) width, y0, z).setColor(0.1f, 0.1f, 0.1f, 1.0f).setUv(l, n);
        bufferBuilder.addVertex(matrix4f, x0, y0, z).setColor(0.1f, 0.1f, 0.1f, 1.0f).setUv(m, n);
        BufferUploader.drawWithShader(bufferBuilder.build());
    }

    private void tickTooltip(double mouseX, double mouseY)
    {
        tooltipText.clear();
        var result = raycastClick(mouseX, mouseY, 15);
        if (result.getType() == HitResult.Type.BLOCK)
        {
            PLCHudRenderer.HIT_RESULT = result;

            var mip1 = MutateInPlace.ITEM.find(minecraft.level, result.getBlockPos(), null);
            var mip2 = MutateInPlace.ENTITY.find(minecraft.level, result.getBlockPos(), null);
            if (mip1 != null)
            {
                var stack = mip1.get();
                if (stack == null || stack.isEmpty())
                    return;

                tooltipText.addAll(stack.getTooltipLines(minecraft.player, TooltipFlag.Default.NORMAL));
            }
            else if (mip2 != null)
            {
                makeEntityTooltip(mip2.get(), tooltipText);
            }
        }
        else
        {
            PLCHudRenderer.HIT_RESULT = null;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        if (button == GLFW.GLFW_MOUSE_BUTTON_2 || button == GLFW.GLFW_MOUSE_BUTTON_3)
        {
            PLCHudRenderer renderer = PLCHudRenderer.getInstance();
            if (renderer != null)
            {
                double sensitivity = 0.4;
                PLCMotionController controller = renderer.getController();
                controller.setPitchYaw(
                        (float) Mth.clamp(controller.getPitch() - (deltaY * sensitivity), -90, 90),
                        (float) (controller.getYaw() - deltaX * sensitivity));
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (super.mouseClicked(mouseX, mouseY, button))
        {
            return true;
        }

        setFocused(null);
        return handleWorldClick(mouseX, mouseY, button);
    }

    protected boolean handleWorldClick(double mouseX, double mouseY, int button)
    {
        if (button == GLFW.GLFW_MOUSE_BUTTON_2 || button == GLFW.GLFW_MOUSE_BUTTON_3)
            return false;

        var result = raycastClick(mouseX, mouseY, 15);

        if (result.getType() == HitResult.Type.MISS)
        {
            return false;
        }

        addArgument(result);

        for (int i = 0; i < 15; ++i)
        {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(MeatlibSounds.PLC_SELECT, 1.0f));
        }

        return true;
    }

    protected void addArgument(BlockHitResult result)
    {
        state.argument(new Argument(result.getBlockPos(), result.getDirection()));
    }

    protected BlockHitResult raycastClick(double mouseX, double mouseY, double range)
    {
        Vec3 camPos = minecraft.gameRenderer.getMainCamera().getPosition();
        Vec3 farPos = screenToWorld(mouseX, mouseY, 0.4f);
        Vec3 nearPos = screenToWorld(mouseX, mouseY, 0.0f);

        Vec3 newFar = nearPos.add(farPos.subtract(nearPos).normalize().scale(range));

        ClipContext raycastContext = new ClipContext(
                nearPos.add(camPos),
                newFar.add(camPos),
                ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, minecraft.player);

        return minecraft.level.clip(raycastContext);
    }

    public static Vec3 screenToWorld(double mouseX, double mouseY, double z)
    {
        var window = Minecraft.getInstance().getWindow();

        Matrix4d mat = new Matrix4d(PLCHudRenderer.PROJECTION).mul(PLCHudRenderer.MODEL_VIEW);

        int[] screenDims = new int[]{0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight()};

        Vector3d worldPos = mat.unproject(mouseX, (window.getGuiScaledHeight() - mouseY), z,
                        screenDims,
                        new Vector3d());

        // Correct
//        Vector4d backToScreen = mat.project(worldPos, screenDims, new Vector4d());

//        Vector3d worldPos = new Matrix4d(PLCHudRenderer.PROJECTION)
//                .mul(PLCHudRenderer.MODEL_VIEW)
//                .unproject(mouseX, (window.getScaledHeight() - mouseY), z,
//                        new int[]{0, 0, window.getScaledWidth(), window.getScaledHeight()},
//                        new Vector3d());

        return new Vec3(worldPos.x, worldPos.y, worldPos.z);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY)
    {
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double amount)
    {
        var hovered = getChildAt(mouseX, mouseY);
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public void removed()
    {
        super.removed();
        if (minecraft.player != null)
            handler.removed(minecraft.player);

        PLCHudRenderer.leave();
    }

    @Override
    public void onClose()
    {
        super.onClose();
        this.minecraft.player.closeContainer();
        PLCHudRenderer.leave();
    }

    @Override
    public PLCScreenHandler getMenu()
    {
        return handler;
    }

    public PLCScreenEditorState getEditor()
    {
        return editor;
    }

    public PLCScreenShellState getInteractive()
    {
        return shell;
    }

    public boolean passEvents()
    {
        return !editor.isEditFieldFocused();
    }

    private void changeLanguage(LanguageMode language)
    {
        handler.changeLanguage.emitter().accept(language);
        editor.changeLanguage(language);
    }

    public abstract static class BaseButton extends PLCScreenButton
    {
        public BaseButton(int x, int y, Component message)
        {
            super(x, y, message);
            tooltip(getMessage());
        }
    }

    class RunButton extends BaseButton
    {
        public RunButton(int x, int y, Component message)
        {
            super(x, y, message);
        }

        @Override
        protected int getU()
        {
            return handler.isRunning() ? 32 : 16;
        }

        @Override
        public void onClick(double mouseX, double mouseY)
        {
            if (handler.isRunning())
                handler.channel.emitter().apply(PLCSyncAction.PAUSE);
            else
                handler.channel.emitter().apply(PLCSyncAction.RUN);
        }
    }

    class LanguageButton extends PLCScreenButton
    {
        public LanguageButton(int x, int y, Component message)
        {
            super(x, y, 48, 16, message);
            tooltipSingle(this::getMessage);
        }

        @Override
        protected int getU()
        {
            return 176;
        }

        @Override
        public void renderWidget(GuiGraphics matrices, int mouseX, int mouseY, float delta)
        {
            super.renderWidget(matrices, mouseX, mouseY, delta);

            GUIUtil.drawCenteredText(matrices,
                    font,
                    Component.nullToEmpty(handler.getLanguage().name()),
                    x0() + nw() / 2f,
                    this.y0() + (nh() - font.lineHeight) / 2f,
                    PLCCols.TEXT.col,
                    true);
        }

        @Override
        public Component getMessage()
        {
            return NeepMeat.translationKey("tooltip", "plc.language", handler.getLanguage().name());
        }

        @Override
        public void onClick(double mouseX, double mouseY)
        {
            if (!(Screen.hasShiftDown() || Screen.hasControlDown()))
            {
                LanguageMode language = MiscUtil.cycle(handler.getLanguage(), LanguageMode.values());
                changeLanguage(language);
            }
            else
            {
                List<GuideNode> path = GuideReloadListener.getInstance().getPath(handler.getLanguage().name().toLowerCase(Locale.ROOT));
                if (path != null)
                {
                    var screen = ProjectorItem.Client.openScreen();

                    for (var node : path)
                    {
                        node.visitScreen(screen);
                    }
                }
            }
        }
    }

    class CompileButton extends BaseButton
    {
        public CompileButton(int x, int y, Component message)
        {
            super(x, y, message);
        }

        @Override
        protected int getU()
        {
            return 96;
        }

        @Override
        public void onClick(double mouseX, double mouseY)
        {
            handler.channel.emitter().apply(PLCSyncAction.COMPILE);
        }
    }

    // What are you doing, step button?
    class StepButton extends BaseButton
    {
        public StepButton(int x, int y, Component message)
        {
            super(x, y, message);
        }

        @Override
        protected int getU()
        {
            return 160;
        }

        @Override
        public void onClick(double mouseX, double mouseY)
        {
            handler.channel.emitter().apply(PLCSyncAction.STEP);
        }
    }

    class StopButton extends BaseButton
    {
        public StopButton(int x, int y, Component message)
        {
            super(x, y, message);
        }

        @Override
        protected int getU()
        {
            return 80;
        }

        @Override
        protected int getYImage(boolean hovered)
        {
            if (handler.hasProgram() > 0)
            {
                if (isHovered())
                    return 2;
                else
                    return 1;
            }
            return 0;
        }

        @Override
        public void onClick(double mouseX, double mouseY)
        {
            handler.channel.emitter().apply(PLCSyncAction.STOP);
        }
    }

    class ModeSwitchButton extends BaseButton
    {
        public ModeSwitchButton(int x, int y)
        {
            super(x, y, Component.empty());
        }

        @Override
        public void onClick(double mouseX, double mouseY)
        {
            mode = MiscUtil.cycle(handler.getMode(), RecordMode.values());
            handler.changeMode.emitter().accept(mode);
            if (mode == RecordMode.IMMEDIATE)
                state = shell;
            else
                state = editor;

            rebuildWidgets();
        }

        @Override
        public Component getMessage()
        {
            return switch (handler.getMode())
            {
                case EDIT -> Component.nullToEmpty("Edit Mode");
                case IMMEDIATE -> Component.nullToEmpty("Interactive Mode: Instructions will be executed immediately");
            };
        }

        @Override
        protected int getU()
        {
            return switch (handler.getMode())
            {
                case EDIT -> 64;
                case IMMEDIATE -> 48;
            };
        }
    }

}
