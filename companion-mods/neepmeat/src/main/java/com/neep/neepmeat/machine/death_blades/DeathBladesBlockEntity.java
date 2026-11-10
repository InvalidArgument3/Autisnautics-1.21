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

package com.neep.neepmeat.machine.death_blades;

import net.minecraft.core.HolderLookup;

import com.neep.meatweapons.init.MWSounds;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.block.entity.MotorisedMachineBlockEntity;
import com.neep.neepmeat.client.screen.tablet.graph.GraphData;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMDamageSources;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.machine.mincer.MincerBlockEnity;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.neepmeat.machine.synthesiser.MobSynthesisRegistry;
import com.neep.neepmeat.transport.block.fluid_transport.entity.FluidDrainBlockEntity;
import com.neep.neepmeat.util.NMMaths;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.math.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeathBladesBlockEntity extends MotorisedMachineBlockEntity
{
    protected float angularSpeed;
    protected float angle;
    protected float clientAngle;

    private final Set<Entity> currentlyHitting = new HashSet<>();

    public static final float MIN_POWER = 0.2f;
    public static final float MAX_DAMAGE = MincerBlockEnity.MAX_DAMAGE;

    public DeathBladesBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state, MIN_POWER, 1.5f);
    }

    public DeathBladesBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.DEATH_BLADES, pos, state);
    }

    @Override
    public boolean motorTick(MotorEntity motor)
    {
        this.angle = Mth.wrapDegrees(this.angle + angularSpeed);

        if (level.getGameTime() % 2 == 0 && angularSpeed > 0)
        {
            AABB box = new AABB(0, 0, 0, 0, 0, 0);
            Direction facing = getBlockState().getValue(DeathBladesBlock.FACING);
            switch (facing)
            {
                case UP, DOWN -> box = new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 1, 2));
                case NORTH, SOUTH -> box = new AABB(worldPosition.offset(-1, -1, 0), worldPosition.offset(2, 2, 1));
                case EAST, WEST -> box = new AABB(worldPosition.offset(0, -1, -1), worldPosition.offset(1, 2, 2));
            }

            float v = Mth.sin(angle * Mth.DEG_TO_RAD);
            float u = Mth.cos(angle * Mth.DEG_TO_RAD);
            float r = 1.5f;

            Vec3 rayStart;
            Vec3 rayEnd;
            Vec3 centre = worldPosition.getCenter();

            // Here is some absolutely cursed code that I'm not going to improve because of how cursed it is.
            // Java will consider rayStart and rayEnd to be definitely initialized *only* if the switch statement
            // yields something. Doesn't matter what.
            boolean ignored = switch (facing)
            {
                case UP, DOWN ->
                {
                    rayStart = centre.add(r * u, 0, r * v);
                    rayEnd = centre.subtract(r * u, 0, r * v);
                    yield false;
                }
                case NORTH, SOUTH ->
                {
                    rayStart = centre.add(r * u, r * v, 0);
                    rayEnd = centre.subtract(r * u, r * v, 0);
                    yield false;
                }
                case EAST, WEST ->
                {
                    rayStart = centre.add(0, r * u, r * v);
                    rayEnd = centre.subtract(0, r * u, r * v);
                    yield false;
                }
            };

//            if (world instanceof ServerWorld serverWorld)
//            {
//                serverWorld.spawnParticles(ParticleTypes.SMOKE, rayEnd.x, rayEnd.y, rayEnd.z, 2, 0.1, 0.1, 0.1, 0);
//                serverWorld.spawnParticles(ParticleTypes.SMOKE, rayStart.x, rayStart.y, rayStart.z, 2, 0.1, 0.1, 0.1, 0);
//            }

            List<Entity> hitting = new ObjectArrayList<>();
            level.getEntities(EntityTypeTest.forClass(LivingEntity.class), box, e -> true)
                    .stream()
                    .filter(e -> NMMaths.raycast(e.getBoundingBox(), rayStart, rayEnd) != null)
                    .filter(e -> !currentlyHitting.contains(e))
                    .filter(e -> !e.isDeadOrDying()).forEach(e ->
                    {
                        if (e.hurtTime == 0)
                            damageEntity((ServerLevel) level, e);

                        hitting.add(e);
                    });

            currentlyHitting.clear();
            currentlyHitting.addAll(hitting);
        }
        return true;
    }

    private void damageEntity(ServerLevel world, LivingEntity entity)
    {
        int damageAmount = (int) Math.max(4 * power, 2);

        if (entity.getHealth() > damageAmount)
        {
            entity.hurt(world.damageSources().generic(), damageAmount);
        }
        else
        {
            killEntity(world, entity);
        }
    }

    public static void killEntity(ServerLevel world, LivingEntity entity)
    {
        if (world.getBlockEntity(entity.blockPosition().below()) instanceof FluidDrainBlockEntity be)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                var damageSource = world.damageSources().source(NMDamageSources.MINCER);

                be.getBuffer(null).insert(NMFluids.TISSUE_SLURRY.variant(), getEntityAmount(entity), transaction);
                world.sendParticles(NMParticles.MEAT_SPLASH, entity.getX(), entity.getY(), entity.getZ(), 20, 0.4, 0.4, 0.4, 0.01);
                world.playSound(null, entity.blockPosition(), MWSounds.BULLET_HIT_LIVING_CRIT, SoundSource.BLOCKS);

                if (entity instanceof Player player)
                    player.hurt(damageSource, Float.MAX_VALUE);
                else
                    entity.discard();

                transaction.commit();
            }
        }
        entity.kill();
    }

    public static long getEntityAmount(LivingEntity entity)
    {
        MobSynthesisRegistry.Entry entry = MobSynthesisRegistry.get(entity);
        if (entry != null)
        {
            return entry.meat() / 2;
        }
        return MobSynthesisRegistry.meatFromSize(entity) / 2;
    }

    @Override
    public void setInputPower(float power)
    {
        if (power < minPower)
        {
            this.angularSpeed = 0;
        }
        else
        {
            this.angularSpeed = power * 20;
        }
        super.setInputPower(power);
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putFloat("angularSpeed", angularSpeed);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.angularSpeed = nbt.getFloat("angularSpeed");
    }

    public static void clientTick(Level world, BlockPos pos, BlockState state, DeathBladesBlockEntity be)
    {
        be.clientAngle = be.angle;;
        be.angle = Mth.wrapDegrees(be.angle + be.angularSpeed);
    }

    public static GraphData makePowerGraph()
    {
        // Minimum and saturation power
        float minAbs = MIN_POWER * PowerUtil.BASE_POWER;

        float f = 4; // Magic number
        float minDamage = 2;
        float damage0 = f * (minAbs / PowerUtil.BASE_POWER);
        float damage1 = MAX_DAMAGE;

        float absAtMinDamage = minDamage / f * PowerUtil.BASE_POWER;
        float maxAbs = damage1 / f * PowerUtil.BASE_POWER;

        DecimalFormat df = new DecimalFormat("###.#");

        return new GraphData()
                .setTitle("Power to Damage")
                .setXRange(new GraphData.Range(150, maxAbs * 1.2f))
                .setYRange(new GraphData.Range(0, damage1 * 1.2f))
                .setXTicks(List.of(
                        new GraphData.Tick(0, false),
                        new GraphData.Tick(minAbs, true),
                        new GraphData.Tick(maxAbs, true)
                        ))
                .setYTicks(List.of(
                        new GraphData.Tick(minDamage, false),
                        new GraphData.Tick(damage1, false)
                ))
                .setXTitle("Input power", "ej/t")
                .setYTitle("Damage per hit", "")
                .semiLogX(true)
                .setPoints(GraphData.join(new float[][]{
                        {0, 0},
                        {minAbs, 0},
                        {minAbs, minDamage}},
                        GraphData.sample(
                                GraphData.logRange(absAtMinDamage, maxAbs, 10),
                                x -> x, x -> (x / 1000) * f),
                        new float[][]{ {maxAbs, damage1},
                        {maxAbs * 1.2f, damage1}
                }))
                ;
    }
}
