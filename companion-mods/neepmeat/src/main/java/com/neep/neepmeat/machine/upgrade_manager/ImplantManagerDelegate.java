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

package com.neep.neepmeat.machine.upgrade_manager;

import com.neep.neepmeat.implant.entity.ImplantManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public interface ImplantManagerDelegate
{
    @Nullable ImplantManager getImplantManager();

    @Nullable
    ItemLike removeUpgrade(ResourceLocation identifier);

//    record SimpleImplantManagerDelegate(ImplantManager manager) implements ImplantManagerDelegate
//    {
//        @Override
//        public @Nullable ImplantManager getImplantManager()
//        {
//            return manager;
//        }
//
//        @Override
//        public @Nullable ItemConvertible removeUpgrade(Identifier identifier)
//        {
//            if (manager.removeImplant(identifier))
//            {
//                return ImplantRegistry.getItem(identifier);
//            }
//            return null;
//        }
//    }
//
//    ImplantManagerDelegate EMPTY = new ImplantManagerDelegate()
//    {
//        @Override
//        public @Nullable ImplantManager getImplantManager()
//        {
//            return null;
//        }
//
//        @Override
//        public @Nullable ItemConvertible removeUpgrade(Identifier identifier)
//        {
//            return null;
//        }
//    };
}
