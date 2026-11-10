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

package com.neep.neepmeat.machine.pylon;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.util.ClientComponents;
import com.neep.meatlib.util.LazySupplier;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.DataPort;
import com.neep.neepmeat.api.DataResource;
import com.neep.neepmeat.api.enlightenment.EnlightenmentUtil;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.entity.GlomeEntity;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.init.NMEntities;
import com.neep.neepmeat.machine.advanced_integrator.AdvancedIntegratorBlockEntity;
import com.neep.neepmeat.machine.advanced_integrator.SimpleDataPort;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.neepmeat.machine.well_head.BlockEntityFinder;
import com.neep.neepmeat.interfaces.EndermanExtension;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.math.*;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.Random;

import static com.neep.neepmeat.machine.well_head.BlockEntityFinder.chunkRange;

public class PylonBlockEntity extends SyncableBlockEntity implements MotorisedBlock
{
    protected final int radius = 7; // Enlightenment and glome radius
    public float angle;
    private float speed;
    private float level;
    // Interval at which to disable teleporting for all nearby endermen.
    // If the pylon is removed or disabled, endermen will revert to normal after this delay.
    private final int endermanLookupPeriod = 20;

    private final SimpleDataPort port = new SimpleDataPort(this);

    private final LazySupplier<BlockEntityFinder<AdvancedIntegratorBlockEntity>> integratorFinder = LazySupplier.of(() ->
            new BlockEntityFinder<>(getLevel(), NMBlockEntities.ADVANCED_INTEGRATOR, 40)
                    .addAll(chunkRange(getBlockPos()))
                    .predicate(be -> be.getBlockPos().closerThan(getBlockPos(), radius * 2)));

    protected final Random random = new Random(0);

    private final ClientComponents.Holder<PylonBlockEntity> holder = new ClientComponents.BlockEntityHolder<>(this);

    public PylonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public PylonBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.PYLON, pos, state);
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, PylonBlockEntity be)
    {
    }

    public boolean isRunning()
    {
        return getSpeed() >= NeepMeat.CONFIG.pylon.runningSpeed.getRealValue();
    }

    @Override
    public boolean motorTick(MotorEntity motor)
    {
        integratorFinder.get().tick();

        float prevSpeed = speed;
        this.speed = motor.getSpeed();
        if (prevSpeed != speed)
            sync();

        if (isRunning())
        {
            long time = level.getGameTime();
            if (time % 80 == 0)
                level = updateLevel(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());

            if (time % endermanLookupPeriod == 0)
                inhibitEndermenTeleporting();

            spawnGlomes();
            enlightenIntegrators();
            enlightenPlayers();
        }

        return false;
    }

    private static float updateLevel(Level world, int x, int y, int z)
    {
        int level = 0;

        for (int depth = 1; depth <= 4; depth++)
        {
            int j = y - depth;
            for (int i = x - depth; i <= x + depth; ++i)
            {
                for (int k = z - depth; k <= z + depth; ++k)
                {
                    if (world.getBlockState(new BlockPos(i, j, k)).is(NMBlocks.MEAT_STEEL_BLOCK))
                        level++;
                }
            }
        }

        return level / 164f;
    }

    protected AABB getBox()
    {
        BlockPos volumeStart = getBlockPos().subtract(new Vec3i(radius, radius, radius));
        int end = 2 * radius + 1;
        return new AABB(volumeStart, volumeStart.offset(end, end, end));
    }

    private void spawnGlomes()
    {
        BlockPos volumeStart = getBlockPos().subtract(new Vec3i(radius, radius, radius));
        int end = 2 * radius + 1;
        if (random.nextFloat() < 0.05 * (effectMultiplier()))
        {
            if (!level.hasNearbyAlivePlayer(getBlockPos().getX(), worldPosition.getY(), worldPosition.getZ(), 32))
                return;

            BlockPos entityBlockPos = volumeStart.offset(random.nextInt(end), random.nextInt(end), random.nextInt(end));
            Vec3 entityPos = Vec3.atCenterOf(entityBlockPos);
            if (level.isEmptyBlock(entityBlockPos))
            {
                GlomeEntity entity = NMEntities.GLOME.create(level);
                entity.setPos(entityPos.x, entityPos.y - 0.2, entityPos.z);
//                GlomeEntity entity = new GlomeEntity(world, entityPos.x, entityPos.y - 0.2, entityPos.z, 0, 0, 0);
                level.addFreshEntity(entity);
            }
        }
    }

    private void enlightenPlayers()
    {
        float enlightenment = (effectMultiplier() * 10);
        Vec3 centre = Vec3.atCenterOf(worldPosition);
        PlayerLookup.around((ServerLevel) level, centre, radius).forEach(p ->
        {
            EnlightenmentUtil.applyDose(p, enlightenment, p.distanceToSqr(centre), 1 / 50.0);
        });
    }

    private void enlightenIntegrators()
    {
        try (Transaction transaction = Transaction.openOuter())
        {
            integratorFinder.get().result().forEach(i ->
            {
                if (random.nextBoolean())
                    i.getDataStorage().insert(DataResource.NORMAL, (long) (1 + effectMultiplier() * 20), transaction);
            });
            transaction.commit();

        }
    }

    private void inhibitEndermenTeleporting()
    {
        for (EnderMan entity : level.getEntitiesOfClass(EnderMan.class, getBox(), e -> true))
        {
            ((EndermanExtension) entity).neepmeat$setDisableTeleport(true, endermanLookupPeriod + 5);
        }
    }

    private float effectMultiplier()
    {
        return Mth.clamp(speed / NeepMeat.CONFIG.pylon.maxSpeed.getRealValue(), 0, 1) * Math.max(level, 0.01f);
    }

    public boolean isUnstable()
    {
        float m = effectMultiplier();
        return m >= 0.7;
    }

    @Override
    public void setInputPower(float power)
    {

    }

    public float getSpeed()
    {
        return speed;
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return this.saveWithoutMetadata();
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putFloat("level", level);
        nbt.putFloat("speed", speed);
        port.writeNbt(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.speed = nbt.getFloat("speed");
        this.level = nbt.getFloat("level");
        port.readNbt(nbt);
    }

    public DataPort getPort(Void unused)
    {
        return port;
    }


    // --- Client things ---

    public void clientTick()
    {
        holder.get().clientTick();
    }
}
