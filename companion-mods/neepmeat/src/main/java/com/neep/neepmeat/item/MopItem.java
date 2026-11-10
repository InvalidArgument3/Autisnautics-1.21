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

package com.neep.neepmeat.item;

import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatweapons.item.WeakTwoHanded;
import com.neep.neepmeat.datagen.tag.NMTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class MopItem extends BaseItem implements WeakTwoHanded
{
    public MopItem(TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(tooltipSupplier, settings);
    }

    public int getUseDuration(ItemStack stack)
    {
        return 40;
    }

    @Override
    public boolean displayArmFirstPerson(ItemStack stack, InteractionHand hand)
    {
        return false;
    }

    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack)
    {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        var itemStack = user.getItemInHand(hand);
        BlockHitResult result = getPlayerPOVHitResult(world, user, ClipContext.Fluid.ANY);

        if (result.getType() == HitResult.Type.BLOCK)
        {
            BlockPos blockPos = result.getBlockPos();
            Direction direction = result.getDirection();
            BlockPos blockPos2 = blockPos.relative(direction);

            if (!world.mayInteract(user, blockPos) || !user.mayUseItemAt(blockPos2, direction, itemStack))
                return InteractionResultHolder.fail(itemStack);

            boolean removed = tryRemove(world, blockPos, world.getBlockState(blockPos));

            BlockPos.MutableBlockPos mutable = blockPos.mutable();
            for (Direction face : Direction.Plane.HORIZONTAL)
            {
                mutable.setWithOffset(blockPos, face);
                var adjState = world.getBlockState(mutable);
                if (tryRemove(world, mutable, adjState))
                    removed = true;
            }

            if (removed)
                return InteractionResultHolder.success(itemStack);
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.NONE;
    }

    private boolean tryRemove(Level world, BlockPos pos, BlockState state)
    {
        var fluidState = state.getFluidState();
        if (state.is(NMTags.MOPPABLE) && fluidState.createLegacyBlock().getBlock() instanceof LiquidBlock drainable)
        {
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            world.playSound(
                    null, pos, drainable.getPickupSound().orElse(SoundEvents.BUCKET_FILL), SoundSource.PLAYERS,
                    (float) world.random.triangle(0.9, 0.1),
                    (float) world.random.triangle(1.4, 0.1));
            return true;
        }

        return false;
    }
}
