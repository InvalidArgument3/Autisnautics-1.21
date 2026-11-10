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

package com.neep.neepmeat.machine.separator;

import net.minecraft.core.HolderLookup;

import com.neep.meatlib.MeatLib;
import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.neepmeat.api.machine.MotorisedBlock;
import com.neep.neepmeat.machine.motor.MotorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public class SeparatorBlockEntity extends SyncableBlockEntity implements MotorisedBlock, MenuProvider
{
    private float power;
    private int remainder = 4;
    private boolean takeBabies = false;
    private int radius = 2;

    public static final int MAX_RADIUS = 4;

    public SeparatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public boolean motorTick(MotorEntity motor)
    {
        int waitTime = (int) (20 + (1 - Math.min(power, 1000) / 1000) * 100);
        if (power > 0 && level.getGameTime() % waitTime == 0)
        {
            BlockPos centre = worldPosition.relative(getBlockState().getValue(SeparatorBlock.FACING), radius + 1);
            AABB box = new AABB(centre);
            box = box.inflate(radius, 2, radius);

            var entities = level.getEntities(EntityTypeTest.forClass(Animal.class), box, this::validEntity);
            entities.stream()
                    .skip(remainder)
                    .forEach(this::processEntity);
        }

        return false;
    }

    private boolean validEntity(Animal entity)
    {
        return takeBabies == entity.isBaby();
    }

    private void processEntity(Animal entity)
    {
        double distance = 1 + Math.max(0, (entity.getBbWidth() - 1) / 2);
        var direction = getBlockState().getValue(SeparatorBlock.FACING).getOpposite();
        Vec3 outputPos = Vec3.atCenterOf(worldPosition).relative(direction, distance);

        if (MeatLib.vsUtil != null)
        {
            if (MeatLib.vsUtil.hasShipAtPosition(worldPosition, this.level))
            {
                Vector3d temp = new Vector3d(0,0,0);
                MeatLib.vsUtil.getShipToWorldMatrix(worldPosition, this.level).transformPosition(outputPos.x(), outputPos.y(), outputPos.z(), temp);
                outputPos = new Vec3(temp.x, temp.y, temp.z);
            }
        }
        entity.setPos(outputPos);
    }

    @Override
    public void setInputPower(float power)
    {
        this.power = power;
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putFloat("power", power);
        nbt.putInt("remainder", remainder);
        nbt.putBoolean("take_babies", takeBabies);
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.power = nbt.getFloat("power");
        this.remainder = nbt.getInt("remainder");
        this.takeBabies = nbt.getBoolean("take_babies");
    }

    @Override
    public Component getDisplayName()
    {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player)
    {
        return new SeparatorScreenHandler(inv, syncId, this);
    }

    public SeparatorWidgetHandler.Values createValues()
    {
        return new SeparatorWidgetHandler.Values(radius, takeBabies, remainder);
    }

    public void setValues(SeparatorWidgetHandler.Values values)
    {
        this.radius = Mth.clamp(1, values.radius(), MAX_RADIUS);
        this.takeBabies = values.takeBabies();
        this.remainder = values.remainder();
    }

    public int getRadius()
    {
        return radius;
    }

    public void setRadius(int radius)
    {
        this.radius = radius;
    }
}
