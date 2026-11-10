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

package com.neep.neepmeat.machine.multitank;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.meatlib.transfer.MultiFluidBuffer;
import com.neep.neepmeat.client.screen.tablet.graph.GraphData;
import com.neep.neepmeat.client.screen.tablet.graph.GraphRegistry;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.Heatable;
import com.neep.neepmeat.recipe.FluidHeatingRecipe;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MultiTankBlockEntity extends SyncableBlockEntity implements Heatable
{
    public static final float MIN_PU = 0.04f;
    public static final float MAX_INCREMENT = 2;

    protected MultiFluidBuffer buffer;
    protected float heat;
    protected ResourceLocation currentRecipeId;
    protected float recipeProgress;
    protected float increment;

    public MultiTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.buffer = new MultiFluidBuffer(8 * FluidConstants.BUCKET, variant -> true, this::sync);
    }

    public MultiTankBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.MULTI_TANK, pos, state);
    }

    public static GraphData makeHeatingPowerGraph()
    {
        long perRecipeAmount = 9000;
        float workUnits = 5;

        // Find the conversion time for 1 bucket
        long droplets = FluidConstants.BUCKET;
        float referenceWork = (droplets / (float) perRecipeAmount) * workUnits;

        return GraphRegistry.arbitraryStandardPower(MIN_PU, MAX_INCREMENT, referenceWork * 1000)
                .setTitle("Heating time for 1 milk bucket");
    }

    public MultiFluidBuffer getStorage()
    {
        return buffer;
    }

    public boolean onUse(Player player, InteractionHand hand)
    {
        if (buffer.handleInteract(level, player, hand))
        {
            return true;
        }
        if (!level.isClientSide())
        {
            showContents((ServerPlayer) player, level, getBlockPos(), getStorage());
            return true;
        }
        return true;
    }

    public static void showContents(ServerPlayer player, Level world, BlockPos pos, MultiFluidBuffer buffer)
    {
        world.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1f, 1.5f);
        long mb = Math.floorDiv(buffer.getTotalAmount(), FluidConstants.BUCKET / 1000);
        player.displayClientMessage(Component.nullToEmpty("Fluid" + ": " + mb + "mb"), true);
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        buffer.writeNbt(nbt);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        buffer.readNbt(nbt);
    }

    @Override
    public void setBurning()
    {

    }

    @Override
    public void setHeat(float heat)
    {
        this.heat = heat;
        if (heat > MIN_PU)
            this.increment = Mth.clamp(heat, 0, MAX_INCREMENT);
        else
            this.increment = 0;
    }

//    @Override
//    public float getHeat()
//    {
//        return heat;
//    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, MultiTankBlockEntity be)
    {
        if (be.currentRecipeId != null)
        {
            be.recipeProgress = Math.max(0, be.recipeProgress - be.increment);
            if (be.recipeProgress == 0)
            {
                MeatlibRecipes.getInstance().get(NMrecipeTypes.HEATING, be.currentRecipeId).ifPresent(recipe ->
                {
                    try (Transaction transaction = Transaction.openOuter())
                    {
                        if (recipe.takeInputs(be, transaction) && recipe.ejectOutputs(be, transaction))
                        {
                            transaction.commit();
                            be.currentRecipeId = null;
                            return;
                        }
                        transaction.abort();
                    }
                });
            }
        }

        if (be.heat > 0 && be.currentRecipeId == null)
        {
            FluidHeatingRecipe recipe = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.HEATING, be).orElse(null);
            if (recipe != null)
            {
                be.currentRecipeId = recipe.getId();
                be.recipeProgress = recipe.getTime();
            }
        }
    }
}