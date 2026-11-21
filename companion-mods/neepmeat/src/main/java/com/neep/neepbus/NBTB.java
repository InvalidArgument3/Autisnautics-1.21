package com.neep.neepbus;

import net.minecraft.network.chat.Component;

// NEEPBus Tooltip Builder
public class NBTB
{
    private final StringBuilder sb = new StringBuilder();

    // Write
    public NBTB w(String text)
    {
        sb.append("On data write:");
        sb.append("\n  ");
        sb.append(text);
        return this;
    }

    // Read
    public NBTB r(String text)
    {
        sb.append("On data read:");
        sb.append("\n  ");
        sb.append(text);
        return this;
    }

    public NBTB other(String on, String text)
    {
        sb.append(on);
        sb.append("\n  ");
        sb.append(text);
        return this;
    }

    public Component build()
    {
        return Component.literal(sb.toString());
    }
}
