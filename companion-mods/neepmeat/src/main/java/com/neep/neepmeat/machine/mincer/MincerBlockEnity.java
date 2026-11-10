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

package com.neep.neepmeat.machine.mincer;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.FluidPump;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.client.screen.tablet.graph.GraphData;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMDamageSources;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.machine.death_blades.DeathBladesBlockEntity;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.neepmeat.transport.fluid_network.node.AcceptorModes;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;

public class MincerBlockEnity extends SyncableBlockEntity implements MotorisedBlock, MotorisedBlock.DiagnosticsProvider
{
    private int damageTime;
    protected boolean running;
    protected float power = 0;

    @Nullable private LivingEntity captured;

    public static final float MIN_POWER = 0.1f;
    public static final float MAX_DAMAGE = 15;

    protected WritableSingleFluidStorage fluidStorage = new WritableSingleFluidStorage(2 * FluidConstants.BUCKET, this::setChanged)
    {
        @Override
        public boolean supportsInsertion()
        {
            return false;
        }
    };

    public MincerBlockEnity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public MincerBlockEnity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.MINCER, pos, state);
    }

    public void serverTick(Level world)
    {
        if (running)
            processEntity(world);
    }

    protected void processEntity(Level world)
    {
        damageTime = Math.max(0, damageTime - 1);

        AABB catchmentBox = new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 3.5, worldPosition.getZ() + 1);
        if (captured == null || !catchmentBox.contains(captured.position()))
        {
            // Capture a new entity
            List<LivingEntity> entities = world.getEntities(EntityTypeTest.forClass(LivingEntity.class), catchmentBox,
                    e -> e instanceof Mob || e instanceof Player);

            LivingEntity entity;
            if (!entities.isEmpty() && !(entity = entities.get(0)).isDeadOrDying())
                captured = entity;
        }
        else
        {
            if (world.getRandom().nextInt(9) == 1)
            {
                ((ServerLevel) world).sendParticles(ParticleTypes.LAVA, getBlockPos().getX() + 0.5, getBlockPos().getY() + 2, getBlockPos().getZ() + 0.5,
                        1, 0.1, 0, 0.2, 0.01);
            }

            captured.setPosRaw(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5 + getYOffset(captured) * 1.4, worldPosition.getZ() + 0.5);

            // This should rotate the entity, but it generally doesn't work, not sure why.
            captured.setYRot(Mth.wrapDegrees(captured.getYRot() + 10f));

            // Prevent the entity from shooting downwards and escaping/dying
            captured.fallDistance = 0;
            Vec3 vel = captured.getDeltaMovement();
            captured.setDeltaMovement(vel.x, 0, vel.z);

            // Damage entity
            if (damageTime == 0)
            {
                if (damageEntity((ServerLevel) world, captured))
                    captured = null;

                damageTime = 10;
            }
        }
    }

    // Returns true if the entity is dead.
    protected boolean damageEntity(ServerLevel world, LivingEntity entity)
    {
        var damageSource = world.damageSources().source(NMDamageSources.MINCER);

        float damageAmount = Mth.clamp(20 * power, 0, 15);
        if (entity.getHealth() <= damageAmount)
        {
            if (entity instanceof Player player)
                player.hurt(damageSource, Float.MAX_VALUE);
            else
                entity.discard();

            insertFluidFromEntity(this, entity);
            world.sendParticles(NMParticles.MEAT_FOUNTAIN, worldPosition.getX() + 0.5, worldPosition.getY() + 1 + 14 / 16f, worldPosition.getZ() + 0.5, 20, 0.4, 0.4, 0.4, 0.01);
            return true;
        }
        else
        {
            entity.hurt(damageSource, damageAmount);
            return false;
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    protected static void insertFluidFromEntity(MincerBlockEnity be, LivingEntity entity)
    {
        try (Transaction transaction = Transaction.openOuter())
        {
            be.getFluidStorage(null).insert(NMFluids.TISSUE_SLURRY.variant(), DeathBladesBlockEntity.getEntityAmount(entity), transaction);
            transaction.commit();
        }
    }

    public void clientTickRunning(Level world)
    {
        RandomSource random = world.random;
        if (random.nextFloat() < 0.1f)
        {
            for (int i = 0; i < random.nextInt(2) + 2; ++i)
            {
                world.addParticle(ParticleTypes.SMOKE, (double) worldPosition.getX() + 0.5 + random.nextDouble() / 4.0 * (double)(random.nextBoolean() ? 1 : -1), (double) worldPosition.getY() + 1 + 14 / 16f, (double)worldPosition.getZ() + 0.5 + random.nextDouble() / 4.0 * (double)(random.nextBoolean() ? 1 : -1), 0.0, 0.005, 0.0);
            }
        }
    }

    protected float getYOffset(LivingEntity entity)
    {
        return entity.getHealth() / entity.getMaxHealth();
    }

    public WritableSingleFluidStorage getFluidStorage(Direction direction)
    {
        return fluidStorage;
    }

    public FluidPump getFluidPump(Direction direction)
    {
        return FluidPump.of(-0.5f, () -> AcceptorModes.PUSH);
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putInt("damageTime", damageTime);
        nbt.putBoolean("running", running);
        nbt.putFloat("power", power);
        fluidStorage.toNbt(nbt);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.damageTime = nbt.getInt("damageTime");
        this.power = nbt.getFloat("power");
        this.running = nbt.getBoolean("running");
        fluidStorage.readNbt(nbt);
    }

    @Override
    public boolean motorTick(MotorEntity motor)
    {
        return false;
    }

    protected void updateBlockstate()
    {
        BlockState state = level.getBlockState(worldPosition);
        if (state.getValue(MincerBlock.RUNNING) != running)
        {
            level.setBlockAndUpdate(worldPosition, state.setValue(MincerBlock.RUNNING, running));
        }
    }

    @Override
    public void setInputPower(float power)
    {
        this.power = power;
        running = power >= MIN_POWER;
        updateBlockstate();
        sync();
    }

    @Override
    public void onMotorRemoved()
    {
        running = false;
        updateBlockstate();
    }

    @Override
    public Diagnostics getDiagnostics()
    {
        return Diagnostics.insufficientPower(!running, power, MIN_POWER);
    }

    public static GraphData makePowerGraph()
    {
        // Minimum and saturation power
        float minAbs = MincerBlockEnity.MIN_POWER * PowerUtil.BASE_POWER;

        // Time taken to impart referenceAbsEnergy
        float damage0 = 20 * (minAbs / PowerUtil.BASE_POWER);
        float damage1 = MincerBlockEnity.MAX_DAMAGE;

        float maxAbs = damage1 / 20 * PowerUtil.BASE_POWER;

        float x0 = 0, x3 = 1.2f * maxAbs, x1 = minAbs, x2 = maxAbs;
        float y1 = damage0, y2 = damage1;

        DecimalFormat df = new DecimalFormat("###.#");

        return new GraphData()
                .setTitle("Power to Damage")
                .setXRange(new GraphData.Range(x0, x3))
                .setYRange(new GraphData.Range(0, y2 * 1.2f))
                .setXTicks(List.of(
                        new GraphData.Tick(0, false),
                        new GraphData.Tick(x1, true),
                        new GraphData.Tick(x2, true)))
                .setYTicks(List.of(
                        new GraphData.Tick(y1, false),
                        new GraphData.Tick(y2, false)
                ))
                .setXTitle("Input power", "ej/t")
                .setYTitle("Damage every 10 ticks", "")
                .setPoints(new float[][]{
                        {0, 0},
                        {x1, 0},
                        {x1, y1},
                        {x2, y2},
                        {x3, y2} // Saturation
                })
                ;
    }
}
