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

package com.neep.neepbus.block;

import com.neep.meatlib.block.BaseBlock;
import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepbus.block.entity.ConfigProvider;
import com.neep.neepbus.screen.NeepBusConfigScreenHandler;
import com.neep.neepbus.util.*;
import com.neep.neepbus.util.message.DataVariant;
import com.neep.neepmeat.transport.api.pipe.DataCable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PortTestBlock extends BaseBlock implements NeepBusProvider, DataCable, EntityBlock
{
    public PortTestBlock(RegistrationContext ctx, Properties settings)
    {
        super(ctx, settings);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (world.getBlockEntity(pos) instanceof PortTestBlockEntity be)
        {
            if (player.isShiftKeyDown())
            {
                player.openMenu(new ExtendedScreenHandlerFactory()
                {
                    @Override
                    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf)
                    {
                        NeepBusConfigScreenHandler.writeOpeningData(be.config, buf);
                    }

                    @Override
                    public Component getDisplayName()
                    {
                        return Component.empty();
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player)
                    {
                        return new NeepBusConfigScreenHandler(playerInventory, syncId, be.config);
                    }
                });
                return InteractionResult.SUCCESS;
            }
            else if (!world.isClientSide())
            {
                be.send();
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public void networkChanged(Level world, BlockPos pos, BlockPos whereChanged)
    {
        if (world.getBlockEntity(pos) instanceof PortTestBlockEntity be)
        {
            be.sender.invalidate();
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
//        return NeepBus.PORT_TEST_BE.instantiate(pos, state);
        return null;
    }

    public static class PortTestBlockEntity extends SyncableBlockEntity implements ConfigProvider
    {
        private int counter;

        private final WritePort inputPort = data ->
        {
            for (Player player : getLevel().players())
            {
                player.displayClientMessage(Component.nullToEmpty(data.stringValue()), false);
            }
        };

        private final CachingSender sender = new CachingSender(this::getLevel, getBlockPos());

        private final DirectReadPort outputPort = new DirectReadPort(new SimpleEntry("brine"), () -> DataVariant.of(counter), sender::send);

//        private final NeepBusConfig config = new NeepBusConfigImpl(
//                List.of(new NeepBusConfig.SimpleEntry("ooer")),
//                List.of(outputPort.entry()),
//                List.of(inputPort),
//                inputPort::invalidateAddress,
//                this::markDirty,
//                applyChanges);

        private final NeepBusConfig config = NeepBusConfig.builder(this::setChanged)
                .input(new SimpleEntry("ooer"), inputPort)
                .output(outputPort.entry(), outputPort)
                .applyChanges(this)
                .build();

        public void send()
        {
            outputPort.send();
            counter++;
        }

        public PortTestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
        {
            super(type, pos, state);
        }

        @Override
        public void saveAdditional(CompoundTag nbt)
        {
            super.saveAdditional(nbt);
            nbt.put("config", config.writeNbt(new CompoundTag()));
        }

        @Override
        public void load(CompoundTag nbt)
        {
            super.load(nbt);
            this.config.readNbt(nbt.getCompound("config"));
        }

        @Override
        public NeepBusConfig getConfig()
        {
            return config;
        }
    }
}
