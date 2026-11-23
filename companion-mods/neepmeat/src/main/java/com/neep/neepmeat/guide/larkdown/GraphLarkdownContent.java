package com.neep.neepmeat.guide.larkdown;

import com.neep.meatlib.larkdown.LarkdownContent;
import com.neep.meatlib.larkdown.LarkdownParseException;
import com.neep.meatlib.larkdown.LarkdownParser;
import com.neep.meatlib.larkdown.command.CommandArguments;
import com.neep.meatlib.larkdown.command.CommandOptions;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

public record GraphLarkdownContent(ResourceLocation id) implements LarkdownContent
{
    public static void parser(CommandOptions options, CommandArguments arguments, LarkdownParser.ContentAcceptor acceptor) throws LarkdownParseException
    {
        try
        {
            ResourceLocation id = ResourceLocation.tryParse(arguments.get(0));
            acceptor.accept(new GraphLarkdownContent(id));
        }
        catch (ResourceLocationException e)
        {
            throw new LarkdownParseException(e.getMessage());
        }
    }
}
