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
    public static final Logger LOG = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);

    public NeepMeat(ModContainer container, Dist dist) {
        IEventBus bus = container.getEventBus();
        BuildingBlocks.bootstrap();
        NeepMachines.register(bus);
        MaterialItems.register(bus);
        BloodFluids.register(bus);
        com.neep.neepmeat.plc.block.entity.PLCBlockEntity.registerNetwork();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TABS.register(bus);
        if (dist.isClient()) {
            bus.addListener(NeepMeatClient::onBlockColors);
            bus.addListener(NeepMeatClient::onItemColors);
            bus.addListener(NeepMeatClient::onScreens);
            bus.addListener(NeepMeatClient::onClientExtensions);
            bus.addListener(NeepMeatClient::onRenderers);
            bus.addListener(NeepMeatClient::onExtraModels);
        }
        ModList.get().getModContainerById("geckolib").ifPresentOrElse(
                mod -> LOG.info("NeepMeat {} loaded with GeckoLib {}", container.getModInfo().getVersion(), mod.getModInfo().getVersion()),
                () -> LOG.info("NeepMeat {} loaded; GeckoLib is not in this runtime", container.getModInfo().getVersion()));
    }
}
