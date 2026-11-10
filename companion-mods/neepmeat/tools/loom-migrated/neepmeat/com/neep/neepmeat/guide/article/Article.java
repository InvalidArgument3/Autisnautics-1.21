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

package com.neep.neepmeat.guide.article;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.neep.neepmeat.client.screen.tablet.ArticleTextWidget;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public interface Article
{
    Map<String, Function<JsonObject, Content>> DESERIALISERS = new HashMap<>();

    Function<JsonObject, Content> TEXT = DESERIALISERS.put("text",
    object ->
    {
        JsonElement textObject = object.get("text");
        if (textObject.isJsonArray())
        {
            MutableComponent text = MutableComponent.create(net.minecraft.network.chat.ComponentContents.EMPTY);
            textObject.getAsJsonArray().forEach(element ->
            {
                text.append(Component.Serializer.fromJson(element));
            });
            return new TextContent(text);
        }
        MutableComponent text = Component.Serializer.fromJson(object);
//        text.setStyle(text.getStyle().withFont(FONT_ID));
        return new TextContent(text);
    });

    Function<JsonObject, Content> CODE = DESERIALISERS.put("code",
            object ->
            {
                JsonElement textObject = object.get("text");
                if (textObject.isJsonArray())
                {
                    MutableComponent text = MutableComponent.create(net.minecraft.network.chat.ComponentContents.EMPTY);
                    textObject.getAsJsonArray().forEach(element ->
                    {
                        text.append(Component.Serializer.fromJson(element));
                    });
                    return new CodeContent(text);
                }
                MutableComponent text = Component.Serializer.fromJson(object);
                return new CodeContent(text);
            });

    Function<JsonObject, Content> CTEXT = DESERIALISERS.put("ctext",
            object ->
            {
                MutableComponent text = Component.Serializer.fromJson(object);
                return new CenteredTextContent(text);
            });
    Function<JsonObject, Content> IMAGE = DESERIALISERS.put("image",
            object ->
            {
                int width = GsonHelper.getAsInt(object, "width");
                int height = GsonHelper.getAsInt(object, "height");
                float scale = 0.5f;
                if (GsonHelper.isNumberValue(object, "scale"))
                    scale = GsonHelper.getAsFloat(object, "scale");
                ResourceLocation image = new ResourceLocation(GsonHelper.getAsString(object, "path"));
                return new ImageContent(width, height, scale, image);
            });

    Article EMPTY = new Simple();

    Iterable<? extends Content> contents();

    default IntList search(String term, float width, ArticleTextWidget parent)
    {
        IntList result = new IntArrayList();

        AtomicInteger h = new AtomicInteger(0);
        IntConsumer append = i -> result.add(h.get() + i);
        for (var content : contents())
        {
            content.search(term, append, width, parent);
            h.addAndGet(content.height(width, parent));
        }

        return result;
    }

    // In need of some structural changes.
    // These are meant to exist independent of the GUI context, but can also be rendered.
    // This was fine when the render method returned the content height, but it's getting inefficient
    // with the added search and height functionality.
    interface Content
    {
        /**
         * @return Height of the drawn content
         */
        int render(GuiGraphics matrices, float x, float y, float width, double scroll, ArticleTextWidget parent, float mx, float my);

        int height(float width, ArticleTextWidget parent);

        void search(String term, IntConsumer result, float width, ArticleTextWidget parent);

        default boolean onClick(float x, float y, float width, double scroll, ArticleTextWidget parent, float mx, float my) { return false; }

        interface SolidW
        {
            float desiredWidth(float maxWidth);
        }
    }

    record Simple(List<Content> contents) implements Article
    {
        public Simple()
        {
            this(new ObjectArrayList<>());
        }

        public static Article fromJson(JsonObject object)
        {
            String id = GsonHelper.getAsString(object, "id");
            Simple article = new Simple();
            GsonHelper.getAsJsonArray(object, "contents", new JsonArray()).forEach(
                    element ->
                    {
                        JsonObject content = (JsonObject) element;

                        // Apply the correct deserialiser and add the result to contents
                        String contentType = GsonHelper.getAsString(content, "type");
                        article.contents.add(DESERIALISERS.getOrDefault(
                                        contentType,
                                        o ->
                                        {
                                            throw new JsonParseException("Unknown article content type '" + contentType + "'");
                                        })
                                .apply(content));
                    });
            return article;
        }
    }
}
