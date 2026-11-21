package com.neep.meatweapons.item.meatgun;

import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.client.meatgun.animation.MeatgunAnimation;
import com.neep.meatweapons.client.meatgun.animation.PistolReloadMeatgunAnimation;
import com.neep.meatweapons.meatgun.RootModuleCache;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.meatweapons.meatgun.module.BaseLongGunModule;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

public class LongGunItem extends MeatgunPistolItem
{
    private final RootModuleCache cache = new RootModuleCache();

    public LongGunItem(TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(tooltipSupplier, settings);
    }

    @Override
    public MeatgunModule createBase(RootModuleHolder.Listener listener)
    {
        return new BaseLongGunModule(listener);
    }


    @Override
    public Supplier<Object> createAnimationManager()
    {
        return new Supplier<Object>()
        {
            @Override
            public Object get()
            {
                return new MeatgunAnimationManager(MeatgunAnimation.EMPTY)
                        .add("reload", new PistolReloadMeatgunAnimation());
            }
        };
    }

    @Override
    public float getWorldHeightOffset()
    {
        return -2 / 16f;
    }

    @Override
    public int getMaxComplexity(ItemStack stack)
    {
        return MeatWeapons.CONFIG.meatgun.longGunBaseMaxComplexity.getRealValue();
    }

    @Override
    public RootModuleCache getCache()
    {
        return cache;
    }
}
