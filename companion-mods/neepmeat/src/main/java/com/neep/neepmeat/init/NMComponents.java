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
 *
 * Field names and ids match upstream. Values are filled by the existing DeferredRegisters, not a second registration.
 */

package com.neep.neepmeat.init;

import com.neep.neepmeat.component.SyncedComponents;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;

public final class NMComponents {
    public static final ComponentKey<?> IMPLANT_MANAGER = SyncedComponents.key("implant_manager");
    public static final ComponentKey<?> WORKPIECE = SyncedComponents.key("workpiece");
    public static final ComponentKey<?> ENLIGHTENMENT_MANAGER = SyncedComponents.key("enlightenment_manager");
    public static final ComponentKey<?> COMPRESSED_AIR = SyncedComponents.key("compressed_air");
    public static final ComponentKey<?> CHUNK_ENLIGHTENMENT = SyncedComponents.key("chunk_enlightenment");
    public static final ComponentKey<?> PRESSURE_WASHER = SyncedComponents.key("pressure_washer");
    public static final ComponentKey<?> PROGRAM_STORAGE = SyncedComponents.key("program_storage");
    public static final ComponentKey<?> PROCESS_STORAGE = SyncedComponents.key("process_storage");
    public static final ComponentKey<?> UNIQUE_ID = SyncedComponents.key("unique_id");
    public static final ComponentKey<?> DISTRIBUTOR = SyncedComponents.key("distributor");
    private NMComponents() {}
}
