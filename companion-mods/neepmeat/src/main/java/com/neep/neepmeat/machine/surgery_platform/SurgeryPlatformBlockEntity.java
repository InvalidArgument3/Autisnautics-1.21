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

package com.neep.neepmeat.machine.surgery_platform;

import com.neep.meatlib.blockentity.SyncableBlockEntity;
import com.neep.meatlib.recipe.ingredient.IngredientType;
import com.neep.meatlib.recipe.ingredient.Ingredients;
import com.neep.meatlib.transfer.EntityVariant;
import com.neep.meatlib.transfer.SingleEntityStorage;
import com.neep.neepmeat.plc.component.MutateInPlace;
import com.neep.neepmeat.plc.component.TableComponent;
import com.neep.meatlib.transfer.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

@SuppressWarnings("UnstableApiUsage")
public class SurgeryPlatformBlockEntity extends SyncableBlockEntity
{
    protected final Component tableComponent = new Component();
    protected final Mutate mutate = new Mutate();

    public SurgeryPlatformBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    public TableComponent<?> getTableComponent(Void ctx)
    {
        return tableComponent;
    }

    public MutateInPlace<Entity> getMutate(Void unused)
    {
        return mutate;
    }

    public class Component implements TableComponent<EntityVariant<?>>
    {
        @Override
        public Storage<EntityVariant<?>> getStorage()
        {
            return SingleEntityStorage.of(getEntity());
        }

        @Override
        public IngredientType<?> getType()
        {
            return Ingredients.ENTITY_MUTATE;
        }

        public Entity getEntity()
        {
            AABB box = new AABB(getBlockPos().above());
            return level.getEntitiesOfClass(LivingEntity.class, box).stream().findFirst().orElse(null);
        }
    };

    public class Mutate implements MutateInPlace<Entity>
    {
        @Override
        public Entity get()
        {
            AABB box = new AABB(getOutputPos().above());
            return level.getEntities(
                    EntityTypeTest.forClass(Entity.class),
                    box,
                    entity -> !entity.isSpectator() && !(entity instanceof Minecart)).stream().findFirst().orElse(null);
        }

        @Override
        public void set(Entity entity)
        {

        }

        public BlockPos getOutputPos()
        {
            return worldPosition.above();
        }

        public Level getWorld()
        {
            return level;
        }
    }
}
