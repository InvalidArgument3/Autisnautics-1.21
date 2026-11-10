package com.neep.meatlib.client.flw;

import com.neep.meatlib.MeatLib;
import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.LightShader;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.LightShaders;
import dev.engine_room.flywheel.lib.material.SimpleLightShader;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import net.minecraft.resources.ResourceLocation;

public class MeatModels
{
    public static final LightShader FLAT_DARKENED = new SimpleLightShader(new ResourceLocation(MeatLib.NAMESPACE, "light/flat_old.glsl"));

    public static final Material SOLID = SimpleMaterial.builder().mipmap(false).light(LightShaders.SMOOTH_WHEN_EMBEDDED).build();
    public static final Material CUTOUT = SimpleMaterial.builder().mipmap(false).light(LightShaders.SMOOTH_WHEN_EMBEDDED).cutout(CutoutShaders.ONE_TENTH).build();

    // The light shader darkens the incredibly bright models
    public static final Material SOLID_UNSHADED = SimpleMaterial.builder().mipmap(false).cardinalLightingMode(CardinalLightingMode.OFF).light(FLAT_DARKENED).build();
    public static final Material CUTOUT_UNSHADED = SimpleMaterial.builder().mipmap(false).cardinalLightingMode(CardinalLightingMode.OFF).light(FLAT_DARKENED).cutout(CutoutShaders.ONE_TENTH).build();

    public static final RendererReloadCache<MaterialOverride, Model> PARTIAL = new RendererReloadCache<>(it ->
            BakedModelBuilder.create(it.partialModel().get())
                    .materialFunc((l, b) -> it.material())
                    .build());

    public static Model withMaterial(PartialModel partialModel, Material override)
    {
        return PARTIAL.get(new MaterialOverride(partialModel, override));
    }

    public static Model cutout(PartialModel partialModel)
    {
        return cutout(partialModel, false);
    }

    public static Model cutout(PartialModel partialModel, boolean shaded)
    {
        return withMaterial(partialModel, shaded ? CUTOUT : CUTOUT_UNSHADED);
    }

    public static Model solid(PartialModel partialModel)
    {
        return solid(partialModel, false);
    }

    public static Model solid(PartialModel partialModel, boolean shaded)
    {
        return withMaterial(partialModel, shaded ? SOLID : SOLID_UNSHADED);
    }

    public record MaterialOverride(PartialModel partialModel, Material material) { }
}
