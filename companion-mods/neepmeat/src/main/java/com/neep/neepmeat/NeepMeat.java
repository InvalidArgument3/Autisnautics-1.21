/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * Derivative NeoForge 1.21.1 port for the Autisnautics pack. Upstream source headers
 * release the work under the GNU General Public License, version 3 or later. The
 * LICENSE file shipped with NeepMeat is the GNU Lesser General Public License,
 * version 3. This port does not relicense the work.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat;

import com.mojang.logging.LogUtils;
import com.neep.neepmeat.building.BuildingBlocks;
import com.neep.neepmeat.fluid.BloodFluids;
import com.neep.neepmeat.item.MaterialItems;
import com.neep.neepmeat.machine.NeepMachines;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(NeepMeat.ID)
public final class NeepMeat {
    public static final String ID = "neepmeat";
    /** Loom sources use {@code NAMESPACE}. */
    public static final String NAMESPACE = ID;
    public static final Logger LOG = LogUtils.getLogger();
    /** Loom sources call {@code LOGGER}. */
    public static final Logger LOGGER = LOG;
    public static final com.neep.neepmeat.config.NMConfig CONFIG = new com.neep.neepmeat.config.NMConfig();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);

    public NeepMeat(ModContainer container, Dist dist) {
        IEventBus bus = container.getEventBus();
        bus.addListener(net.neoforged.bus.api.EventPriority.HIGHEST, false, com.neep.meatlib.registry.RegistrationContext::capture);
        bus.addListener(net.neoforged.bus.api.EventPriority.HIGHEST, false, com.neep.neepmeat.api.big_block.StructureBlockEntities::capture);
        bus.addListener(net.neoforged.bus.api.EventPriority.LOWEST, false, com.neep.meatlib.registry.RegistrationContext::finish);
        BuildingBlocks.bootstrap();
        NeepMachines.register(bus);
        com.neep.neepmeat.machine.StageMachines.register(bus);
        com.neep.neepmeat.machine.live_machine.LivingMachines.register(bus);
        com.neep.neepmeat.transport.block.VascularConduits.register(bus);
        com.neep.neepmeat.recipe.NeepComponents.COMPONENTS.register(bus);
        MaterialItems.register(bus);
        com.neep.neepmeat.block.RecipeBlocks.register(bus);
        BloodFluids.register(bus);
        com.neep.neepmeat.fluid.EtherealFuel.register(bus);
        com.neep.neepmeat.fluid.MeatFluid.register(bus);
        com.neep.neepmeat.fluid.TissueSlurry.bootstrap(bus);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(com.neep.meatlib.recipe.MeatlibRecipes::onServerStarted);
        com.neep.neepmeat.fluid.CompressedAir.register(bus);
        com.neep.neepmeat.fluid.WorkFluid.register(bus);
        com.neep.neepmeat.machine.EffectMachines.register(bus);
        com.neep.neepmeat.entity.NeepEntities.register(bus);
        com.neep.neepmeat.init.NMSounds.SOUNDS.register(bus);
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) -> com.neep.neepmeat.init.NMBlockEntities.bind());
        com.neep.neepmeat.component.NMAttachments.ATTACHMENTS.register(bus);
        bus.addListener(com.neep.neepmeat.network.RawPayload::register);
        com.neep.neepmeat.advancement.NMCriteria.TRIGGERS.register(bus);
        com.neep.neepmeat.init.NMScreenHandlers.register(bus);
        com.neep.neepmeat.plc.block.entity.PLCBlockEntity.registerNetwork();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TABS.register(bus);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(com.neep.neepmeat.transport.worm.DistributorRouting::onLevelTick);
        if (dist.isClient()) {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(com.neep.neepmeat.client.hud.BlockPlacementHelper::onClientTick);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(com.neep.neepmeat.client.hud.BlockPlacementHelper::onHighlight);
            bus.addListener(NeepMeatClient::onBlockColors);
            bus.addListener(NeepMeatClient::onItemColors);
            bus.addListener(NeepMeatClient::onScreens);
            bus.addListener(NeepMeatClient::onClientExtensions);
            bus.addListener(NeepMeatClient::onRenderers);
            bus.addListener(NeepMeatClient::onExtraModels);
        }
        geckolibLog(container);
    }

    public static net.minecraft.network.chat.MutableComponent translationKey(String prefix, String path) {
        return net.minecraft.network.chat.Component.translatable(prefix + "." + ID + "." + path);
    }

    private static void geckolibLog(ModContainer container) {
        ModList.get().getModContainerById("geckolib").ifPresentOrElse(
                mod -> LOG.info("NeepMeat {} loaded with GeckoLib {}", container.getModInfo().getVersion(), mod.getModInfo().getVersion()),
                () -> LOG.info("NeepMeat {} loaded; GeckoLib is not in this runtime", container.getModInfo().getVersion()));
    }
}
