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

package com.neep.neepmeat.documentation;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.larkdown.LarkdownContent;
import com.neep.meatlib.larkdown.LarkdownParseException;
import com.neep.meatlib.larkdown.LarkdownParser;
import com.neep.meatlib.larkdown.command.LarkdownCommands;
import com.neep.meatlib.larkdown.content.HeadingLarkdownContent;
import com.neep.meatlib.larkdown.parser.Alignment;
import com.neep.meatlib.larkdown.parser.LineCommandParser;
import com.neep.meatlib.larkdown.parser.TextContentParser;
import com.neep.neepmeat.guide.article.Article;
import com.neep.neepmeat.thord.documentation.CategoryCommand;
import com.neep.neepmeat.thord.documentation.CategoryLarkdownContent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import com.neep.meatlib.resource.FabricResources.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DocumentationReloadListener implements SimpleSynchronousResourceReloadListener
{
    private final String folder;

    // Use linked to preserve order
    private final Map<String, Category> catEntries = new LinkedHashMap<>();
    private final Map<String, Description> docEntries = new LinkedHashMap<>();

    public DocumentationReloadListener(String folder)
    {
        this.folder = folder;
    }

    @Nullable
    public DocumentationReloadListener.Category get(String id)
    {
        return catEntries.get(id);
    }

    public Collection<Category> getEntries()
    {
        return catEntries.values();
    }

    @Override
    public ResourceLocation getFabricId()
    {
        return ResourceLocation.fromNamespaceAndPath(MeatLib.NAMESPACE, "documentation");
    }

    private Predicate<ResourceLocation> pathPredicate(String language)
    {
        return identifier ->
        {
            String path = identifier.getPath();
            return path.contains(language + "/") && path.endsWith(".md");
        };
    }

    public void onResourceManagerReload(ResourceManager manager)
    {
        catEntries.clear();
        docEntries.clear();

        String language = Minecraft.getInstance().getLanguageManager().getSelected();

        LarkdownCommands.Mutable commands = new LarkdownCommands.Mutable();
        commands.register("cat", new CategoryCommand());

        List<LarkdownParser.ContentParser> parsers = List.of(
            new LarkdownParser.HeadingContentParser(),
            new LarkdownParser.BulletContentParser(),
            new LarkdownParser.CodeContentParser(),
            new LineCommandParser(commands),
            new TextContentParser(Alignment.LEFT)
        );

        // Load defaults first
        process(manager, "en_us", parsers);

        // Load localised files. If entries are missing, defaults will already be present.
        if (!language.equals("en_us"))
        {
            process(manager, language, parsers);
        }
    }

    private void process(ResourceManager manager, String language, List<LarkdownParser.ContentParser> parsers)
    {
        Map<ResourceLocation, Resource> foundIds = manager.listResources("docs/" + folder, pathPredicate(language));

        DocumentationLarkdownVisitor visitor = new DocumentationLarkdownVisitor();

        for (var found : foundIds.entrySet())
        {
            try (InputStream stream = found.getValue().open())
            {
                LarkdownParser parser = new LarkdownParser(new InputStreamReader(stream, StandardCharsets.UTF_8), parsers);
                List<LarkdownContent> contents = parser.getContents();

                List<Category> entries = new ObjectArrayList<>();
                entries.add(new Category("", "", new ObjectArrayList<>()));

                for (int i = 0; i < contents.size(); ++i)
                {
                    LarkdownContent content = contents.get(i);

                    if (content instanceof CategoryLarkdownContent category)
                    {
                        i = readCategoryEntry(i, category.id(), contents, entries::add) - 1;
                    }
                    else if (content instanceof HeadingLarkdownContent heading && heading.level() == 2)
                    {
                        // Note lower case
                        String name = heading.text().toLowerCase(Locale.ROOT).split(" ")[0];
                        i = readDocEntry(visitor, i, name, contents, entries.get(entries.size() - 1)) - 1;
                    }
                }

                for (var entry : entries)
                {
                    catEntries.put(entry.id, entry);
                    for (var desc : entry.contents)
                    {
                        docEntries.put(desc.name, desc);
                    }
                }
            }
            catch (IOException | LarkdownParseException e)
            {
                MeatLib.LOGGER.error("Error while loading markdown file {}", found.getKey().toString(), e);
            }
        }
    }

    private int readCategoryEntry(int current, String id, List<LarkdownContent> input, Consumer<Category> consumer)
    {
        int next = current + 1;
        if (next < input.size())
        {
            if (input.get(next) instanceof HeadingLarkdownContent heading && heading.level() == 1)
            {
                consumer.accept(new Category(id, heading.text(), new ObjectArrayList<>()));
            }
            return next + 1;
        }

        return next;
    }

    private int readDocEntry(DocumentationLarkdownVisitor visitor, int current, String name, List<LarkdownContent> input, Category entry)
    {
        List<LarkdownContent> contents = new ObjectArrayList<>();

        int i;
        for (i = current; i < input.size(); ++i)
        {
            LarkdownContent content = input.get(i);

            if (endEntry(content))
            {
                if (!contents.isEmpty())
                {
                    break;
                }
            }

            contents.add(content);
        }

        entry.contents.add(new Description(name, visitor.apply(contents)));
        return i;
    }

    private boolean endEntry(LarkdownContent content)
    {
        return content instanceof HeadingLarkdownContent || content instanceof CategoryLarkdownContent;
    }

    public record Category(String id, String name, List<Description> contents)
    {
    }

    public record Description(String name, List<Article.Content> contents)
    {
    }
}
