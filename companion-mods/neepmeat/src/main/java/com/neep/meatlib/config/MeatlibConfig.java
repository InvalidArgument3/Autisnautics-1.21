package com.neep.meatlib.config;

import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;

public class MeatlibConfig extends ReflectiveConfig
{
    @Comment("Applies to all config files that have an associated '.default' file")
    @Comment("When true: All config fields left at their default value will be AUTOMATICALLY UPDATED if the default value changes due to a mod update")
    @Comment("Modpack developers should probably disable this. Regular users should probably keep it enabled.")
    public final TrackedValue<Boolean> updateDefaultValues = value(true);

    @Comment("Applies to Flywheel visuals")
    @Comment("When true: Dummy block entity renderers will be registered in addition to Flywheel visuals.")
    @Comment("This can fix issues where visuals become misplaced or are missing")
    public final TrackedValue<Boolean> registerDummyBERs = value(true);

    @Comment("Applies to Flywheel visuals with dummy block entity renderers")
    @Comment("When true: All block entities with Flywheel visuals (that are aware of this config) will not skip the vanilla block entity render.")
    @Comment("Probably does nothing except make the game run a tiny bit slower.")
    public final TrackedValue<Boolean> dontSkipVanillaRender = value(false);
}
