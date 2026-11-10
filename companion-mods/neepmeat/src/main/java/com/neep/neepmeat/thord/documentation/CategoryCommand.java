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

import com.neep.meatlib.larkdown.LarkdownParseException;
import com.neep.meatlib.larkdown.LarkdownParser;
import com.neep.meatlib.larkdown.command.CommandArguments;
import com.neep.meatlib.larkdown.command.CommandOptions;
import com.neep.meatlib.larkdown.command.LarkdownCommand;

import java.util.Locale;

public class CategoryCommand implements LarkdownCommand
{
    @Override
    public void apply(CommandOptions options, CommandArguments arguments, LarkdownParser.ContentAcceptor acceptor, LarkdownParser parser) throws LarkdownParseException
    {
        String id = arguments.get(0).strip().toLowerCase(Locale.ROOT);

        acceptor.accept(new CategoryLarkdownContent(id));
//        acceptor.accept(new HeadingLarkdownContent(name, 1));
    }
}
