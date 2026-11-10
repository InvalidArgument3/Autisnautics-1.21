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

package com.neep.neepmeat.api.live_machine;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.live_machine.metrics.DataLog;
import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.entity.MachineWormEntity;
import com.neep.neepmeat.init.NMEntities;
import com.neep.neepmeat.init.NMFluids;
import com.neep.neepmeat.machine.live_machine.LivingMachineComponents;
import com.neep.neepmeat.machine.live_machine.Processes;
import com.neep.neepmeat.machine.live_machine.block.TestLivingMachineBlock;
import com.neep.neepmeat.machine.live_machine.block.entity.MotorPortBlockEntity;
import com.neep.neepmeat.machine.live_machine.component.ItemOutputComponent;
import com.neep.neepmeat.machine.live_machine.component.PoweredComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public abstract class LivingMachineBlockEntity extends SyncableBlockEntity implements ComponentHolder
{
    public static Codec<EnumMap<StructureProperty, Float>> PROPERTIES_CODEC = Codec.unboundedMap(StructureProperty.CODEC, Codec.FLOAT).xmap(map -> map.isEmpty() ? new EnumMap<>(StructureProperty.class) : new EnumMap<>(map), Function.identity());

    private final RandomSource random = RandomSource.create();

    protected List<DamageableStructureEntry> damageableStructures = new ObjectArrayList<>();
    protected List<DamageableStructureEntry> structures = new ObjectArrayList<>();
    private ComponentMap componentMap = new ComponentMap();
    private EnumMap<StructureProperty, Float> properties = new EnumMap<>(StructureProperty.class);

    protected DegradationManager degradationManager = new DegradationManager(this::degradationRate, RandomSource.create(), this::updateBlockState);
    private final float rateMultiplier = 1;

    private final DataLog dataLog;

    protected long age = 0;
    protected int updateInterval = 80;
    protected int maxSize = NeepMeat.CONFIG.livingMachines.maxBlocks.getRealValue();

    protected float power;
    protected float repairAmount;

    protected boolean updateProcess = true;
    @Nullable private Process process;

    // Convenient combined item output
    private Storage<ItemVariant> combinedItemInput = Storage.empty();
    private Storage<ItemVariant> combinedItemOutput = Storage.empty();
    private Storage<FluidVariant> combinedFluidInput = Storage.empty();
    private Storage<FluidVariant> combinedFluidOutput = Storage.empty();

    // Public, non-persistent round-robin counter
    public int inputSequence;

    // Use only on client. Use structures.size() elsewhere.
    private int numStructures;
    private int numComponents;
    private final List<ResourceLocation> components = new ArrayList<>();
    private Component processName = Process.NO_PROCESS;

    private List<Component> problems = List.of();

    public LivingMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        dataLog = new DataLog(500, 40);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.age = nbt.getLong("age");
        this.properties = PROPERTIES_CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("properties")).result().orElseGet(() -> new EnumMap<>(StructureProperty.class));
        this.power = nbt.getFloat("power");
        degradationManager.readNbt(nbt);

        // For client only
        this.numStructures = nbt.getInt("num_structures");
        this.numComponents = nbt.getInt("num_components");
        this.processName = Component.nullToEmpty(nbt.getString("process_name"));
        this.repairAmount = nbt.getFloat("repair");

        components.clear();
        ListTag list = nbt.getList("components", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); ++i)
        {
             components.add(ResourceLocation.tryParse(list.getString(i)));
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putLong("age", age);
        PROPERTIES_CODEC.encodeStart(NbtOps.INSTANCE, properties).get().ifLeft(r -> nbt.put("properties", r));
        nbt.putFloat("power", power);
        degradationManager.writeNbt(nbt);

        // For rendering on client only
        nbt.putInt("num_structures", structures.size());
        nbt.putInt("num_components", getNumComponents());
        nbt.putString("process_name", getProcess().getString());
        nbt.putFloat("repair", repairAmount);

        ListTag componentList = new ListTag();

        int i = componentMap.nextSetBit(0);
        while (i != -1 && i < componentMap.length())
        {
            i = componentMap.nextSetBit(i);

            ComponentType<?> type = ComponentType.ID_TO_TYPE.get(i);
            ResourceLocation id = ComponentType.REGISTRY.getKey(type);
            if (id != null)
                componentList.add(StringTag.valueOf(id.toString()));

            i++;
        }

        nbt.put("components", componentList);
    }

    protected void tickDegradation()
    {
        repairAmount = 0;
        if (getProgressIncrement() == 0)
        {
            double repairPerDroplet = 1 / (4 * 81000f); // Decrease in degradation from one droplet
            long maxConsume = 30;
            long consumePerTick = Math.min(Mth.ceil(degradationManager.getDegradation() / repairPerDroplet), maxConsume);

//        long consumePerTick = 2; // 2d per tick will consume approximately one bucket per half hour
            AtomicLong satisfied = new AtomicLong();
            withComponents(LivingMachineComponents.INTEGRATION_PORT).ifPresent(w ->
            {
                try (Transaction transaction = Transaction.openOuter())
                {
                    for (var port : w.t1())
                    {
                        if (satisfied.get() < consumePerTick)
                            satisfied.addAndGet(port.getFluidStorage(null).extract(NMFluids.WORK_FLUID.variant(),
                                    consumePerTick - satisfied.get(), transaction));
                        else
                            break;
                    }
                    transaction.commit();
                }
            });

            if (satisfied.get() > 0 && satisfied.get() >= consumePerTick)
            {
                repairAmount += (float) (rateMultiplier * repairPerDroplet * satisfied.get());
            }
        }

        float selfRepair = getSelfRepair();
        if (selfRepair > 0)
        {
            repairAmount += rateMultiplier * selfRepair;
        }

        degradationManager.tick();
    }

    private void updateBlockState()
    {
        TestLivingMachineBlock.Status status = TestLivingMachineBlock.Status.INVALID_STRUCTURE;

        if (process != null)
            status = TestLivingMachineBlock.Status.ACTIVE;

        if (getHealth() <= 0)
            status = TestLivingMachineBlock.Status.BROKEN;

        getLevel().setBlockAndUpdate(getBlockPos(), getBlockState().setValue(TestLivingMachineBlock.STATUS, status));
    }

    public void serverTick()
    {
        if (age % 20 == 0)
        {
            withComponents(LivingMachineComponents.ITEM_OUTPUT).ifPresent(r ->
            {
                r.t1().forEach(ItemOutputComponent::tryEject);
            });
        }

        if (age % updateInterval == 0)
        {
            updateStructure();
            sync();

            if (process != null)
                problems = process.getProblems(this);
        }

        if (updateProcess)
        {
            updateSpecialStorage();

            Process newProcess = Processes.getInstance().getFirstMatch(componentMap.bits);

            if (newProcess != process)
            {
                if (process != null)
                    process.onStop(this);

                this.process = newProcess;

                if (newProcess != null)
                    newProcess.onStart(this);
            }

            updateProcess = false;
            updateBlockState();
        }

        age++;
        tickDegradation();

        Collection<MotorPortBlockEntity> motors = getComponent(LivingMachineComponents.MOTOR_PORT);
        float nextPower = 0;
        if (!motors.isEmpty())
        {
            for (var motor : motors)
            {
                nextPower = Math.max(nextPower, motor.getPower());
            }
        }
        else
        {
            nextPower = 0;
        }
        power = nextPower;

        int i = componentMap.nextSetBit(0);
        while (i != -1 && i < componentMap.length())
        {
            i = componentMap.nextSetBit(i);

            for (Iterator<LivingMachineComponent> it = componentMap.<LivingMachineComponent>get(i).iterator(); it.hasNext();)
            {
                LivingMachineComponent component = it.next();
                if (component.componentRemoved())
                {
                    it.remove();
                    removeComponent(component);
                    sync();
                    updateProcess = true;
                }
                else
                {
                    if (component instanceof PoweredComponent powered)
                    {
                        powered.setProgressIncrement(getProgressIncrement());
                    }
                }
            }

            ++i;
        }

        if (process != null)
        {
            process.serverTick(this);
        }

        dataLog.log(getLevel().getGameTime(), this);

        if (getHealth() <= 0)
        {
            becomeEntity();
        }
    }

    protected void updateStructure()
    {
//        clearComponents();
        List<DamageableStructureEntry> newStructures = new ObjectArrayList<>();
        List<DamageableStructureEntry> newDamageable = new ObjectArrayList<>();
        var newComponentMap = new ComponentMap();

        search(getBlockPos(), newStructures, newDamageable, newComponentMap);

        structures = newStructures;
        damageableStructures = newDamageable;

        // Process the new components
        processComponents(newComponentMap);
        componentMap = newComponentMap;

        processStructure();
        updateSpecialStorage();
        updateProcess = true;
    }

    protected void processComponents(ComponentMap newComponentMap)
    {
        // We want to determine which components are present in the old map but not in the new one
        int i = componentMap.nextSetBit(0);
        while (i != -1 && i < componentMap.length())
        {
            i = componentMap.nextSetBit(i);

            var oldSet = componentMap.<LivingMachineComponent>get(i);
            if (newComponentMap.has(i))
            {
                // Components of same type are still present, but some may have been removed
                var newSet = newComponentMap.get(i);
                for (LivingMachineComponent oldComponent : oldSet)
                {
                    if (!newSet.contains(oldComponent))
                        oldComponent.shutdown();
                }
            }
            else
            {
                // Notify all of them
                for (LivingMachineComponent oldComponent : oldSet)
                {
                    oldComponent.shutdown();
                }
            }

            i++;
        }

    }

    protected void processStructure()
    {
        // Is this bad? Should I be using AtomicDouble and AtomicInteger in single-threaded code? Leave your answer in the comments below.

        // Count the number of values to average (not add) in all structure blocks
        EnumMap<StructureProperty, Integer> present = new EnumMap<>(StructureProperty.class);
        for (var structure : structures)
        {
            structure.structure.getProperties().forEach((property, value) ->
            {
                if (value.function().average())
                    present.compute(property, (p, v) -> v == null ? 1 : v + 1);
            });
        }

        properties.clear();
        for (var structure : structures)
        {
            structure.structure.getProperties().forEach((property, entry) ->
            {
                int numberPresent = present.getOrDefault(property, 1);
                properties.compute(property, (p, prev) ->
                {
                    if (prev == null)
                        prev = p.defaultValue();

                    return entry.apply(prev, numberPresent);
                });
            });
        }

        properties.compute(StructureProperty.MAX_POWER, (k, v) -> Math.max(40, v != null ? v : 0));
    }

    private void updateSpecialStorage()
    {
        combinedItemInput = new CombinedStorage<>(getComponent(LivingMachineComponents.ITEM_INPUT).stream().map(l -> l.getStorage(null)).toList());
        combinedItemOutput = new StorageDelegate(getComponent(LivingMachineComponents.ITEM_OUTPUT).stream().map(ItemOutputComponent::getInternalStorage).toList());

        combinedFluidInput = new CombinedStorage<>(Streams.concat(getComponent(LivingMachineComponents.FLUID_INPUT).stream(), getComponent(LivingMachineComponents.STOMACH).stream()).map(l -> l.getStorage(null)).toList());
        combinedFluidOutput = new CombinedStorage<>(getComponent(LivingMachineComponents.FLUID_OUTPUT).stream().map(l -> l.getStorage(null)).toList());
    }

    protected float getProperty(StructureProperty property)
    {
        return properties.computeIfAbsent(property, StructureProperty::defaultValue);
    }

    public <T extends LivingMachineComponent> Collection<T> getComponent(ComponentType<T> type)
    {
        return componentMap.get(type.getBitIdx());
    }

    // Assumes that the component has been removed.
    private void removeComponent(LivingMachineComponent component)
    {
        componentMap.onRemove(component);
    }

    protected void clearComponents()
    {
        int i = componentMap.nextSetBit(0);
        while (i != -1 && i < componentMap.length())
        {
            i = componentMap.nextSetBit(i);

            componentMap.clear(i, c -> c.setController(null));
            ++i;
        }
    }

    protected void search(BlockPos start, List<DamageableStructureEntry> structures, List<DamageableStructureEntry> damageableStructures, ComponentMap newComponentMap)
    {
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
                    visited.add(mutable.immutable());

                    BlockState nextState = level.getBlockState(mutable);
                    if (nextState.isAir())
                        continue;

                    LivingMachineComponent component;
                    if (structures.size() >= maxSize)
                        return;

                    if (nextState.getBlock() instanceof LivingMachineBlock)
                    {
                        structures.clear();
                        damageableStructures.clear();
                        clearComponents();
                        return;
                    }

                    if (nextState.getBlock() instanceof LivingMachineStructure structure)
                    {
                        structures.add(new DamageableStructureEntry(structure, mutable.immutable()));
                        if (structure.canOpen())
                            damageableStructures.add(new DamageableStructureEntry(structure, mutable.immutable()));

                        queue.add(mutable.immutable());
                    }
                    else if ((component = LivingMachineComponent.LOOKUP.find(level, mutable, null)) != null)
                    {
                        component.setController(worldPosition);
                        newComponentMap.add(component);
                        queue.add(mutable.immutable());
                    }
                }
            }
        }
    }

    public float getProgressIncrement()
    {
        return power * getEfficiency();
    }

    public float getEfficiency()
    {
        // Take into account performance degradation and block types
//        return getProperty(StructureProperty.SPEED)
//                * (1 - degradationManager.getDegradation());
        return (float) (1 - Math.pow(degradationManager.getDegradation(), 4));
    }

    public float getSelfRepair()
    {
        return getProperty(StructureProperty.SELF_REPAIR);
    }

    public float getRatedPower()
    {
        return getProperty(StructureProperty.MAX_POWER) / PowerUtil.referencePower();
    }

    public double degradationRate(double degradation)
    {
        double rate = -repairAmount;
        float rated = getRatedPower();

        if (power <= 0 && rate >= 0)
            return 0;

        if (rated <= 0)
            return Float.MAX_VALUE;

        if (power <= rated * 0.75)
        {
            return rate;
        }

        if (power > rated * 2)
            rate += (rateMultiplier * (0.00001f * power / rated));
        else if (power > rated)
            rate += (rateMultiplier * (0.000007f * power / rated));
        else
            rate += (rateMultiplier * (0.000003f));

        return rate;
    }

    public float getPower()
    {
        return power;
    }

    public double getRulHours()
    {
        return degradationManager.estimateRul() / (20 * 3600.0);
    }

    public long getRulSecs()
    {
        long ticks = degradationManager.estimateRul();
        if (ticks == -1)
            return -1;

        return ticks / 20;
    }

    public float getCurrentDegradationRate()
    {
        return (float) degradationRate(degradationManager.getDegradation());
    }

    public void onBlockRemoved()
    {
        if (process != null)
            process.onStop(this);

        // Remove all components and set their power to 0.
        int i = componentMap.nextSetBit(0);
        while (i != -1 && i < componentMap.length())
        {
            i = componentMap.nextSetBit(i);

            componentMap.clear(i, LivingMachineComponent::shutdown);

            ++i;
        }
    }

    protected void becomeEntity()
    {
        MachineWormEntity entity = NMEntities.MACHINE_WORM.create(level);
        if (entity != null)
        {
            level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());
            Vec3 ePos = Vec3.upFromBottomCenterOf(worldPosition, 0.05);
            entity.setPos(ePos.x, ePos.y, ePos.z);
            entity.setPosRaw(ePos.x, ePos.y, ePos.z);
            float yaw = getBlockState().getValue(TestLivingMachineBlock.FACING).toYRot();
            entity.setYRot(yaw);
            entity.setYHeadRot(yaw);
            level.addFreshEntity(entity);
            level.playSound(null, worldPosition, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1, 1.3f);
        }
    }

    public RandomSource getRandom()
    {
        return random;
    }

    public DataLog getDataLog()
    {
        return dataLog;
    }

    public long getAge()
    {
        return age;
    }

    public DegradationManager getDegradationManager()
    {
        return degradationManager;
    }

    public float getHealth()
    {
        return 1 - degradationManager.getDegradation();
    }

    public int getNumStructures()
    {
        if (!level.isClientSide())
            return structures.size();

        return numStructures;
    }

    public int getNumComponents()
    {
        if (!level.isClientSide())
        {
            int count = 0;
            for (var set : componentMap.componentMap)
            {
                if (set == null)
                    continue;

                count += set.size();
            }
            return count;
        }

        return numComponents;
    }

    public Component getProcess()
    {
        if (!level.isClientSide())
        {
            return process != null ? process.getName() : Process.NO_PROCESS;
        }

        return processName != null ? processName : Process.NO_PROCESS;
    }

    public List<ResourceLocation> getDisplayComponents()
    {
        return components;
    }

    public Storage<ItemVariant> getCombinedItemInput()
    {
        return combinedItemInput;
    }

    public Storage<ItemVariant> getCombinedItemOutput()
    {
        return combinedItemOutput;
    }

    public Storage<FluidVariant> getCombinedFluidInput()
    {
        return combinedFluidInput;
    }

    public Storage<FluidVariant> getCombinedFluidOutput()
    {
        return combinedFluidOutput;
    }

    public List<Component> getProblems()
    {
        return problems;
    }

    private static class StorageDelegate implements Storage<ItemVariant>
    {
        private final Storage<ItemVariant> storage;
//        private final BlockPos outPos;
//        private final Direction outDir;
//        private final boolean empty;

//        private StorageDelegate(List<Storage<ItemVariant>> storages, BlockPos outPos, Direction outDir, boolean empty)
        private StorageDelegate(List<Storage<ItemVariant>> storages)
        {
            this.storage = new CombinedStorage<>(storages);
//            this.outPos = outPos;
//            this.outDir = outDir;
//            this.empty = empty;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            return storage.insert(resource, maxAmount, transaction);
//            if (!empty && inserted < maxAmount)
//            {
//                long difference = maxAmount - inserted;
//
//                ItemPipeUtil.stackToAny((ServerWorld) world, outPos, outDir, resource, difference, transaction);
//            }
//            return maxAmount;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction)
        {
            return storage.extract(resource, maxAmount, transaction);
        }

        @Override
        public @NotNull Iterator<StorageView<ItemVariant>> iterator()
        {
            return storage.iterator();
        }
    }

    protected record DamageableStructureEntry(LivingMachineStructure structure, BlockPos pos) { }
}
