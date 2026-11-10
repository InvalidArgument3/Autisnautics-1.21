package com.neep.meatweapons.init;

import com.neep.meatlib.registry.SoundRegistry;
import com.neep.meatweapons.MeatWeapons;
import net.minecraft.sounds.SoundEvent;

public class MWSounds
{
    public static SoundEvent LONG_BOI_STEADY = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "long_boi_steady");
    public static SoundEvent BULLET_HIT_LIVING_CRIT = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "bullet_hit/living_crit");

    public static SoundEvent GUN_FIRE_0 = SoundRegistry.registerSound(MeatWeapons.NAMESPACE, "gun_fire/fire_0");

    public static void init()
    {
        SoundRegistry.flush();
    }
}
