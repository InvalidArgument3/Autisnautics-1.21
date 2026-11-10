package com.neep.neepmeat.api.theme;

import com.neep.neepmeat.NeepMeat;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public class GuideCols
{
    public static final ResourceLocation FONT = new ResourceLocation(NeepMeat.NAMESPACE, "guide");


    public static Style style() { return Style.EMPTY.withFont(FONT); }

    public static int textCol() { return 0xFF00BB00; }
    public static int darkCol() { return 0xFF008800; }
    public static int borderCol() { return darkCol(); }
    public static int backgroundCol() { return 0xcc000000; }
    public static int codeCol() { return 0xFF00BE50; }
}
