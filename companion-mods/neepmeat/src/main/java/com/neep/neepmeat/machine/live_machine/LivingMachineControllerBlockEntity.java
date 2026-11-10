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

import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.api.live_machine.ComponentMap;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.DegradationManager;
import com.neep.neepmeat.api.live_machine.LivingMachineBlock;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.api.live_machine.LivingMachineStructure;
import com.neep.neepmeat.api.live_machine.Process;
import com.neep.neepmeat.api.live_machine.PropertyValue;
import com.neep.neepmeat.api.live_machine.StructureProperty;
import com.neep.neepmeat.entity.MachineWormEntity;
import com.neep.neepmeat.entity.NeepEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
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
    private final DegradationManager degradation = new DegradationManager(this::degradationRate);
    private boolean formed;
    private float power;
    private long age;
    private @Nullable Process process;

    public LivingMachineControllerBlockEntity(BlockPos pos, BlockState state) {
        super(LiveMachines.CONTROLLER_BE.get(), pos, state);
    }

    public void serverTick() {
        if (age % 80 == 0) {
            updateStructure();
            selectProcess();
        }
        age++;
        float nextPower = 0;
        for (MotorPortBlockEntity port : this.<MotorPortBlockEntity>components(LivingMachineComponents.MOTOR_PORT)) {
            nextPower = Math.max(nextPower, port.getInputSignal());
        }
        power = nextPower;
        degradation.tick();
        if (process != null) {
            process.serverTick(this);
        }
        if (getHealth() <= 0) {
            becomeWorm();
        }
    }

    public float power() {
        return power;
    }

    public float getHealth() {
        return 1 - degradation.getDegradation();
    }

    public void readDegradation(CompoundTag tag) {
        degradation.read(tag);
    }

    public @Nullable Process process() {
        return process;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> components(ComponentType<?> type) {
        return new ArrayList<>((Collection<T>) components.get(type.getBitIdx()));
    }

    private void selectProcess() {
        Processes.init();
        Process next = Processes.getInstance().getRecipeFor(components.bits);
        if (next != process) {
            if (process != null) {
                process.onStop(this);
            }
            process = next;
            if (next != null) {
                next.onStart(this);
            }
        }
    }

    /**
     * Upstream rate. Rated power is max power divided by the 1000 reference.
     * At or below 75 percent of rated power the rate is zero. Above rated it climbs.
     */
    private double degradationRate(double ignored) {
        float rated = property(StructureProperty.MAX_POWER) / PowerUtil.BASE_POWER;
        if (power <= 0 || rated <= 0) {
            return 0;
        }
        if (power <= rated * 0.75f) {
            return 0;
        }
        if (power > rated * 2) {
            return 0.00001f * power / rated;
        }
        if (power > rated) {
            return 0.000007f * power / rated;
        }
        return 0.000003f;
    }

    private void becomeWorm() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        MachineWormEntity worm = NeepEntities.MACHINE_WORM.get().create(server);
        if (worm == null) {
            return;
        }
        Vec3 position = Vec3.atCenterOf(getBlockPos());
        worm.moveTo(position.x, position.y, position.z, 0, 0);
        server.addFreshEntity(worm);
        server.setBlock(getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        degradation.write(tag);
        tag.putLong("age", age);
        tag.putFloat("power", power);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        degradation.read(tag);
        age = tag.getLong("age");
        power = tag.getFloat("power");
    }

    public record StructureEntry(LivingMachineStructure structure, BlockPos pos) {}
}
