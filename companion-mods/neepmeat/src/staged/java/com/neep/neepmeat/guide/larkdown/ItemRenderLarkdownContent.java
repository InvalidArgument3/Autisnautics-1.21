package com.neep.neepmeat.guide.larkdown;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.larkdown.LarkdownContent;
import com.neep.meatlib.larkdown.LarkdownParseException;
import com.neep.meatlib.larkdown.LarkdownParser;
import com.neep.meatlib.larkdown.command.CommandArguments;
import com.neep.meatlib.larkdown.command.CommandOptions;
import java.io.IOException;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record ItemRenderLarkdownContent(ItemStack stack, int height) implements LarkdownContent
{
    public static void parser(CommandOptions options, CommandArguments arguments, LarkdownParser.ContentAcceptor acceptor, LarkdownParser parser) throws LarkdownParseException, IOException
    {
        int defaultHeight = 80;
        try
        {
            var id = ResourceLocation.tryParse(arguments.get(0));
            int height = options.getInt("height", defaultHeight);
            acceptor.accept(new ItemRenderLarkdownContent(BuiltInRegistries.ITEM.get(id).getDefaultInstance(), height));
        }
        catch (ResourceLocationException e )
        {
            acceptor.accept(new ItemRenderLarkdownContent(ItemStack.EMPTY, defaultHeight));
            MeatLib.LOGGER.error("Loading item render larkdown component: {}", e.getMessage());
        }
    }
}
