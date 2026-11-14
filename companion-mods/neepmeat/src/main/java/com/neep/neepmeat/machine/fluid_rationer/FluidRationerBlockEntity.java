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

package com.neep.neepmeat.machine.fluid_rationer;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepbus.block.entity.ConfigProvider;
import com.neep.neepbus.util.NeepBusConfig;
import com.neep.neepbus.util.SimpleEntry;
import com.neep.neepbus.util.WritePort;
import com.neep.neepbus.util.message.DataVariant;
import com.neep.neepmeat.api.FluidPump;
import com.neep.neepmeat.api.storage.DynamicLazyBlockApiCache;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.screen_handler.FluidRationerScreenHandler;
import com.neep.neepmeat.transport.FluidTransport;
import com.neep.neepmeat.transport.api.AutoEjector;
import com.neep.neepmeat.transport.fluid_network.node.AcceptorModes;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import com.neep.meatlib.transfer.FluidConstants;
import com.neep.meatlib.transfer.FluidStorage;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Storage;
import com.neep.meatlib.transfer.StorageView;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class FluidRationerBlockEntity extends SyncableBlockEntity implements ExtendedScreenHandlerFactory, ConfigProvider
{
    protected final FluidRationerStorage outputStorage;
    protected AcceptorModes inMode = AcceptorModes.PULL;
    protected AcceptorModes outMode = AcceptorModes.EXTRACT_ONLY;
    protected final FluidPump outPump = FluidPump.of(-0.1f, this::getOutMode);
    protected State state;

    private final WritePort writeAmount = this::receiveAmount;
    private final WritePort writeEject = this::receiveActivate;

    private boolean activateSignalReceived;

    protected final NeepBusConfig config = NeepBusConfig.builder(this::setChanged)
            .input(new SimpleEntry("Eject amount", "On data write:\nSet the desired eject amount to the received number."), writeAmount)
            .input(new SimpleEntry("Eject", "On data write:\nTry to consume and eject fluid."), writeEject)
            .build();

    protected final LazyBlockApiCache<Storage<FluidVariant>, Direction> cache;

    protected final AutoEjector<FluidVariant> ejector;

    protected int targetAmount = 81000;

    protected final ContainerData propertyDelegate = new ContainerData()
    {
        @Override
        public int get(int index)
        {
            return switch (index)
            {
                case 0 -> targetAmount;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value)
        {
            switch (index)
            {
                case 0 -> targetAmount = value;
            }
            setChanged();
        }

        @Override
        public int getCount()
        {
            return FluidRationerScreenHandler.PROPERTIES;
        }
    };

    public FluidRationerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.outputStorage = new FluidRationerStorage(FluidConstants.BUCKET * 4, this::setChanged);
        this.state = State.IDLE;
        this.cache = DynamicLazyBlockApiCache.facing(FluidStorage.SIDED, this, () -> getBlockState().getValue(FluidRationerBlock.FACING).getOpposite());
        this.ejector = AutoEjector.simpleFluid(() -> getBlockState().getValue(FluidRationerBlock.FACING), this, outputStorage, outPump);
    }

    public FluidRationerBlockEntity(BlockPos pos, BlockState state)
    {
        this(NMBlockEntities.FLUID_RATIONER, pos, state);
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, FluidRationerBlockEntity be)
    {
        be.tickInput();
    }

    protected void tickInput()
    {
        if (state == State.IDLE && level.getGameTime() % 2 == 0
                && (getBlockState().getValue(FluidRationerBlock.ACTIVE) || activateSignalReceived))
        {
            activateSignalReceived = false;

            try (Transaction transaction = Transaction.openOuter())
            {
                Storage<FluidVariant> storage = cache.find();

                if (storage == null)
                    return;

                for (StorageView<FluidVariant> view : storage)
                {
                    FluidVariant variant = view.getResource();
                    if (!view.isResourceBlank() && view.getAmount() >= targetAmount && outputStorage.matchesFilter(variant))
                    {
                        long extracted = view.extract(variant, targetAmount, transaction);
                        if (extracted == targetAmount)
                        {
                            if (outputStorage.insert(variant, targetAmount, transaction) == targetAmount)
                            {
                                state = State.PUSHING;
                                inMode = AcceptorModes.NONE;
                                outMode = AcceptorModes.PUSH;
                                transaction.commit();
                                break;
                            }
                        }
                    }
                }
            }
        }
        else if (state == State.PUSHING)
        {
            ejector.tick(null);

            if (outputStorage.getAmount() == 0)
            {
                state = State.IDLE;
                inMode = AcceptorModes.PULL;
                outMode = AcceptorModes.EXTRACT_ONLY;
            }
        }
    }

    private AcceptorModes getInMode()
    {
        return inMode;
    }

    private AcceptorModes getOutMode()
    {
        return outMode;
    }

    public Storage<FluidVariant> getStorage(Direction direction)
    {
        Direction facing = getBlockState().getValue(FluidRationerBlock.FACING);
        if (direction == null || direction == facing)
        {
            return outputStorage;
        }
        return null;
    }

    public FluidPump getPump(Direction direction)
    {
        Direction facing = getBlockState().getValue(FluidRationerBlock.FACING);
        if (direction == facing)
        {
            return outPump;
        }
        return null;
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        outputStorage.toNbt(nbt);
        nbt.putInt("state", state.ordinal());
        nbt.putInt("inMode", inMode.ordinal());
        nbt.putInt("outMode", outMode.ordinal());
        nbt.putInt("targetAmount", targetAmount);

        nbt.put("config", config.writeNbt(new CompoundTag()));
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        outputStorage.readNbt(nbt);
        this.state = State.values()[nbt.getInt("state")];
        this.inMode = AcceptorModes.values()[nbt.getInt("inMode")];
        this.outMode = AcceptorModes.values()[nbt.getInt("outMode")];
        this.targetAmount = nbt.getInt("targetAmount");

        config.readNbt(nbt.getCompound("config"));
    }

    @Override
    public Component getDisplayName()
    {
        return FluidTransport.FLUID_RATIONER.getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player)
    {
        return new FluidRationerScreenHandler(syncId, inv, outputStorage.inventory, propertyDelegate, config);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf)
    {
        buf.writeVarInt(targetAmount);
    }

    private void receiveAmount(DataVariant amount)
    {
        this.targetAmount = Math.max(0, amount.intValue());
        setChanged();
    }

    private void receiveActivate(DataVariant value)
    {
        if (value.floatValue() > 0)
            activateSignalReceived = true;
    }

    @Override
    public NeepBusConfig getConfig()
    {
        return config;
    }

    public enum State
    {
        IDLE,
        PUSHING
    }
}
