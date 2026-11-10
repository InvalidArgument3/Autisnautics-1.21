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

package com.neep.neepmeat;

import com.neep.meatlib.MeatLibRegistration;
import com.neep.meatlib.config.ConfigUtil;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepbus.NeepBus;
import com.neep.neepmeat.api.Burner;
import com.neep.neepmeat.api.DataType;
import com.neep.neepmeat.api.block.PressureWashable;
import com.neep.neepmeat.api.enlightenment.EnlightenmentEvent;
import com.neep.neepmeat.api.enlightenment.EnlightenmentUtil;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.api.processing.BlockCrushingRegistry;
import com.neep.neepmeat.api.processing.DrillHarvesting;
import com.neep.neepmeat.api.processing.OreDustRegistry;
import com.neep.neepmeat.api.processing.OreFatRegistry;
import com.neep.neepmeat.api.processing.ball_mill.BallMillRecipeGenerator;
import com.neep.neepmeat.api.processing.random_ores.RandomOres;
import com.neep.neepmeat.block.entity.FlameJetBlockEntity;
import com.neep.neepmeat.block.entity.FurnaceBurnerImpl;
import com.neep.neepmeat.client.datagen.NMModelProvider;
import com.neep.neepmeat.config.NMConfig;
import com.neep.neepmeat.datagen.NMAdvancements;
import com.neep.neepmeat.datagen.NMBlockTagProvider;
import com.neep.neepmeat.datagen.NMItemTagProvider;
import com.neep.neepmeat.datagen.NMRecipeGenerator;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.enlightenment.EnlightenmentEventManager;
import com.neep.neepmeat.enlightenment.event.FollowerSpawnEnlightenmentEvent;
import com.neep.neepmeat.enlightenment.event.LimbEnlightenmentEvent;
import com.neep.neepmeat.entity.effect.NMStatusEffects;
import com.neep.neepmeat.entity.worm.WormActions;
import com.neep.neepmeat.guide.GuideReloadListener;
import com.neep.neepmeat.implant.NMImplants;
import com.neep.neepmeat.implant.entity.PlayerImplantManager;
import com.neep.neepmeat.init.*;
import com.neep.neepmeat.item.filter.Filters;
import com.neep.neepmeat.machine.charnel_compactor.CharnelCompactorStorage;
import com.neep.neepmeat.machine.homogeniser.FoodAdditives;
import com.neep.neepmeat.machine.integrator.IntegratorBlockEntity;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.reactor.IntrusionReactor;
import com.neep.neepmeat.machine.synthesiser.MobSynthesisRegistry;
import com.neep.neepmeat.meatgun.NMMeatgunModules;
import com.neep.neepmeat.network.MachineDiagnosticsRequest;
import com.neep.neepmeat.network.NMTrackedData;
import com.neep.neepmeat.network.ToolTransformPacket;
import com.neep.neepmeat.plc.PLCBlocks;
import com.neep.neepmeat.plc.recipe.PLCRecipes;
import com.neep.neepmeat.potion.NMPotions;
import com.neep.neepmeat.transport.FluidTransport;
import com.neep.neepmeat.transport.ItemTransport;
import com.neep.neepmeat.transport.block.fluid_transport.TankBlock;
import com.neep.neepmeat.transport.block.fluid_transport.machine.TankType;
import com.neep.neepmeat.transport.blood_network.BloodNetworkManager;
import com.neep.neepmeat.transport.worm.GlobalDistributorRouting;
import com.neep.neepmeat.util.Bezier;
import com.neep.neepmeat.util.SightUtil;
import com.neep.neepmeat.util.TagSuggestions;
import com.neep.neepmeat.util.WrenchUtil;
import com.neep.neepmeat.world.NMFeatures;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.GeckoLib;

public class NeepMeat implements ModInitializer
{
	public static final String NAMESPACE = "neepmeat";
	public static final RegistrationContext C = new RegistrationContext(NAMESPACE, () -> NMItemGroups.GENERAL);
	public static final Logger LOGGER = LogManager.getLogger(NAMESPACE);

	public static final NMConfig CONFIG = ConfigUtil.createDefaults(FabricLoader.getInstance().getConfigDir(), "neep", NAMESPACE, NMConfig.class);

	public static MutableComponent translationKey(String prefix, String things)
	{
		return Component.translatable(prefix + "." + NAMESPACE + "." + things);
	}

	public static MutableComponent translationKey(String prefix, String things, Object... args)
	{
		return Component.translatable(prefix + "." + NAMESPACE + "." + things, args);
	}

	public static MutableComponent translationKey(String prefix, String things, String args)
	{
//		StringBuilder builder = new StringBuilder();
//		for (int i = 0; i < things.length; ++i)
//		{
//			builder.append(things[i]);
//			if (i <= things.length - 1)
//				builder.append('.');
//		}
//		return Text.translatable(prefix + "." + NAMESPACE + "." + builder);
		return Component.translatable(prefix + "." + NAMESPACE + "." + things, args);
	}

	@Override
	public void onInitialize()
	{
		MeatLibRegistration.forContext(NMBlocks.class, NMBlocks.C);
		MeatLibRegistration.forContext(FluidTransport.class, FluidTransport.C);
		MeatLibRegistration.forContext(LivingMachines.class, LivingMachines.C);
		MeatLibRegistration.forContext(IntrusionReactor.class, IntrusionReactor.C);
		MeatLibRegistration.forContext(PLCBlocks.class, PLCBlocks.C);
		MeatLibRegistration.forContext(NeepBus.class, NeepBus.C);
		MeatLibRegistration.forContext(NMItems.class, NMItems.C);
		MeatLibRegistration.forContext(ItemTransport.class, ItemTransport.C);

		LOGGER.info("Hello from NeepMeat!");
		new Bezier();

		GeckoLib.initialize();


		NMItems.init();
		NMLootTables.init();
		NMTags.init();
		NMParticles.init();
		new NMSounds();

		NMrecipeTypes.init();

		PLCBlocks.init();
		NMDocs.init();
		LivingMachineComponents.init();
		LivingMachines.init();
		IntrusionReactor.init();

		NMFluids.initialise();
		NMBlockEntities.initialise();
		NMEntities.initialise();
		OreFatRegistry.init();
		NMStatusEffects.init();
		NMPotions.init();
		NMGraphicsEffects.init();
		Filters.init();
		WrenchUtil.init();

		NMItemGroups.init();
		DataType.init();
		NMCommands.init();
		NMCriteria.init();
        NMLootConditions.init();

		// --- Transport module ---
		ItemTransport.init();
		FluidTransport.init();
		BloodNetworkManager.init();

//		EnlightenmentUtil.init();
//		EnlightenmentEventManager.init();

		// --- Other misc things ---
		ToolTransformPacket.registerReceiver();
		MachineDiagnosticsRequest.registerReceiver();
		MotorisedBlock.DiagnosticsProvider.init();

		NMTrackedData.init();

		NMFeatures.init();

		TagSuggestions.init();

		ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) -> CharnelCompactorStorage.getStorage(world, pos, direction), NMBlocks.CHARNEL_COMPACTOR);
		FluidStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) -> blockEntity instanceof IntegratorBlockEntity be ? be.getStorage(world, pos, state, direction) : null, NMBlocks.INTEGRATOR_EGG);

		Burner.LOOKUP.registerForBlockEntity(FurnaceBurnerImpl::get, BlockEntityType.FURNACE);
		Burner.LOOKUP.registerForBlocks((world, pos, state, blockEntity, context) -> () -> 40, Blocks.LAVA, Blocks.LAVA_CAULDRON, Blocks.MAGMA_BLOCK, Blocks.FIRE);
		Burner.LOOKUP.registerForBlocks((world, pos, state, blockEntity, context) -> () -> 60, Blocks.SOUL_FIRE);
		Burner.LOOKUP.registerForBlocks((world, pos, state, blockEntity, context) -> () -> state.getValue(CampfireBlock.LIT) ? 40 : 0, Blocks.CAMPFIRE);
		Burner.LOOKUP.registerForBlocks((world, pos, state, blockEntity, context) -> () -> state.getValue(CampfireBlock.LIT) ? 60 : 0, Blocks.SOUL_CAMPFIRE);
		Burner.LOOKUP.registerForBlocks((world, pos, state, blockEntity, context) -> () -> 10, Blocks.TORCH, Blocks.LANTERN, Blocks.SOUL_LANTERN);
        Burner.LOOKUP.registerForBlockEntity(FlameJetBlockEntity::getBurner, NMBlockEntities.FLAME_JET);

		PLCRecipes.init();

		NMScreenHandlers.registerScreenHandlers();

		// Meat additives
		FoodAdditives.init();

		// Resource reload listeners
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(GuideReloadListener.getInstance());
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(MobSynthesisRegistry.getInstance());
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(OreFatRegistry.INSTANCE);
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(OreDustRegistry.ORE_DUST);
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(OreDustRegistry.CRUSHED_ORE);
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(RandomOres.INSTANCE);
		BlockCrushingRegistry.init();
        BallMillRecipeGenerator.init();
		RandomOres.init();
		DrillHarvesting.init();

		WormActions.init();

		PlayerImplantManager.init();

//			PlayerAttachmentManager.registerAttachment(PlayerImplantManager.ID, PlayerImplantManager::new);
//			Registry.register(PlayerImplantRegistry.REGISTRY, ExtraMouthImplant.ID, ExtraMouthImplant::new);
//			Registry.register(PlayerImplantRegistry.REGISTRY, SkeltalImplant.ID, SkeltalImplant::new);

		NMImplants.init();

		Registry.register(EnlightenmentEventManager.EVENTS, new ResourceLocation(NAMESPACE, "limb_spawn"), new EnlightenmentEvent.SimpleFactory(LimbEnlightenmentEvent::new));
		Registry.register(EnlightenmentEventManager.EVENTS, new ResourceLocation(NAMESPACE, "follower_spawn"), new EnlightenmentEvent.SimpleFactory(FollowerSpawnEnlightenmentEvent::new));
		NMEnlightenmentEvents.init();

		NMCommonNetwork.init();

		EnlightenmentEventManager.init();
		EnlightenmentUtil.init();

		MeatLibRegistration.flush();

		FluidStorage.ITEM.registerForItems(TankBlock.createStorageProvider(TankType.BASIC_GLASS), FluidTransport.BASIC_GLASS_TANK.asItem());
		FluidStorage.ITEM.registerForItems(TankBlock.createStorageProvider(TankType.BASIC), FluidTransport.BASIC_TANK.asItem());
		FluidStorage.ITEM.registerForItems(TankBlock.createStorageProvider(TankType.ADVANCED), FluidTransport.ADVANCED_TANK.asItem());

        PressureWashable.initDefaults();

        GlobalDistributorRouting.init();
		NMSounds.init();

//        NMItemGroups.GENERAL_EXTRA.add(OreDustRegistry.CRUSHED_ORE::addToItemGroup);
//        NMItemGroups.GENERAL_EXTRA.add(OreDustRegistry.ORE_DUST::addToItemGroup);

        NMMeatgunModules.init();

		CompostingChanceRegistry.INSTANCE.add(NMItems.BLOOD_BUBBLE, 0.3f);
		CompostingChanceRegistry.INSTANCE.add(NMBlocks.BLOOD_BUBBLE_SAPLING, 0.5f);
		CompostingChanceRegistry.INSTANCE.add(NMBlocks.BLOOD_BUBBLE_LEAVES, 0.3f);
		CompostingChanceRegistry.INSTANCE.add(NMBlocks.BLOOD_BUBBLE_LEAVES_FLOWERING, 0.4f);
	}

	public static void cowThingy(Cow cow, Player player, InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		if (stack.is(NMItems.ENLIGHTENED_BRAIN))
		{
			var random = cow.getRandom();
			int count = random.nextIntBetweenInclusive(2, 4);

			if (!player.isCreative())
			{
				stack.shrink(1);
			}

			for (int i = 0; i < count; ++i)
			{
				cow.spawnAtLocation(new ItemStack(NMItems.ROUGH_BRAIN), 0.5f);
			}
			player.level().playLocalSound(cow.getX(), cow.getY(), cow.getZ(),
					SoundEvents.HONEYCOMB_WAX_ON, SoundSource.NEUTRAL, 1, 1, false);
			cow.setDropsLoot(false);
			cow.kill();
		}
	}
}
