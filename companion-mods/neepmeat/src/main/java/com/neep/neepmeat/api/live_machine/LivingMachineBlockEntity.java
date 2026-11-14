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

package com.neep.neepmeat.api.live_machine;

import com.neep.meatlib.transfer.CombinedStorage;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.component.FluidInputComponent;
import com.neep.neepmeat.machine.live_machine.component.FluidOutputComponent;
import com.neep.neepmeat.machine.live_machine.component.ItemInputComponent;
import com.neep.neepmeat.machine.live_machine.component.ItemOutputComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Upstream controller base. The registered block entity still extends this class.
 * Combined storages are built from the components the structure has collected.
 */
public class LivingMachineBlockEntity extends BlockEntity implements ComponentHolder {
    public int inputSequence;
    private final RandomSource random = RandomSource.create();

    public LivingMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public float getPower() {
        return 0;
    }

    public float getProgressIncrement() {
        return 0;
    }

    public RandomSource getRandom() {
        return random;
    }

    public <T> List<T> components(ComponentType<?> type) {
        return List.of();
    }

    @Override
    public <T extends LivingMachineComponent> Collection<T> getComponent(ComponentType<T> type) {
        return components(type);
    }

    public Storage<ItemVariant> getCombinedItemInput() {
        List<Storage<ItemVariant>> parts = new ArrayList<>();
        for (ItemInputComponent input : this.<ItemInputComponent>components(LivingMachineComponents.ITEM_INPUT)) {
            parts.add(input.getStorage(null));
        }
        return new CombinedStorage<>(parts);
    }

    public Storage<ItemVariant> getCombinedItemOutput() {
        List<Storage<ItemVariant>> parts = new ArrayList<>();
        for (ItemOutputComponent output : this.<ItemOutputComponent>components(LivingMachineComponents.ITEM_OUTPUT)) {
            parts.add(output.getInternalStorage());
        }
        return new CombinedStorage<>(parts);
    }

    public Storage<FluidVariant> getCombinedFluidInput() {
        List<Storage<FluidVariant>> parts = new ArrayList<>();
        for (FluidInputComponent input : this.<FluidInputComponent>components(LivingMachineComponents.FLUID_INPUT)) {
            parts.add(input.getStorage(null));
        }
        return new CombinedStorage<>(parts);
    }

    public Storage<FluidVariant> getCombinedFluidOutput() {
        List<Storage<FluidVariant>> parts = new ArrayList<>();
        for (FluidOutputComponent output : this.<FluidOutputComponent>components(LivingMachineComponents.FLUID_OUTPUT)) {
            parts.add(output.getStorage(null));
        }
        return new CombinedStorage<>(parts);
    }
}
