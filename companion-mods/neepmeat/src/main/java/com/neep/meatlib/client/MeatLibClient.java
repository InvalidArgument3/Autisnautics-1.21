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

package com.neep.meatlib.client;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.client.network.screen.ScreenPacketClient;
import com.neep.meatlib.client.screen.EMIScreenSupport;
import com.neep.meatlib.client.screen.REIScreenSupport;
import com.neep.meatlib.graphics.client.GraphicsEffectClient;
import com.neep.meatlib.network.BlockEntitySync;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.meatlib.util.PlayerVelocity;
import com.neep.meatlib.util.ValkyrienSkiesUtil;
import net.fabricmc.api.ClientModInitializer;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

public class MeatLibClient implements ClientModInitializer
{
    @Nullable
    public static EMIScreenSupport EMI_UTIL = null;
    @Nullable public static REIScreenSupport REI_UTIL = null;

    @Override
    public void onInitializeClient()
    {
        if (MeatLib.vsUtil != null)
            MeatLib.vsUtil.CLIENT = new ValkyrienSkiesUtil.Client();

        MeatlibRecipes.initClient();
        GraphicsEffectClient.init();
        ScreenPacketClient.init();
        BlockEntitySync.Client.init();
        ScrollableItemsClient.init();
        PlayerVelocity.Client.init();

        if (ModList.get().isLoaded("emi"))
        {
            EMI_UTIL = new EMIScreenSupport();
        }

        if (ModList.get().isLoaded("roughlyenoughitems"))
        {
            REI_UTIL = new REIScreenSupport();
        }

    }
}
