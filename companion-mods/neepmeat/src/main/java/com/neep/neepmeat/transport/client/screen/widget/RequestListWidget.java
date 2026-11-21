/*
 * Copyright (c) MeatWheeze 2026.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.transport.client.screen.widget;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.auto.Panel;
import com.neep.meatlib.client.screen.auto.ScreenInitialisable;
import com.neep.meatlib.client.screen.auto.ScrollVStackPanel;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.client.screen.plc.PLCScreenButton;
import com.neep.neepmeat.transport.api.item_network.RoutingNetwork;
import com.neep.neepmeat.transport.screen.widget.RequestListWidgetHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RequestListWidget extends ScrollVStackPanel
{
    @org.jetbrains.annotations.NotNull
    private final RequestListWidgetHandler handler;
    private final Multimap<BlockPos, RoutingNetwork.DeferredRequest> posToRequest = Multimaps.newListMultimap(new HashMap<>(), ObjectArrayList::new);
    private final Font textRenderer = Minecraft.getInstance().font;;

    public RequestListWidget(RequestListWidgetHandler handler)
    {
        super(0, 2, false, false);
        this.handler = handler;

        handler.syncEntriesS2C.receiver(this::receiveEntries);
    }

    private void receiveEntries(List<RoutingNetwork.DeferredRequest> deferredRequests)
    {
        clearChildren();
        posToRequest.clear();
        for (RoutingNetwork.DeferredRequest request : deferredRequests)
        {
            // Adjust position to get the block that actually requested, not just the output pipe.
            BlockPos requesterPos = request.requesterPos.relative(request.requesterFace);
            posToRequest.put(requesterPos, request);
        }

        for (Map.Entry<BlockPos, Collection<RoutingNetwork.DeferredRequest>> entry : posToRequest.asMap().entrySet())
        {
            Level world = Minecraft.getInstance().level;
            BlockState requestState = world.getBlockState(entry.getKey());

            // Manual requester's output pos is itself unlike other blocks
            if (requestState.isAir())
                requestState = world.getBlockState(entry.getValue().iterator().next().requesterPos);

            var widget = new EntryWidget(entry.getKey(), entry.getValue(), requestState);
            addChild(widget);
        }

        init();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);
    }

    private class EntryWidget extends Panel implements Rectangle.Mutable, ScreenInitialisable
    {
        private final BlockPos requestPos;
        private final Collection<RoutingNetwork.DeferredRequest> requests;

        private final DeleteButton deleteButton;
        private final Item requestIcon;

        public EntryWidget(BlockPos requestPos, Collection<RoutingNetwork.DeferredRequest> requests, BlockState iconState)
        {
            this.requestPos = requestPos;
            this.requests = requests;

            this.deleteButton = new DeleteButton(0, 0, Component.empty());

            requestIcon = iconState.getBlock().asItem();
        }

        public void init()
        {
            int rows = (int) Math.ceil((double) (requests.size() * 20) / itemsW());
            h = 20 + rows * 20;

            deleteButton.setPos(x1() - 18, y1() - 18);
        }

        private int itemsW()
        {
            return nw() - 20;
        }

        @Override
        protected List<Renderable> drawables()
        {
            return List.of(deleteButton);
        }

        @Override
        public List<GuiEventListener> children()
        {
            return List.of(deleteButton);
        }

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
        {
            super.render(context, mouseX, mouseY, delta);

            GUIUtil.renderBorder(context, x0(), this.y0(), nw(), nh(), PLCCols.BORDER.col, 0);

            context.renderItem(requestIcon.getDefaultInstance(), x1() - 18, this.y0() + 3);
            GUIUtil.renderBorderInner(context, x1() - 19, this.y0() + 2, 18, 18, PLCCols.BORDER.col, 0);
            GUIUtil.drawText(context, textRenderer,
                    NeepMeat.translationKey("screen", "item_requester.from", requestPos.getX(), requestPos.getY(), requestPos.getZ()),
                    x0() + 2, this.y0() + 2, PLCCols.TEXT.col, false);


            int cols = Math.floorDiv(itemsW(), 20);

            // GGGggGGGGGGGGGGGGGGaauuuuaaau
            int iy = this.y0() + 2 + 20;
            int ix = x0() + 2;
            int i = 0;
            for (var request : requests)
            {
                ItemStack stack = request.getItemVariant().toStack((int) request.getRequested());
                context.renderItem(stack, ix, iy);
                context.renderItemDecorations(textRenderer, stack, ix, iy);
                ix += 20;
                i++;
                if (i % cols == 0)
                {
                    ix = x0() + 2;
                    iy += 20;
                }
            }
        }

        private class DeleteButton extends PLCScreenButton
        {
            public DeleteButton(int x, int y, Component message)
            {
                super(x, y, message);
                tooltip(NeepMeat.translationKey("screen", "item_requester.cancel"));
            }

            @Override
            public void onClick(double mouseX, double mouseY)
            {
                super.onClick(mouseX, mouseY);
                handler.removeEntriesFromC2S.emitter().accept(requests.stream().map(r -> r.requesterPos).toList());
            }

            @Override
            protected int getU()
            {
                return 112;
            }

            @Override
            protected int getV()
            {
                return 48;
            }
        }
    }
}
