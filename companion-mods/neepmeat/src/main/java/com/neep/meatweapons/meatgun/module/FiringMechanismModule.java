package com.neep.meatweapons.meatgun.module;

import com.neep.meatweapons.meatgun.RootModuleHolder;
import java.util.List;

public abstract class FiringMechanismModule extends AbstractMeatgunModule
{
    public FiringMechanismModule(RootModuleHolder.Listener listener)
    {
        super(listener, List.of());
    }

    @Override
    public abstract  Type<? extends MeatgunModule> getType();

    public abstract float getOverclock();
}
