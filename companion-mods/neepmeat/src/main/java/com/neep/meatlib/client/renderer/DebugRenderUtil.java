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

package com.neep.meatlib.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public class DebugRenderUtil
{
    public static void renderLine(PoseStack matrices, float x0, float y0, float z0, float x1, float y1, float z1, MultiBufferSource vcp)
    {
        renderLine(matrices, x0, y0, z0, x1, y1, z1, 0, 0, 0, vcp);
    }

    public static void renderLine(PoseStack matrices, float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float b, MultiBufferSource vcp)
    {
        float k = x1 - x0;
        float l = y1 - y0;
        float m = z1 - z0;
        float n = Mth.sqrt(k * k + l * l + m * m);
        k /= n;
        l /= n;
        m /= n;

        PoseStack.Pose entry = matrices.last();
        var buffer = vcp.getBuffer(RenderType.lines());
        buffer.addVertex(entry.pose(), x0, y0, z0).setColor(r, g, b, 1).setNormal(entry.normal(), k, l, m);
        buffer.addVertex(entry.pose(), x1, y1, z1).setColor(r, g, b, 1).setNormal(entry.normal(), k, l, m);
    }

    public static void renderHCircle(PoseStack matrices, float x, float y, float z, float radius, float r, float g, float b, MultiBufferSource vcp)
    {
        // I know this is a terribly inefficient way of doing it, but I am losing patience.
        PoseStack.Pose entry = matrices.last();
        var buffer = vcp.getBuffer(RenderType.lines());

        int points = 10;
        for (int i = 0; i < points; ++i)
        {
            float d = (float) i / points;
            float d1 = (float) (i + 1) / points;
            float dx = radius * Mth.sin(d * Mth.PI * 2);
            float dz = radius * Mth.cos(d * Mth.PI * 2);

            float dx1 = radius * Mth.sin((d1 + 1) * Mth.PI * 2);
            float dz1 = radius * Mth.cos((d1 + 1) * Mth.PI * 2);

            float x0 = x + dx;
            float z0 = z + dz;

            float x1 = x + dx1;
            float z1 = z + dz1;

            // Normal in line direction
            float k = x1 - x0;
            float l = 0;
            float m = z1 - z0;
            float n = Mth.sqrt(k * k + l * l + m * m);
            k /= n;
            l /= n;
            m /= n;

            buffer.addVertex(entry.pose(), x + dx, y, z + dz).setColor(r, g, b, 1).setNormal(entry.normal(), k, l, m);
            buffer.addVertex(entry.pose(), x + dx1, y, z + dz1).setColor(r, g, b, 1).setNormal(entry.normal(), k, l, m);
        }
    }
}
