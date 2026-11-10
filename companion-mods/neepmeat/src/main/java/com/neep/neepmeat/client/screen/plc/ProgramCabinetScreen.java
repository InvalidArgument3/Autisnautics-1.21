package com.neep.neepmeat.client.screen.plc;

import com.neep.meatlib.client.screen.auto.*;
import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.slot.MobileSlot;
import com.neep.meatlib.client.screen.slot.SimpleSlotBlockWidget;
import com.neep.meatlib.client.screen.slot.VanillaInventoryWidget;
import com.neep.meatlib.client.screen.widget.ClickableWidget;
import com.neep.meatlib.client.screen.widget.IntNumberTextField;
import com.neep.meatlib.client.screen.widget.config.ConfigPanel;
import com.neep.meatlib.screen.ConfigWidgetHandler;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.theme.GuideCols;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.plc.block.entity.ProgramCabinetBlockEntity;
import com.neep.neepmeat.plc.screen.ProgramCabinetScreenHandler;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ProgramCabinetScreen extends BetterHandledScreen<ProgramCabinetScreenHandler>
{
    private final ConfigPanel1 configPanel;

    public ProgramCabinetScreen(ProgramCabinetScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
        this.configPanel = new ConfigPanel1(handler.handler);
    }

    @Override
    protected void init()
    {
        imageWidth = PLCCols.INV_W;
        imageHeight = 166 + PLCCols.INV_H;

        super.init();

        addRenderableWidget(
                new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                        .setLeft(new SnapPanel<>(new Background(new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                                .setLeft(
                                    new LabeledPanel(Component.literal("Program Disks").setStyle(Style.EMPTY.withFont(GuideCols.FONT)),
                                            addSlotBlock(new SlotBlock(5, 6, menu.cabinetSlots, this)))
                                            .col(0x87686a))
                                .setRight(new MeatBlackBackgroundPanel(configPanel.offset))
                                .setFit(SplitPanel.Fit.FIRST)
                        )))
                        .setRight(addSlotBlock(new VanillaInventoryWidget(menu.playerSlots, this)))
                        .setFit(SplitPanel.Fit.SECOND)
                        .centre(true)
                        )
                .setDims(leftPos, topPos, imageWidth, imageHeight);

        ScreenReady.ready();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }

    private static class Background extends HashedBackgroundPanel
    {
        public Background(ClickableWidget element)
        {
            super(element);
//            solidW();
//            solidH();
            w = 128;
            h = 166;
//            setSize(128, 166);
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            GUIUtil.drawTexture(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/gui/plc/program_cabinet.png"),
                context, x0(), y0(), 0, 0, nw(), nh());
            element.render(context, mouseX, mouseY, delta);
        }

    }

    private static class SlotBlock extends SimpleSlotBlockWidget
    {
        public SlotBlock(int rows, int cols, List<Slot> slots, BetterHandledScreen<?> handler)
        {
            super(0, 0, rows, cols, slots, handler);
        }

        @Override
        protected void drawSlot(GuiGraphics context, MobileSlot mobileSlot)
        {
            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/gui/plc/program_cabinet.png");
            if (mobileSlot.slot().hasItem())
                GUIUtil.drawTexture(tex, context, mobileSlot.x0() - 1, mobileSlot.y0() - 1, 128 + 18, 0, 18, 25);
            else
                GUIUtil.drawTexture(tex, context, mobileSlot.x0() - 1, mobileSlot.y0() - 1, 128, 0, 18, 25);
//            super.drawSlot(context, mobileSlot);
        }

        @Override
        protected void setSlotPositions()
        {
            setSlotPositions(x + 2, y + 2, slots, cols, rows);
        }

        @Override
        protected static void setSlotPositions(int x, int y, List<MobileSlot> slots, int nx, int ny)
        {
            int dx = 20; // Usually 18
            int dy = 25; // Usually 18

            int idx = 0;
            int m, l;
            for (m = 0; m < ny; ++m)
            {
                for (l = 0; l < nx; ++l)
                {
                    slots.get(idx).setPos(x + l * dx, y + m * dy);
                    ++idx;
                }
            }
        }

        @Override
        public int nw()
        {
            return cols * 20 - 2 + 2;
        }

        @Override
        public int nh()
        {
            return rows * 25 - 2 + 2;
        }
    }

    private static class SegmentOffsetWidget extends IntNumberTextField
    {
        public SegmentOffsetWidget(Font textRenderer, Component prefix, Consumer<IntNumberTextField> update)
        {
            super(textRenderer, 0, 0, 100, PLCCols.ENTRY_H, prefix, update, 1, 1024, 1, 30);
            tooltip(NeepMeat.translationKey("tooltip", "program_cabinet.segment_offset", ProgramCabinetBlockEntity.SLOTS));
        }

        @Override
        public void renderButton(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            super.renderButton(context, mouseX, mouseY, delta);

            int prefixStart = x0() + (drawFancyBackground ? 4 : 2);
            int start = getTextStart(prefixStart) + textRenderer.width(this.text()) + 3;
            // Format the offset of the last slot
            GUIUtil.drawText(context, textRenderer, Component.nullToEmpty("- %s".formatted(parse() + ProgramCabinetBlockEntity.SLOTS - 1)),
                    start, getTextY(),
                    PLCCols.TEXT.col, false);
        }
    }

    private static class ConfigPanel1 extends ConfigPanel<ProgramCabinetScreenHandler.WidgetHandler.Values>
    {
        public final IntNumberTextField offset;

        public ConfigPanel1(ConfigWidgetHandler<ProgramCabinetScreenHandler.WidgetHandler.Values> handler)
        {
            super(handler);
            offset = addTrackedField(new SegmentOffsetWidget(textRenderer, Component.literal("Offset"),
            f -> update()));
        }

        @Override
        protected ProgramCabinetScreenHandler.WidgetHandler.Values createValues()
        {
            return new ProgramCabinetScreenHandler.WidgetHandler.Values(offset.parse());
        }

        @Override
        protected void updateParams(ProgramCabinetScreenHandler.WidgetHandler.Values values)
        {
            offset.setNumberText(values.offset());
        }
    }
}
