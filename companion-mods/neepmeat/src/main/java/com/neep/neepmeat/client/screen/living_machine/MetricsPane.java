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

package com.neep.neepmeat.client.screen.living_machine;

import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.theme.PLCCols;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.api.live_machine.StructurePropertyFormatter;
import com.neep.neepmeat.client.screen.button.OpenGuideButton;
import com.neep.neepmeat.client.screen.plc.PLCScreenButton;
import com.neep.neepmeat.screen_handler.LivingMachineScreenHandler;
import it.unimi.dsi.fastutil.Pair;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public class MetricsPane extends LivingMachineScreen.PaneWidget
{
    private final Font textRenderer = Minecraft.getInstance().font;

    private final LivingMachineScreenHandler handler;
    private final LivingMachineScreen parent;
    private final List<Pair<Component, Supplier<String>>> entries = new ArrayList<>();

    private final DecimalFormat floatFormat = new DecimalFormat("####.#");
    private final DecimalFormat smallFloatFormat = new DecimalFormat("####.####");
    private final DecimalFormat intFormat = new DecimalFormat("####");
    private List<Component> problems = List.of();

    public MetricsPane(LivingMachineScreenHandler handler, LivingMachineScreen parent)
    {
        this.handler = handler;
        this.parent = parent;

        entries.add(Pair.of(
                NeepMeat.translationKey("screen", "living_machine.rated_power"),
                () -> PowerUtil.perUnitToText(handler.getBlockEntity().getRatedPower()).getString()));
        entries.add(Pair.of(
                NeepMeat.translationKey("screen", "living_machine.power"),
                () -> PowerUtil.perUnitToText(handler.getBlockEntity().getPower()).getString()));
        entries.add(Pair.of(
                NeepMeat.translationKey("screen", "living_machine.efficiency"),
                () -> intFormat.format(100 * handler.getBlockEntity().getEfficiency()) + "%"));
        entries.add(Pair.of(
                NeepMeat.translationKey("screen", "living_machine.degradation_rate"),
                () -> formatRepair(handler.getBlockEntity().getCurrentDegradationRate())));
        entries.add(Pair.of(
                NeepMeat.translationKey("screen", "living_machine.self_repair"),
                () -> formatRepair(handler.getBlockEntity().getSelfRepair())));
        entries.add(Pair.of(
                NeepMeat.translationKey("screen", "living_machine.rul"),
                () -> formatRUL(handler.getBlockEntity().getRulSecs())));
        entries.add(Pair.of(Component.empty(), () -> ""));
        entries.add(Pair.of(
                NeepMeat.translationKey("screen", "living_machine.size"),
                () -> intFormat.format(handler.getBlockEntity().getNumStructures())));
        entries.add(Pair.of(
                NeepMeat.translationKey("screen", "living_machine.components"),
                () -> intFormat.format(handler.getBlockEntity().getNumComponents())));

        entries.add(Pair.of(
                NeepMeat.translationKey("screen", "living_machine.process"), () -> ""));
        entries.add(Pair.of(
                Component.empty(),
                () -> handler.getBlockEntity().getProcess().getString()));

        entries.add(Pair.of(
                NeepMeat.translationKey("screen", "living_machine.problems"), () -> problems.isEmpty() ? "None" : ""));

        entries.add(Pair.of(Component.empty(), () ->
        {
            StringBuilder sb = new StringBuilder();
            for (Component problem : problems)
            {
                sb.append(problem.getString()).append("\n");
            }
            return sb.toString();
        }));
    }

    @Override
    public void init()
    {
        super.init();
        addChild(new ThingyButton(x0() + 2, y0() + nh() - 16 - 2, 112, parent::switchMode));
        var guideButton = addChild(new OpenGuideButton("living_machines"));
        guideButton.setPos(x0() + 2 + 17, y1() - 16 - 2);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta)
    {
        super.render(context, mouseX, mouseY, delta);

        int yStride = textRenderer.lineHeight + 2;

        int padding = 3;
        int yOffset = y0() + padding;
        int xOffset = x0() + padding;
        int xRight = x0() + nw() - padding;
        int textCol = PLCCols.TEXT.col;
        int numberCol = PLCCols.SELECTED.col;
        for (var entry : entries)
        {
            GUIUtil.drawText(context, textRenderer, entry.first(), xOffset, yOffset, textCol, false);
            String number = entry.second().get();
            var lines = textRenderer.split(FormattedText.of(number), nw());
            if (lines.isEmpty())
            {
                yOffset += yStride;
            }
            else
            {
                for (var line : lines)
                {
                    int numberWidth = textRenderer.width(line);

                    GUIUtil.drawText(context, textRenderer, line, xRight - numberWidth, yOffset, numberCol, false);

                    yOffset += yStride;
                }
            }
        }
    }

    private static String formatRUL(long secs)
    {
        if (secs == -1)
            return "infinite";

        Duration duration = Duration.ofSeconds(secs);
        if (duration.toHoursPart() > 0)
        {
            return duration.toHoursPart() + "hr " + duration.toMinutesPart() + "m";
        }
        else if (duration.toMinutesPart() > 0)
        {
            return duration.toMinutesPart() + "m " + duration.toSecondsPart() + "s";
        }
        else
        {
            return duration.toSecondsPart() + "s";
        }
    }

    private String formatRepair(float repair)
    {
        return StructurePropertyFormatter.formatRepair(repair);
    }

    public void updateProblems(List<Component> problems)
    {
        this.problems = problems;
    }

    public void tick()
    {

    }

    public static class ThingyButton extends PLCScreenButton
    {
        private final int u;
        private final Runnable onClick;

        public ThingyButton(int x, int y, int u, Runnable onClick)
        {
            super(x, y, Component.empty());
            this.u = u;
            this.onClick = onClick;
        }

        @Override
        protected int getU()
        {
            return u;
        }

        @Override
        public void onClick(double mouseX, double mouseY)
        {
            onClick.run();
        }
    }
}
