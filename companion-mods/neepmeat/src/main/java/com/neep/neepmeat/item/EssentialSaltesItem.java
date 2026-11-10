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

package com.neep.neepmeat.item;
import net.minecraft.world.item.Item;

import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.machine.synthesiser.MobSynthesisRegistry;
import com.neep.meatlib.transfer.ItemVariant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EssentialSaltesItem extends BaseItem
{
    public EssentialSaltesItem(RegistrationContext ctx, TooltipSupplier tooltip, Properties settings)
    {
        super(tooltip, settings);
    }

    @Override
    public boolean isFoil(ItemStack stack)
    {
        return true;
    }

    @Override
    public Component getName(ItemStack stack)
    {
        if (!stack.hasTag())
            return super.getName(stack);

        String id = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getString("id");
        if (id != null)
        {
            return Component.translatable(this.getDescriptionId(), BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id)).getDescription());
        }
        else return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext world, List<Component> tooltip, TooltipFlag tooltipContext)
    {
        super.appendHoverText(itemStack, world, tooltip, tooltipContext);
        EntityType<?> type = getEntityType(itemStack);
        var entry = MobSynthesisRegistry.get(type);
        if (entry != null)
        {
            tooltip.add(Component.translatable(getDescriptionId() + ".meat", Component.nullToEmpty(
                    String.format("%.2f", entry.meat() / 81f) + "mb"
            )));
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand)
    {
//        storeEntity(stack, entity);
//        user.setStackInHand(hand, stack);
//        return ActionResult.SUCCESS;
        return super.interactLivingEntity(stack, user, entity, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
//        Entity entity = createEntity(context.getStack(), context.getWorld(), true);
//
//        if (entity != null)
//        {
//            Vec3d hitPos = context.getHitPos();
//            entity.setPos(hitPos.x, hitPos.y + 1, hitPos.z);
//            context.getWorld().spawnEntity(entity);
//        }

        return super.useOn(context);
    }

    // Ignores fire immunity
    public static boolean isOnFire(LivingEntity entity)
    {
        boolean bl = entity.level() != null;
//        return (entity.getFireTicks() > 0 || bl && entity.getFlag(ON_FIRE_FLAG_INDEX));
        return entity.getRemainingFireTicks() > 0 || entity.isInLava();
    }

    public static void onEntityDeath(LivingEntity livingEntity)
    {
        if (!isOnFire(livingEntity))
            return;

        if (MobSynthesisRegistry.get(livingEntity.getType()) == null)
        {
            return;
        }

        Level world = livingEntity.level();
        Vec3 pos = livingEntity.position();
        ItemStack stack = NMItems.ESSENTIAL_SALTES.getDefaultInstance();
        EssentialSaltesItem.storeEntity(stack, livingEntity);
        ItemEntity entity = new ItemEntity(world, pos.x, pos.y, pos.z, stack);
        world.addFreshEntity(entity);
    }

    public static void storeEntity(ItemStack stack, LivingEntity entity)
    {
        CompoundTag entityCompound = new CompoundTag();
        entity.saveWithoutId(entityCompound);
        CompoundTag nbt = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        nbt.put("entity_data", entityCompound);
        nbt.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
        stack.setTag(nbt);
    }

    public static Entity createEntity(ItemStack stack, Level world, boolean exactCopy)
    {
        CompoundTag nbt = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        String id = nbt.getString("id");
        CompoundTag entityCompound = nbt.getCompound("entity_data");
        if (id != null && entityCompound != null)
        {
            EntityType<? extends Entity> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id));
            Entity entity = type.create(world);
            if (entity != null)
            {
                if (exactCopy)
                {
                    entityCompound.remove(Entity.UUID_TAG);
                    entity.load(entityCompound);
                }
                return entity;
            }
        }
        return null;
    }

    public static void putEntityType(ItemStack stack, EntityType<?> type)
    {
        CompoundTag nbt = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        nbt.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        stack.setTag(nbt);
    }

    @Nullable
    public static EntityType<?> getEntityType(ItemStack stack)
    {
        if (!stack.hasTag())
            return null;

        CompoundTag nbt = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        String id = nbt.getString("id");
        if (id != null)
        {
            return BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id));
        }
        return null;
    }

    @Nullable
    public static EntityType<?> getEntityType(ItemVariant variant)
    {
        if (!variant.hasNbt())
            return null;

        CompoundTag nbt = variant.getNbt();
        String id = nbt.getString("id");
        if (id != null)
        {
            return BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id));
        }
        return null;
    }
}
