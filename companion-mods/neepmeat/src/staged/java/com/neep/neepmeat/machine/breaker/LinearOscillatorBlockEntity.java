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

package com.neep.neepmeat.machine.breaker;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.block.BaseFacingBlock;
import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.client.screen.tablet.graph.GraphData;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.machine.motor.MotorEntity;
import com.neep.neepmeat.transport.util.ItemPipeUtil;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.math.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class LinearOscillatorBlockEntity extends SyncableBlockEntity implements MotorisedBlock, MotorisedBlock.DiagnosticsProvider
{
    public static final String NBT_COOLDOWN = "cooldown";

    public static final float MAX_POWER = 1;
    public static final float MIN_POWER = 0.01f;
    public static final int MAX_COOLDOWN = 20;
    public static final float WORK_MULTIPLIER = 2.5f;

    public float cooldown = 0;
    private float cooldownIncrement;

    public float prevExtension = 0f;
    public float extension = 0f;
    public boolean extended = false;
    protected boolean running;

    // Rendering only
    public long extensionTime = 0;
    public float clientExtension = 0;
    private float power;

    public LinearOscillatorBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.LINEAR_OSCILLATOR, pos, state);
    }

    public LinearOscillatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public void tick()
    {
        if (!running)
            return;

        cooldown = Math.max(0, cooldown - cooldownIncrement);

        if (cooldown <= 0)
        {
            extend();
        }
        else
        {
            extended = false;
        }

        prevExtension = extension;
        extension = cooldown / (float) MAX_COOLDOWN;
        sync();
    }

    public void extend()
    {
        if (level.hasNeighborSignal(worldPosition))
            return;

        Direction facing = getBlockState().getValue(BaseFacingBlock.FACING);
        BlockPos facingPos = getBlockPos().relative(facing);
        BlockState facingState = level.getBlockState(facingPos);

        float hardness = facingState.getDestroySpeed(level, facingPos);

        // Break the block hardness is lower than obsidian
        if (!facingState.isAir() && facingState.getFluidState().isEmpty() && hardness >= 0 && hardness < Blocks.OBSIDIAN.defaultDestroyTime())
        {
            List<ItemStack> dropped = Block.getDrops(facingState, (ServerLevel) level, worldPosition, level.getBlockEntity(worldPosition));
            if (level.destroyBlock(facingPos, false))
            {
                try (Transaction transaction = Transaction.openOuter())
                {
                    for (var stack : dropped)
                    {
                        var ejected = ItemPipeUtil.EjectOperation.of(level, worldPosition, facing.getOpposite())
                                .useRouting(true).
                                stackToAny(ItemVariant.of(stack), stack.getCount(), transaction)
                                .amount();
                        if (ejected < stack.getCount())
                        {
                            ItemStack newStack = new ItemStack(stack.getItem());
                            newStack.setCount((int) (stack.getCount() - ejected));
                            Block.popResource(level, facingPos, newStack);
                        }
                    }
                    transaction.commit();
                }
            }
        }
        else
        {
            return;
        }

        AABB box = AABB.of(BoundingBox.fromCorners(facingPos, facingPos));
        List<LivingEntity> entities = level.getEntities(EntityTypeTest.forClass(LivingEntity.class), box, entity -> true);
        entities.forEach(entity ->
        {
            entity.hurt(level.damageSources().generic(), power * 20);
            double mult = 0.5;
            entity.push(facing.getStepX() * mult, facing.getStepY() * mult, facing.getStepZ() * mult);
        });

        this.cooldown = MAX_COOLDOWN;
        this.extended = true;
    }

    public boolean onUse(Player player, InteractionHand hand)
    {
        return false;
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.cooldown = nbt.getFloat(NBT_COOLDOWN);
    }

    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putFloat(NBT_COOLDOWN, cooldown);
    }

    @Override
    public void fromClientTag(CompoundTag nbt)
    {
        this.prevExtension = nbt.getFloat("prev_extension");
        this.extension= nbt.getFloat("extension");

        if (this.cooldown == MAX_COOLDOWN)
        {
            this.extensionTime = nbt.getLong("world_time");
        }
    }

    @Override
    public void toClientTag(CompoundTag nbt)
    {
        nbt.putLong("world_time", getLevel().getGameTime());
        nbt.putFloat("prev_extension", prevExtension);
        nbt.putFloat("extension", extension);
    }

    @Override
    public boolean motorTick(MotorEntity motor)
    {
        tick();
        return true;
    }

    @Override
    public void setInputPower(float power)
    {
        this.running = power != 0;
        this.power = power;
        if (power > MIN_POWER)
            this.cooldownIncrement = Mth.clamp(power * WORK_MULTIPLIER, 0, MAX_POWER);
        else
            cooldownIncrement = 0;
    }

    public static GraphData makePowerGraph()
    {
        // Minimum and saturation power
        float minAbs = MIN_POWER * 1000;
        float maxAbs = MAX_POWER * 1000;

        // Time taken to reach MAX_COOLDOWN
        float time0 = MAX_COOLDOWN / (WORK_MULTIPLIER * minAbs / 1000) / 20;
        float time1 = MAX_COOLDOWN / (WORK_MULTIPLIER * maxAbs / 1000) / 20;

        float x0 = 0;
        float x1 = minAbs;
        float x2 = maxAbs;
        float x3 = maxAbs * 1.5f;
        float yEnd = 1.1f * time0;

        DecimalFormat df = new DecimalFormat("###.#");

        return new GraphData()
                .semiLogX(true)
                .setTitle("Time between extensions")
                .setXRange(new GraphData.Range(x0, x3))
                .setYRange(new GraphData.Range(0, yEnd))
                .setXTicks(List.of(
                        new GraphData.Tick(0, false),
                        new GraphData.Tick(x1, true, df.format(minAbs)),
                        new GraphData.Tick(x2, true, df.format(maxAbs))))
                .setYTicks(List.of(
                        new GraphData.Tick(time0, false, df.format(time0)),
                        new GraphData.Tick(time1, false, df.format(time1))
                ))
                .setXTitle("Input power", "ej/t")
                .setYTitle("Time", "s")
                .setPoints(GraphData.join(
                        new float[][]{{0, 0}, {x1, 0}, {x1, time0}},
                        GraphData.sample(
                                GraphData.logRange(x1, x2, 10),
                                x -> x, x -> (MAX_COOLDOWN / (WORK_MULTIPLIER * x / 1000) / 20)),
                        new float[][]{{x3, time1}}
                ))
                ;
    }

    @Override
    public @Nullable Diagnostics getDiagnostics()
    {
        return Diagnostics.insufficientPower(!running, power, MIN_POWER);
    }
}
