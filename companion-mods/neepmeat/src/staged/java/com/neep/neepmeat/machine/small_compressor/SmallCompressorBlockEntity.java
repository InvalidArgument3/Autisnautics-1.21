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

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.inventory.ImplementedInventory;
import com.neep.neepmeat.component.PowerResourceComponent;
import com.neep.neepmeat.init.NMComponents;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SmallCompressorBlockEntity extends SyncableBlockEntity implements MenuProvider
{
    private final ImplementedInventory inventory = ImplementedInventory.ofSize(1);

    private final BurnerBehaviour burner = new BurnerBehaviour(inventory, this::updateState);
    private final RandomSource random = RandomSource.create();

    private boolean updateBlockState = true;

    private void updateState(boolean burning)
    {
        if (getBlockState().getValue(SmallCompressorBlock.LIT) != burning)
        {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(SmallCompressorBlock.LIT, burning));
        }
    }

    public SmallCompressorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public void serverTick()
    {
        burner.tick();

        if (burner.getBurnTime() > 0)
        {
            powerEntities(getLevel(), getBlockPos());
        }

        if (updateBlockState)
        {
            updateState(burner.getBurnTime() > 0);
            updateBlockState = false;
        }
    }

    public static void powerEntities(Level world, BlockPos pos)
    {
        int r = 25;
        AABB box = AABB.ofSize(Vec3.atCenterOf(pos), r, r, r);

        List<Player> players = world.getEntitiesOfClass(Player.class, box, p -> true);

        players.forEach(player ->
        {
            PowerResourceComponent component = NMComponents.COMPRESSED_AIR.getNullable(player);

            if (component != null)
                component.insertAir(10);
        });
    }

    public void clientTick()
    {
        if (getBlockState().getValue(SmallCompressorBlock.LIT) && random.nextInt(4) == 0)
        {
            this.getLevel().addParticle(ParticleTypes.LARGE_SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.8, worldPosition.getZ() + 0.5, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);

        inventory.writeNbt(nbt);
        burner.writeNbt(nbt);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);

        inventory.readNbt(nbt);
        burner.readNbt(nbt);
    }

    public ImplementedInventory getInventory()
    {
        return inventory;
    }

    @Override
    public Component getDisplayName()
    {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player)
    {
        return new SmallCompressorScreenHandler(playerInventory, inventory, syncId, burner.getDelegate());
    }

    public void dropItems()
    {
        Containers.dropContents(getLevel(), getBlockPos(), inventory);
    }
}
