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
 * Beam particles and the riding player are not in this entity. Breaking uses the upstream delta.
 */

package com.neep.neepmeat.machine.phage_ray;

import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.TransactionContext;
import com.neep.neepmeat.entity.NeepEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PhageRayEntity extends Entity {
    public boolean externalTrigger;
    private boolean trigger;
    @Nullable
    private PhageRayBlockEntity parent;
    private int range = 8;
    private final Map<BlockPos, Float> targets = new HashMap<>();

    public PhageRayEntity(EntityType<? extends PhageRayEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public static PhageRayEntity create(Level level) {
        return new PhageRayEntity(NeepEntities.PHAGE_RAY.get(), level);
    }

    public void setParent(PhageRayBlockEntity parent) {
        this.parent = parent;
    }

    public void setRange(int range) {
        this.range = Math.max(1, range);
    }

    public Vec3 getBeamOrigin() {
        return position();
    }

    public Vec3 getBeamEnd() {
        return getBeamOrigin().add(getLookAngle().scale(range));
    }

    public boolean isRunning() {
        return parent != null && parent.canRun();
    }

    public void tickProcess(boolean canHarvest, boolean silkTouch, Storage<ItemVariant> output, TransactionContext transaction) {
        if (isRunning() && (trigger || externalTrigger)) {
            if (!level().isClientSide()) {
                breakBlocks(canHarvest, output, transaction);
            }
        }
    }

    private void breakBlocks(boolean canHarvest, Storage<ItemVariant> output, TransactionContext transaction) {
        if (level().getGameTime() % 2 == 0) {
            BlockHitResult hit = level().clip(new ClipContext(getBeamOrigin(), getBeamEnd(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            targets.keySet().removeIf(pos -> hit.getType() != HitResult.Type.BLOCK || !pos.equals(hit.getBlockPos()));
            if (hit.getType() == HitResult.Type.BLOCK) {
                targets.putIfAbsent(hit.getBlockPos(), 0f);
            }
        }
        ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
        Iterator<Map.Entry<BlockPos, Float>> iterator = targets.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Float> target = iterator.next();
            BlockPos pos = target.getKey();
            BlockState state = level().getBlockState(pos);
            if (target.getValue() >= 1f) {
                if (canHarvest && level() instanceof ServerLevel server) {
                    LootParams.Builder builder = new LootParams.Builder(server)
                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                            .withParameter(LootContextParams.TOOL, tool)
                            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, level().getBlockEntity(pos));
                    List<ItemStack> dropped = state.getDrops(builder);
                    for (ItemStack stack : dropped) {
                        output.insert(ItemVariant.of(stack), stack.getCount(), transaction);
                    }
                }
                level().destroyBlock(pos, false);
                iterator.remove();
            } else {
                target.setValue(target.getValue() + calcBlockBreakingDelta(state, level(), pos, 20f));
            }
        }
    }

    public static float calcBlockBreakingDelta(BlockState state, BlockGetter world, BlockPos pos, float speed) {
        float hardness = state.getDestroySpeed(world, pos);
        if (hardness < 0f) {
            return 0f;
        }
        return speed / Math.max(hardness, 0.01f) / 30f;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}
}
