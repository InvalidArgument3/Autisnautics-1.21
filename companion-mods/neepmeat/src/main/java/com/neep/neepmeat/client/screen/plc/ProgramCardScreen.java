package com.neep.neepmeat.client.screen.plc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.auto.Panel;
import com.neep.meatlib.client.screen.auto.SimplePanel;
import com.neep.meatlib.client.screen.auto.VStackPanel;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.meatlib.client.screen.widget.config.ConfigPanel;
import com.neep.neepmeat.client.screen.widget.MultiLineConfigTextField;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.plc.screen.ProgramCardScreenHandler;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ProgramCardScreen extends BetterHandledScreen<ProgramCardScreenHandler>
{
    private final ConfigWidget configWidget = new ConfigWidget(menu.handler);

    public ProgramCardScreen(ProgramCardScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
    }

    @Override
    protected void init()
    {
        imageWidth = 191;
        imageHeight = 59;

        super.init();

        var main = new Background(new VStackPanel(0, 0, false)
                .addChild(configWidget.nameField)
        );

        addRenderableWidget(main).setDims(leftPos, topPos, imageWidth, imageHeight);

        ScreenReady.ready();
    }

    private static class Background extends SimplePanel
    {
        private final Panel vStackPanel;

        public Background(VStackPanel vStackPanel)
        {
            this.vStackPanel = vStackPanel;
            this.w = 191;
            this.h = 59;
        }

        @Override
        protected ClickableWidget initMainElement()
        {
            setDims(vStackPanel, x0(), y0(), nw(), nh());
            return vStackPanel;
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            GUIUtil.drawTexture(ResourceLocation.tryBuild(NeepMeat.NAMESPACE, "textures/gui/plc/program_cabinet.png"), context,
                    x0(), y0(), 0, 166, nw(), nh());
            super.render(context, mouseX, mouseY, delta);
        }
    }

    private static class LabelLinesField extends MultiLineConfigTextField
    {
        public LabelLinesField(int x, int y, int width, int height, Component placeholder, Consumer<MultiLineConfigTextField> update)
        {
            super(x, y, width, height, placeholder, update);
        }

        @Override
        public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            PoseStack matrices = context.pose();
            int x = x0();
            int y = y0();

            if (this.visible)
            {
                context.enableScissor(x + 1, y + 1, x + width - 1, y + height - 1);

                matrices.pushPose();
                matrices.translate(0.0, -this.scrollAmount(), 0.0);
                renderContents(context, mouseX, mouseY, delta);
                matrices.popPose();

                context.disableScissor();
                renderOverlay(matrices);
            }
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers)
        {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        protected int textCol()
        {
            // Lighter colour for placeholder
            return getText().isEmpty() ? 0xffaaaaaa : 0xff000000;
        }

        @Override
        protected int cursorCol()
        {
            return 0xff333333;
        }
    }

    public static class ConfigWidget extends ConfigPanel<ProgramCardScreenHandler.CardWidgetHandler.Values>
    {
        public final MultiLineConfigTextField nameField;

        public ConfigWidget(ConfigWidgetHandler<ProgramCardScreenHandler.CardWidgetHandler.Values> handler)
        {
            super(handler);

            nameField = addTrackedField(new LabelLinesField(0, 0, nw(), 32, Component.literal("Program name"), f -> update()));
        }

        @Override
        protected ProgramCardScreenHandler.CardWidgetHandler.Values createValues()
        {
            return new ProgramCardScreenHandler.CardWidgetHandler.Values(nameField.getText());
        }

        @Override
        protected void updateParams(ProgramCardScreenHandler.CardWidgetHandler.Values values)
        {
            nameField.setTextSilent(values.name());
        }
    }
}
