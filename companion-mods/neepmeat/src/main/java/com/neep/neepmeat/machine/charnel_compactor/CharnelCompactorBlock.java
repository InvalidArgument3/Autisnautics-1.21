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

package com.neep.neepmeat.machine.charnel_compactor;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.machine.integrator.Integrator;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CharnelCompactorBlock extends BaseBlock implements DataCable
{
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_COMPOSTER;
    protected static final VoxelShape OUTLINE_SHAPE;
    private static final VoxelShape RAYCAST_SHAPE = box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);

    static
    {
        OUTLINE_SHAPE = Shapes.join(Shapes.block(), RAYCAST_SHAPE, BooleanOp.ONLY_FIRST);
    }

    public CharnelCompactorBlock(RegistrationContext ctx, ItemSettings itemSettings, Properties settings)
    {
        super(ctx, itemSettings, settings);
        this.registerDefaultState((this.stateDefinition.any()).setValue(LEVEL, 0));
    }

    public static float getIncreaseChance(Item item)
    {
        return item.getDefaultInstance().is(NMTags.CHARNEL_COMPACTOR) ? 1 : 0;
    }

    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit)
    {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        int i = state.getValue(LEVEL);
        ItemStack itemStack = player.getItemInHand(hand);
        float chance = getIncreaseChance(itemStack.getItem());
        Integrator integrator = Integrator.findIntegrator(world, pos, 10, 0);
        if (i < 8 && chance > 0)
        {
            if (integrator != null && integrator.canEnlighten())
            {
                if (i < 7 && !world.isClientSide)
                {
                    CharnelCompactorStorage.addLevel(new CharnelCompactorStorage.WorldLocation(world, pos));

                    player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                    if (!player.getAbilities().instabuild)
                    {
                        itemStack.shrink(1);
                    }
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
            else if (integrator == null)
            {
                player.displayClientMessage(Component.translatable("message." + NeepMeat.NAMESPACE + ".compactor.not_found"), true);
            }
            else if (!integrator.canEnlighten())
            {
                player.displayClientMessage(Component.translatable("message." + NeepMeat.NAMESPACE + ".compactor.immature"), true);
            }
        }

        if (i == 8)
        {
            CharnelCompactorStorage.extractOutput(new CharnelCompactorStorage.WorldLocation(world, pos), true);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify)
    {
        if (state.getValue(LEVEL) == 7)
        {
            world.scheduleTick(pos, state.getBlock(), 20);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
    {
        if (state.getValue(LEVEL) == 7)
        {
            world.setBlock(pos, state.cycle(LEVEL), Block.UPDATE_ALL);
            world.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(LEVEL);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state)
    {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos)
    {
        return state.getValue(LEVEL);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
    {

        super.entityInside(state, world, pos, entity);
        if (!world.isClientSide() && entity instanceof ItemEntity item && entity.onGround())
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                ItemStack stack = item.getItem();
                long inserted = CharnelCompactorStorage.getStorage(world, pos, Direction.UP).insert(ItemVariant.of(stack), stack.getCount(), transaction);
                stack.shrink((int) inserted);
                transaction.commit();
            }
        }
    }

    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
    {
        return OUTLINE_SHAPE;
    }
}
