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

package com.neep.meatlib;

import com.mojang.blaze3d.systems.RenderSystem;
import com.neep.meatlib.api.event.DataPackPostProcess;
import com.neep.meatlib.api.event.InitialTicks;
import com.neep.meatlib.config.MeatlibConfig;
import com.neep.meatlib.graphics.GraphicsEffects;
import com.neep.meatlib.init.MeatlibSounds;
import com.neep.meatlib.item.ScrollableItems;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.meatlib.recipe.ingredient.RecipeInputs;
import com.neep.meatlib.recipe.ingredient.RecipeOutputs;
import com.neep.meatlib.screen.ScreenReady;
import com.neep.meatlib.storage.StorageEvents;
import com.neep.meatlib.util.MeatlibItemGroups;
import com.neep.meatlib.util.MinecraftServerAccess;
import com.neep.meatlib.util.ValkyrienSkiesUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class MeatLib implements ModInitializer
{
    public static final String NAMESPACE = "meatlib";
    public static final Logger LOGGER = LogManager.getLogger(NAMESPACE);
    public static final MeatlibConfig CONFIG = MeatlibConfig.createToml(FabricLoader.getInstance().getConfigDir(), "neep", NAMESPACE, MeatlibConfig.class);

    public static final String FLOAT_REGEX = "[+-]?([0-9]*[.])?[0-9]+";
    public static final String[] METRIC_PREFIXES = new String[]{"f", "p", "n", "µ", "m", "", "k", "M", "G", "T"};

    /**
     * This should remain null unless VS2 is loaded.
     */
    @Nullable public static ValkyrienSkiesUtil vsUtil = null;

    public static BlockApiLookup<Void, Void> VOID_LOOKUP =
            BlockApiLookup.get(new ResourceLocation(NAMESPACE, "sided_void"), Void.class, Void.class);

    public static String formatEngineering(double value, int dp, boolean usePrefix, String unit)
    {
        if (value == 0)
            return String.format("%." + dp + "f%s", 0.0, unit);

        double log10 = Math.log10(Math.abs(value));

        int threes = (int) Math.floor(log10 / 3);

        value /= Math.pow(10, threes * 3);

        int idx = threes + 5;
        if (usePrefix && idx > 0 && idx < METRIC_PREFIXES.length)
        {
            return String.format("%." + dp + "f%s%s", value, METRIC_PREFIXES[idx], unit);
        }
        else
        {
            return String.format("%." + dp + "fE%d%s", value, threes * 3, unit);

        }
    }

    @Override
    public void onInitialize()
    {
        MinecraftServerAccess.init();

        GraphicsEffects.init();
//        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(MeatRecipeManager.getInstance());
        MeatlibRecipes.init();
        InitialTicks.init();
        StorageEvents.init();
        RecipeInputs.init();
        MeatlibItemGroups.init();
        ScrollableItems.init();
        RecipeOutputs.init();
        MeatlibSounds.init();

        // Things that need to access the server's data resources
        ServerLifecycleEvents.SERVER_STARTED.register(server -> DataPackPostProcess.AFTER_DATA_PACK_LOAD.invoker().event(server));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, manager, success) -> DataPackPostProcess.AFTER_DATA_PACK_LOAD.invoker().event(server));

        // Synchronise after data packs are loaded/reloaded for every player on the server
        DataPackPostProcess.AFTER_DATA_PACK_LOAD.register(DataPackPostProcess.END, server ->
                DataPackPostProcess.SYNC.invoker().sync(server, new HashSet<>(server.getPlayerList().getPlayers())));

        if (FabricLoader.getInstance().isModLoaded("valkyrienskies"))
        {
            vsUtil = new ValkyrienSkiesUtil();
        }

        MeatLibRegistration.flush();

        ScreenReady.init();
    }

//    public static class Context implements AutoCloseable
//    {
//        private static boolean ACTIVE;
//
//        protected Context(String namespace)
//        {
//            if (ACTIVE) throw new IllegalStateException("Meatlib: " + namespace + " attempted to get context while it belongs to " + CURRENT_NAMESPACE);
//
//            CURRENT_NAMESPACE = namespace;
//            ACTIVE = true;
//        }
//
//        @Override
//        public void close()
//        {
//            BlockRegistry.flush();
//            ItemRegistry.flush();
//
//            CURRENT_NAMESPACE = null;
//            ACTIVE = false;
//        }
//    }

    /**
     * A mildly horrible way of checking environment. Use as a last resort or not at all.
     */
    public static boolean isClient()
    {
        // On a dedicated server, the first check will return false without trying to load RenderSystem.
        // On an integrated server, the first check will be true, but it will be safe to load RenderSystem.
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && RenderSystem.isOnRenderThread();
    }
}
