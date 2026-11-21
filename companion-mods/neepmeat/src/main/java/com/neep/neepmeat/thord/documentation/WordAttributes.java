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

package com.neep.neepmeat.thord.documentation;

import com.neep.neepmeat.NeepMeat;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;

public class WordAttributes
{
    public static final Map<String, Entry> ENTRIES = new HashMap<>();

    public static void register(String word, Category category)
    {
        ENTRIES.put(word, new Entry(word, NeepMeat.translationKey("word", word + ".desc"), category));
    }

    public static Entry get(String word)
    {
        return ENTRIES.get(word);
    }

    public static void init()
    {
        register("blank", Category.MISC);
    }

    public record Entry(String name, Component description, Category category) { }

    public enum Category
    {
        STACK(Component.translatable("category.neepmeat.thord.stack")),
        ARITHMETIC(Component.translatable("category.neepmeat.thord.arithmetic")),
        COMPARISON(Component.translatable("category.neepmeat.thord.comparison")),
        FLOW_CONTROL(Component.translatable("category.neepmeat.thord.flow_control")),
        MISC(Component.translatable("category.neepmeat.thord.misc"));

        ;
        public final Component name;

        Category(Component name)
        {
            this.name = name;
        }
    }
}
