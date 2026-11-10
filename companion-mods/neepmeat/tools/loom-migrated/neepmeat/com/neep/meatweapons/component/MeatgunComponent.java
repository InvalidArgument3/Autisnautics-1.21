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

package com.neep.meatweapons.component;

import com.neep.meatweapons.client.meatgun.RecoilManager;
import com.neep.meatweapons.item.TriggerReceiver;
import com.neep.meatweapons.item.meatgun.MeatgunAnimationManager;
import com.neep.meatweapons.meatgun.MeatgunCore;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import dev.onyxstudios.cca.api.v3.component.Component;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface MeatgunComponent extends Component, TriggerReceiver
{
    RootModuleHolder getRootHolder();

    RootModuleHolder initUUID();

    void tickUUIDResolution(Player playerEntity);

    void commonTick(Player player);

    void clientTick(Player player);

    void markDirty();

    RecoilManager getRecoil();

    @Nullable MeatgunAnimationManager getAnimationManager();

    ItemStack getStack();

    UUID getUuid();

    MeatgunCore getCore();
}
