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

package com.neep.neepmeat.machine.live_machine.block;

import com.neep.meatlib.item.ItemSettings;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.machine.live_machine.block.entity.ItemInputPortBlockEntity;
import com.neep.neepmeat.screen_handler.ItemInputScreenHandler;
import com.neep.neepmeat.transport.api.pipe.ItemPipeConnectable;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ItemInputPortBlock extends PortBlock<ItemInputPortBlockEntity> implements ItemPipeConnectable
{
    public ItemInputPortBlock(RegistrationContext ctx, ItemSettings itemSettings, Supplier<BlockEntityType<ItemInputPortBlockEntity>> factory, Properties settings)
    {
        super(ctx, itemSettings.factory(ItemPortBlockItem.forList(LivingMachines.ITEM_PORTS)), factory, settings);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof ItemInputPortBlockEntity be)
        {
            player.openMenu((SimpleScreenHandlerFactory) (syncId, playerInventory, p) ->
                    new ItemInputScreenHandler(playerInventory, be.getInventory(), syncId));
            return InteractionResult.SUCCESS;
        }
        return super.use(state, world, pos, player, hand, hit);
    }
}
