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

package com.neep.neepmeat.machine.reactor.block.entity;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.machine.reactor.*;
import com.neep.neepmeat.util.IterateRandomly;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ReactionCoreBlockEntity extends SyncableBlockEntity
{
    private final ReactionCoreParameters parameters = new ReactionCoreParameters();
    private final RandomSource random = RandomSource.create();
    private int age = 0;

    private final Object2IntMap<ReceiverOrganismStructure> structures = new Object2IntOpenHashMap<>();

    private BlockPos lastOrigin;

    public double lerpIncidentZoneRadius;
    public double clientIncidentZoneRadius;

    private final Object2FloatMap<ReceiverOrganismStructure.Property> properties = new Object2FloatArrayMap<>();

    private final Set<ReceiverOrganismComponent> components = new HashSet<>();
    @Nullable private CoreSensorBlockEntity sensor;

    // To be set to zero after use.
    private int disruption;
    private float maxOrgIncrease;

    public ReactionCoreBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.lastOrigin = pos;
    }

    public void serverTick()
    {
        ++age;

        if (age % 80 == 0)
        {
            updateStructure(getBlockPos());
        }

        int dt = 5;

        if (level.getGameTime() % dt == 0)
        {
            parameters.tick(disruption, properties.getOrDefault(ReceiverOrganismStructure.Property.ORGANISATION, 0), maxOrgIncrease, 5);
        }
        disruption = 0;
        maxOrgIncrease = 0;

        if (level.getGameTime() % 10 == 0)
        {
            double stored = parameters.getStoredExudate();

            float factor = 1;

            // Convert amount to blocks
            int toPlace = (int) Math.floor(stored * factor);

            int placed = placeExudate(toPlace);

            // Convert blocks to amount
            double toExtract = placed / factor;

            parameters.extractStored(toExtract);
            sync();
        }

        components.removeIf(ReceiverOrganismComponent::isComponentRemoved);

        parameters.tickIncidentZone();

        if (level.getGameTime() % dt == 0 && sensor != null)
        {
            parameters.emitSensorData(sensor);
        }
    }

    private void updateStructure(BlockPos origin)
    {
        List<ReceiverOrganismStructure> structures = new ObjectArrayList<>();

        findStructures(level, origin, structures, components);

        EnumMap<ReceiverOrganismStructure.Property, Integer> present = new EnumMap<>(ReceiverOrganismStructure.Property.class);
        for (var structure : structures)
        {
            structure.getProperties().forEach((property, value) ->
                    {
                        if (value.function().average())
                            present.compute(property, (p, v) -> v == null ? 1 : v + 1);
                    });
        }

        properties.clear();
        for (var structure : structures)
        {
            structure.getProperties().forEach((property, entry) ->
            {
                int numberPresent = present.getOrDefault(property, 1);
                properties.compute(property, (p, prev) ->
                {
                    if (prev == null)
                        prev = p.defaultValue();

                    return entry.apply(prev, numberPresent);
                });
            });
        }

        for (var component : components)
        {
            if (component instanceof CoreSensorBlockEntity sensor1)
            {
                this.sensor = sensor1;
            }
        }
    }

    private void findStructures(Level world, BlockPos origin, List<ReceiverOrganismStructure> structures, Set<ReceiverOrganismComponent> components)
    {
        LongSet visited = new LongOpenHashSet();
        Queue<BlockPos> queue = new ArrayDeque<>();

        queue.add(origin);
        visited.add(origin.asLong());

        while (!queue.isEmpty())
        {
            BlockPos current = queue.poll();

            BlockPos.MutableBlockPos mutable = current.mutable();
            for (Direction direction : Direction.values())
            {
                mutable.setWithOffset(current, direction);

                if (!visited.contains(mutable.asLong()))
                {
                    visited.add(mutable.asLong());

                    BlockState nextState = world.getBlockState(mutable);

                    if (nextState.getBlock() instanceof ReceiverOrganismStructure structure)
                    {
                        structures.add(structure);
                        queue.add(mutable.immutable());
                    }
                    else if (nextState.getBlock() instanceof ReceiverOrganismComponentProvider provider)
                    {
                        var component = provider.get(world, mutable, nextState);
                        if (component != null)
                            components.add(component);
                    }
                }
            }
        }
    }

    private int placeExudate(int toPlace)
    {
        int placed = placeExudateInIncidentZone(level, toPlace);

        if (placed < toPlace)
        {
            placed += extrudeExudate(level, toPlace - placed);
        }

        return placed;
    }

    // Randomly replaces blocks in the incident zone
    private int placeExudateInIncidentZone(Level world, int toPlace)
    {
        int placed = 0;
        for (int i = 0; i < toPlace; ++i)
        {
            BlockPos randomPos = randomPosInSphere(worldPosition, (float) parameters.getIncidentZoneRadius());

            BlockState prevState = world.getBlockState(randomPos);
            // TODO: tag
            if (canReplace(prevState) && (prevState.canBeReplaced() || random.nextBoolean()))
            {
                world.setBlockAndUpdate(randomPos, IntrusionReactor.ACTIVE_WASTE.defaultBlockState());
                ++placed;
            }
        }

        return placed;
    }

    private BlockPos randomPosInSphere(BlockPos origin, float radius)
    {
        // Random vector
        float rx = random.nextFloat() - 0.5f;
        float ry = random.nextFloat() - 0.5f;
        float rz = random.nextFloat() - 0.5f;

        float inverse = Mth.invSqrt(rx * rx + ry * ry + rz * rz);

        // Random length within incident zone
        float rl = inverse * random.nextFloat() * radius;

        // Normalise and extend to new length
        int x = origin.getX() + Math.round(rx * rl);
        int y = origin.getY() + Math.round(ry * rl);
        int z = origin.getZ() + Math.round(rz * rl);

        return new BlockPos(x, y, z);
    }

    // Uses a BFS to place exudate blocks in air spaces
    private int extrudeExudate(Level world, int toPlace)
    {
        List<BlockPos> potential = traverse(world, lastOrigin, 800, toPlace);

        int canPlace = Math.min(potential.size(), toPlace);
        for (int i = 0; i < canPlace; ++i)
        {
            BlockPos pos = potential.get(potential.size() - i - 1);

            world.setBlockAndUpdate(pos, IntrusionReactor.ACTIVE_WASTE.defaultBlockState());
        }

        if (canPlace == 0)
        {
            lastOrigin = worldPosition;
        }
        else
        {
            if (!lastOrigin.equals(worldPosition))
                lastOrigin = worldPosition;
            else
                lastOrigin = potential.get(potential.size() - 1);
        }

        return canPlace;
    }

    private List<BlockPos> traverse(Level world, BlockPos origin, int maxVisit, int maxToPlace)
    {
        // Climb down more?
        maxToPlace *= 2;

        Direction[] horDirections = new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP};

        LongSet visited = new LongOpenHashSet();
        Queue<BlockPos> queue = new ArrayDeque<>();
        List<BlockPos> positions = new ArrayList<>();

        if (!isValidBlock(world.getBlockState(origin)))
            return List.of();

        visited.add(origin.asLong());
        queue.add(origin);

        int blocksVisited = 0;
        while (!queue.isEmpty()
                && blocksVisited < maxVisit
                && positions.size() < maxToPlace
        )
        {
            BlockPos current = queue.poll();

            BlockPos.MutableBlockPos mutable = current.mutable();
            for (int i : new IterateRandomly(horDirections.length))
            {
                Direction direction = horDirections[i];

                mutable.setWithOffset(current, direction);
                if (!visited.contains(mutable.asLong()))
                {
                    visited.add(mutable.asLong());

                    BlockState offsetState = world.getBlockState(mutable);
//                    if (direction != Direction.UP && offsetState.isAir())
                    if (offsetState.isAir())
                    {
                        positions.add(mutable.immutable());
                    }
                    else if (isValidBlock(offsetState))
                    {
                        blocksVisited++;
                        queue.add(mutable.immutable());
                    }
                }
            }
        }
        return positions;
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putInt("age", age);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.age = nbt.getInt("age");
    }

    @Override
    public void toClientTag(CompoundTag nbt)
    {
        super.toClientTag(nbt);
        nbt.putDouble("incident_zone_radius", parameters.getIncidentZoneRadius());
    }

    @Override
    public void fromClientTag(CompoundTag nbt)
    {
        super.fromClientTag(nbt);
        this.clientIncidentZoneRadius = nbt.getDouble("incident_zone_radius");
    }

    private boolean isValidBlock(BlockState blockState)
    {
        return blockState.is(IntrusionReactor.ACTIVE_WASTE)
                || blockState.is(IntrusionReactor.REACTION_CORE)
                || blockState.getBlock() instanceof ReceiverOrganismStructure
                ;
    }

    private boolean canReplace(BlockState blockState)
    {
        return !isValidBlock(blockState)
                && !blockState.is(IntrusionReactor.CORE_SENSOR)
                && !blockState.is(NMBlocks.DATA_CABLE)
                ;
    }

    public void setDisruptorParams(int disruption, float maxOrgIncrease)
    {
        this.disruption = disruption;
        this.maxOrgIncrease = maxOrgIncrease;
    }
}
