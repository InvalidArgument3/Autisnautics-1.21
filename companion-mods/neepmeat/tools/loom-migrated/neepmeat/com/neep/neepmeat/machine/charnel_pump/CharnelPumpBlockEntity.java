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

package com.neep.neepmeat.machine.charnel_pump;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.util.ClientComponents;
import com.neep.meatlib.util.LazySupplier;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.enlightenment.EnlightenmentUtil;
import com.neep.neepmeat.api.live_machine.ComponentType;
import com.neep.neepmeat.api.live_machine.LivingMachineComponent;
import com.neep.neepmeat.component.ChunkEnlightenmentComponent;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.enlightenment.PlayerEnlightenmentManager;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.component.PoweredComponent;
import com.neep.neepmeat.machine.live_machine.process.CharnelPumpProcess;
import com.neep.neepmeat.machine.well_head.BlockEntityFinder;
import com.neep.neepmeat.machine.well_head.WellHeadBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.Set;

public class CharnelPumpBlockEntity extends SyncableBlockEntity implements LivingMachineComponent, PoweredComponent
{
    private final RandomSource random = RandomSource.create();

    private final LazySupplier<BlockEntityFinder<WellHeadBlockEntity>> wellHeadFinder = LazySupplier.of(() ->
        new BlockEntityFinder<>(getLevel(), NMBlockEntities.WELL_HEAD, 20).addAll(BlockEntityFinder.chunkRange(getBlockPos())));

    private final LazySupplier<BlockEntityFinder<WrithingEarthSpoutBlockEntity>> writhingSpoutFinder = LazySupplier.of(() ->
        new BlockEntityFinder<>(getLevel(), NMBlockEntities.WRITHING_EARTH_SPOUT, 20).addAll(BlockEntityFinder.chunkRange(getBlockPos())));

    private final ClientComponents.Holder<?> holder = new ClientComponents.BlockEntityHolder<>(this);

    public float animationTicks;
    private float progressIncrement;

    public boolean hasAir;
    private boolean hasFluid;
    public boolean hasFluidCurrentCycle; // Render fluid colum for the current animation cycle

    private float progress;

    public CharnelPumpBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public void serverTick(double puPower, Storage<FluidVariant> inputStorage, Transaction transaction)
    {
        wellHeadFinder.get().tick();
        writhingSpoutFinder.get().tick();

        Set<WellHeadBlockEntity> found = wellHeadFinder.get().result();

        // Consume compressed air
        boolean hasAir = false;
        try (Transaction inner = transaction.openNested())
        {
            long droplets = Math.max(10, (long) (puPower * NeepMeat.CONFIG.charnelPump.powerToAir.getRealValue()));
            long extracted = inputStorage.extract(NMFluids.COMPRESSED_AIR.variant(), droplets, inner);
            if (extracted == droplets)
            {
                hasAir = true;
                inner.commit();
            }
        }

        if (this.hasAir != hasAir)
        {
            this.hasAir = hasAir;
            sync();
        }

        if (CharnelPumpProcess.canRun(puPower, this))
        {
            spawnSpouts();

            if (hasAir)
                emitEnlightenment();

            // Consume work fluid and eject ores
            boolean fluidAvailable = StorageUtil.simulateExtract(inputStorage, NMFluids.WORK_FLUID.variant(), CharnelPumpProcess.DISTRIBUTE_AMOUNT, transaction) == CharnelPumpProcess.DISTRIBUTE_AMOUNT;

            if (hasAir && fluidAvailable)
            {
                progress = (float) Math.max(0.0f, progress - puPower);

                if (progress <= 0)
                {
                    progress = 100;

                    boolean consumed = false;
                    for (var wellHead : found)
                    {
                        try (Transaction inner = transaction.openNested())
                        {
                            long extracted = inputStorage.extract(NMFluids.WORK_FLUID.variant(), CharnelPumpProcess.DISTRIBUTE_AMOUNT, inner);
                            if (extracted == CharnelPumpProcess.DISTRIBUTE_AMOUNT)
                            {
                                wellHead.receiveFluid(CharnelPumpProcess.DISTRIBUTE_AMOUNT, inner);
                                consumed = true;
                                inner.commit();
                            }
                            else
                            {
                                inner.abort();
                            }
                        }

                        // Only use one well head
                        break;
                    }
                }
            }

            if (this.hasFluid != fluidAvailable)
            {
                this.hasFluid = fluidAvailable;
                sync();
            }
        }
    }

    private boolean canSpoutSpawn(BlockPos surfacePos)
    {
        BlockState surfaceState = level.getBlockState(surfacePos);
        return BlockEntityFinder.chunkRange(worldPosition).contains(level.getChunk(surfacePos).getPos())
                && (surfaceState.is(NMTags.WRITHING_EARTH_REPLACABLE)
        );
    }

    // Check for existing sprouts. If none are found, spawn a new one.
    private void spawnSpouts()
    {
        if (!level.getBlockState(worldPosition.below()).is(NMBlocks.WRITHING_STONE))
            level.setBlock(worldPosition.below(), NMBlocks.WRITHING_STONE.defaultBlockState(), Block.UPDATE_ALL);

        var writhing = writhingSpoutFinder.get();

        if (writhing.notDirty() && writhing.result().isEmpty())
        {
            // Find a position on the surface within the adjacent 3x3 square of chunks.
            BlockPos rand = BlockPos.randomInCube(random, 1, getBlockPos(), 25).iterator().next();

            ChunkAccess chunk = level.getChunk(rand);
            BlockPos columnBottom = new BlockPos(rand.getX(), worldPosition.getY() - 10, rand.getZ());
            BlockPos columnTop = new BlockPos(rand.getX(), worldPosition.getY() + 25, rand.getZ());
            for (BlockPos blockPos : BlockPos.betweenClosed(columnBottom, columnTop))
            {
                BlockState state = chunk.getBlockState(blockPos);
                if (state.isAir() || !state.getFluidState().isEmpty())
                {
                    BlockPos surfacePos = blockPos.below();

                    if (canSpoutSpawn(surfacePos))
                    {
                        level.setBlock(surfacePos, NMBlocks.WRITHING_EARTH_SPOUT.defaultBlockState(), Block.UPDATE_ALL);
                        level.playSound(null, surfacePos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1, 1);

                        ((ServerLevel) level).sendParticles(NMParticles.BODY_COMPOUND_SHOWER, surfacePos.getX() + 0.5, surfacePos.getY() + 1, surfacePos.getZ() + 0.5,
                                50,
                                1, 10, 1, 0.1);

                        return;
                    }
                }
            }

        }
    }

    private void emitEnlightenment()
    {
        if (level.getGameTime() % 2 == 0)
        {
            float enlightenment = PlayerEnlightenmentManager.FILTER_LENGTH * 2.5f;
            Vec3 centre = Vec3.atCenterOf(worldPosition);
            getLevel().getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(16, 16, 16), p -> true)
                    .forEach(p ->
                    {
                        EnlightenmentUtil.applyDose((ServerPlayer) p, enlightenment, p.distanceToSqr(centre), 1 / 50.0);
                    });
        }

        ChunkEnlightenmentComponent.distribute(level, worldPosition, 10, 1);
    }

    public boolean canRun()
    {
        return CharnelPumpProcess.canRun(progressIncrement, this);
    }

    @Override
    public void toClientTag(CompoundTag nbt)
    {
        nbt.putFloat("power", progressIncrement);
        nbt.putBoolean("has_air", hasAir);
        nbt.putBoolean("has_fluid", hasFluid);
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.progressIncrement = nbt.getFloat("power");
        this.hasAir = nbt.getBoolean("has_air");
        this.hasFluid = nbt.getBoolean("has_fluid");
    }

    @Override
    public boolean componentRemoved()
    {
        return isRemoved();
    }

    @Override
    public ComponentType<? extends LivingMachineComponent> getComponentType()
    {
        return LivingMachineComponents.CHARNEL_PUMP;
    }

    private void spawnAirParticles()
    {
        if (level.getGameTime() % 2 == 0)
        {
            double x = getBlockPos().getX() + 0.5;
            double y = getBlockPos().getY();
            double z = getBlockPos().getZ() + 0.5;

            level.addParticle(ParticleTypes.POOF, x + 1, y + 1, z + 1, 0.1, 0.1, 0.1);
            level.addParticle(ParticleTypes.POOF, x - 1, y + 1, z + 1, -0.1, 0.1, 0.1);
            level.addParticle(ParticleTypes.POOF, x + 1, y + 1, z - 1, 0.1, 0.1, -0.1);
            level.addParticle(ParticleTypes.POOF, x - 1, y + 1, z - 1, -0.1, 0.1, -0.1);
        }
    }

    private void clientTickAir()
    {
        if (hasAir)
        {
            if (!CharnelPumpProcess.canRun(progressIncrement, this))
            {
                spawnAirParticles();
            }
            else if (animationTicks < 30)
            {
                spawnAirParticles();
            }
        }

    }

    public static void clientTick(Level world, BlockPos pos, BlockState state, CharnelPumpBlockEntity be)
    {
        be.clientTickAir();

        if (be.progressIncrement > 0 && be.hasAir)
        {
            be.animationTicks = Math.max(0, be.animationTicks - be.progressIncrement());
            if (be.animationTicks == 0)
            {
                be.animationTicks = 100;
                be.hasFluidCurrentCycle = be.hasFluid;
            }
        }
        else
        {
            be.animationTicks = Math.max(0, be.animationTicks - 1);
        }

        be.holder.get().clientTick();
    }

    @Override
    public float progressIncrement()
    {
        return progressIncrement;
    }

    @Override
    public void setProgressIncrement(float progressIncrement)
    {
        if (progressIncrement != this.progressIncrement)
        {
            this.progressIncrement = Math.min(1.9f, progressIncrement);
            sync();
        }
    }

    public static boolean isRising(float animationTicks)
    {
        return animationTicks < 60;
    }
}
