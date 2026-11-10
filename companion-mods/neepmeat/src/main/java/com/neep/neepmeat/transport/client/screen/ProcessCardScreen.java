package com.neep.neepmeat.transport.client.screen;

import com.neep.meatlib.client.screen.auto.*;
import com.neep.meatlib.client.screen.BetterHandledScreen;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.primitive.Point;
import com.neep.meatlib.client.screen.slot.VanillaInventoryWidget;
import com.neep.meatlib.client.screen.widget.AbstractClickableWidget;
import com.neep.meatlib.client.screen.widget.TextureWidget;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.transport.screen.ProcessCardScreenHandler;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ProcessCardScreen extends BetterHandledScreen<ProcessCardScreenHandler>
{
    public ProcessCardScreen(ProcessCardScreenHandler handler, Inventory inventory, Component title)
    {
        super(handler, inventory, title);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void containerTick()
    {
        super.containerTick();
    }

    @Override
    protected void init()
    {
        imageWidth = 200;
        imageHeight = 80 + PLCCols.INV_H;
        super.init();

        var arrow = new TextureWidget(GUIUtil.SCREEN_BORDER, 48, 241, 22, 15, PLCCols.BORDER.col);

        var main = addRenderableWidget(new SplitPanel(SplitPanel.Split.VERTICAL, 0)
                .centre(true)
                .setLeft(new HashedBackgroundPanel(new BlackBackgroundPanel(
                        new SplitPanel(SplitPanel.Split.HORIZONTAL, 0)
                                .setLeft(new SnapPanel<>(SnapPanel.Snap.LEFT, new Grid()))
                                .setRight(new GridPanel(1, 2)
                                        .set(0, 0, new SnapPanel<>(arrow))
                                        .set(1, 0, new SnapPanel<>(new ItemSlotWidget(0, 0, () ->
                                                menu.slotsWidgetHandler.resultStack, menu.slotsWidgetHandler::setResultStack))))
                )))
                .setRight(addSlotBlock(new VanillaInventoryWidget(menu.slots, this))))
                .setFit(SplitPanel.Fit.SECOND)
                ;

        main.setDims(leftPos, topPos, imageWidth, imageHeight);

        ScreenReady.ready();
    }

    private ItemStack getItem(int slotIndex)
    {
        return menu.slotsWidgetHandler.inventory.getItem(slotIndex);
    }

    private class Grid extends GeneralPanel
    {
        public Grid()
        {

        }

        public void init()
        {
            int slotsW = 5;
            int slotsH = 3;
            clearChildren();
            for (int j = 0; j < slotsH; j++)
            {
                for (int i = 0; i < slotsW; i++)
                {
                    int slotIdx = i + j * slotsW;
                    addChild(new ItemSlotWidget(x0() + i * 17, y0() +  + 17 * j,
                            () -> getItem(slotIdx), s ->
                    {
                        menu.slotsWidgetHandler.inventory.setItem(slotIdx, s);
                        menu.slotsWidgetHandler.inventory.setChanged();
                    }));
                }
            }

            h = slotsH * 17;
            w = slotsW * 17;
        }

        public boolean mouseScrolled(double mouseX, double mouseY, double amount)
        {
            return super.mouseScrolled(mouseX, mouseY, amount);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        for (var child : children())
        {
            if (child.isMouseOver(mouseX, mouseY))
            {
                if (child.mouseReleased(mouseX, mouseY, button))
                    return true;
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    public class ItemSlotWidget extends AbstractClickableWidget implements Point.Mutable
    {
        private final Supplier<ItemStack> get;
        private final Consumer<ItemStack> set;

        public ItemSlotWidget(int x, int y, Supplier<ItemStack> get, Consumer<ItemStack> set)
        {
            super(x, y, 18, 18, Component.empty());
            this.get = get;
            this.set = set;
            tooltip(() ->
            {
                ItemStack stack = get.get();
                if (!stack.isEmpty())
                {
                    var list = stack.getTooltipLines(client.player, TooltipFlag.NORMAL);
                    list.add(Component.nullToEmpty("Scroll to change count"));
                    return list;
                }
                else
                {
                    return List.of(Component.nullToEmpty("Click with an item or drag one from EMI"));
                }
            });
            tooltipDelayMs = 0;
        }

        protected void renderButton(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            GUIUtil.renderBorderInner(context, x0(), y0(), 18, 18, PLCCols.BORDER.col, 0);
            GUIUtil.renderBorderInner(context, x0(), y0(), 18, 18, PLCCols.TRANSPARENT.col, -1);

            ItemStack stack = get.get();
            if (!stack.isEmpty())
            {
                context.renderItem(stack, x0() + 1, y0() + 1);
                context.renderItemDecorations(font, stack, x0() + 1, y0() + 1);
            }

//            if (isMouseOver(mouseX, mouseY) && !stack.isEmpty() && handler.getCursorStack().isEmpty())
//            {
//                GlStateManager._disableScissorTest();
//                TooltipHelper.get().draw(context, mouseX, mouseY, stack.getTooltip(client.player, TooltipContext.BASIC));
////                GlStateManager._enableScissorTest();
//            }
        }

        public boolean mouseScrolled(double mouseX, double mouseY, double amount)
        {
            ItemStack stack = get.get();
            if (!stack.isEmpty())
            {
                int newCount = Mth.clamp(stack.getCount() + (int) amount, 1, 64);
                if (newCount != stack.getCount())
                {
                    var player = client.player;
                    client.getSoundManager().play(new SimpleSoundInstance(
                            MeatlibSounds.CLICK, SoundSource.PLAYERS,
                            0.4f, Mth.clamp(1 + 1.5f * (newCount / 64f), 1f, 1.5f), SoundInstance.createUnseededRandom(),
                            player.getX(), player.getY(), player.getZ()));
                    setStack(stack.copyWithCount(newCount));
                }
            }

            return super.mouseScrolled(mouseX, mouseY, amount);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button)
        {
            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public void onClick(double mouseX, double mouseY)
        {
            super.onClick(mouseX, mouseY);
            setStack(menu.getCarried());
        }

        public void setStack(ItemStack stack)
        {
            set.accept(stack);
        }
    }
}
