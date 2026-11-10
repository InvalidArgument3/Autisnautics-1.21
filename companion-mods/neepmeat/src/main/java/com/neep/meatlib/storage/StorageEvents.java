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

package com.neep.meatlib.storage;

public class StorageEvents
{
    public static void init()
    {
        // The map uses strong keys which are not cleared when the server dies, This means that Inventories that are
        // BlockEntities prevent GC of their worlds, which causes the accumulation of CCA components and WorldChunks
        // that they reference.
        // It's very naughty, but should fix the leak with no side effects.
//        ServerLifecycleEvents.SERVER_STOPPED.register(server ->
//        {
//            InventoryStorageAccessor.getWRAPPERS().clear();
//        });

        // I don't think it works outside dev
    }
}
