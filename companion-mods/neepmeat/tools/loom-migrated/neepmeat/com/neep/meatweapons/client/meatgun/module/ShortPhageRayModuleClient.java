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

package com.neep.meatweapons.client.meatgun.module;

import com.neep.meatweapons.meatgun.module.ShortPhageRayModule;
import com.neep.meatweapons.mixin.client.ClientPlayerInteractionManagerAccessor;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class ShortPhageRayModuleClient
{
    public static void init()
    {
        ShortPhageRayModule.BREAK_PROGRESS_S2C.receiver(EnvType.CLIENT, (stop, pos, progress) ->
        {
            Minecraft client = Minecraft.getInstance();
            var accessor = (ClientPlayerInteractionManagerAccessor) client.gameMode;

            if (accessor != null)
            {
                if (stop)
                {
                    client.level.destroyBlockProgress(client.player.getId(), new BlockPos(-1, -1, -1), 0);
                }
                client.level.destroyBlockProgress(client.player.getId(), pos, (int) (progress * 10f));
            }
        });

//        ShortPhageRayModule.ON_BREAK_S2C.receiver(EnvType.CLIENT, (pos) ->
//        {
//            var client = MinecraftClient.getInstance();
//            client.world.breakBlock(pos, false);
//        });
    }
}
