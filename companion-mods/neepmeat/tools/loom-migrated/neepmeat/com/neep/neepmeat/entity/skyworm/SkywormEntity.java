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

package com.neep.neepmeat.entity.skyworm;

import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.entity.SimpleEntity;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.init.NMEntities;
import com.neep.neepmeat.network.ParticleSpawnS2C;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SkywormEntity extends SimpleEntity
{
    private SkywormAction<?> action;

    public static final EntityDataAccessor<Integer> BULGE = SynchedEntityData.defineId(SkywormEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> FROM_BELOW = SynchedEntityData.defineId(SkywormEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> ALLOW_WOBBLE = SynchedEntityData.defineId(SkywormEntity.class, EntityDataSerializers.BOOLEAN);

    public double targetY;

    public int bulgeDirection = 0;

    public static void spawnEvent(ItemEntity entity, Level world, BlockPos pos, @Nullable Entity owner)
    {
        if (world instanceof ServerLevel serverWorld
                && owner instanceof Player player
//                && player.getStackInHand(Hand.OFF_HAND).isOf(NMItems.PROJECTOR)
                )
        {
            if (!checkBlocks(world, pos))
                return;

            entity.discard();

            // Delete the lava
            if (world.getBlockState(pos).is(Blocks.LAVA))
                world.setBlockAndUpdate(pos, NMBlocks.CONTAMINATED_RUBBLE.defaultBlockState());

            Vec3 centrePos = Vec3.upFromBottomCenterOf(pos, 1);
            PlayerLookup.around(serverWorld, pos, 32).forEach(p ->
            {
                ParticleSpawnS2C.send(p, ParticleTypes.FLAME, centrePos, new Vec3(0, 2, 0), new Vec3(0.2, 2, 0.2), 10);
            });

//            for (
//            ParticleSpawnS2C.send(
//            if (world.getBlockState(pos.down()).isOf(Blocks.LAVA))
//                world.setBlockState(pos.down(), NMBlocks.CONTAMINATED_DIRT.getDefaultState());

            // Position the worm
            var worm = NMEntities.SKYWORM.create(world);
            Vec3 targetPosition = pos.getCenter().add(0, 4, 0);
            worm.setAction(new SkywormDropEggAction(worm, targetPosition.y));
            Vec3 position = targetPosition.add(0, 200, 0);
            worm.setPosRaw(position.x, position.y, position.z);
            worm.setPos(position.x, position.y, position.z);

            world.addFreshEntity(worm);
        }
    }

    public static SkywormEntity setup(Level world, Vec3 position)
    {
        var worm = NMEntities.SKYWORM.create(world);
        worm.setPosRaw(position.x, position.y, position.z);
        worm.setPos(position.x, position.y, position.z);

        return worm;
    }

    public void setAction(SkywormAction<?> action)
    {
        this.action = action;
    }

    private static boolean checkBlocks(Level world, BlockPos origin)
    {
        BlockPos.MutableBlockPos mutable = origin.mutable();
        for (int i = -1; i <= 1; ++i)
        {
            for (int j = -1; j <= 1; ++j)
            {
                if (i == 0 && j == 0)
                    continue;

                mutable.setWithOffset(origin, i, 0, j);
                if (!world.getBlockState(mutable).is(NMTags.WORM_ALTAR))
                    return false;
            }
        }

        return true;
    }

    public SkywormEntity(EntityType<? extends SkywormEntity> type, Level world)
    {
        super(type, world);
    }

    public void setTargetY(double y)
    {
        this.targetY = y;
    }

    @Override
    protected void defineSynchedData()
    {
        entityData.define(BULGE, -1);
        entityData.define(FROM_BELOW, false);
        entityData.define(ALLOW_WOBBLE, true);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {

    }

    @Override
    public void tick()
    {
        super.tick();

        if (action != null && !level().isClientSide())
            action.tick();

        if (this.isControlledByLocalInstance())
        {
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }

        interpolatePosition();

        tickBulge();
    }

    // I hate myself
    private void tickBulge()
    {
        if (tickCount % 2 == 0)
        {
            int bulge = getBulge();
            setBulge(Mth.clamp(bulge + bulgeDirection, -1, getLength()));
        }
    }

    public void setBulgeDirection(int direction)
    {
        this.bulgeDirection = direction;
    }

    @Override
    protected void onBelowWorld()
    {
        // Prevent the entity from getting deleted when it rises from below the world
    }

    // rawr x3 nuzzles owo whats this?
    public int getBulge()
    {
        return entityData.get(BULGE);
    }

    public void setBulge(int bulge)
    {
        entityData.set(BULGE, bulge);
    }

    public void setFromBelow(boolean fromBelow)
    {
        entityData.set(FROM_BELOW, fromBelow);
    }

    public boolean getFromBelow()
    {
        return entityData.get(FROM_BELOW);
    }

    public void setAllowWobble(boolean wobble)
    {
        entityData.set(ALLOW_WOBBLE, wobble);
    }

    @Override
    public boolean canCollideWith(Entity other)
    {
        return false;
    }

    @Override
    public boolean isColliding(BlockPos pos, BlockState state)
    {
        return false;
    }

    public int getLength()
    {
        return 50;
    }

    @Override
    public boolean shouldBeSaved()
    {
        return false;
    }

    public boolean allowWobble()
    {
        return entityData.get(ALLOW_WOBBLE);
    }

    public RandomSource getRandom()
    {
        return random;
    }
}
