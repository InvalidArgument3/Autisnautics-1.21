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

package com.neep.neepmeat.client;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.implant.*;
import com.neep.neepmeat.client.model.entity.SpiderLegsRenderer;
import com.neep.neepmeat.client.model.legs.StandardSpiderLegsRenderer;
import com.neep.neepmeat.client.renderer.PlayerLegsRenderer;
import com.neep.neepmeat.client.renderer.entity.CatEarsFeatureRenderer;
import com.neep.neepmeat.implant.entity.CatEarsImplant;
import com.neep.neepmeat.implant.entity.ExtraMouthImplant;
import com.neep.neepmeat.implant.entity.PoweredKneesImplant;
import com.neep.neepmeat.implant.entity.SpiderLegsImplant;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class NMImplantsClient
{

    public static final Map<ResourceLocation, SpiderLegsRenderer<?>> SPIDER_LEGS_MODELS = new HashMap<>();
    public static PartialModel PLAYER_ABDOMEN = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/body/small_abdomen"));
    public static PartialModel SPIDER_LEG_UPPER = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/body/player_spider_leg_0"));
    public static PartialModel SPIDER_LEG_TH_0_0 = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/body/spider_leg_th_0_0"));
    public static PartialModel SPIDER_LEG_TH_0_1 = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/body/spider_leg_th_0_1"));
    public static PartialModel SPIDER_LEG_TH_1_0 = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/body/spider_leg_th_1_0"));
    public static PartialModel SPIDER_LEG_TH_1_1 = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/body/spider_leg_th_1_1"));
    public static PartialModel SPIDER_LEG_TH_2_0 = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/body/spider_leg_th_2_0"));
    public static PartialModel SPIDER_LEG_TH_2_1 = PartialModel.of(new ResourceLocation(NeepMeat.NAMESPACE, "entity/body/player_spider_leg_0"));

    public static final SpiderLegsRenderer<?> DEFAULT_RENDERER = new StandardSpiderLegsRenderer<>(
            SPIDER_LEG_UPPER.modelLocation(),
            SPIDER_LEG_UPPER.modelLocation(),
            PLAYER_ABDOMEN.modelLocation());

    public static <E extends LivingEntity, T extends SpiderLegsRenderer<E>> T registerSpiderLegsModel(ResourceLocation id, T model)
    {
        SPIDER_LEGS_MODELS.put(id, model);
        return model;
    }

    public static void init()
    {
        PlayerLegsRenderer.init();

        registerSpiderLegsModel(SpiderLegsImplant.DEFAULT_MODEL, DEFAULT_RENDERER);

        registerSpiderLegsModel(SpiderLegsImplant.TH_0, new StandardSpiderLegsRenderer<>(
                SPIDER_LEG_TH_0_1.modelLocation(),
                SPIDER_LEG_TH_0_0.modelLocation(),
                PLAYER_ABDOMEN.modelLocation()));

        registerSpiderLegsModel(SpiderLegsImplant.TH_1, new StandardSpiderLegsRenderer<>(
                SPIDER_LEG_TH_1_1.modelLocation(),
                SPIDER_LEG_TH_1_0.modelLocation(),
                PLAYER_ABDOMEN.modelLocation()));

        registerSpiderLegsModel(SpiderLegsImplant.BLACK_BOOTS, new StandardSpiderLegsRenderer<>(
                SPIDER_LEG_TH_2_1.modelLocation(),
                SPIDER_LEG_TH_2_0.modelLocation(),
                PLAYER_ABDOMEN.modelLocation()));

//        CatEarsFeatureRenderer.TEXTURES.put(CatEarsImplant.CAT_EARS_PLAYER_SKIN, null);
        CatEarsFeatureRenderer.TEXTURES.put(CatEarsImplant.CAT_EARS_COLOUR, CatEarsFeatureRenderer.COLOUR_TEXTURE);

        ImplantConfigClient.register(SpiderLegsImplant.ID, HexapodConfigPanel::new);
        ImplantConfigClient.register(PoweredKneesImplant.ID, PoweredKneesConfigPanel::new);
        ImplantConfigClient.register(CatEarsImplant.ID, CatEarsConfigPanel::new);
        ImplantConfigClient.register(ExtraMouthImplant.ID, ExtraMouthConfigPanel::new);
    }

    public static SpiderLegsRenderer<?> getSpiderLegsRenderer(ResourceLocation id)
    {
        return SPIDER_LEGS_MODELS.getOrDefault(id, DEFAULT_RENDERER);
    }
}
