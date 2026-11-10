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

package com.neep.neepmeat.entity;

import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.init.NMEntities;
import com.neep.neepmeat.machine.live_machine.block.LargestHopperBlock;
import com.neep.neepmeat.util.IterateRandomly;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ActiveWasteFallingBlockEntity extends FallingBlockEntity
{
    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(ActiveWasteFallingBlockEntity.class, EntityDataSerializers.BLOCK_POS);
    public int time;
    public boolean dropItem = true;
    @Nullable
    public CompoundTag blockData;
    private BlockState block = Blocks.SAND.defaultBlockState();
    private boolean destroyedOnLanding;
    private boolean hurtEntities;
    private int fallHurtMax = 40;
    private float fallHurtAmount;

    public ActiveWasteFallingBlockEntity(EntityType<? extends ActiveWasteFallingBlockEntity> entityType, Level world)
    {
        super(entityType, world);
    }

    private ActiveWasteFallingBlockEntity(Level world, double x, double y, double z, BlockState block)
    {
        this(NMEntities.FALLING_ACTIVE_WASTE, world);
        this.block = block;
        this.blocksBuilding = true;
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.setStartPos(this.blockPosition());
    }

    public static ActiveWasteFallingBlockEntity spawnFromBlock(ServerLevel serverWorld, BlockPos pos, BlockState state)
    {
        ActiveWasteFallingBlockEntity fallingBlockEntity = new ActiveWasteFallingBlockEntity(
                serverWorld,
                (double) pos.getX() + 0.5,
                (double) pos.getY(),
                (double) pos.getZ() + 0.5,
                state.hasProperty(BlockStateProperties.WATERLOGGED) ? state.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)) : state
        );
        serverWorld.setBlock(pos, state.getFluidState().createLegacyBlock(), Block.UPDATE_ALL);
        serverWorld.addFreshEntity(fallingBlockEntity);
        return fallingBlockEntity;
    }

    @Override
    public boolean isAttackable()
    {
        return false;
    }

    public BlockPos getStartPos()
    {
        return this.entityData.get(DATA_START_POS);
    }

    public void setStartPos(BlockPos pos)
    {
        this.entityData.set(DATA_START_POS, pos);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission()
    {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        this.builder.define(DATA_START_POS, BlockPos.ZERO);
    }

    @Override
    public boolean isPickable()
    {
        return !this.isRemoved();
    }

    private boolean canPlaceAt(Level world, BlockPos pos, BlockState state)
    {
        BlockState oldState = world.getBlockState(pos);

        if (oldState.is(Blocks.MOVING_PISTON))
            return false;

        boolean canReplace = world.getBlockState(pos).canBeReplaced(new DirectionalPlaceContext(this.level(), pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
//        boolean bl4 = FallingBlock.canFallThrough(this.getWorld().getBlockState(blockPos.down()));
        boolean canPlace = state.canSurvive(this.level(), pos);

        return canReplace && canPlace;
    }

    @Override
    public void tick()
    {
        if (this.block.isAir())
        {
            this.discard();
        }
        else
        {
            Block block = this.block.getBlock();
            ++this.time;
            if (!this.isNoGravity())
            {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());

            if (!this.level().isClientSide)
            {
                BlockPos blockPos = this.blockPosition();
                if (this.onGround())
                {
                    BlockState oldState = this.level().getBlockState(blockPos);
                    if (LargestHopperBlock.isLargeHopper(oldState)
                            || LargestHopperBlock.isLargeHopper(level().getBlockState(blockPos.below())))
                    {
                        discard();
                        this.callOnBrokenAfterFall(block, blockPos);
                        this.dropItemExact(block, 0);
                        return;
                    }

                    if (random.nextBoolean() && jump(level(), blockPos))
                    {
                    }
                    else
                    {
                        this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));

                        // Find the next viable place
                        BlockPos placePos = blockPos;
                        while (!canPlaceAt(level(), placePos, this.block))
                        {
                            placePos = placePos.above();
                        }

                        if (this.block.hasProperty(BlockStateProperties.WATERLOGGED) && this.level().getFluidState(placePos).getType() == Fluids.WATER)
                        {
                            this.block = this.block.setValue(BlockStateProperties.WATERLOGGED, Boolean.TRUE);
                        }

                        if (this.level().setBlock(placePos, this.block, Block.UPDATE_ALL))
                        {
                            ((ServerLevel) this.level())
                                    .getChunkSource()
                                    .chunkMap
                                    .broadcast(this, new ClientboundBlockUpdatePacket(placePos, this.level().getBlockState(placePos)));
                            this.discard();
                            if (block instanceof Fallable)
                            {
                                ((Fallable) block).onLand(this.level(), placePos, this.block, oldState, this);
                            }

                            if (this.blockData != null && this.block.hasBlockEntity())
                            {
                                BlockEntity blockEntity = this.level().getBlockEntity(placePos);
                                if (blockEntity != null)
                                {
                                    CompoundTag nbtCompound = blockEntity.saveWithoutMetadata();

                                    for (String string : this.blockData.getAllKeys())
                                    {
                                        nbtCompound.put(string, this.blockData.get(string).copy());
                                    }

                                    try
                                    {
                                        blockEntity.load(nbtCompound);
                                    }
                                    catch (Exception var15)
                                    {
                                        NeepMeat.LOGGER.error("Failed to load block entity from falling block", var15);
                                    }

                                    blockEntity.setChanged();
                                }
                            }
                        }
                        else if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))
                        {
                            this.discard();
                            this.callOnBrokenAfterFall(block, blockPos);
                            this.spawnAtLocation(block);
                        }
                    }
                }
                else if (!this.level().isClientSide
                        && (this.time > 100 && (blockPos.getY() <= this.level().getMinBuildHeight() || blockPos.getY() > this.level().getMaxBuildHeight()) || this.time > 600))
                {
                    if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS))
                    {
                        this.spawnAtLocation(block);
                    }

                    this.discard();
                }
            }

            this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        }
    }

    @Nullable
    public ItemEntity dropItemExact(ItemLike item, float yOffset)
    {
        if (this.level().isClientSide)
        {
            return null;
        }
        else
        {
            ItemStack stack = item.asItem().getDefaultInstance();
            ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY() + (double) yOffset, this.getZ(), stack, 0, 0, 0);
            itemEntity.setDefaultPickUpDelay();
            this.level().addFreshEntity(itemEntity);
            return itemEntity;
        }
    }

    private boolean jump(Level world, BlockPos pos)
    {
        Direction[] directions = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

        for (int i : new IterateRandomly(4))
        {
            BlockPos side = pos.relative(directions[i]);
            BlockPos sideDown = side.below();

            if (world.isEmptyBlock(side) && world.isEmptyBlock(sideDown))
            {
                setPosRaw(side.getX() + 0.5, getY(), side.getZ() + 0.5);
                setPos(side.getX() + 0.5, getY(), side.getZ() + 0.5);
                setStartPos(side);
                setOnGround(false);
                return true;
            }
        }

        return false;
    }

    @Override
    public BlockState getBlockState()
    {
        return block;
    }

    @Override
    public void setHurtsEntities(float fallHurtAmount, int fallHurtMax)
    {
        this.hurtEntities = true;
        this.fallHurtAmount = fallHurtAmount;
        this.fallHurtMax = fallHurtMax;
    }

    @Override
    public void callOnBrokenAfterFall(Block block, BlockPos pos)
    {
        if (block instanceof Fallable)
        {
            ((Fallable) block).onBrokenAfterFall(this.level(), pos, this);
        }
    }

    @Override
    public void disableDrop()
    {
        destroyedOnLanding = true;
    }

    @Override
    protected Component getTypeName()
    {
        return Component.translatable("entity.minecraft.falling_block_type", this.block.getBlock().getName());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(net.minecraft.server.level.ServerEntity entity)
    {
        return new ClientboundAddEntityPacket(this, Block.getId(this.getBlockState()));
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet)
    {
        super.recreateFromPacket(packet);
        this.block = Block.stateById(packet.getData());
        this.blocksBuilding = true;
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        this.setPos(d, e, f);
        this.setStartPos(this.blockPosition());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        nbt.put("BlockState", NbtUtils.writeBlockState(this.block));
        nbt.putInt("Time", this.time);
        nbt.putBoolean("DropItem", this.dropItem);
        nbt.putBoolean("HurtEntities", this.hurtEntities);
        nbt.putFloat("FallHurtAmount", this.fallHurtAmount);
        nbt.putInt("FallHurtMax", this.fallHurtMax);
        if (this.blockData != null)
        {
            nbt.put("TileEntityData", this.blockData);
        }

        nbt.putBoolean("CancelDrop", this.destroyedOnLanding);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {
        this.block = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), nbt.getCompound("BlockState"));
        this.time = nbt.getInt("Time");
        if (nbt.contains("HurtEntities", Tag.TAG_ANY_NUMERIC))
        {
            this.hurtEntities = nbt.getBoolean("HurtEntities");
            this.fallHurtAmount = nbt.getFloat("FallHurtAmount");
            this.fallHurtMax = nbt.getInt("FallHurtMax");
        }
        else if (this.block.is(BlockTags.ANVIL))
        {
            this.hurtEntities = true;
        }

        if (nbt.contains("DropItem", Tag.TAG_ANY_NUMERIC))
        {
            this.dropItem = nbt.getBoolean("DropItem");
        }

        if (nbt.contains("TileEntityData", Tag.TAG_COMPOUND))
        {
            this.blockData = nbt.getCompound("TileEntityData");
        }

        this.destroyedOnLanding = nbt.getBoolean("CancelDrop");
        if (this.block.isAir())
        {
            this.block = Blocks.SAND.defaultBlockState();
        }
    }
}
