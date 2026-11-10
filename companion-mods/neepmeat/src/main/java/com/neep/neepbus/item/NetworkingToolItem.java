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

package com.neep.neepbus.item;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.api.DataPort;
import com.neep.neepmeat.init.NMBlocks;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class NetworkingToolItem extends BaseItem
{
    public NetworkingToolItem(RegistrationContext registryName, TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(tooltipSupplier, settings.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Player player = context.getPlayer();
        if (player == null || context.getLevel().isClientSide())
            return super.useOn(context);

        // Clear data
        if (context.getPlayer().isShiftKeyDown())
        {
            context.getItemInHand().removeTagKey("networking");

            return InteractionResult.SUCCESS;
        }

        DataPort port = DataPort.DATA_PORT.find(context.getLevel(), context.getClickedPos(), null);

        if (port != null)
        {
            CompoundTag nbt = context.getItemInHand().getOrCreateTagElement("networking");

            if (!nbt.contains("first"))
            {
                nbt.put("first", NbtUtils.writeBlockPos(context.getClickedPos()));
                nbt.putString("first_name", context.getLevel().getBlockState(context.getClickedPos()).getBlock().getName().getString());
                BlockPos first = context.getClickedPos();
                context.getPlayer().sendSystemMessage(Component.translatable(getDescriptionId() + ".from", first.getX(), first.getY(), first.getZ()));
            }
            else
            {
                BlockPos first = NbtUtils.readBlockPos(nbt.getCompound("first"));
                BlockPos second = context.getClickedPos();
                DataPort firstPort = DataPort.DATA_PORT.find(context.getLevel(), first, null);
                if (firstPort != null && findRoute(context.getLevel(), first, second, 20))
                {
                    firstPort.setTarget(second);
                    context.getPlayer().sendSystemMessage(Component.translatable(getDescriptionId() + ".connected",
                            first.getX(), first.getY(), first.getZ(),
                            second.getX(), second.getY(), second.getZ()));
                    context.getItemInHand().removeTagKey("networking");
                }
                else
                {
                    player.displayClientMessage(Component.nullToEmpty("Connection failed: no route"), false);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    private boolean findRoute(Level world, BlockPos from, BlockPos to, int maxDist)
    {
        Queue<BlockPos> queue = Queues.newArrayDeque();
        Set<BlockPos> visited = Sets.newHashSet();
        queue.add(from);

        while (!queue.isEmpty())
        {
            BlockPos current = queue.poll();
            BlockPos.MutableBlockPos mutable = current.mutable();
            for (Direction direction : Direction.values())
            {
                mutable.setWithOffset(current, direction);

                if (mutable.equals(to))
                {
                    return true;
                }

                if (from.distManhattan(mutable) > maxDist || visited.contains(mutable))
                    continue;

                if (world.getBlockState(mutable).is(NMBlocks.DATA_CABLE))
                {
                    queue.add(mutable.immutable());
                    visited.add(mutable.immutable());
                }
            }
        }
        return false;
    }

    public void appendHoverText(ItemStack itemStack, Level world, List<Component> tooltip, TooltipFlag tooltipContext)
    {
        super.appendHoverText(itemStack, world, tooltip, tooltipContext);
        CompoundTag nbt = itemStack.getTagElement("networking");
        if (nbt != null)
        {
            String name = nbt.getString("first_name");
            BlockPos pos = NbtUtils.readBlockPos(nbt.getCompound("first"));
            tooltip.add(Component.translatable(getDescriptionId() + ".from", pos.getX(), pos.getY(), pos.getZ()));
        }
    }
}
