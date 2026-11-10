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

package com.neep.neepmeat.enlightenment;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.neep.neepmeat.NeepMeat;
import com.neep.meatlib.client.screen.GUIUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class EnlightenmentSleepOverlays
{
    private static final Minecraft CLIENT = Minecraft.getInstance();

    private static int prevSleepTimer;
    @Nullable private static SleepOverlay ACTIVE = null;
    private static final RandomSource RANDOM = RandomSource.create();

    public static void render(GuiGraphics context, float tickDelta)
    {
        LocalPlayer player = CLIENT.player;
        if (player != null)
        {
            int timer = player.getSleepTimer();

            // Detect the rising edge and choose an event
            if (timer > 0 && prevSleepTimer == 0)
            {
//                float p = EnlightenmentUtil.getEventProbability(player) * 4;
                float p = EnlightenmentManager.get(player).getTotal() > 1000 ? 0.1f : 0;
                float f = RANDOM.nextFloat();
                if (f < p)
                {
                    ACTIVE = POTATO_CAT;
                }
            }

            if (ACTIVE != null)
            {
                ACTIVE.render(context, tickDelta, timer);
            }

            if (ACTIVE != null && ACTIVE.finished(timer))
            {
                ACTIVE = null;
            }

            prevSleepTimer = timer;
        }
    }

    public interface SleepOverlay
    {
        void render(GuiGraphics context, float tickDelta, int timer);

        default boolean finished(int timer)
        {
            return timer > 100;
        }
    }

    private static final SleepOverlay POTATO_CAT = new SleepOverlay()
    {
        private static final ResourceLocation POTATO_CAT_TEXTURE = new ResourceLocation(NeepMeat.NAMESPACE, "textures/gui/misc/potato_cat.png");

        @Override
        public void render(GuiGraphics context, float tickDelta, int timer)
        {
            if (timer > 0)
            {
                Window window = CLIENT.getWindow();

                float j = timer / 100.0F;
                if (j > 1.0F)
                {
                    j = 0;
                }
                else
                {
                    j = Mth.clamp(j * j, 0.f, 0.2f);
                }

                float wWidth = window.getGuiScaledWidth();
                float wHeight = window.getGuiScaledHeight();
                float scale = 0.5f;
                float width = wWidth * scale;
                float height = width; // Lock aspect ratio
                int x1 = (int) (wWidth - width);
                int x2 = (int) wWidth;

                int y1 = (int) (wHeight - height);
                int y2 = (int) wHeight;

                // The fragment shader discards anything with alpha < 0.1. Very annoying.
                RenderSystem.setShaderColor(1, 1 , 1, j);
                GUIUtil.drawTexture(POTATO_CAT_TEXTURE, context, x1, x2, y1, y2, 0,
                        256, 256, 0, 0, 256, 256, 1, 1, 1, 1);
            }
        }
    };
}
