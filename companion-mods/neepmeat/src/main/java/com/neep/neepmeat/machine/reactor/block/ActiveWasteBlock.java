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

package com.neep.neepmeat.machine.reactor.block;

import com.neep.meatlib.block.MeatlibBlock;
import com.neep.meatlib.item.BaseBlockItem;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.entity.ActiveWasteFallingBlockEntity;
import com.neep.neepmeat.machine.live_machine.block.LargestHopperBlock;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ActiveWasteBlock extends FallingBlock implements MeatlibBlock
{
    public ActiveWasteBlock(RegistrationContext ctx, Properties settings)
    {
        super(settings);
        ctx.appendItem(this, new BaseBlockItem(this, ctx, ItemSettings.block()));
    }

    private static void dropStackHere(Level world, BlockPos pos, ItemStack stack)
    {
        double d = EntityType.ITEM.getHeight() / 2.0;
        double e = pos.getX() + 0.5;
        double f = pos.getY() + 0.5 - d;
        double g = pos.getZ() + 0.5;
        popResource(world, () -> new ItemEntity(world, e, f, g, stack, 0, 0, 0), stack);
    }

    private static void popResource(Level world, Supplier<ItemEntity> itemEntitySupplier, ItemStack stack)
    {
        if (!world.isClientSide && !stack.isEmpty() && world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS))
        {
            ItemEntity itemEntity = itemEntitySupplier.get();
            world.addFreshEntity(itemEntity);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
    {
        BlockState down = world.getBlockState(pos.below());
        if (LargestHopperBlock.isLargeHopper(down))
        {
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

            getDrops(state, world, pos, null, null, ItemStack.EMPTY)
                    .forEach(stack -> dropStackHere(world, pos, stack));
        }
        else
        {
            if (isFree(down) && pos.getY() >= world.getMinBuildHeight())
            {
                ActiveWasteFallingBlockEntity fallingBlockEntity = ActiveWasteFallingBlockEntity.spawnFromBlock(world, pos, state);
                this.configureFallingBlockEntity(fallingBlockEntity);
            }
        }
    }

    protected void configureFallingBlockEntity(ActiveWasteFallingBlockEntity entity)
    {
    }
}
