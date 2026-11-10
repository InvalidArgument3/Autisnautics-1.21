package com.neep.neepmeat.guide.larkdown;

import com.neep.meatlib.larkdown.LarkdownContent;
import com.neep.meatlib.larkdown.LarkdownParseException;
import com.neep.meatlib.larkdown.LarkdownParser;
import com.neep.meatlib.larkdown.command.CommandArguments;
import com.neep.meatlib.larkdown.command.CommandOptions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public record ColumnsLarkdownContent(List<LarkdownContent> left, List<LarkdownContent> right, Fit fit) implements LarkdownContent
{
    public enum Fit
    {
        FIRST,
        SECOND,
        MIDDLE
    }

    public static void parser(CommandOptions options, CommandArguments arguments, LarkdownParser.ContentAcceptor acceptor, LarkdownParser parser) throws LarkdownParseException, IOException
    {
        var reader = new BufferedReader(new StringReader(arguments.get(0)));
        var column1 = new ObjectArrayList<LarkdownContent>();
        parser.parseSection(reader, column1::add);

        reader = new BufferedReader(new StringReader(arguments.get(1)));
        var column2 = new ObjectArrayList<LarkdownContent>();
        parser.parseSection(reader, column2::add);

        var fit = options.getString("fit", "middle");

        acceptor.accept(new ColumnsLarkdownContent(column1, column2, fit.equals("first") ? Fit.FIRST : fit.equals("second") ? Fit.SECOND : Fit.MIDDLE));
    }
}
