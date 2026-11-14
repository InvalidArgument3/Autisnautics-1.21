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

package com.neep.neepmeat.entity.compressor_scutter;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.meatlib.screen.SimpleScreenHandlerFactory;
import com.neep.neepmeat.entity.scutter.ScutterEntity;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.machine.small_compressor.BurnerBehaviour;
import com.neep.neepmeat.machine.small_compressor.SmallCompressorBlockEntity;
import com.neep.neepmeat.machine.small_compressor.SmallCompressorScreenHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CompressorScutterEntity extends ScutterEntity implements FollowingScutter
{
    protected static final EntityDataAccessor<Boolean> LIT = SynchedEntityData.defineId(CompressorScutterEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(CompressorScutterEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private final ImplementedInventory inventory = ImplementedInventory.ofSize(1);
    private final BurnerBehaviour burner = new BurnerBehaviour(inventory, this::updateBlockState);

    private boolean sitting;

    public CompressorScutterEntity(EntityType<? extends ScutterEntity> type, Level world)
    {
        super(type, world);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        super.defineSynchedData(builder);
        builder.define(LIT, false);
        builder.define(OWNER_UUID, Optional.empty());
    }

    private void updateBlockState(boolean b)
    {
        entityData.set(LIT, b);
    }

    @Override
    protected void registerGoals()
    {
        super.registerGoals();
        goalSelector.addGoal(1, new ScutterFollowOwnerGoal(this, 1, 10, 2, true, 24));
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag nbt)
    {
        super.saveWithoutId(nbt);
        inventory.writeNbt(nbt);
        burner.writeNbt(nbt);

        if (this.getOwnerUuid() != null)
            nbt.putUUID("owner", this.getOwnerUuid());

        return nbt;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        inventory.readNbt(nbt);
        burner.readNbt(nbt);

        if (nbt.contains("owner"))
            setOwner(nbt.getUUID("owner"));
    }

    @Override
    public float getSpeed()
    {
        return 0.2f;
    }

    @Override
    public void tick()
    {
        super.tick();
        if (!level().isClientSide())
        {
            burner.tick();

            if (burner.getBurnTime() > 0)
            {
                SmallCompressorBlockEntity.powerEntities(level(), blockPosition());
            }
        }

        if (level().isClientSide() && entityData.get(LIT) && this.random.nextInt(4) == 0)
        {
            this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.8, this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitPos, InteractionHand hand)
    {
        if (player.isShiftKeyDown())
        {
            pickUp();

            return InteractionResult.SUCCESS;
        }
        else
        {
            player.openMenu((SimpleScreenHandlerFactory) ((s, i, p) ->
                    new SmallCompressorScreenHandler(i, inventory, s, burner.getDelegate())));
        }

        return super.interactAt(player, hitPos, hand);
    }

    @Override
    protected void writeStackNbt(CompoundTag nbt)
    {
        nbt.put("inventory", inventory.writeNbt(new CompoundTag()));
        nbt.put("burner", burner.writeNbt(new CompoundTag()));
    }

    @Override
    public void readStackNbt(CompoundTag nbt)
    {
        inventory.readNbt(nbt.getCompound("inventory"));
        burner.readNbt(nbt.getCompound("burner"));
    }

    @Override
    public @Nullable ItemStack getPickResult()
    {
        return NMItems.COMPRESSOR_SCUTTER.getDefaultInstance();
    }

    @Nullable
    public UUID getOwnerUuid()
    {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    public void setOwner(UUID uuid)
    {
        entityData.set(OWNER_UUID, Optional.of(uuid));
    }

    @Override
    @Nullable
    public LivingEntity getOwner()
    {
        UUID uUID = this.getOwnerUuid();
        return uUID == null ? null : this.level().getPlayerByUUID(uUID);
    }

    @Override
    public boolean isStatic()
    {
        return sitting;
    }
}
