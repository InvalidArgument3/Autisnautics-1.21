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

package com.neep.neepmeat.machine.integrator;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.DataResource;
import com.neep.neepmeat.api.data.DataUtil;
import com.neep.neepmeat.init.NMBlockEntities;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.init.NMSounds;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class IntegratorBlockEntity extends SyncableBlockEntity implements Integrator, GeoBlockEntity
{
    protected int growthTimeRemaining = 1000;
    protected final IntegratorStorage storage;

    protected BlockPos lookTarget = new BlockPos(0, 0, 0);
    public boolean isMature = false;
    public float facing = 0f;
    public float targetFacing = 0f;

    public static final long MAX_DATA = 8 * DataUtil.GIEB;
    protected long data;

    public final long animationOffset = (long) (Math.random() * 20);

    protected SnapshotParticipant<Long> dataSnapshot = new SnapshotParticipant<>()
    {
        @Override
        protected Long createSnapshot()
        {
            return data;
        }

        @Override
        protected void readSnapshot(Long snapshot)
        {
            data = snapshot;
        }

        @Override
        protected void onFinalCommit()
        {
            super.onFinalCommit();
            sync();
        }
    };

    private final AnimatableInstanceCache instanceCache = GeckoLibUtil.createInstanceCache(this);
    private boolean hatching;

    public IntegratorBlockEntity(BlockPos pos, BlockState state)
    {
        super(NMBlockEntities.INTEGRATOR, pos, state);
        this.storage = new IntegratorStorage(this);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return saveWithoutMetadata();
    }

    @Override
    public void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putInt("growth_remaining", growthTimeRemaining);
        tag.putBoolean("fully_grown", isMature);
        tag.put("lookTarget", NbtUtils.writeBlockPos(lookTarget));
        tag.putLong("data", data);
        storage.writeNbt(tag);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        growthTimeRemaining = tag.getInt("growth_remaining");
        isMature = tag.getBoolean("fully_grown");
        this.lookTarget = NbtUtils.readBlockPos(tag.getCompound("lookTarget"));
        this.data = tag.getLong("data");
        storage.readNbt(tag);
    }

    public static void serverTick(Level world, BlockPos blockPos, BlockState blockState, IntegratorBlockEntity be)
    {
        if (be.canGrow())
        {
            be.grow();
            be.sync();
        }

        if (be.isMature)
        {
            be.data = Math.min(MAX_DATA, be.data + 1);
            if ((world.getGameTime() % 20) == 0) be.sync();

            if ((world.getGameTime() % 60) == 0) be.pointToEntity();
        }
    }

    public void pointToEntity()
    {
        if (level.getBlockEntity(worldPosition) instanceof IntegratorBlockEntity be)
        {
            AABB box = new AABB(worldPosition.getX() - 2, worldPosition.getY() - 2, worldPosition.getZ() - 2, worldPosition.getX() + 3, worldPosition.getY() + 3, worldPosition.getZ() + 3);
            List<Entity> players = be.getLevel().getEntities(EntityTypeTest.forClass(Entity.class), box, (e) -> true);
            if (players.size() > 0)
            {
                be.setLookPos(players.get(0).blockPosition());
                sync();
            }
        }
    }

    public boolean canGrow()
    {
        return !isMature && storage.immatureStorage.getAmount() == storage.immatureStorage.capacity;
    }

    public void grow()
    {
        if (storage.immatureStorage.getAmount() >= storage.immatureStorage.getCapacity())
            --growthTimeRemaining;

        if (growthTimeRemaining <= 0)
        {
            isMature = true;
        }
    }

    public static final Map<Item, Integer> DATA_MAP = Map.of(
            NMItems.WHISPER_FLOUR, 500
    );

    public boolean takeFromHand(Player player, InteractionHand hand)
    {
        ItemStack handStack = player.getItemInHand(hand);
        Integer inc = DATA_MAP.get(handStack.getItem());
        if (inc != null)
        {
            try (Transaction transaction = Transaction.openOuter())
            {
                float ins = insertEnlightenment(inc, transaction);
                if (ins == inc)
                {
                    transaction.commit();
                    level.playSound(null, worldPosition, SoundEvents.HORSE_EAT, SoundSource.BLOCKS, 1, 1);
                    handStack.shrink(1);
                    return true;
                }
                else transaction.abort();

            }
        }
        return false;
    }

    public void showContents(ServerPlayer player)
    {
        if (!canEnlighten())
        {
            int percent = (int) (storage.immatureStorage.getAmount() / (float) FluidConstants.BUCKET * 100);
            player.displayClientMessage(Component.nullToEmpty("Blood: " + percent + "%"), true);
        }
        else
        {
            player.displayClientMessage(Component.translatable("message." + NeepMeat.NAMESPACE + ".integrator.data",
                    DataUtil.formatData(data),
                    DataUtil.formatData(MAX_DATA)), true);
        }

    }

    private <E extends GeoBlockEntity> PlayState predicate(AnimationState<E> event)
    {
        event.getController().transitionLength(20);
        if (this.hatching)
        {
            event.getController().setAnimation(RawAnimation.begin().thenPlay("animation.integrator.hatch"));
            hatching = false;
        }
        else
        {
            event.getController().setAnimation(RawAnimation.begin().thenLoop("animation.integrator.flex"));
        }

        return PlayState.CONTINUE;
    }

    public Storage<FluidVariant> getStorage(Level world, BlockPos pos, BlockState state, Direction direction)
    {
        return storage.getFluidStorage(world, pos, state, direction);
    }

    public Storage<ItemVariant> getItemStorage(Direction direction)
    {
        return storage.itemStorage;
    }

    public float insertEnlightenment(float maxAmount, TransactionContext transaction)
    {
        float inserted = Math.min(MAX_DATA - data, maxAmount);
        if (inserted > 0)
        {
            dataSnapshot.updateSnapshots(transaction);
            data += inserted;
            return inserted;
        }
        return 0;
    }

    @Override
    public boolean canEnlighten()
    {
        return isMature;
    }

    @Override
    public BlockPos getBlockPos() { return getBlockPos(); }


    @Override
    public void setLookPos(BlockPos pos)
    {
        this.lookTarget = pos;
        sync();
    }

    @Override
    public void spawnBeam(Level world, BlockPos pos)
    {
        Integrator.spawnBeam((ServerLevel) world, getBlockPos().above(), pos);
        world.playSound(null, pos, NMSounds.COSMIC_BEAM, SoundSource.BLOCKS, 1, 0.8f);
    }

    @Override
    public long getData(DataResource variant)
    {
        return data;
    }

    @Override
    public float extract(DataResource variant, long amount, TransactionContext transaction)
    {
        long extracted = Math.min(amount, data);
        if (extracted > 0)
        {
            dataSnapshot.updateSnapshots(transaction);
            data -= extracted;
            return extracted;
        }
        return 0;
    }

    public Vec3 getLookTarget()
    {
        return Vec3.atCenterOf(lookTarget);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache()
    {
        return instanceCache;
    }
}
