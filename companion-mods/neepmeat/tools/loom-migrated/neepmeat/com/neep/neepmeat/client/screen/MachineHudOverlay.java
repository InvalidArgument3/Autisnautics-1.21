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

package com.neep.neepmeat.client.screen;

import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.neepmeat.network.MachineDiagnosticsRequest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import java.awt.*;

@Environment(EnvType.CLIENT)
public class MachineHudOverlay
{
    static MachineHudOverlay INSTANCE = new MachineHudOverlay();

    public static MachineHudOverlay getInstance()
    {
        return INSTANCE;
    }

    protected MotorisedBlock.Diagnostics diagnostics = null;
    protected int updateInterval = 10;
    protected int updateCounter = 0;

    public MachineHudOverlay()
    {
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) ->
        {
            getInstance().update(Minecraft.getInstance(), Minecraft.getInstance().level);
            getInstance().onRender(Minecraft.getInstance(), matrixStack, tickDelta);
        });
    }

    public void receive(MotorisedBlock.Diagnostics diagnostics)
    {
        this.diagnostics = diagnostics;
    }

    public void update(Minecraft client, ClientLevel world)
    {
        updateCounter = Math.min(updateInterval, updateCounter + 1);
        if (world == null || client.player == null || updateCounter < updateInterval)
        {
            return;
        }

        updateCounter = 0;

        if (client.hitResult != null && client.hitResult instanceof BlockHitResult result)
        {
            BlockPos pos = result.getBlockPos();
            if (world.getBlockEntity(pos) instanceof MotorisedBlock.DiagnosticsProvider)
            {
                MachineDiagnosticsRequest.Client.send(world, pos);
            }
            else
            {
                diagnostics = null;
            }
        }
    }

    public void onRender(Minecraft client, GuiGraphics context, float tickDelta)
    {
        if (diagnostics != null)
        {
            Font textRenderer = client.font;

            float width = client.getWindow().getGuiScaledWidth();
            float height = client.getWindow().getGuiScaledHeight();

            {
                boolean problem = diagnostics.problem();
                if (problem)
                {
                    Component t = diagnostics.title();
                    Component m = diagnostics.message();

                    int textWidth = textRenderer.width(t);
                    int textHeight = textRenderer.lineHeight;

                    GUIUtil.drawText(context, textRenderer, t,
                            (width - textWidth) / 2, (height + 1.5f * textHeight) / 2, Color.RED.getRGB(), true);

                    textWidth = textRenderer.width(m);
                    GUIUtil.drawText(context, textRenderer, m,
                            (width - textWidth) / 2, (height + 3.5f * textHeight) / 2, Color.RED.getRGB(), true);
                }
            }
        }
    }

    public static void init()
    {
    }
}
