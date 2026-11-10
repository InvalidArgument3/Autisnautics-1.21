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

package com.neep.neepmeat.machine.power_flower;

import net.minecraft.core.HolderLookup;

import com.google.common.collect.Iterators;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.FluidPump;
import com.neep.neepmeat.api.processing.MeatFluidUtil;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.api.storage.WritableSingleFluidStorage;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.transport.fluid_network.node.AcceptorModes;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

public class PowerFlowerControllerBlockEntity extends SyncableBlockEntity
{
    public static final FluidVariant INPUT_VARIANT = FluidVariant.of(Fluids.WATER);

    private int topPanels;
    private int blocks;

    public boolean multipleControllers = false;

    private final FluidPump fluidPump = FluidPump.of(-0.5f, () -> AcceptorModes.PUSH);

    private final WritableSingleFluidStorage waterStorage = new WritableSingleFluidStorage(4 * FluidConstants.BUCKET, this::setChanged)
    {
        @Override
        protected boolean canInsert(FluidVariant variant)
        {
            return variant.isOf(Fluids.WATER);
        }
    };

    private final WritableSingleFluidStorage foodStorage = new WritableSingleFluidStorage(4 * FluidConstants.BUCKET, this::setChanged)
    {
        @Override
        protected boolean canInsert(FluidVariant variant)
        {
            return foodEnergy(variant).energy != 0;
        }
    };


    private final WritableSingleFluidStorage outputStorage = new WritableSingleFluidStorage(4 * FluidConstants.BUCKET, this::setChanged)
    {
        @Override
        protected boolean canInsert(FluidVariant variant)
        {
            return variant.isOf(NMFluids.ETHEREAL_FUEL.still());
        }
    };

    private final CombinedStorage storage = new CombinedStorage();

    public PowerFlowerControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public static final FoodEnergyEntry NONE = new FoodEnergyEntry(0, 0);
    public static final FoodEnergyEntry MEAT = new FoodEnergyEntry(3, 1);
    public static final FoodEnergyEntry FEED = new FoodEnergyEntry(4, 1);

    /**
     * @return A pair of energy per consumption and the consume to consume.
     */
    public static FoodEnergyEntry foodEnergy(FluidVariant fluid)
    {
        if (fluid.isOf(NMFluids.MEAT.still()))
            return MEAT;

        if (fluid.isOf(NMFluids.ANIMAL_FEED.still()))
            return FEED;

        if (fluid.isOf(NMFluids.FOOD.still()))
        {
            return new FoodEnergyEntry(1 + (int) Math.ceil(MeatFluidUtil.getHunger(fluid)), 1);
        }

        return NONE;
    }

    public void serverTick()
    {
        if (level.getGameTime() % 80 == 0)
        {
            search(worldPosition);
            updateState();
        }

        try (Transaction transaction = Transaction.openOuter())
        {
            long outputAmount = 0;
            var food = foodEnergy(foodStorage.variant);
            int consume = 1;
            if (food.energy != 0)
            {
                // The number of full blocks determines output power via food consumption rate
                int foodDroplets = consume * (blocks - topPanels);

                long foodExtracted = foodStorage.extract(foodStorage.variant, foodDroplets, transaction);

                outputAmount += PowerUtil.absToAmount(NMFluids.ETHEREAL_FUEL.still(), foodExtracted * food.energy);
            }

            if (topPanels > 0)
            {
                // The number of top blocks determines output power directly
                int passivePower = NeepMeat.CONFIG.powerFlower.solarCoefficient.getRealValue() * topPanels;

                // Calculate consume of fluid to be produced from current power output
                long passiveAmount = PowerUtil.absToAmount(NMFluids.ETHEREAL_FUEL.still(), passivePower);

                // Extract from water tank
                long extracted = waterStorage.extract(INPUT_VARIANT, passiveAmount, transaction);

                // Insert output fluid into internal buffer
                outputAmount += Math.min(passiveAmount, extracted);
            }

            long inserted = outputStorage.insert(FluidVariant.of(NMFluids.ETHEREAL_FUEL.still()), outputAmount, transaction);

            if (inserted == outputAmount)
                transaction.commit();
            else
                transaction.abort();
        }
    }

    protected void updateState()
    {
        level.setBlockAndUpdate(worldPosition, NMBlocks.POWER_FLOWER_CONTROLLER.defaultBlockState().setValue(PowerFlowerControllerBlock.VALID, blocks > 0));
    }

    private void search(BlockPos start)
    {
        blocks = 0;
        topPanels = 0;
        multipleControllers = false;

        Set<BlockPos> visited = Sets.newHashSet();
        Queue<BlockPos> queue = Queues.newArrayDeque();
        visited.add(worldPosition);

        queue.add(start);

        while (!queue.isEmpty())
        {
            BlockPos current = queue.poll();
            BlockPos.MutableBlockPos mutable = current.mutable();
            for (Direction direction : Direction.values())
            {
                mutable.setWithOffset(current, direction);

                if (!visited.contains(mutable))
                {
                    BlockState nextState = level.getBlockState(mutable);
                    if (nextState.getBlock() instanceof PowerFlowerGrowthBlock)
                    {
                        if (nextState.getValue(PowerFlowerGrowthBlock.GROWTH) == 1)
                            topPanels++;

                        blocks++;

                        queue.add(mutable.immutable());
                        visited.add(mutable.immutable());

                        if (blocks >= NeepMeat.CONFIG.powerFlower.maxBlocks.getRealValue())
                        {
                            return;
                        }
                    }
                    else if (nextState.is(NMBlocks.POWER_FLOWER_CONTROLLER))
                    {
                        topPanels = 0;
                        blocks = 0;
                        multipleControllers = true;
                        return;
                    }
                    else if (level.getBlockEntity(mutable) instanceof PowerFlowerFluidPortBlock.PFPortBlockEntity port)
                    {
                        port.setController(worldPosition);
                        visited.add(mutable.immutable());
                    }
                }
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putInt("blocks", blocks);
        nbt.putInt("panels", topPanels);
        nbt.put("water_storage", waterStorage.toNbt(new CompoundTag()));
        nbt.put("food_storage", foodStorage.toNbt(new CompoundTag()));
        nbt.put("output_storage", outputStorage.toNbt(new CompoundTag()));
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.topPanels = nbt.getInt("panels");
        this.blocks = nbt.getInt("blocks");
        waterStorage.readNbt(nbt.getCompound("water_storage"));
        foodStorage.readNbt(nbt.getCompound("food_storage"));
        outputStorage.readNbt(nbt.getCompound("output_storage"));
    }

    public Storage<FluidVariant> getFluidStorage()
    {
        return storage;
    }

    public FluidPump getFluidPump(Direction direction)
    {
        return fluidPump;
    }

    public Storage<FluidVariant> getOutputStorage(Direction direction)
    {
        return outputStorage;
    }

    public int getTop()
    {
        return topPanels;
    }

    public int getNumBlocks()
    {
        return blocks;
    }

    public record FoodEnergyEntry(int energy, int consume)
    {

    }

    private class CombinedStorage implements Storage<FluidVariant>
    {
        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction)
        {
            if (resource.isOf(Fluids.WATER))
                return waterStorage.insert(resource, maxAmount, transaction);

            return foodStorage.insert(resource, maxAmount, transaction);
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction)
        {
            return 0;
        }

        @Override
        public Iterator<StorageView<FluidVariant>> iterator()
        {
            return Iterators.concat(waterStorage.iterator(), foodStorage.iterator());
        }
    }
}
