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

package com.neep.neepmeat.transport.util;

import com.neep.meatlib.transfer.InventoryStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import com.neep.neepmeat.transport.block.item_transport.ItemPipeBlock;
import com.neep.neepmeat.transport.block.item_transport.entity.ItemPipeBlockEntity;
import com.neep.neepmeat.transport.item_network.PipeRouteFinder;
import com.neep.neepmeat.transport.ItemRoute;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Moves a stack into the block the port faces: an item pipe, an item handler, or the world.
 * A filter pipe rejects an item before anything is inserted, matching an empty filter
 * that allows every item and a populated filter that allows only its entries.
 */
public final class ItemPipeUtil {
    private ItemPipeUtil() {}

    public static boolean canDumpInto(Level level, BlockPos pos, BlockState state) {
        return state.isAir() || state.canBeReplaced();
    }

    public static final class EjectOperation {
        private final ServerLevel world;
        private final BlockPos pos;
        private final Direction out;
        private boolean routing = true;
        private long amount;

        public static EjectOperation of(Level level, BlockPos pos, Direction facing) {
            return new EjectOperation((ServerLevel) level, pos, facing);
        }

        private EjectOperation(ServerLevel world, BlockPos pos, Direction out) {
            this.world = world;
            this.pos = pos;
            this.out = out;
        }

        public EjectOperation useRouting(boolean routing) {
            this.routing = routing;
            return this;
        }

        public EjectOperation pipeToAny(com.neep.neepmeat.transport.item_network.ItemInPipe item, TransactionContext transaction) {
            BlockPos offset = pos.relative(out);
            BlockState state = world.getBlockState(offset);
            if (state.getBlock() instanceof ItemPipeBlock && world.getBlockEntity(offset) instanceof ItemPipeBlockEntity pipe
                    && !pipe.allows(item.resource().toStack((int) Math.min(item.amount(), Integer.MAX_VALUE)))) {
                this.amount = 0;
                return this;
            }
            return stackToAny(item.resource(), item.amount(), transaction);
        }

        public EjectOperation stackToAny(ItemVariant resource, long amount, TransactionContext transaction) {
            this.amount = move(resource, amount, transaction);
            return this;
        }

        public long amount() {
            return amount;
        }

        private long move(ItemVariant resource, long requested, TransactionContext transaction) {
            if (resource.isBlank() || requested <= 0) {
                return 0;
            }
            int count = (int) Math.min(requested, Integer.MAX_VALUE);
            ItemStack stack = resource.toStack(count);
            BlockPos offset = pos.relative(out);
            BlockState state = world.getBlockState(offset);
            if (state.isAir() || state.canBeReplaced()) {
                ItemEntity entity = new ItemEntity(world, offset.getX() + 0.5, offset.getY() + 0.5, offset.getZ() + 0.5, stack.copy());
                entity.setDeltaMovement(out.getStepX() * 0.2, out.getStepY() * 0.2, out.getStepZ() * 0.2);
                world.addFreshEntity(entity);
                if (transaction instanceof Transaction open) {
                    open.snapshot(entity::discard);
                }
                return count;
            }
            if (state.getBlock() instanceof ItemPipeBlock && world.getBlockEntity(offset) instanceof ItemPipeBlockEntity pipe) {
                ItemRoute route = ItemRoute.EMPTY;
                if (routing) {
                    route = PipeRouteFinder.find(world, offset, out.getOpposite(), stack);
                }
                int inserted = pipe.insert(stack, out.getOpposite(), route);
                return inserted;
            }
            if (world.getBlockEntity(offset) instanceof Container container) {
                return InventoryStorage.of(container, out.getOpposite()).insert(resource, count, transaction);
            }
            IItemHandler handler = world.getCapability(Capabilities.ItemHandler.BLOCK, offset, out.getOpposite());
            if (handler == null) {
                return 0;
            }
            ItemStack leftover = ItemHandlerHelper.insertItem(handler, stack.copy(), false);
            int inserted = count - leftover.getCount();
            if (inserted > 0 && transaction instanceof Transaction open) {
                ItemStack taken = stack.copyWithCount(inserted);
                open.snapshot(() -> extractBack(handler, taken));
            }
            return inserted;
        }

        private static void extractBack(IItemHandler handler, ItemStack taken) {
            int left = taken.getCount();
            for (int slot = 0; slot < handler.getSlots() && left > 0; slot++) {
                ItemStack simulated = handler.extractItem(slot, left, true);
                if (!ItemStack.isSameItemSameComponents(simulated, taken)) {
                    continue;
                }
                ItemStack extracted = handler.extractItem(slot, simulated.getCount(), false);
                left -= extracted.getCount();
            }
        }
    }
}
