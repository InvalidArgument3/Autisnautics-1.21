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

package com.neep.meatlib.larkdown.command;

import com.neep.meatlib.larkdown.LarkdownParseException;
import com.neep.meatlib.util.StringTokenView;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CommandOptions
{
    private final Map<String, String> kvs = new HashMap<>();

    public CommandOptions()
    {

    }

    public CommandOptions(String options)
    {
        StringTokenView view = new StringTokenView(options);

        Pair<String, String> kv;
        do
        {
            kv = readKeyValue(view);

            if (kv != null)
                kvs.put(kv.key(), kv.value());
        }
        while (!view.eof() && kv != null);
    }

    @Nullable
    private static Pair<String, String> readKeyValue(StringTokenView view)
    {
        String key = view.nextIdentifier();
        if (!key.isEmpty())
        {
            if (view.nextThing() == '=')
            {
                StringBuilder sb = new StringBuilder();
                char c;
                do
                {
                    c = view.next();
                    if (c != ',')
                        sb.append(c);
                }
                while (c != ',' && !view.eof());

                return Pair.of(key, sb.toString());
            }
        }

        return null;
    }

    @Nullable
    private String getString(String key)
    {
        return kvs.get(key);
    }

    public String getString(String key, String fallback) throws LarkdownParseException
    {
        return kvs.getOrDefault(key, fallback);
    }

    public int getInt(String key, int fallback) throws LarkdownParseException
    {
        String value = getString(key);

        if (value == null)
            return fallback;

        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            throw new LarkdownParseException(e.getMessage());
        }
    }

    public float getFloat(String key, float fallback) throws LarkdownParseException
    {
        String value = getString(key);

        if (value == null)
            return fallback;

        try
        {
            return Float.parseFloat(value);
        }
        catch (NumberFormatException e)
        {
            throw new LarkdownParseException(e.getMessage());
        }
    }
}
