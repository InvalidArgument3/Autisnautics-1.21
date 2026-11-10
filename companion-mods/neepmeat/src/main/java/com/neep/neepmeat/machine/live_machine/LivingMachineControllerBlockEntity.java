/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * Flood fill matches upstream: structure blocks and components are collected from
 * every face, a second controller clears the result, and the search stops at 128 blocks.
 */

package com.neep.neepmeat.machine.live_machine;

import com.neep.neepmeat.api.live_machine.ComponentMap;
import com.neep.neepmeat.api.live_machine.LivingMachineBlock;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.api.live_machine.LivingMachineStructure;
import com.neep.neepmeat.api.live_machine.PropertyValue;
import com.neep.neepmeat.api.live_machine.StructureProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class LivingMachineControllerBlockEntity extends BlockEntity {
    public static final int MAX_BLOCKS = 128;

    private final List<StructureEntry> structures = new ArrayList<>();
    private final EnumMap<StructureProperty, Float> properties = new EnumMap<>(StructureProperty.class);
    private ComponentMap components = new ComponentMap();
    private boolean formed;

    public LivingMachineControllerBlockEntity(BlockPos pos, BlockState state) {
        super(LiveMachines.CONTROLLER_BE.get(), pos, state);
    }

    public void serverTick() {
        updateStructure();
    }

    public void updateStructure() {
        List<StructureEntry> found = new ArrayList<>();
        ComponentMap next = new ComponentMap();
        if (level != null) {
            search(getBlockPos(), found, next);
        }
        structures.clear();
        structures.addAll(found);
        components = next;
        aggregate();
        formed = !structures.isEmpty() || components.bits.cardinality() > 0;
    }

    public boolean formed() {
        return formed;
    }

    public int structureCount() {
        return structures.size();
    }

    public float property(StructureProperty property) {
        return properties.getOrDefault(property, property.defaultValue());
    }

    public int componentCount(com.neep.neepmeat.api.live_machine.ComponentType<?> type) {
        return components.get(type.getBitIdx()).size();
    }

    private void search(BlockPos start, List<StructureEntry> found, ComponentMap next) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos current = queue.remove();
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (!visited.add(neighbor)) {
                    continue;
                }
                BlockState state = level.getBlockState(neighbor);
                if (state.isAir()) {
                    continue;
                }
                if (found.size() >= MAX_BLOCKS) {
                    return;
                }
                if (state.getBlock() instanceof LivingMachineBlock) {
                    found.clear();
                    next.bits.clear();
                    return;
                }
                if (state.getBlock() instanceof LivingMachineStructure structure) {
                    found.add(new StructureEntry(structure, neighbor));
                    queue.add(neighbor);
                } else {
                    LivingMachineComponent component = LivingMachineComponent.find(level, neighbor);
                    if (component != null) {
                        component.setController(start);
                        next.add(component);
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    private void aggregate() {
        EnumMap<StructureProperty, Integer> present = new EnumMap<>(StructureProperty.class);
        for (StructureEntry entry : structures) {
            entry.structure().getProperties().forEach((property, value) -> {
                if (value.function().average()) {
                    present.merge(property, 1, Integer::sum);
                }
            });
        }
        properties.clear();
        for (StructureEntry entry : structures) {
            entry.structure().getProperties().forEach((property, value) -> {
                int count = present.getOrDefault(property, 1);
                properties.compute(property, (key, previous) -> {
                    float current = previous == null ? key.defaultValue() : previous;
                    return value.apply(current, count);
                });
            });
        }
    }

    public record StructureEntry(LivingMachineStructure structure, BlockPos pos) {}
}
