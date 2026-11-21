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
 * Descend, hold, and ascend use the animation times. An altar of {@code neepmeat:worm_altar}
 * with a thrown item and a player owner calls {@link #spawnFromAltar}.
 */

package com.neep.neepmeat.entity.skyworm;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.entity.NeepEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SkywormEntity extends Entity {
    private double startY;
    private double targetY;
    private int age;
    private SkywormAnimationTimes times = new SkywormAnimationTimes(1, 1, 1, 1);

    public SkywormEntity(EntityType<? extends SkywormEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public static final TagKey<net.minecraft.world.level.block.Block> ALTAR = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(NeepMeat.ID, "worm_altar"));

    /** Upstream discards the item, clears lava in the centre, and drops a worm from above. */
    public static boolean spawnFromAltar(ItemEntity entity, Level world, BlockPos pos, @Nullable Entity owner) {
        if (!(world instanceof net.minecraft.server.level.ServerLevel) || !(owner instanceof Player) || !altarReady(world, pos)) {
            return false;
        }
        entity.discard();
        if (world.getBlockState(pos).is(Blocks.LAVA)) {
            world.setBlockAndUpdate(pos, com.neep.neepmeat.block.RecipeBlocks.CONTAMINATED_RUBBLE.get().defaultBlockState());
        }
        Vec3 target = pos.getCenter().add(0, 4, 0);
        Vec3 start = target.add(0, 200, 0);
        SkywormEntity worm = setup(world, start);
        worm.begin(target.y, new SkywormAnimationTimes(40, 10, 40, 10));
        world.addFreshEntity(worm);
        return true;
    }

    private static boolean altarReady(Level world, BlockPos origin) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }
                if (!world.getBlockState(origin.offset(x, 0, z)).is(ALTAR)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static SkywormEntity setup(Level world, Vec3 position) {
        SkywormEntity worm = new SkywormEntity(NeepEntities.SKYWORM.get(), world);
        worm.setPos(position.x, position.y, position.z);
        worm.startY = position.y;
        return worm;
    }

    public void begin(double targetY, SkywormAnimationTimes times) {
        this.targetY = targetY;
        this.times = times;
        this.startY = getY();
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        if (age <= times.descend()) {
            double progress = age / (double) Math.max(1, times.descend());
            setPos(getX(), startY + (targetY - startY) * progress, getZ());
        } else if (age > times.total()) {
            discard();
        }
        setDeltaMovement(Vec3.ZERO);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}
}
