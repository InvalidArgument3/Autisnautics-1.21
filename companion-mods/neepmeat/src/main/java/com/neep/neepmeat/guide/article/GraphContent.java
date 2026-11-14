package com.neep.neepmeat.guide.article;

import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.neepmeat.client.screen.tablet.ArticleTextWidget;
import com.neep.neepmeat.client.screen.tablet.graph.Graph;
import com.neep.neepmeat.client.screen.tablet.graph.GraphData;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class GraphContent implements Article.Content
{
    private final Supplier<GraphData> graphData;
//    private final Graph graph;

    public GraphContent(Supplier<GraphData> graphData)
    {
        this.graphData = graphData;
//        this.graph = new Graph(graphData);
    }

    @Override
    public int render(GuiGraphics matrices, float x, float y, float width, double scroll, ArticleTextWidget parent, float mx, float my)
    {
        int h = 100;
        Rectangle bounds = new Rectangle.ImmutableRect((int) x, (int) y, (int) (width - 3), h);
        var graph = new Graph(graphData.get()); // TODO: debug only

        var client = Minecraft.getInstance();
        float mxf = (float) (client.mouseHandler.xpos() * (double)client.getWindow().getGuiScaledWidth() / (double)client.getWindow().getScreenWidth());
        float myf = (float) (client.mouseHandler.ypos() * (double)client.getWindow().getGuiScaledHeight() / (double)client.getWindow().getScreenHeight());

        graph.render(matrices, bounds, mxf, myf);
        return h;
    }

    @Override
    public int height(float width, ArticleTextWidget parent)
    {
        return 0;
    }

    @Override
    public void search(String term, IntConsumer result, float width, ArticleTextWidget parent)
    {

    }
}
