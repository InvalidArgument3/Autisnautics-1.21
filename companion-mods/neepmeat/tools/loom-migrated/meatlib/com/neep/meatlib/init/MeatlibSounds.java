package com.neep.meatlib.init;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.registry.SoundRegistry;
import net.minecraft.sounds.SoundEvent;

public class MeatlibSounds
{
    public static SoundEvent PLC_SELECT = SoundRegistry.registerSound(MeatLib.NAMESPACE, "plc_select");
    public static SoundEvent UI_BEEP = SoundRegistry.registerSound(MeatLib.NAMESPACE, "ui_beep");
    public static SoundEvent WRENCH_CLICK = SoundRegistry.registerSound(MeatLib.NAMESPACE, "wrench_click");
    public static SoundEvent CLICK = SoundRegistry.registerSound(MeatLib.NAMESPACE, "click");

    public static void init()
    {
        SoundRegistry.flush();
    }
}
