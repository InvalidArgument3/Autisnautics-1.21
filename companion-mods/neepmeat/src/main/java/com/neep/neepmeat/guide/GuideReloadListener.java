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

package com.neep.neepmeat.guide;

import com.google.gson.*;
import com.neep.meatlib.larkdown.LarkdownContent;
import com.neep.meatlib.larkdown.LarkdownParseException;
import com.neep.meatlib.larkdown.LarkdownParser;
import com.neep.meatlib.larkdown.command.LarkdownCommands;
import com.neep.meatlib.larkdown.parser.Alignment;
import com.neep.meatlib.larkdown.parser.LineCommandParser;
import com.neep.meatlib.larkdown.parser.TextContentParser;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.theme.GuideCols;
import com.neep.neepmeat.guide.article.Article;
import com.neep.neepmeat.guide.larkdown.ColumnsLarkdownContent;
import com.neep.neepmeat.guide.larkdown.GraphLarkdownContent;
import com.neep.neepmeat.guide.larkdown.GuideLarkdownVisitor;
import com.neep.neepmeat.guide.larkdown.ItemRenderLarkdownContent;
import com.neep.meatlib.resource.FabricResources.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GuideReloadListener implements SimpleSynchronousResourceReloadListener
{
    private static final GuideReloadListener INSTANCE = new GuideReloadListener();
    private final Map<String, Article> articles = new HashMap<>();
    private final Map<String, GuideNode.ArticleNode> articleNodes = new HashMap<>();
    private final Map<String, GuideNode.MenuNode> menuNodes = new HashMap<>();
    private final Set<String> lookupItems = new HashSet<>();
    private @Nullable GuideNode root;

    private final Deque<GuideNode> persistentPath = new LinkedList<>();
    @Nullable private Article persistentArticle;

    private final List<LarkdownParser.ContentParser> larkdownParsers;

    public GuideReloadListener()
    {
        LarkdownCommands.Mutable commands = new LarkdownCommands.Mutable();
        commands.register("graph", (options, arguments, acceptor, parser) -> GraphLarkdownContent.parser(options, arguments, acceptor));
        commands.register("columns", ColumnsLarkdownContent::parser);
        commands.register("item_render", ItemRenderLarkdownContent::parser);

        larkdownParsers = List.of(
                new LarkdownParser.HeadingContentParser(),
                new LarkdownParser.BulletContentParser(),
                new LarkdownParser.CodeContentParser(),
                new LineCommandParser(commands),
                new TextContentParser(Alignment.LEFT)
        );
    }

    public static GuideReloadListener getInstance()
    {
        return INSTANCE;
    }

    @Override
    public ResourceLocation getFabricId()
    {
        return ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "guide");
    }

    public boolean isValid()
    {
        return getRootNode() != null;
    }

    private Predicate<ResourceLocation> pathPredicate(String language)
    {
        return id ->
        {
            String path = id.getPath();
            if (path.endsWith(language + ".json"))
            {
                return true;
            }
            else if (path.endsWith(".md"))
            {
                return path.contains(language + "/");
            }

            return false;
        };
    }

    @Override
    public void reload(ResourceManager manager)
    {
        root = null;
        articleNodes.clear();
        menuNodes.clear();
        articles.clear();
        persistentPath.clear();
        persistentArticle = null;
        lookupItems.clear();

        String language = Minecraft.getInstance().getLanguageManager().getSelected();
        String defaultLanguage = "en_us";

        Map<ResourceLocation, Resource> foundIds = manager.listResources("guide", pathPredicate(language));
        if (foundIds.isEmpty())
        {
            // Fall back to en_us for 'Murica!
            foundIds = manager.listResources("guide", pathPredicate(defaultLanguage));
        }

        // Slightly janky
        var partitioned = foundIds.entrySet().stream().collect(Collectors.partitioningBy(entry -> !entry.getKey().getPath().endsWith("json")));
        List<Map.Entry<ResourceLocation, Resource>> jsonFiles = partitioned.get(false);
        List<Map.Entry<ResourceLocation, Resource>> markdownFiles = partitioned.get(true);

        for (var entry : jsonFiles)
        {
            try (InputStream stream = entry.getValue().open())
            {
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                JsonElement rootElement = JsonParser.parseReader(reader);

                processArticles((JsonObject) rootElement);

                // Get the index object. It could feasibly be anywhere.
                if (GsonHelper.isObjectNode((JsonObject) rootElement, "index"))
                {
                    if (root != null)
                    {
                        throw new JsonParseException("Multiple index elements found");
                    }
                    root = processNode(GsonHelper.getAsJsonObject((JsonObject) rootElement, "index"));
                }
            }
            catch (Exception e)
            {
                NeepMeat.LOGGER.error("Error occurred while loading guide file {}", entry.getKey().toString(), e);
            }
        }

        for (var entry : markdownFiles)
        {
            try (InputStream stream = entry.getValue().open())
            {
                // Each md file should contain one article

                LarkdownParser parser = new LarkdownParser(new InputStreamReader(stream, StandardCharsets.UTF_8), larkdownParsers);

                List<LarkdownContent> contents = parser.getContents();
                Map<String, String> preamble = parser.getPreamble();

                String id = preamble.get("id");
                if (id == null)
                    throw new LarkdownParseException("No ID specified in preamble");

                var articleNode = articleNodes.get(id);
                if (articleNode != null && preamble.containsKey("lookup"))
                {
                    for (var term : preamble.get("lookup").split(","))
                    {
                        term = term.strip();
                        articleNode.addLookupTerm(term);
                        lookupItems.add(term);
                    }
                }

                Article article = new GuideLarkdownVisitor(contents).makeArticle();
                articles.put(id, article);

            }
            catch (IOException | LarkdownParseException e)
            {
                NeepMeat.LOGGER.error("Error occurred while loading guide article {}", entry.getKey().toString(), e);
            }
        }

        if (root == null)
        {
            NeepMeat.LOGGER.error("Failed to load guide due to invalid JSON or missing index element");
        }

        // Fire the event
        GuideReloadEvent.POST.invoker().apply(this);
    }

    protected void processArticles(JsonObject root)
    {
        JsonArray array = GsonHelper.getAsJsonArray(root, "articles", new JsonArray());
        array.forEach(e ->
        {
            String id = GsonHelper.getAsString((JsonObject) e, "id");
            Article article = Article.Simple.fromJson((JsonObject) e);
            articles.put(id, article);
        });
    }

    protected GuideNode processNode(JsonObject object)
    {
        String id = GsonHelper.getAsString(object, "id");
        String icon = GsonHelper.getAsString(object, "icon");
        String text = GsonHelper.getAsString(object, "text");

        if (GsonHelper.isArrayNode(object, "entries") || GsonHelper.getAsString(object, "type").equals("menu"))
        {
            List<GuideNode> nodes = new ArrayList<>();

            // Allow empty menus as placeholders (THORD)
            if (GsonHelper.isArrayNode(object, "entries"))
            {
                // Using recursion
                GsonHelper.getAsJsonArray(object, "entries").forEach(o -> nodes.add(processNode((JsonObject) o)));
            }

            var menu = new GuideNode.MenuNode(id, ResourceLocation.fromNamespaceAndPath(icon), Component.nullToEmpty(text).copy().setStyle(Style.EMPTY.withFont(GuideCols.FONT)), nodes);
            menuNodes.put(id, menu);
            return menu;
        }

        Set<String> lookup = new HashSet<>();
        if (GsonHelper.isArrayNode(object, "lookup"))
        {
            for (var term : GsonHelper.getAsJsonArray(object, "lookup"))
            {
                lookup.add(term.getAsString());
            }
        }
        else if (GsonHelper.isStringValue(object, "lookup"))
        {
            String l = GsonHelper.getAsString(object, "lookup");
            lookup.add(l);
            lookupItems.add(l);
        }

        GuideNode.ArticleNode node = new GuideNode.ArticleNode(id, lookup, ResourceLocation.fromNamespaceAndPath(icon), Component.nullToEmpty(text).copy().setStyle(Style.EMPTY.withFont(GuideCols.FONT)));
        articleNodes.put(id, node);
        return node;
    }

    @Nullable
    public GuideNode getRootNode()
    {
        return root;
    }

    public Article getArticle(String id)
    {
        return articles.get(id);
    }

    /// Quickly checks whether this item is a valid lookup term.
    public boolean isValidLookup(ItemStack stack)
    {
        return stack.getItem()
                .builtInRegistryHolder()
                .unwrapKey()
                .map(e -> lookupItems.contains(e.location().toString()))
                .orElse(false);
    }

    @Nullable
    public List<GuideNode> getPath(ItemStack stack)
    {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        GuideNode target = articleNodes.values().stream().filter(n -> n.matchesLookupTerm(itemId.toString())).findFirst().orElse(null);
        if (target != null)
        {
            List<GuideNode> route = new ArrayList<>();
            recursiveDfs(root, target, route);

            // If no route is found, we will go directly to the target.
            route.add(target);

            return route;
        }
        return null;
    }

    @Nullable
    public List<GuideNode> getPath(String article)
    {
        GuideNode target = articleNodes.values().stream().filter(n -> n.getId().equals(article)).findFirst().orElse(null);

        if (target != null)
        {
            List<GuideNode> route = new ArrayList<>();
            recursiveDfs(root, target, route);

            // If no route is found, we will go directly to the target.
            route.add(target);

            return route;
        }
        return null;
    }

    private boolean recursiveDfs(GuideNode node, GuideNode target, List<GuideNode> route)
    {
        route.add(route.size(), node);
        for (var child : node.getChildren())
        {
            if (child == target)
                return true;

            boolean foundSub = recursiveDfs(child, target, route);
            if (foundSub)
                return true;
        }
        route.remove(route.size() - 1);
        return false;
    }

    public Collection<Article> getArticles()
    {
        return articles.values();
    }

    public Collection<GuideNode.ArticleNode> getArticleNodes()
    {
        return articleNodes.values();
    }

    public Deque<GuideNode> getPersistentPath()
    {
        return persistentPath;
    }

    public @Nullable Article getPersistentArticle()
    {
        return persistentArticle;
    }

    public void setPersistentArticle(@Nullable Article article)
    {
        this.persistentArticle = article;
    }

    public void insert(String name, Article article)
    {
        articles.put(name, article);
    }

    @Nullable
    public GuideNode.MenuNode getMenu(String name)
    {
        return menuNodes.get(name);
    }
}
