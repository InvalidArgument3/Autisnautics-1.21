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

package com.neep.neepmeat.machine.mixer;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.block.MeatlibBlockSettings;
import com.neep.meatlib.block.multi.TallBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.util.ItemUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MixerBlock extends TallBlock implements EntityBlock
{
    public MixerBlock(RegistrationContext registryName, ItemSettings itemSettings, Properties settings)
    {
        super(registryName, itemSettings, settings.noOcclusion());
    }

    @Override
    protected Structure createStructure(RegistrationContext ctx)
    {
        return ctx.append(this, new Structure(MeatlibBlockSettings.copyOf(properties))
        {
            @Override
            protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
            {
                if (ItemUtil.playerHoldingPipe(player, hand))
                    return super.useWithoutItem(state, world, pos, player, hit);

                if (world.getBlockEntity(pos.below()) instanceof MixerBlockEntity be)
                    return InteractionResult.sidedSuccess(ItemUtil.singleVariantInteract(player, hand, be.getItemStorage(null)));

                return super.useWithoutItem(state, world, pos, player, hit);
            }
        }, MeatlibBlock::structure);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (ItemUtil.playerHoldingPipe(player, hand))
            return super.useWithoutItem(state, world, pos, player, hit);

        if (world.getBlockEntity(pos) instanceof MixerBlockEntity be)
            return InteractionResult.sidedSuccess(ItemUtil.singleVariantInteract(player, hand, be.getItemStorage(null)));

        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        super.setPlacedBy(world, pos, state, placer, itemStack);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved)
    {
        if (!newState.is(state.getBlock()))
        {
            if (world.getBlockEntity(pos) instanceof MixerBlockEntity be)
            {
                be.dropItems();
            }
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new MixerBlockEntity(pos, state);
    }

}