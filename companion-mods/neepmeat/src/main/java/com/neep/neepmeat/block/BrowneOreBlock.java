package com.neep.neepmeat.block;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;

public class BrowneOreBlock extends BaseBlock
{
    public BrowneOreBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
    }

    public boolean autoGenDrop()
    {
        return false;
    }
}
