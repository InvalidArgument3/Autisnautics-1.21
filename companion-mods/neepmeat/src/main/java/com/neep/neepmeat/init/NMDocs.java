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

package com.neep.neepmeat.init;

import com.neep.neepmeat.documentation.DocumentationReloadListener;
import com.neep.neepmeat.guide.GuideReloadEvent;
import com.neep.neepmeat.plc.instruction.gui.InstructionAttributes;
import com.neep.neepmeat.thord.documentation.ThordWordArticle;
import com.neep.neepmeat.thord.documentation.WordAttributes;
import com.neep.meatlib.resource.FabricResources.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class NMDocs
{
    public static final DocumentationReloadListener THORD_WORD_DOCS = new DocumentationReloadListener("thord");

    public static void init()
    {
        InstructionAttributes.init();
        WordAttributes.init();
        GuideReloadEvent.POST.register(guide ->
        {
            var menu = guide.getMenu("thord_words");
            if (menu != null)
                guide.insert("thord_words_article", new ThordWordArticle(menu));
        });
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(THORD_WORD_DOCS);
    }
}
