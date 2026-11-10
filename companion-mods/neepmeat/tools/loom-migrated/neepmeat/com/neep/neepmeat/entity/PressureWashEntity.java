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

package com.neep.neepmeat.entity;

import com.neep.neepmeat.api.block.PressureWashable;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.network.NMTrackedData;
import com.neep.neepmeat.particle.PressureWashParticleType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import java.util.HashSet;
import java.util.Set;

// Should simulate the water jet glancing off surfaces for more satisfying effects
public class PressureWashEntity extends SimpleProjectileEntity
{
    private static final EntityDataAccessor<Integer> COLOUR = SynchedEntityData.defineId(PressureWashEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Fluid> FLUID = SynchedEntityData.defineId(PressureWashEntity.class, NMTrackedData.FLUID);
    private final Set<BlockPos> visited = new HashSet<>();
    private int power;
    private FluidVariant fluidVariant = FluidVariant.blank();

    public PressureWashEntity(EntityType<? extends PressureWashEntity> type, Level world)
    {
        super(type, world);
    }

    public void setPower(int power, FluidVariant fluidVariant)
    {
        this.power = power;
        this.fluidVariant = fluidVariant;
        entityData.set(FLUID, fluidVariant.getFluid());
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        entityData.define(COLOUR, 0xffffff);
        entityData.define(FLUID, Fluids.EMPTY);
    }

    protected void processBlock(BlockPos pos, BlockState state)
    {
        if (!level().isClientSide() && power > 0)
        {
            if (visited.contains(pos))
                return;

            visited.add(pos);

            var washable = PressureWashable.get(state);

            if (washable != null && mayInteract(level(), pos))
            {
                int col = washable.tint();
                if (col != -1)
                    entityData.set(COLOUR, col);

                int removed = washable.wash(level(), pos, state, getDeltaMovement(), power, fluidVariant);
                power = Math.max(0, power - removed);
            }

            if (power <= 0)
                splashRemove();
        }
    }

    @Override
    protected void onInsideBlock(BlockState state)
    {
        super.onInsideBlock(state);

//        BlockPos blockPos = getBlockPos();
//
//        // This method will be executed for many blocks, sometimes repeatedly for the same block in the same tick
//        // The check allows the entity to break multiple blocks
//        if (getWorld().getBlockState(blockPos) == state)
//        {
//            processBlock(blockPos, state);
//        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult)
    {
        super.onHitEntity(entityHitResult);
        if (!level().isClientSide())
        {
            var owner = getOwner();
            Entity entity = entityHitResult.getEntity();
            entity.hurt(this.damageSources().mobProjectile(this, owner instanceof LivingEntity livingEntity ? livingEntity : null),
                    1F);
            if (FluidVariantAttributes.getTemperature(fluidVariant) >= FluidConstants.LAVA_TEMPERATURE)
            {
                setOnFireFor(entity, 40);
            }
        }
    }

    public static void setOnFireFor(Entity entity, int ticks)
    {
        if (entity instanceof LivingEntity livingEntity)
        {
            ticks = ProtectionEnchantment.getFireAfterDampener(livingEntity, ticks);
        }

        if (entity.getRemainingFireTicks() < ticks)
            entity.setRemainingFireTicks(ticks);
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult)
    {
        super.onHitBlock(blockHitResult);

//        processBlock(blockHitResult.getBlockPos(), getWorld().getBlockState(blockHitResult.getBlockPos()));
        if (FluidVariantAttributes.getTemperature(FluidVariant.of(entityData.get(FLUID))) >= 1000
                && level().isClientSide())
        {
            level().addParticle(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(),
                    random.triangle(0, 0.1),
                    random.triangle(0, 0.1),
                    random.triangle(0, 0.1));
        }
    }

    protected void applyToNearbyBlocks()
    {
        AABB box = this.getBoundingBox();
        double d = 0.1;
        BlockPos blockPos = BlockPos.containing(box.minX - d, box.minY - d, box.minZ - d);
        BlockPos blockPos2 = BlockPos.containing(box.maxX + d, box.maxY + d, box.maxZ + d);

        if (this.level().hasChunksAt(blockPos, blockPos2))
        {
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            for (int i = blockPos.getX(); i <= blockPos2.getX(); i++)
            {
                for (int j = blockPos.getY(); j <= blockPos2.getY(); j++)
                {
                    for (int k = blockPos.getZ(); k <= blockPos2.getZ(); k++)
                    {
                        mutable.set(i, j, k);
                        BlockState blockState = this.level().getBlockState(mutable);
                        processBlock(mutable, blockState);
                    }
                }
            }
        }
    }

    @Override
    public void tick()
    {
        super.tick();

        var vel = getDeltaMovement();
        if (level().isClientSide())
        {
            int col = entityData.get(COLOUR);
            var fluid = entityData.get(FLUID);

            level().addParticle(
                    new PressureWashParticleType.Parameters(NMParticles.PRESSURE_WASH_BEAM, 0, col, fluid), getX(), getY(), getZ(),
                    vel.x + random.triangle(0, 0.1),
                    vel.y + random.triangle(0, 0.1),
                    vel.z + random.triangle(0, 0.1)
            );
        }

        if (onGround())
        {
            setDeltaMovement(getDeltaMovement().scale(0.8));
        }

        if (tickCount > 40)
            discard();

        if (vel.length() < 0.05)
        {
            splashRemove();
        }

        applyToNearbyBlocks();
    }

    private void splashRemove()
    {
        if (level() instanceof ServerLevel serverWorld)
        {
//            if (fluidVariant.isOf(Fluids.WATER))
//                serverWorld.spawnParticles(ParticleTypes.SPLASH, getX(), getY(), getZ(), 10, 0.6, 0.6, 0.6, 0.1);
//            else if (fluidVariant.isOf(Fluids.LAVA))

//            serverWorld.spawnParticles(ParticleTypes.FALLING_LAVA, getX(), getY(), getZ(), 10, 0.6, 0.6, 0.6, 0.1);
            serverWorld.sendParticles(new PressureWashParticleType.Parameters(NMParticles.FLUID_DRIP, 1, 0xffffff, fluidVariant.getFluid()), getX(), getY(), getZ(), 10, 0.6, 0.6, 0.6, 0.1);
        }
        discard();
    }

    @Override
    protected double getGravity()
    {
        return 0.04;
    }

    @Override
    public boolean isNoGravity()
    {
        return false;
    }
}
