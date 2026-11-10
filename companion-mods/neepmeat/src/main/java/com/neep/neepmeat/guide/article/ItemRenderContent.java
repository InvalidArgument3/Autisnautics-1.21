package com.neep.neepmeat.guide.article;

import com.mojang.blaze3d.platform.Lighting;
import com.neep.meatlib.client.MeatLibClient;
import com.neep.meatlib.client.screen.GUIUtil;
import com.neep.meatlib.client.screen.primitive.Rectangle;
import com.neep.neepmeat.client.screen.tablet.ArticleTextWidget;
import com.neep.neepmeat.guide.larkdown.ItemRenderLarkdownContent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.TooltipFlag;

public record ItemRenderContent(ItemRenderLarkdownContent content) implements Article.Content, Article.Content.SolidW
{
    @Override
    public int render(GuiGraphics ctx, float x, float y, float width, double scroll, ArticleTextWidget parent, float mx, float my)
    {
        var renderer = parent.getItemRenderer();

        int h = content.height();

        var matrices = ctx.pose();
        matrices.pushPose();
//        matrices.translate(8, 8, 0);

        float scale = h * 0.9f;
        matrices.scale(scale, scale, scale);
        float cx = x + width / 2;
        float cy = y + h / 2f;
        matrices.translate(cx / scale, cy / scale, 0.5);

        var model = renderer.getItemModelShaper().getItemModel(content.stack());
        Lighting.setupForFlatItems();
        matrices.mulPoseMatrix(new Matrix4f().scaling(1, -1, 1));
        renderer.render(content.stack(),
                ItemDisplayContext.GUI,
                false,
                ctx.pose(),
                ctx.bufferSource(),
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                model);
        ctx.flush();

        matrices.popPose();

        int bw = h;
        Rectangle rect = new Rectangle.ImmutableRect((int) (x + (width - bw) / 2), (int) y, h, h);
        if (rect.isWithin(mx, my))
        {
            GUIUtil.renderBorderInner(ctx, (int) (x + (width - bw) / 2), (int) y, h, h, parent.getCodeCol(), 0);
            int col = parent.getCol();
//            List<Text> tooltip = content.stack().getTooltip(parent.getPlayer(), TooltipContext.BASIC)
//                    .stream()
//                    .map(t -> t.copy().fillStyle(t.getStyle().withColor(col))) // Formatting codes override style
//                    .collect(Collectors.toList());
            List<Component> tooltip = new ArrayList<>(content.stack().getTooltipLines(parent.getPlayer(), TooltipFlag.NORMAL));

            if (MeatLibClient.EMI_UTIL != null)
                tooltip.add(Component.literal("Click to view recipe").setStyle(Style.EMPTY.withColor(parent.getDarkCol())));
            else
                tooltip.add(Component.literal("Install EMI to view recipe").setStyle(Style.EMPTY.withColor(parent.getDarkCol())));

            // Heuristic for whether the tooltip will spread to the left or right due to screen borders
            // Adjust x position so that the tooltip will not cover the item
            float tootX = x + width / 2 < parent.x0() + parent.regionWidth() / 2 ? rect.x1() : rect.x0();

            parent.setNextTooltip(tooltip, tootX, rect.y0(), parent.getCodeCol(), false);
        }
        else
        {
            GUIUtil.renderBorderInner(ctx, (int) (x + (width - bw) / 2), (int) y, h, h, parent.getCol(), 0);
        }


        return h;
    }

    @Override
    public boolean onClick(float x, float y, float width, double scroll, ArticleTextWidget parent, float mx, float my)
    {
        var h = content.height();
        Rectangle rect = new Rectangle.ImmutableRect((int) (x + (width - h) / 2), (int) y, h, h);

        if (rect.isWithin(mx, my))
        {
            if (MeatLibClient.EMI_UTIL != null)
            {
                MeatLibClient.EMI_UTIL.openRecipe(content.stack());
            }
            return true;
        }

        return false;
    }

    @Override
    public int height(float width, ArticleTextWidget parent)
    {
        return content.height();
    }

    @Override
    public float desiredWidth(float maxWidth)
    {
        return content.height() + 5;
    }

    @Override
    public void search(String term, IntConsumer result, float width, ArticleTextWidget parent)
    {

    }
}
