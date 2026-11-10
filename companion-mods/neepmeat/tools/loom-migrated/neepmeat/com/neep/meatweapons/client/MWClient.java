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

package com.neep.meatweapons.client;

import com.neep.meatlib.graphics.client.GraphicsEffectClient;
import com.neep.meatweapons.MWItems;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.client.meatgun.module.ShortPhageRayModuleClient;
import com.neep.meatweapons.client.model.BulletEntityModel;
import com.neep.meatweapons.client.model.CannonBulletEntityModel;
import com.neep.meatweapons.client.model.PlasmaEntityModel;
import com.neep.meatweapons.client.network.MeatgunClient;
import com.neep.meatweapons.client.renderer.*;
import com.neep.meatweapons.client.renderer.meatgun.MeatgunModuleRenderers;
import com.neep.meatweapons.client.renderer.meatgun.MeatgunParticleManager;
import com.neep.meatweapons.client.sound.AirtruckSoundInstance;
import com.neep.meatweapons.client.sound.LongBoiSoundInstance;
import com.neep.meatweapons.client.sound.SoundInstanceCache;
import com.neep.meatweapons.component.GunTickHandler;
import com.neep.meatweapons.item.AssaultDrillItem;
import com.neep.meatweapons.meatgun.module.LongBoiModule;
import com.neep.meatweapons.particle.*;
import dev.monarkhes.myron_neepmeat.api.Myron;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

@Environment(value= EnvType.CLIENT)
public class MWClient implements ClientModInitializer
{
    public static final ModelLayerLocation MODEL_BULLET_LAYER = new ModelLayerLocation(new ResourceLocation(MeatWeapons.NAMESPACE, "bullet"), "main");
    public static final ModelLayerLocation MODEL_CANNON_BULLET_LAYER = new ModelLayerLocation(new ResourceLocation(MeatWeapons.NAMESPACE, "cannon_bullet"), "main");

    public static final ModelLayerLocation ZAP_LAYER = new ModelLayerLocation(new ResourceLocation(MeatWeapons.NAMESPACE, "zap"), "main");
    public static final ModelLayerLocation FUSION_BLAST_LAYER = new ModelLayerLocation(new ResourceLocation(MeatWeapons.NAMESPACE, "fusion_blast"), "main");
    public static final ModelLayerLocation MODEL_PLASMA_LAYER = new ModelLayerLocation(new ResourceLocation(MeatWeapons.NAMESPACE, "plasma"), "main");
    public static final ModelLayerLocation MODEL_SHELL_LAYER = new ModelLayerLocation(new ResourceLocation(MeatWeapons.NAMESPACE, "shell"), "main");
    public static final ModelLayerLocation BOUNCE_GRENADE_LAYER = new ModelLayerLocation(new ResourceLocation(MeatWeapons.NAMESPACE, "bounce_grenade"), "main");

    public static void registerEntityModels()
    {
        MWInstances.init();

        EntityRendererRegistry.register(MeatWeapons.BULLET, BulletEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(MODEL_BULLET_LAYER, BulletEntityModel::getTexturedModelData);

        EntityRendererRegistry.register(MeatWeapons.CANNON_BULLET, CannonBulletEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(MODEL_CANNON_BULLET_LAYER, CannonBulletEntityModel::getTexturedModelData);

        EntityRendererRegistry.register(MeatWeapons.ZAP, ZapEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(ZAP_LAYER, CannonBulletEntityModel::getTexturedModelData);

        EntityRendererRegistry.register(MeatWeapons.FUSION_BLAST, PlasmaEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(FUSION_BLAST_LAYER, PlasmaEntityModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(MODEL_PLASMA_LAYER, PlasmaEntityModel::getTexturedModelData);

        EntityRendererRegistry.register(MeatWeapons.EXPLODING_SHELL, ShellEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(MODEL_SHELL_LAYER, PlasmaEntityModel::getTexturedModelData);

        EntityRendererRegistry.register(MeatWeapons.BOUNCE_GRENADE, GrenadeEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(BOUNCE_GRENADE_LAYER, PlasmaEntityModel::getTexturedModelData);

        EntityRendererRegistry.register(MeatWeapons.SHOCK_STAFF_PROJECTILE, ShockStaffProjectileEntityRenderer::new);
    }

    public static void registerAnimations()
    {
//        GeoItemRenderer.registerItemRenderer(MWItems.HAND_CANNON, new BaseGunRenderer<>(new HandCannonItemModel()));
//        GeoItemRenderer.registerItemRenderer(MWItems.FUSION_CANNON, new BaseGunRenderer<>(new FusionCannonItemModel()));
//        GeoItemRenderer.registerItemRenderer(MWItems.MACHINE_PISTOL, new BaseGunRenderer<>(new PistolItemModel()));
//        GeoItemRenderer.registerItemRenderer(MWItems.LMG, new BaseGunRenderer<>(new LMGItemModel()));
//        GeoItemRenderer.registerItemRenderer(MWItems.HEAVY_CANNON, new BaseGunRenderer<>(new HeavyCannonItemModel()));
//        GeoItemRenderer.registerItemRenderer(MWItems.MA75, new BaseGunRenderer<>(new BaseGunModel<>(new Identifier(MeatWeapons.NAMESPACE, "geo/ma75.geo.json"), new Identifier(MeatWeapons.NAMESPACE, "textures/general/ma75.png"), new Identifier(MeatWeapons.NAMESPACE, "animations/ma75.animation.json"))));
//        GeoItemRenderer.registerItemRenderer(MWItems.BLASTER, new BaseGunRenderer<>(new BaseGunModel<>(new Identifier(MeatWeapons.NAMESPACE, "geo/blaster.geo.json"), new Identifier(MeatWeapons.NAMESPACE, "textures/general/thingy.png"), new Identifier(MeatWeapons.NAMESPACE, "animations/blaster.animation.json"))));

//        GeoItemRenderer.registerItemRenderer(MWItems.ASSAULT_DRILL, new DrillItemRenderer(new DrillItemModel()));

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(MeatWeapons.AIRTRUCK, AirtruckEntityRenderer::new);

    }

    @Override
    public void onInitializeClient()
    {
        Myron.registerNamespace(MeatWeapons.NAMESPACE);

        registerEntityModels();
        registerAnimations();
        MWParticles.initClient();
        MWKeys.registerKeybinds();
        AirtruckSoundInstance.initEvent();

        MeatgunClient.init();

        AssaultDrillItem.Client.init();

        BeamEffect.Client.init();

        GraphicsEffectClient.registerEffect(MWGraphicsEffects.BEAM, BeamEffect::new);
        GraphicsEffectClient.registerEffect(MWGraphicsEffects.BULLET_TRAIL, BulletTrailEffect::new);
        GraphicsEffectClient.registerEffect(MWGraphicsEffects.RICOCHET_BULLET_TRAIL, (world, start, end, velocity, scale, maxAge, buf) -> new BulletTrailEffect(world, start, end, velocity, scale, maxAge, buf, true));
        GraphicsEffectClient.registerEffect(MWGraphicsEffects.LONG_BULLET_TRAIL, LongBulletTrailEffect::new);
        GraphicsEffectClient.registerEffect(MWGraphicsEffects.ZAP, ZapBeamEffect::new);
        GraphicsEffectClient.registerEffect(MWGraphicsEffects.SMALL_PHAGE_RAY, SmallPhageRayGraphicsEffect::new);
        GraphicsEffectClient.registerEffect(MWGraphicsEffects.BONE_STEALER, BoneStealerGraphicsEffect::new);

        ModelLoadingPlugin.register(MWExtraModels.EXTRA_MODELS);

        BuiltinItemRendererRegistry.INSTANCE.register(MWItems.BASIC_PISTOL, new MeatgunPistolRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(MWItems.MEATGUN_PISTOL, new MeatgunPistolRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(MWItems.MEATGUN_STAFF, new MeatgunStaffRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(MWItems.LONG_GUN, new MeatgunLongGunRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(MWItems.ASSAULT_DRILL, new AssaultDrillItemRenderer());

        MeatgunModuleRenderers.init();
        MeatgunParticleManager.init();
        ShortPhageRayModuleClient.init();
        GunTickHandler.Client.init();

        MWScreens.init();

        LongBoiModule.SYNC_SOUND.receiverHandler(EnvType.CLIENT, (player, buf, responseSender) ->
        {
            return (entity, start) ->
            {
                var client = Minecraft.getInstance();
                var sourceEntity = client.level.getEntity(entity);
                if (start)
                {

                    client.getSoundManager().play(LONG_BOI_SOUNDS.add(entity, new LongBoiSoundInstance(SoundSource.PLAYERS, sourceEntity)));
                }
                else
                {
                    client.getSoundManager().stop(LONG_BOI_SOUNDS.get(entity));
                }
            };
        });
    }

    private static final SoundInstanceCache<LongBoiSoundInstance> LONG_BOI_SOUNDS = new SoundInstanceCache<>();
}
