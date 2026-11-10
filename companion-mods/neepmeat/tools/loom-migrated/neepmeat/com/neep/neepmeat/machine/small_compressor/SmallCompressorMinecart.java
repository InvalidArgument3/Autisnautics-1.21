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

package com.neep.neepmeat.machine.small_compressor;

import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.init.NMEntities;
import com.neep.neepmeat.init.NMItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SmallCompressorMinecart extends AbstractMinecart implements MenuProvider
{
    private final ImplementedInventory inventory = ImplementedInventory.ofSize(1);
    private final BurnerBehaviour burner = new BurnerBehaviour(inventory, this::updateBlockState);

    public SmallCompressorMinecart(EntityType<?> entityType, Level world)
    {
        super(entityType, world);
    }

    public SmallCompressorMinecart(Level world, double x, double y, double z)
    {
        super(NMEntities.SMALL_COMPRESSOR_MINECART, world, x, y, z);
    }

    private void updateBlockState(boolean burning)
    {
        BlockState state = getDisplayBlockState();
        if (state.getBlock() instanceof SmallCompressorBlock)
        {
            setDisplayBlockState(state.setValue(SmallCompressorBlock.LIT, burning));
        }
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag nbt)
    {
        super.saveWithoutId(nbt);
        inventory.writeNbt(nbt);
        burner.writeNbt(nbt);
        return nbt;
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        inventory.readNbt(nbt);
        burner.readNbt(nbt);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (!level().isClientSide())
        {
            burner.tick();
        }

        if (burner.getBurnTime() > 0)
        {
            SmallCompressorBlockEntity.powerEntities(level(), blockPosition());
        }

        if (level().isClientSide() && getDisplayBlockState().getValue(SmallCompressorBlock.LIT) && this.random.nextInt(4) == 0)
        {
            this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.8, this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    public BlockState getDefaultDisplayBlockState()
    {
        return NMBlocks.SMALL_COMPRESSOR.defaultBlockState();
    }

    @Override
    protected Item getDropItem()
    {
        return NMItems.SMALL_COMPRESSOR_MINECART;
    }

    @Override
    public Type getMinecartType()
    {
        return null;
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitPos, InteractionHand hand)
    {
        player.openMenu(this);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand)
    {
        return super.interact(player, hand);
    }

    @Override
    public ItemStack getPickResult()
    {
        return NMItems.SMALL_COMPRESSOR_MINECART.getDefaultInstance();
    }

    @Override
    public void destroy(DamageSource damageSource)
    {
        super.destroy(damageSource);
        Containers.dropContents(level(), this, inventory);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player)
    {
        return new SmallCompressorScreenHandler(playerInventory, inventory, syncId, burner.getDelegate());
    }
}
