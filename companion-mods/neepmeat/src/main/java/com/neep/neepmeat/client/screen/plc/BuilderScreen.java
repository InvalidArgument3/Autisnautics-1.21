package com.neep.neepmeat.client.screen.plc;

import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.NMTextField;
import com.neep.meatlib.client.screen.auto.*;
import com.neep.meatlib.client.screen.slot.VanillaInventoryWidget;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.screen.util.BeigeSlotBlockWidget;
import com.neep.neepmeat.plc.screen.BuilderScreenHandler;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class BuilderScreen extends BetterHandledScreen<BuilderScreenHandler>
{
    private NMTextField textField;

    public BuilderScreen(BuilderScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
    }

    @Override
    protected void init()
    {
        ScreenReady.ready();
        imageWidth = PLCCols.INV_W;
        imageHeight = 3 * 40 + 8 + PLCCols.INV_H;

        super.init();

        this.textField = new NMTextField(font, 0, 0, 0, PLCCols.ENTRY_H, Component.empty())
                                .drawFancyBackground(false);
        textField.setEditable(false);
        textField.tooltip(() -> List.of(NeepMeat.translationKey("screen", "builder.pattern.tooltip")));

        var main = new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                .setLeft(new HashedBackgroundPanel(new VStackPanel(0, 1, false)
                        .addChild(new LabeledPanel(Component.literal("Materials"), new SnapPanel<>(
                            addSlotBlock(new SlotBlock(3, 9, menu.internalSlots, this)))))
                        .addChild(new BlackBackgroundPanel(textField).solidH()))
                )
                .setRight(addSlotBlock(new VanillaInventoryWidget(menu.playerSlots, this)));

        addRenderableWidget(main);
        main.setDims(leftPos, topPos, imageWidth, imageHeight);
    }

    @Override
    protected void containerTick()
    {
        super.containerTick();
        this.textField.setText(menu.pattern);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
        if (menu.selectedSlot == -1)
        {
            GUIUtil.renderBorderInner(context, textField, PLCCols.TEXT.col, 0);
        }
    }

    private class SlotBlock extends BeigeSlotBlockWidget
    {
        public SlotBlock(int rows, int cols, List<Slot> slots, BetterHandledScreen<?> handler)
        {
            super(rows, cols, slots, handler);
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            super.render(context, mouseX, mouseY, delta);

            var sel = BuilderScreen.this.menu.selectedSlot;
            if (sel >= 0 && sel < rows * cols)
            {
                int row = Math.floorDiv(sel, cols);
                int col = sel % cols;
                GUIUtil.renderBorderInner(context, this.x0() + col * 18 + 1, this.y0() + row * 18 + 1,
                        18 - 1, 18 - 1, PLCCols.BORDER.col, 0);
            }
        }
    }
}
