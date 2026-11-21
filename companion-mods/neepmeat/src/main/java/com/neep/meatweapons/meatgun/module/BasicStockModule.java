package com.neep.meatweapons.meatgun.module;

import com.neep.meatweapons.meatgun.RootModuleHolder;
import java.util.List;
import net.minecraft.nbt.CompoundTag;

public class BasicStockModule extends AbstractMeatgunModule
{
    public BasicStockModule(RootModuleHolder.Listener listener)
    {
        super(listener, List.of());
    }

    public BasicStockModule(RootModuleHolder.Listener listener, CompoundTag nbt)
    {
        this(listener);
    }

    @Override
    public Type<? extends MeatgunModule> getType()
    {
        return MeatgunModules.BASIC_STOCK;
    }
}
