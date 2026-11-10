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

import com.neep.meatlib.block.BaseBuildingBlock;
import com.neep.meatlib.block.PaintedBlockCollection;
import com.neep.meatlib.graphics.client.GraphicsEffectClient;
import com.neep.neepbus.client.input.InputInterceptorsClient;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.block.PlacementPreview;
import com.neep.neepmeat.api.processing.BlockCrushingRegistry;
import com.neep.neepmeat.api.processing.OreFatSyncData;
import com.neep.neepmeat.client.effect.ReminaGraphicsEffect;
import com.neep.neepmeat.client.fluid.NMFluidsClient;
import com.neep.neepmeat.client.hud.*;
import com.neep.neepmeat.client.input.ImplanterInterceptorHandler;
import com.neep.neepmeat.client.instance.TreeVacuumVisual;
import com.neep.neepmeat.client.item.BlockAttackListenerThings;
import com.neep.neepmeat.client.item.DrillChassisItemClient;
import com.neep.neepmeat.client.model.GenericModel;
import com.neep.neepmeat.client.model.GlassTankModel;
import com.neep.neepmeat.client.model.GlomeEntityModel;
import com.neep.neepmeat.client.model.entity.GeneralBipedEntityModel;
import com.neep.neepmeat.client.model.entity.HoundEntityModel;
import com.neep.neepmeat.client.particle.PhageRayGraphicsEffect;
import com.neep.neepmeat.client.plc.PLCClient;
import com.neep.neepmeat.client.plc.PLCHudRenderer;
import com.neep.neepmeat.client.renderer.*;
import com.neep.neepmeat.client.renderer.block.AdvancedIntegratorRenderer;
import com.neep.neepmeat.client.renderer.entity.*;
import com.neep.neepmeat.client.renderer.item.DrillChassisItemRenderer;
import com.neep.neepmeat.client.renderer.item.MopRenderer;
import com.neep.neepmeat.client.renderer.item.TissueOperatorRenderer;
import com.neep.neepmeat.client.screen.*;
import com.neep.neepmeat.client.screen.button.ImplanterScreen;
import com.neep.neepmeat.client.screen.living_machine.*;
import com.neep.neepmeat.client.screen.plc.BuilderScreen;
import com.neep.neepmeat.client.screen.plc.PLCProgramScreen;
import com.neep.neepmeat.client.screen.plc.ProgramCabinetScreen;
import com.neep.neepmeat.client.screen.plc.ProgramCardScreen;
import com.neep.neepmeat.client.world.NMDimensionEffects;
import com.neep.neepmeat.entity.follower.FollowerRenderer;
import com.neep.neepmeat.init.*;
import com.neep.neepmeat.item.DosimeterItem;
import com.neep.neepmeat.machine.bottler.BottlerRenderer;
import com.neep.neepmeat.machine.casting_basin.CastingBasinRenderer;
import com.neep.neepmeat.machine.charnel_pump.CharnelPumpRenderer;
import com.neep.neepmeat.machine.charnel_pump.ContaminatedGrassBlock;
import com.neep.neepmeat.machine.crucible.AlembicRenderer;
import com.neep.neepmeat.machine.crucible.CrucibleRenderer;
import com.neep.neepmeat.machine.death_blades.DeathBladesRenderer;
import com.neep.neepmeat.machine.fabricator.FabricatorScreen;
import com.neep.neepmeat.machine.fabricator.FabricatorVisual;
import com.neep.neepmeat.machine.item_mincer.ItemMincerRenderer;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.mixer.MixerRenderer;
import com.neep.neepmeat.machine.multitank.MultiTankRenderer;
import com.neep.neepmeat.machine.phage_ray.PhageRayClientComponent;
import com.neep.neepmeat.machine.reactor.ReactionCoreRenderer;
import com.neep.neepmeat.machine.separator.SeparatorScreen;
import com.neep.neepmeat.machine.small_compressor.SmallCompressorScreen;
import com.neep.neepmeat.machine.small_trommel.SmallTrommelRenderer;
import com.neep.neepmeat.machine.synthesiser.SynthesiserRenderer;
import com.neep.neepmeat.machine.trough.TroughRenderer;
import com.neep.neepmeat.network.*;
import com.neep.neepmeat.plc.PLCBlocks;
import com.neep.neepmeat.plc.block.entity.ImplanterBlockEntity;
import com.neep.neepmeat.transport.FluidTransport;
import com.neep.neepmeat.transport.ItemTransport;
import com.neep.neepmeat.transport.block.fluid_transport.FilterPipeBlock;
import com.neep.neepmeat.transport.block.fluid_transport.FluidPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.DisplayPlateBlock;
import com.neep.neepmeat.transport.client.TransportClient;
import com.neep.neepmeat.transport.client.renderer.WindowPipeRenderer;
import com.neep.neepmeat.transport.client.screen.AdvancedRouterScreen;
import com.neep.neepmeat.transport.client.screen.filter.FilterScreen;
import com.neep.neepmeat.util.SightUtil;
import com.neep.neepspeak.audio.SoundFiles;
import dev.monarkhes.myron_neepmeat.api.Myron;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

@Environment(value= EnvType.CLIENT)
public class NeepMeatClient implements ClientModInitializer
{
    public static final ModelLayerLocation MODEL_GLASS_TANK_LAYER = new ModelLayerLocation(new ResourceLocation(NeepMeat.NAMESPACE, "glass_tank"), "main");
    public static final ModelLayerLocation TANK_MINECART = new ModelLayerLocation(new ResourceLocation(NeepMeat.NAMESPACE, "tank_minecart"), "main");
    public static final ModelLayerLocation SMALL_COMPRESSOR_MINECART = new ModelLayerLocation(new ResourceLocation(NeepMeat.NAMESPACE, "small_compressor_minecart"), "main");
    public static final ModelLayerLocation GLOME = new ModelLayerLocation(new ResourceLocation(NeepMeat.NAMESPACE, "glome"), "main");
    public static final ModelLayerLocation EGG = new ModelLayerLocation(new ResourceLocation(NeepMeat.NAMESPACE, "egg"), "main");

    @Override
    public void onInitializeClient()
    {
        Myron.registerNamespace(NeepMeat.NAMESPACE);

        NMClientComponents.init();

        registerRenderers();
        registerLayers();
        registerScreens();
        NMFluidsClient.registerFluidRenderers();
        HUDOverlays.init();
        MachineHudOverlay.init();
        FarmingScutterHudRenderer.init();
        BlockPlacementHelper.init();
        DrillChassisItemClient.init();
        NMFeatureRenderers.init();
        BlockAttackListenerThings.init();

        TransportClient.init();

        TankMessagePacket.Client.registerReceiver();
        ParticleSpawnS2C.Client.registerReceiver();
        BlockSoundPacket.Client.registerReceiver();
        ParticleSpawnS2C.Client.registerReceiver();
        PlayerImplantStatusS2CPacket.Client.registerReceiver();
        EntityAnimationS2C.Client.registerReceiver();
        MachineDiagnosticsRequest.Client.registerReceiver();
        OreFatSyncData.Client.init();
        BlockCrushingRegistry.Client.init();
        NMClientNetwork.init();
        NMKeys.registerKeybindings();
        WrenchPlacementHelper.init();
        NMImplantsClient.init();

        GraphicsEffectClient.registerEffect(NMGraphicsEffects.REMINA, (world, start, end, velocity, scale, maxAge, buf) -> new ReminaGraphicsEffect(world, buf));
        GraphicsEffectClient.registerEffect(NMGraphicsEffects.PHAGE_RAY, PhageRayGraphicsEffect::new);

        NMDimensionEffects.init();

        PLCHudRenderer.init();
        PLCClient.init();

        DosimeterItem.Client.init();

        PhageRayClientComponent.init();
        FabricatorVisual.initNetwork();
        TreeVacuumVisual.initNetwork();

        GuideLookupThings.init();
        NarratorSayS2C.Client.init();
        NEEPSpeakSayS2C.Client.init();

//        ImplantAttributes.register(Impla);

        SoundFiles.preload();

        NMMeatgunModuleRenderers.init();

        PlacementPreviewRegistry.register((PlacementPreview) PLCBlocks.PROGRAM_CARD, ProgramCardPlacementHelper::new);

        ItemProperties.register(
                Items.ROTTEN_FLESH,
                new ResourceLocation(NeepMeat.NAMESPACE, "enlightened"),
                (stack, world, entity, seed) ->
                {
                    var player = Minecraft.getInstance().player;
                    if (player != null)
                    {
                        return SightUtil.canPlayerSeeBog(player) ? 1 : 0;
                    }
                    return 0;
                }
        );
    }

    public static void registerRenderers()
    {
        EntityModelLayerRegistry.registerModelLayer(GLOME, GlomeEntityModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(EGG, GlomeEntityModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(HoundEntityRenderer.HOUND_LAYER, HoundEntityModel::getTexturedModelData);

        EntityModelLayerRegistry.registerModelLayer(GeneralBipedEntityRenderer.MAIN, () -> LayerDefinition.create(
                GeneralBipedEntityModel.createMesh(CubeDeformation.NONE, 0), 64, 64));

//        TexturedModelData keeperMainData = TexturedModelData.of(BipedEntityModel.getModelData(Dilation.NONE, 0), 64, 64);
//        TexturedModelData keeperInnerLayer = TexturedModelData.of(BipedEntityModel.getModelData(new Dilation(0.5f), 0), 64, 64);
//        TexturedModelData keeperOuterLayer = TexturedModelData.of(BipedEntityModel.getModelData(new Dilation(1f), 0), 64, 64);
//        EntityModelLayerRegistry.registerModelLayer(KeeperEntityRenderer.KEEPER, () -> keeperMainData);
//        EntityModelLayerRegistry.registerModelLayer(KeeperEntityRenderer.KEEPER_INNER, () -> keeperInnerLayer);
//        EntityModelLayerRegistry.registerModelLayer(KeeperEntityRenderer.KEEPER_OUTER, () -> keeperOuterLayer);

//        EntityRendererRegistry.register(NMEntities.TANK_MINECART, ctx -> new TankMinecartRenderer(ctx, TANK_MINECART));
        EntityRendererRegistry.register(NMEntities.SMALL_COMPRESSOR_MINECART, ctx -> new SmallCompressorMinecartRenderer(ctx, SMALL_COMPRESSOR_MINECART));
        EntityRendererRegistry.register(NMEntities.GLOME, ctx -> new GlomeEntityRenderer(ctx, GLOME));
        EntityRendererRegistry.register(NMEntities.EGG, ctx -> new EggEntityRenderer(ctx, EGG));
        EntityRendererRegistry.register(NMEntities.LIMB, DummyEntityRenderer::new);
        EntityRendererRegistry.register(NMEntities.WORM, WormEntityRenderer::new);
        EntityRendererRegistry.register(NMEntities.KEEPER, KeeperEntityRenderer::new);
        EntityRendererRegistry.register(NMEntities.BOG_FOLLOWER, GeneralBipedEntityRenderer.create(new ResourceLocation(NeepMeat.NAMESPACE, "textures/entity/path_legion/bog_fighter.png")));
        EntityRendererRegistry.register(NMEntities.HOUND, HoundEntityRenderer::new);
        EntityRendererRegistry.register(NMEntities.BOVINE_HORROR, BovineHorrorRenderer::new);
        EntityRendererRegistry.register(NMEntities.REPEATER, c -> new RepeaterEntityRenderer(c,
                new GenericModel<>(NeepMeat.NAMESPACE,
                        "geo/repeater.geo.json",
                        "textures/entity/repeater.png",
                        "animations/repeater.animation.json")));
        EntityRendererRegistry.register(NMEntities.ACID_SPRAY, DummyEntityRenderer::new);
        EntityRendererRegistry.register(NMEntities.PHAGE_RAY, DummyEntityRenderer::new);
        EntityRendererRegistry.register(NMEntities.FOLLOWER, FollowerRenderer::new);
        EntityRendererRegistry.register(NMEntities.SMUDGE, SmudgeEntityRenderer::new);
        EntityRendererRegistry.register(NMEntities.FALLING_ACTIVE_WASTE, FallingBlockRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(TANK_MINECART, MinecartModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(SMALL_COMPRESSOR_MINECART, MinecartModel::createBodyLayer);

//        GeoArmorRenderer.regi(new GogglesArmourRenderer(new GenericModel<>(
//                NeepMeat.NAMESPACE,
//                "geo/goggles.geo.json",
//                "textures/entity/armour/goggles.png",
//                "animations/goggles.animation.json"
//                )), NMItems.GOGGLES);

        // Custom baked models
        ModelLoadingPlugin.register(NeepMeatModelProvider.INSTANCE);
        ModelLoadingPlugin.register(NMExtraModels.INSTANCE);
        NMParticles.Client.init();

        NMVisuals.init();

        // BlockEntity renderers
        BlockEntityRenderers.register(NMBlockEntities.WINDOW_PIPE, WindowPipeRenderer::new);

        BlockEntityRenderers.register(NMBlockEntities.GLASS_TANK, GlassTankRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(MODEL_GLASS_TANK_LAYER, GlassTankModel::getTexturedModelData);
        BlockEntityRenderers.register(NMBlockEntities.FLUID_BUFFER, FluidBufferRenderer::new);
        BlockEntityRenderers.register(NMBlockEntities.ITEM_BUFFER_BLOCK_ENTITY, c -> new ItemBlockEntityRenderer<>(
                c, be -> be.getStorage(null).getAsStack(), (be) ->
        {
            float target = be.getStorage(null).getAmount() <= 0 ? 0.3f : 0;
            be.stackRenderDelta = Mth.lerp(0.2f, be.stackRenderDelta, target);
            return (be.getBlockState().getValue(DisplayPlateBlock.TOP) ? 1.15f : 0.25f) + be.stackRenderDelta;
        }));
//        BlockEntityRendererFactories.register(NMBlockEntities.TROMMEL, TrommelRenderer::new);
        BlockEntityRenderers.register(NMBlockEntities.SMALL_TROMMEL, SmallTrommelRenderer::new);
        BlockEntityRenderers.register(NMBlockEntities.INTEGRATOR, IntegratorEggRenderer::new);
        BlockEntityRenderers.register(NMBlockEntities.ADVANCED_INTEGRATOR, AdvancedIntegratorRenderer::new);
        BlockEntityRenderers.register(NMBlockEntities.BIG_LEVER, BigLeverRenderer::new);
        BlockEntityRenderers.register(NMBlockEntities.ITEM_PIPE, ItemPipeRenderer::new);
        BlockEntityRenderers.register(NMBlockEntities.FILTER_ITEM_PIPE, ItemPipeRenderer::new);
        BlockEntityRenderers.register(NMBlockEntities.SPEED_ITEM_PIPE, ItemPipeRenderer::new);
        BlockEntityRenderers.register(NMBlockEntities.ENCASED_PNEUMATIC_PIPE, ItemPipeRenderer::new);
        BlockEntityRenderers.register(ItemTransport.STORAGE_BUS_BE, ItemPipeRenderer::new);
        BlockEntityRenderers.register(NMBlockEntities.MERGE_ITEM_PIPE, MergePipeRenderer::new);
//        BlockEntityRendererFactories.register(NMBlockEntities.ITEM_PUMP, ItemPumpRenderer::new);
//        BlockEntityRendererFactories.register(NMBlockEntities.EJECTOR, EjectorRenderer::new);
//        BlockEntityRendererFactories.register(ItemTransport.FILTERED_EJECTOR_BE, EjectorRenderer::new);
//        BlockEntityRendererFactories.register(NMBlockEntities.CONVERTER, ConverterRenderer::new);
//        BlockEntityRendererFactories.register(NMBlockEntities.ADVANCED_MOTOR, AdvancedMotorRenderer::new);
        BlockEntityRenderers.register(NMBlockEntities.DEPLOYER, DeployerRenderer::new);
//        BlockEntityRendererFactories.register(NMBlockEntities.AGITATOR, AgitatorRenderer::new);
//        BlockEntityRendererFactories.register(NMBlockEntities.VAT_CONTROLLER, VatRenderer::new);
        BlockEntityRenderers.register(NMBlockEntities.MIXER, MixerRenderer::new);
        BlockEntityRenderers.register(NMBlockEntities.MULTI_TANK, MultiTankRenderer::new);

        BlockEntityRenderers.register(NMBlockEntities.CRUCIBLE, CrucibleRenderer::new);
        BlockEntityRenderers.register(NMBlockEntities.ALEMBIC, AlembicRenderer::new);
        BlockEntityRenderers.register(NMBlockEntities.CASTING_BASIN, CastingBasinRenderer::new);

        BlockEntityRenderers.register(NMBlockEntities.PEDESTAL, c -> new ItemBlockEntityRenderer<>(
                c, be -> be.getStorage(null).getResource().toStack(), be -> 0.67f));
//        BlockEntityRendererFactories.register(NMBlockEntities.ASSEMBLER, AssemblerRenderer::new);

        BlockEntityRenderers.register(NMBlockEntities.DEATH_BLADES, DeathBladesRenderer::new);

        BlockEntityRenderers.register(NMBlockEntities.FEEDING_TROUGH, TroughRenderer::new);

        BlockEntityRenderers.register(NMBlockEntities.BOTTLER, BottlerRenderer::new);

//        BlockEntityRendererFactories.register(NMBlockEntities.PYLON, PylonInstance::new);

        BlockEntityRenderers.register(NMBlockEntities.SYNTHESISER, SynthesiserRenderer::new);

        BlockEntityRenderers.register(LivingMachines.CHARNEL_PUMP_BE, CharnelPumpRenderer::new);

//        BlockEntityRendererFactories.register(PLCBlocks.PLC_ENTITY, PLCInstance::new);

        BlockEntityRenderers.register(NMBlockEntities.ITEM_MINCER, ItemMincerRenderer::new);

        BlockEntityRenderers.register(NMBlockEntities.REACTION_CORE, ReactionCoreRenderer::new);

//        BlockEntityRendererFactories.register(PLCBlocks.ROBOTIC_ARM_ENTITY, RoboticArmRenderer::new);

        BuiltinItemRendererRegistry.INSTANCE.register(NMItems.DRILL_CHASSIS, new DrillChassisItemRenderer(Minecraft.getInstance()));
//        BuiltinItemRendererRegistry.INSTANCE.register(NMItems.ROLLED_NEWSPAPER, RolledNewspaperRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(NMItems.TISSUE_OPERATOR, TissueOperatorRenderer.INSTANCE);
        BuiltinItemRendererRegistry.INSTANCE.register(NMItems.MOP, MopRenderer.INSTANCE);


        // Coloured blocks
        for (PaintedBlockCollection.PaintedBlock block : NMBlocks.SMOOTH_TILE.entries)
        {
            ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> block.getRawCol(), block);
            ColorProviderRegistry.ITEM.register((stack, tintIndex) -> block.getRawCol(), block.asItem());
        }

        // Pipes
        for (var pipe : FluidTransport.COLOURED_FLUID_PIPES.values())
        {
            ColorProviderRegistry.BLOCK.register(FluidPipeBlock::getTint, pipe);
            BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), pipe);
            ColorProviderRegistry.ITEM.register(FluidPipeBlock::getItemTint, pipe.asItem());
        }
        ColorProviderRegistry.BLOCK.register(FilterPipeBlock::getTint, FluidTransport.FILTER_PIPE);
        ColorProviderRegistry.BLOCK.register(ContaminatedGrassBlock::getTint, NMBlocks.CONTAMINATED_GRASS_BLOCK);
        ColorProviderRegistry.ITEM.register(ContaminatedGrassBlock::getItemTint, NMBlocks.CONTAMINATED_GRASS_BLOCK);

        ColorProviderRegistry.ITEM.register(NMItems.ORE_DUST::getTint, NMItems.ORE_DUST);
        ColorProviderRegistry.ITEM.register(NMItems.CRUSHED_ORE::getTint, NMItems.CRUSHED_ORE);
        ColorProviderRegistry.ITEM.register(NMItems.FOOD_CARTON::getTint, NMItems.FOOD_CARTON);
    }

    public static void registerScreens()
    {
        MenuScreens.register(NMScreenHandlers.BUFFER_SCREEN_HANDLER, BufferScreen::new);
        MenuScreens.register(NMScreenHandlers.CONTENT_DETECTOR_SCREEN_HANDLER, ContentDetectorScreen::new);
        MenuScreens.register(NMScreenHandlers.ROUTER, RouterScreen::new);
        MenuScreens.register(NMScreenHandlers.ADVANCED_ROUTER, AdvancedRouterScreen::new);
        MenuScreens.register(NMScreenHandlers.STIRLING_ENGINE, StirlingEngineScreen::new);
        MenuScreens.register(NMScreenHandlers.ALLOY_KILN, AlloyKilnScreen::new);
//        HandledScreens.register(ScreenHandlerInit.GUIDE, GuideMainScreen::new);
        MenuScreens.register(NMScreenHandlers.FLUID_RATIONER, FluidRationerScreen::new);
        MenuScreens.register(NMScreenHandlers.SEPARATOR, SeparatorScreen::new);
        MenuScreens.register(NMScreenHandlers.UPGRADE_MANAGER, ImplantManagerScreen::new);
        MenuScreens.register(NMScreenHandlers.DISPLAY_PLATE, DisplayPlateScreen::new);
        MenuScreens.register(NMScreenHandlers.FABRICATOR, FabricatorScreen::new);
        MenuScreens.register(NMScreenHandlers.SMALL_COMPRESSOR, SmallCompressorScreen::new);
        MenuScreens.register(NMScreenHandlers.FILTER, FilterScreen::new);
        MenuScreens.register(NMScreenHandlers.DEPLOYER, DeployerScreen::new);
        MenuScreens.register(NMScreenHandlers.VASCULAR_CONDENSER, VascularCondenserScreen::new);

        MenuScreens.register(NMScreenHandlers.LIVING_MACHINE, LivingMachineScreen::new);
        MenuScreens.register(NMScreenHandlers.ITEM_INPUT, ItemInputPortScreen::new);
        MenuScreens.register(NMScreenHandlers.ITEM_OUTPUT, ItemOutputPortScreen::new);
        MenuScreens.register(NMScreenHandlers.DISTRIBUTOR_POINT, DistributorPointScreen::new);
        MenuScreens.register(NMScreenHandlers.PHAGE_RAY, PhageRayScreen::new);

        MenuScreens.register(NMScreenHandlers.PLC, PLCProgramScreen::new);
        MenuScreens.register(NMScreenHandlers.PROGRAM_CABINET, ProgramCabinetScreen::new);
        MenuScreens.register(NMScreenHandlers.PROGRAM_CARD, ProgramCardScreen::new);
        MenuScreens.register(NMScreenHandlers.BUILDER, BuilderScreen::new);
        MenuScreens.register(NMScreenHandlers.IMPLANTER, ImplanterScreen::new);
        InputInterceptorsClient.register(ImplanterBlockEntity.ID, ImplanterInterceptorHandler::new);
    }

    public static void registerLayers()
    {
        // Fluids
        BlockRenderLayerMap.INSTANCE.putFluids(RenderType.translucent(), NMFluids.BLOOD.still(), NMFluids.BLOOD.flowing());
        BlockRenderLayerMap.INSTANCE.putFluids(RenderType.translucent(), NMFluids.PATINA_TREATMENT.still(), NMFluids.PATINA_TREATMENT.flowing());
//        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), NMFluids.STILL_ENRICHED_BLOOD, NMFluids.FLOWING_ENRICHED_BLOOD);
        BlockRenderLayerMap.INSTANCE.putFluids(RenderType.translucent(), NMFluids.ELDRITCH_ENZYMES.still(), NMFluids.ELDRITCH_ENZYMES.flowing());

        // Other blocks
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), FluidTransport.BASIC_GLASS_TANK);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), FluidTransport.MULTI_TANK);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), FluidTransport.FLUID_BUFFER);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.MESH_PANE);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), FluidTransport.PUMP);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.RUSTED_BARS);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), ItemTransport.ITEM_PIPE);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), ItemTransport.SPEED_ITEM_PIPE);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), ItemTransport.ADVANCED_EJECTOR);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), ItemTransport.FILTER_ITEM_PIPE);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), ItemTransport.OPAQUE_ITEM_PIPE); // Some faces are transparent
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), ItemTransport.STORAGE_BUS);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), ItemTransport.MERGE_ITEM_PIPE);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), ItemTransport.ITEM_PUMP);
//        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), NMBlocks.CONVERTER);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.DEPLOYER);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.MIXER);
//        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), NMBlocks.LEADED_GLASS);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.COLLECTOR);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.TRANSDUCER);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.SMALL_TROMMEL);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), LivingMachines.LARGE_TROMMEL);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), LivingMachines.TREE_VACUUM);
//        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), NMBlocks.CAUTION_TAPE);
//        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), NMBlocks.ASSEMBLER);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.FABRICATOR);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.BOTTLER);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.BLOOD_BUBBLE_SAPLING);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.BLOOD_BUBBLE_LEAVES);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.BLOOD_BUBBLE_LEAVES_FLOWERING);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.BLOOD_BUBBLE_TRAPDOOR);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.BLOOD_BUBBLE_DOOR);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.CAUTION_BLOCK_DOOR);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.WHISPER_WHEAT);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.FLESH_POTATO);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.GRAVE_BROME);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.SCAFFOLD_PLATFORM);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), (Block) NMBlocks.SCAFFOLD_PLATFORM.stairs);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), (Block) NMBlocks.SCAFFOLD_PLATFORM.slab);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.BLUE_SCAFFOLD);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), (Block) NMBlocks.BLUE_SCAFFOLD.stairs);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), (Block) NMBlocks.BLUE_SCAFFOLD.slab);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.YELLOW_SCAFFOLD);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), (Block) NMBlocks.YELLOW_SCAFFOLD.stairs);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), (Block) NMBlocks.YELLOW_SCAFFOLD.slab);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.POWER_FLOWER_SEEDS);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.ADVANCED_INTEGRATOR);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.PHAGE_RAY);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.LARGE_MOTOR);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.CHARNEL_PUMP);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.REINFORCED_GLASS);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), ((BaseBuildingBlock) NMBlocks.REINFORCED_GLASS).stairs);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), ((BaseBuildingBlock) NMBlocks.REINFORCED_GLASS).slab);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.SCAFFOLD_TRAPDOOR);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.ENCASED_DATA_CABLE);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.ENCASED_VASCULAR_CONDUIT);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), FluidTransport.ENCASED_FLUID_PIPE);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), ItemTransport.ENCASED_ITEM_PIPE);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.CHUTE);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), FluidTransport.FLUID_DRAIN);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), PLCBlocks.ROBOTIC_ARM);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), FluidTransport.WINDOW_PIPE);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.HOLDING_TRACK);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.CONTROL_TRACK);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.DUMPING_TRACK);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.LARGE_FAN);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), NMBlocks.CONTAMINATED_GRASS_BLOCK);

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), LivingMachines.LIVING_MACHINE_CONTROLLER);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), LivingMachines.OPEN_MACHINE_BLOCK);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), LivingMachines.OPEN_MEAT_STEEL_MACHINE_BLOCK);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), LivingMachines.OPEN_MEAT_STEEL_MACHINE_BLOCK_2);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), LivingMachines.OPEN_SKIN_MACHINE_BLOCK);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), LivingMachines.OPEN_BLOOD_BUBBLE_MACHINE_BLOCK);
    }
}
