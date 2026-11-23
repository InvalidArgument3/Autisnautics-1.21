/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NeoForge port. GPL-3.0-or-later headers, LGPL-3.0 LICENSE file. Not relicensed.
 * The entity standing on the block above is the mutation target. Minecarts are ignored.
 */

package com.neep.neepmeat.machine.surgery;

import com.neep.neepmeat.machine.StageMachines;
import com.neep.neepmeat.plc.component.MutateInPlace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class SurgeryPlatformBlockEntity extends BlockEntity {
    private final Mutate mutate = new Mutate();

    public SurgeryPlatformBlockEntity(BlockPos pos, BlockState state) {
        super(StageMachines.SURGERY_BE.get(), pos, state);
    }

    public MutateInPlace<Entity> mutate() {
        return mutate;
    }

    private final class Mutate implements MutateInPlace<Entity> {
        @Override
        public @Nullable Entity get() {
            Level world = getLevel();
            if (world == null) {
                return null;
            }
            AABB box = new AABB(getBlockPos().above());
            return world.getEntities(EntityTypeTest.forClass(Entity.class), box,
                    entity -> !entity.isSpectator() && !(entity instanceof AbstractMinecart))
                    .stream().findFirst().orElse(null);
        }

        @Override
        public void set(Entity value) {
        }
    }
}
