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

package com.neep.neepmeat.machine.pedestal;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.recipe.ImplementedRecipe;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.meatlib.util.NbtSerialisable;
import com.neep.meatweapons.particle.MWGraphicsEffects;
import com.neep.meatweapons.particle.MWParticles;
import com.neep.neepmeat.api.storage.WritableStackStorage;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.integrator.Integrator;
import com.neep.neepmeat.plc.component.MutateInPlace;
import com.neep.neepmeat.recipe.EnlighteningRecipe;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class PedestalBlockEntity extends SyncableBlockEntity
{
    protected WritableStackStorage storage;
    protected final RecipeBehaviour recipeBehaviour;
    protected boolean powered;
    public static final int MAX_COOLDOWN = 10;
    protected int cooldown;

    private final MIP mip = new MIP();

    public PedestalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.recipeBehaviour = new PedestalBlockEntity.RecipeBehaviour();
        this.storage = new WritableStackStorage(this::sync, 1)
        {
            @Override
            protected void onFinalCommit()
            {
                super.onFinalCommit();
                level.updateNeighbourForOutputSignal(pos, getBlockState().getBlock());
            }
        };
    }

    public PedestalBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.PEDESTAL, pos, state);
    }

    public void update(boolean redstone)
    {
        powered = redstone;
    }

    @Override
    public void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putInt("cooldown", cooldown);
        recipeBehaviour.writeNbt(nbt);
        storage.writeNbt(nbt);
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.cooldown = nbt.getInt("cooldown");
        recipeBehaviour.readNbt(nbt);
        storage.readNbt(nbt);
    }

    public void spawnParticles(int count, double dy, double speed)
    {
        if (level instanceof ServerLevel serverWorld)
        {
            serverWorld.sendParticles(MWParticles.PLASMA_PARTICLE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5 - 2, worldPosition.getZ() + 0.5, count, 0, dy, 0, 0.1);
        }
    }

    public static void spawnBeam(ServerLevel world, BlockPos startPos, BlockPos endPos)
    {
        Vec3 start = Vec3.upFromBottomCenterOf(startPos, 0.9);
        Vec3 end = Vec3.upFromBottomCenterOf(endPos, 0.8f);
        for (ServerPlayer player : PlayerLookup.around(world, start, 32d))
        {
            MWGraphicsEffects.syncBeamEffect(player, MWGraphicsEffects.BEAM, world, start, end, new Vec3(0, 0, 0), 0.5f, 50);
        }
    }

    public WritableStackStorage getStorage(@Nullable Direction dir)
    {
        return storage;
    }

    public void tick()
    {
        this.cooldown = Math.max(0, cooldown - 1);

        if (cooldown == 0)
        {
            cooldown = MAX_COOLDOWN;
            recipeBehaviour.update();
        }
    }

    public class RecipeBehaviour extends com.neep.meatlib.recipe.RecipeBehaviour<EnlighteningRecipe> implements ImplementedRecipe.DummyInventory, NbtSerialisable
    {
        public WritableStackStorage getStorage()
        {
            return storage;
        }
        protected BlockPos integrator = BlockPos.ZERO;

        @Override
        public void startRecipe(EnlighteningRecipe recipe)
        {
            setRecipe(recipe);

            getIntegrator().setLookPos(worldPosition);
            level.scheduleTick(worldPosition, getBlockState().getBlock(), 50);
//            world.playSound(null, pos, NMSounds.COSMIC_BEAM, SoundCategory.BLOCKS, 10, 0.8f);
            getIntegrator().spawnBeam(level, worldPosition);
//            spawnBeam((ServerWorld) world, integrator.up(), pos);
            sync();
        }

        @Override
        public void interrupt()
        {
            setRecipe(null);
        }

        @Override
        public void finishRecipe()
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                if (currentRecipe != null)
                    currentRecipe.ejectOutputs(this, transaction);

                transaction.commit();
            }
            setRecipe(null);
            sync();
        }

        public void update()
        {
            if (currentRecipe == null && !storage.isEmpty())
            {
                EnlighteningRecipe recipe = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.ENLIGHTENING, this).orElse(null);
                if (recipe != null)
                {
                    Integrator integrator = Integrator.findIntegrator(level, worldPosition, 10, recipe.getData());
                    if (integrator == null || !integrator.canEnlighten())
                        return;

                    this.integrator = integrator.getBlockPos();
                    startRecipe(recipe);
                    sync();
                }
            }
        }

        @Override
        public CompoundTag writeNbt(CompoundTag nbt)
        {
            super.writeNbt(nbt);
            nbt.put("integrator", NbtUtils.writeBlockPos(integrator));
            return nbt;
        }

        @Override
        public void readNbt(CompoundTag tag)
        {
            super.readNbt(tag);
            this.integrator = NbtUtils.readBlockPos(tag.getCompound("integrator"));
        }

        public Integrator getIntegrator()
        {
            if (level.getBlockEntity(integrator) instanceof Integrator be)
                return be;
            return null;
        }
    }

    public void extractFromItem(ItemEntity itemEntity)
    {
        ItemStack itemStack = itemEntity.getItem();
        if (itemStack.isEmpty())
            return;

        try (Transaction transaction = Transaction.openOuter())
        {
            int transferred = (int) storage.insert(ItemVariant.of(itemStack), itemStack.getCount(), transaction);
            itemStack.shrink(transferred);
            if (itemStack.getCount() <= 0)
            {
                itemEntity.discard();
            }

            transaction.commit();
        }
    }

    public MutateInPlace<ItemStack> getMutateInPlace(Void unused)
    {
        return mip;
    }

    public class MIP implements MutateInPlace<ItemStack>
    {
        @Override
        public ItemStack get()
        {
            return storage.getAsStack();
        }

        @Override
        public void set(ItemStack stack)
        {
            storage.setStack(stack);
        }

        @Override
        public BlockPos getOutputPos()
        {
            return worldPosition.above();
        }

        @Override
        public Level getWorld()
        {
            return level;
        }
    }
}
