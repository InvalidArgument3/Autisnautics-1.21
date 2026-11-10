package com.neep.neepmeat.client.renderer.entity;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.model.entity.GeneralBipedEntityModel;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class GeneralBipedEntityRenderer<T extends Mob, M extends GeneralBipedEntityModel<T>> extends HumanoidMobRenderer<T, M>
{
    public static final ModelLayerLocation MAIN = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "general_biped"), "main");

    private final ResourceLocation texture;

    public GeneralBipedEntityRenderer(EntityRendererProvider.Context ctx, Function<ModelPart, M> modelFactory, ResourceLocation texture)
    {
        this(ctx, MAIN, ModelLayers.ZOMBIE_INNER_ARMOR, ModelLayers.ZOMBIE_OUTER_ARMOR, modelFactory, texture);
    }

    public GeneralBipedEntityRenderer(EntityRendererProvider.Context ctx, ModelLayerLocation layer, ModelLayerLocation legsArmorLayer, ModelLayerLocation bodyArmorLayer, Function<ModelPart, M> modelFactory, ResourceLocation texture)
    {
        super(ctx, modelFactory.apply(ctx.bakeLayer(layer)), 0.5f);
//        super(ctx, new KeeperEntityModel(ctx.getPart(layer)), new KeeperEntityModel(ctx.getPart(legsArmorLayer)), new KeeperEntityModel(ctx.getPart(bodyArmorLayer)));
//        this.addFeature(new ArmorFeatureRenderer<>(this, new KeeperEntityModel(ctx.getPart(legsArmorLayer)), new KeeperEntityModel(ctx.getPart(bodyArmorLayer))));
        this.texture = texture;
    }

    public ResourceLocation getTexture(T entity)
    {
        return texture;
    }

    public static <T extends Mob> EntityRendererProvider<T> create(ResourceLocation texture)
    {
        return ctx -> new GeneralBipedEntityRenderer<>(ctx, GeneralBipedEntityModel::new, texture);
    }
}
