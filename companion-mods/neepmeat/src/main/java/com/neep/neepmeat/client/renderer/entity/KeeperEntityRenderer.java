/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.client.renderer.entity;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.model.entity.KeeperEntityModel;
import com.neep.neepmeat.entity.keeper.KeeperEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

@OnlyIn(Dist.CLIENT)
public class KeeperEntityRenderer extends HumanoidMobRenderer<KeeperEntity, KeeperEntityModel>
{
    protected static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NeepMeat.NAMESPACE, "textures/entity/keeper/keeper.png");

//    public static final EntityModelLayer KEEPER = new EntityModelLayer(new Identifier(NeepMeat.NAMESPACE, "keeper"), "main");
//    public static final EntityModelLayer KEEPER_INNER = new EntityModelLayer(new Identifier(NeepMeat.NAMESPACE, "keeper"), "inner_armor");
//    public static final EntityModelLayer KEEPER_OUTER = new EntityModelLayer(new Identifier(NeepMeat.NAMESPACE, "keeper"), "outer_armor");

    public KeeperEntityRenderer(EntityRendererProvider.Context context)
    {
        this(context, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_INNER_ARMOR, ModelLayers.ZOMBIE_OUTER_ARMOR);
    }

    public KeeperEntityRenderer(EntityRendererProvider.Context ctx, ModelLayerLocation layer, ModelLayerLocation legsArmorLayer, ModelLayerLocation bodyArmorLayer)
    {
        super(ctx, new KeeperEntityModel(ctx.bakeLayer(layer)), 0.5f);
//        super(ctx, new KeeperEntityModel(ctx.getPart(layer)), new KeeperEntityModel(ctx.getPart(legsArmorLayer)), new KeeperEntityModel(ctx.getPart(bodyArmorLayer)));
//        this.addFeature(new ArmorFeatureRenderer<>(this, new KeeperEntityModel(ctx.getPart(legsArmorLayer)), new KeeperEntityModel(ctx.getPart(bodyArmorLayer))));
    }

    public ResourceLocation getTexture(KeeperEntity mobEntity)
    {
        return TEXTURE;
    }
}

