package com.neep.meatweapons.config;

import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedName;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;

public class MWConfig extends ReflectiveConfig
{
    @SerializedName("meatgun")
    public final Meatgun meatgun = new Meatgun();
    public static final class Meatgun extends folk.sisby.kaleido.lib.quiltconfig.api.ReflectiveConfig.Section
    {
        public final TrackedValue<Float> test = value(0f);

        @Comment("Drill Chassis max complexity is found in neepmeat.toml")
        public final TrackedValue<Integer> rawGunMaxComplexity = value(8);
        public final TrackedValue<Integer> pistolBaseMaxComplexity = value(16);
        public final TrackedValue<Integer> staffBaseMaxComplexity = value(16);
        public final TrackedValue<Integer> longGunBaseMaxComplexity = value(20);

        public final TrackedValue<Float> pistolAttackDamage = value(4f);
        @Comment("Multiplied by 5 internally")
        public final TrackedValue<Integer> pistolCapacity = value(16);


        public final TrackedValue<Float> chuggerAttackDamage = value(7f);
        @Comment("Multiplied by 5 internally")
        public final TrackedValue<Integer> chuggerCapacity = value(16);


        public final TrackedValue<Float> bosherAttackDamage = value(4f);
        @Comment("Multiplied by 5 internally")
        public final TrackedValue<Integer> bosherCapacity = value(16);


        public final TrackedValue<Float> longBoiBaseDamage = value(15f);
        @Comment("Multiplied by 5 internally")
        public final TrackedValue<Integer> longBoiCapacity = value(16);


        @Comment("Multiplied by 5 internally")
        public final TrackedValue<Integer> grenadeLauncherCapacity = value(16);

        public final TrackedValue<Float> bloodThrowerDamage = value(2f);
        @Comment("Multiplied by 5 internally")
        public final TrackedValue<Integer> bloodThrowerCapacity = value(48);


        public final TrackedValue<Float> halberdDamage = value(8f);


        public final TrackedValue<Float> shockStaffDamage = value(8f);
        @Comment("Multiplied by 5 internally")
        public final TrackedValue<Integer> shockStaffCapacity = value(16);


        @Comment("Multiplied by 5 internally")
        public final TrackedValue<Integer> batteryCapacity = value(24);
    }

    @Comment("Multiplied by 5 internally")
    @SerializedName("ammunition")
    public final Ammunition ammunition = new Ammunition();

    public static final class Ammunition extends folk.sisby.kaleido.lib.quiltconfig.api.ReflectiveConfig.Section
    {
        public final TrackedValue<Integer> smallBallisticAmount = value(12);
        public final TrackedValue<Integer> mediumBallisticAmount = value(24);
        public final TrackedValue<Integer> largeBallisticAmount = value(48);
        public final TrackedValue<Integer> smallEnergyAmount = value(12);
        public final TrackedValue<Integer> mediumEnergyAmount = value(24);
    }
}
