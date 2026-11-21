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

package com.neep.neepmeat.machine.heater;

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.api.storage.DynamicLazyBlockApiCache;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.neepmeat.client.screen.tablet.graph.GraphData;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.machine.Heatable;
import com.neep.neepmeat.machine.motor.LiquidFuelMachine;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class HeaterBlockEntity extends LiquidFuelMachine implements BloodAcceptor, MotorisedBlock.DiagnosticsProvider
{
    private float influx;
    private float liquidInflux;

    private final LazyBlockApiCache<Heatable, Void> cache;
    private int copperTime;

    public static final int MIN_POWER = 40;

    protected HeaterBlockEntity(BlockEntityType<HeaterBlockEntity> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        cache = DynamicLazyBlockApiCache.of(this::getLevel, Heatable.LOOKUP,
                () -> pos.relative(getBlockState().getValue(HeaterBlock.FACING)), () -> null);
    }

    public HeaterBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.HEATER, pos, state);
        addListener(cache);
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, HeaterBlockEntity blockEntity)
    {
        blockEntity.tick();
    }

    public void tick()
    {
        if (!fluidStorage.isResourceBlank())
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                long extracted = extractEnergy(maxPower(), transaction);

                if (extracted > 0)
                    liquidInflux = extracted / (float) PowerUtil.BASE_POWER;
                else
                    liquidInflux = 0;

                transaction.commit();
            }
        }
        else
        {
            liquidInflux = 0;
        }

        var heatable = cache.find();
        if (heatable != null)
        {
            double runningRate = getPUPower();
            if (this.getPUPower() * PowerUtil.BASE_POWER >= MIN_POWER)
            {
                heatable.setBurning();
                heatable.setHeat((float) runningRate);
                heatBlock();
            }

            Direction facing = getBlockState().getValue(HeaterBlock.FACING);
            BlockPos furnacePos = worldPosition.relative(facing);
            BlockState state = getLevel().getBlockState(furnacePos);
            heatable.updateState(getLevel(), furnacePos, state);
        }
        else if (this.getPUPower() > 0.05)
        {
            heatBlock();
        }
    }

    public double getPUPower()
    {
        return Math.max(influx, liquidInflux);
    }

    protected void onRemove()
    {
        var heatable = cache.find();
        if (heatable != null)
            heatable.setHeat(0);
    }

    public void heatBlock()
    {
        BlockPos facingPos = getBlockPos().relative(getBlockState().getValue(BaseFacingBlock.FACING));
        BlockState facingState = level.getBlockState(facingPos);
        if (facingState.getBlock() instanceof WeatheringCopper oxidizable)
        {
            ++copperTime;
//            getWorld().addParticle(ParticleTypes.LAVA, facingPos.getX() + 0.5, facingPos.getY() + 1.5, facingPos.getZ() + 0.5, 0, 0, 0);
//            if (copperTime % 5 == 0)
//                spawnOxidiseParticles((ServerWorld) getWorld(), ParticleTypes.DRIPPING_LAVA, facingPos, new Random(world.getTime()), copperTime / 2, 16);

            if (copperTime == 100)
            {
                copperTime = 0;
                Optional<BlockState> nextBlock = oxidizable.getNext(facingState);
                if (canOxidise(level, facingPos) && nextBlock.isPresent())
                {
                    level.setBlockAndUpdate(facingPos, nextBlock.get());
                }
            }
        }
        else
        {
            copperTime = 0;
        }
    }

    public static boolean canOxidise(Level world, BlockPos pos)
    {
        for (Direction direction : Direction.values())
        {
            BlockPos offset = pos.relative(direction);
            if (world.getBlockState(offset).is(NMFluids.PATINA_TREATMENT.getBlock()))
            {
                return true;
            }
        }
        return false;
    }

    protected static void spawnOxidationParticles(ServerLevel world, SimpleParticleType particle, BlockPos pos, Random random, int amount, int radius)
    {
        for (ServerPlayer player : PlayerLookup.around(world, pos, radius))
        {
            double dx = random.nextDouble() * 1.1;
            double dy = random.nextDouble() * 1.1;
            double dz = random.nextDouble() * 1.1;
//            ParticleSpawnPacket.send(player, particle, new Vec3d(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz), new Vec3d(0, 0, 0), amount);
//            ParticleSpawnPacket.send(player, particle, pos, amount);
        }
    }

    @Override
    public float updateInflux(float influx)
    {
        this.influx = influx;
        return influx;
    }

    @Override
    public Mode getMode()
    {
        return Mode.SINK;
    }

    @Override
    public MotorisedBlock.@Nullable Diagnostics getDiagnostics()
    {
        float influx = Math.max(this.influx, this.liquidInflux);
        return MotorisedBlock.Diagnostics.insufficientPower(influx * PowerUtil.BASE_POWER < MIN_POWER, influx, (float) MIN_POWER / PowerUtil.BASE_POWER);
    }

    public static GraphData makePowerGraph()
    {
        DecimalFormat df = new DecimalFormat("###.#");

        float minAbs = MIN_POWER;
        int minRate = Heatable.getFurnaceTickIncrement(minAbs / 1000);
        int maxRate = 128;
        float maxAbs = Heatable.heatForTickIncrement(maxRate) * 1000;

        return new GraphData()
                .setTitle("Furnace speed")
                .setYTitle("Furnace speed", "")
                .setXTitle("Input power", "eJ/t")
                .setXTicks(List.of(
                    new GraphData.Tick(0, false),
                    new GraphData.Tick(0.25f, true, df.format(minAbs)),
        //                        new GraphData.Tick(0.25f, true, df.format(absAtMinDamage)),
                    new GraphData.Tick(0.75f, true, df.format(maxAbs))
                ))
                .setYTicks(List.of(
                        new GraphData.Tick(0.2f, false, df.format(minRate) + "x"),
                        new GraphData.Tick(0.8f, false, df.format(maxRate) + "x")
                ))
                .setPoints(new float[][]{
                        {0, 0},
                        {0.25f, 0},
                        {0.25f, 0.2f},
                        {0.75f, 0.8f},
                        {1, 0.8f}
                })
                ;
    }

}
