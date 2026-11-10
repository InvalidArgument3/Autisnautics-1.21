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

package com.neep.neepmeat.implant.entity;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.datagen.tag.NMTags;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PipeClimbImplant implements EntityImplant
{
    public static final ResourceLocation ID = new ResourceLocation(NeepMeat.NAMESPACE, "pipe_climber");

    public static Optional<BlockPos> applyPipeClimb(Player entity)
    {
        Level world = entity.level();

        double offset = entity.getBbWidth() / 2;

        Vec3 forward = getTestPos(entity, offset);
        BlockPos forwardPos = BlockPos.containing(forward);

        BlockState forwardState = world.getBlockState(forwardPos);

        if (forwardState.is(NMTags.PIPE_CLIMBABLE))
        {
            return Optional.of(forwardPos);
        }

        return Optional.empty();
    }

    private static Vec3 getTestPos(Player entity, double offset)
    {
        Vec3 centrePos;

        // Pipes are not full blocks vertically, so the test position must be raised to avoid entering climb mode
        // when walking on pipes.
        if (entity.onGround())
            centrePos = new Vec3(entity.getX(), entity.getY() + 8 / 16f, entity.getZ());
        else
            centrePos = entity.position(); // Allow climbing the top edge when already airborne

        Direction facing = entity.getDirection();

        // Use the player's width to determine whether the relevant bounding box face is within the pipe block
        return centrePos.add(facing.getStepX() * offset, 0, facing.getStepZ() * offset);
    }

    public PipeClimbImplant(Entity entity)
    {

    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void tick(Runnable remove)
    {

    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        return nbt;
    }

    @Override
    public void readNbt(CompoundTag nbt)
    {

    }
}
