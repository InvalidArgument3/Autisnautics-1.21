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

package com.neep.neepmeat.plc.instruction.gui;

import com.google.common.collect.Maps;
import com.neep.neepmeat.plc.Instructions;
import com.neep.neepmeat.plc.instruction.InstructionProvider;
import java.util.Locale;
import java.util.Map;
import net.minecraft.network.chat.Component;

public class InstructionAttributes
{
    private static final Map<InstructionProvider, InstructionTooltip> TOOLTIPS = Maps.newHashMap();

    public static void register(InstructionProvider provider, InstructionTooltip tooltip)
    {
        TOOLTIPS.put(provider, tooltip);
    }

    private static void register(InstructionProvider provider, Category category)
    {
        register(provider, new InstructionTooltip()
                .tooltip(Component.translatable("instruction.neepmeat." + provider.getParseName().toLowerCase(Locale.ROOT) + ".desc"))
                .category(category));
    }

    public static InstructionTooltip get(InstructionProvider provider)
    {
        return TOOLTIPS.getOrDefault(provider, InstructionTooltip.EMPTY);
    }

    public static void init()
    {
        register(Instructions.END, Category.CONTROL);
        register(Instructions.RESTART, Category.CONTROL);
        register(Instructions.RET, Category.CONTROL);
        register(Instructions.CALL, Category.CONTROL);
        register(Instructions.DYCALL, Category.CONTROL);
        register(Instructions.DYLOC, Category.CONTROL);
        register(Instructions.IHANDLER, Category.CONTROL);
        register(Instructions.IWAIT, Category.CONTROL);
        register(Instructions.ICLEAR, Category.CONTROL);
        register(Instructions.IENABLE, Category.CONTROL);
        register(Instructions.IDISABLE, Category.CONTROL);
        register(Instructions.PUSH, Category.VARIABLE);
        register(Instructions.POP, Category.VARIABLE);
        register(Instructions.DUP, Category.VARIABLE);
        register(Instructions.SWAP, Category.VARIABLE);
        register(Instructions.OVER, Category.VARIABLE);
        register(Instructions.ROT, Category.VARIABLE);
        register(Instructions.PICK, Category.VARIABLE);
        register(Instructions.TO_RET, Category.VARIABLE);
        register(Instructions.FROM_RET, Category.VARIABLE);
        register(Instructions.RET_FETCH, Category.VARIABLE);
        register(Instructions.EXTFETCH, Category.VARIABLE);
        register(Instructions.FREE, Category.VARIABLE);
        register(Instructions.STORE, Category.VARIABLE);
        register(Instructions.FETCH, Category.VARIABLE);
        register(Instructions.EXTSTORE, Category.VARIABLE);
        register(Instructions.EXTFETCH, Category.VARIABLE);
        register(Instructions.DELAY, Category.CONTROL);
        register(Instructions.EQ, Category.COMPARISON);
        register(Instructions.LT, Category.COMPARISON);
        register(Instructions.LTEQ, Category.COMPARISON);
        register(Instructions.GT, Category.COMPARISON);
        register(Instructions.GTEQ, Category.COMPARISON);

        register(Instructions.INC, Category.ARITHMETIC);
        register(Instructions.DEC, Category.ARITHMETIC);
        register(Instructions.NEG, Category.ARITHMETIC);
        register(Instructions.ADD, Category.ARITHMETIC);
        register(Instructions.SUB, Category.ARITHMETIC);
        register(Instructions.MUL, Category.ARITHMETIC);
        register(Instructions.DIV, Category.ARITHMETIC);
        register(Instructions.NOT, Category.ARITHMETIC);
        register(Instructions.AND, Category.ARITHMETIC);
        register(Instructions.OR, Category.ARITHMETIC);
        register(Instructions.NAND, Category.ARITHMETIC);
        register(Instructions.NOR, Category.ARITHMETIC);
        register(Instructions.XOR, Category.ARITHMETIC);
        register(Instructions.XNOR, Category.ARITHMETIC);
        register(Instructions.SHR, Category.ARITHMETIC);
        register(Instructions.SHL, Category.ARITHMETIC);

        register(Instructions.JUMP, Category.CONTROL);
        register(Instructions.BIT, Category.CONTROL);
        register(Instructions.BIF, Category.CONTROL);
        register(Instructions.SAY, Category.MISC);
        register(Instructions.REMOVE, Category.MANUFACTURE);
        register(Instructions.ROBOT, Category.MANUFACTURE);
        register(Instructions.MOVETO, Category.MANUFACTURE);
        register(Instructions.EXEC, Category.CONTROL);
        register(Instructions.COMBINE, Category.MANUFACTURE);
        register(Instructions.MOVE, Category.MANUFACTURE);
        register(Instructions.IMPLANT, Category.MANUFACTURE);
        register(Instructions.INJECT, Category.MANUFACTURE);

        register(Instructions.WAIT_REDSTONE, Category.REDSTONE);
        register(Instructions.EMIT_REDSTONE, Category.REDSTONE);
        register(Instructions.READ_REDSTONE, Category.REDSTONE);
        register(Instructions.RISING_EDGE, Category.COMPARISON);

        register(Instructions.NEEPBUS_WRITE, Category.NEEPBUS);
        register(Instructions.NEEPBUS_READ, Category.NEEPBUS);

        register(Instructions.REQUEST, Category.ROUTING);
        register(Instructions.ROUTE, Category.ROUTING);
        register(Instructions.COUNT, Category.ROUTING);
        register(Instructions.STOCK, Category.ROUTING);
        register(Instructions.FINDSLOT, Category.ROUTING);

        register(Instructions.BPLACE, Category.BLOCK);
        register(Instructions.BBREAK, Category.BLOCK);
        register(Instructions.SELSLOT, Category.BLOCK);
        register(Instructions.SELITEM, Category.BLOCK);
        register(Instructions.AREA3, Category.BLOCK);
    }

    public static class InstructionTooltip
    {
        private Category category = Category.MISC;
        private Component description = Component.empty();

        public InstructionTooltip()
        {

        }

        public InstructionTooltip tooltip(Component text)
        {
            this.description = text;
            return this;
        }

        public InstructionTooltip category(Category category)
        {
            this.category = category;
            return this;
        }

        public Category category()
        {
            return category;
        }

        public Component description()
        {
            return description;
        }

        public static final InstructionTooltip EMPTY = new InstructionTooltip();
    }

    public enum Category
    {
        ARITHMETIC(Component.translatable("category.neepmeat.instruction.arithmetic")),
        MANUFACTURE(Component.translatable("category.neepmeat.instruction.manufacture")),
        COMPARISON(Component.translatable("category.neepmeat.instruction.comparison")),
        CONTROL(Component.translatable("category.neepmeat.instruction.control")),
        VARIABLE(Component.translatable("category.neepmeat.instruction.variable")),
        REDSTONE(Component.translatable("category.neepmeat.instruction.redstone")),
        NEEPBUS(Component.translatable("category.neepmeat.instruction.neepbus")),
        ROUTING(Component.translatable("category.neepmeat.instruction.routing")),
        MISC(Component.translatable("category.neepmeat.instruction.misc")),
        BLOCK(Component.translatable("category.neepmeat.instruction.block"));

        public final Component name;

        Category(Component name)
        {
            this.name = name;
        }
    }
}
