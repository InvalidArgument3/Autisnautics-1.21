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

import com.neep.meatlib.client.MeatlibFluidRendering;
import com.neep.meatlib.item.BaseItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatweapons.item.GunItem;
import com.neep.meatweapons.item.WeakTwoHanded;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.neepmeat.entity.SimpleProjectileEntity;
import com.neep.neepmeat.init.NMComponents;
import com.neep.neepmeat.init.NMEntities;
import com.neep.neepmeat.init.NMParticles;
import com.neep.neepmeat.init.NMSounds;
import com.neep.neepmeat.network.ParticleSpawnS2C;
import com.neep.neepmeat.particle.PressureWashParticleType;
import com.neep.meatlib.util.MiscUtil;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.FluidVariantAttributes;
import com.neep.meatlib.transfer.InventoryStorage;
import com.neep.meatlib.transfer.ItemVariant;
import com.neep.meatlib.transfer.StoragePreconditions;
import com.neep.meatlib.transfer.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import com.neep.meatlib.transfer.Transaction;
import com.neep.meatlib.transfer.TransactionContext;
import net.fabricmc.fabric.impl.transfer.item.CursorSlotWrapper;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PressureWasherItem extends BaseItem implements GunItem, WeakTwoHanded
{
    private final RandomSource random = RandomSource.create();

    public PressureWasherItem(TooltipSupplier tooltipSupplier, Properties settings)
    {
        super(tooltipSupplier, settings.stacksTo(1));
        FluidStorage.ITEM.registerForItems(this::getStorage, this);
    }

    @Override
    public boolean displayArmFirstPerson(ItemStack stack, InteractionHand hand)
    {
        return false;
    }

    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack)
    {
        return false;
    }

    @Override
    public Vec3 getMuzzleOffset(LivingEntity entity, ItemStack stack)
    {
        boolean sneak = entity.isShiftKeyDown();
        return new Vec3(
                sneak ? 0 : entity.getMainHandItem().equals(stack) == (entity.getMainArm() == HumanoidArm.RIGHT) ? -0.2 : 0.2,
                sneak ? -0.25 : -0.2,
                .5);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        if (world instanceof ServerLevel serverWorld && user.isShiftKeyDown())
        {
            ItemStack stack = user.getItemInHand(hand);
            var component = NMComponents.PRESSURE_WASHER.get(stack);
            long amount = component.getAmount();
            component.setAmount(0);
            component.setFluid(FluidVariant.BLANK);
            stack.setDamageValue(getMaxDamage());

            if (amount > 0)
                world.playSound(null, user.blockPosition(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.2f, 1);

            return InteractionResultHolder.success(stack);
        }

        return super.use(world, user, hand);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickType, Player player)
    {
        return super.overrideStackedOnOther(stack, slot, clickType, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference)
    {
        if (player.containerMenu != null && !otherStack.isEmpty())
        {
            CursorSlotWrapper wrapper = CursorSlotWrapper.get(player.containerMenu);
            var slotStack = slot.getItem();
            var storage = FluidStorage.ITEM.find(otherStack, ContainerItemContext.ofSingleSlot(wrapper));
            var inventory = InventoryStorage.of(player.inventory, Direction.UP);
            var inventorySlot = inventory.getSlot(slot.getContainerSlot());
            var washerStorage = getStorage(stack, ContainerItemContext.ofPlayerSlot(player, inventorySlot));

            try (Transaction transaction = Transaction.openOuter())
            {
                AtomicReference<FluidVariant> lastVariant = new AtomicReference<>(FluidVariant.BLANK);
                long moved = StorageUtil.move(storage, washerStorage, v ->
                {
                    lastVariant.set(v);
                    return true;
                }, washerStorage.getCapacity(), transaction);

                if (moved > 0 && !lastVariant.get().isBlank())
                    player.playSound(FluidVariantAttributes.getFillSound(lastVariant.get()), 1, 1);

                transaction.commit();
            }

            return true;
        }

        return super.overrideOtherStackedOnMe(stack, otherStack, slot, clickType, player, cursorStackReference);
    }

    @Override
    public void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        if (world instanceof ServerLevel serverWorld)
        {
            var component = NMComponents.PRESSURE_WASHER.get(stack);
            long perEntity = 450;

            long amount = component.getAmount();
            FluidVariant variant = component.getFluid();
            if (amount >= perEntity && !variant.isBlank())
            {
                var eyePos = player.getEyePosition();

                double speed = 1;
                double spread = 0.05;
                Vec3 transform = getMuzzleOffset(player, stack).xRot((float) -pitch).yRot((float) -yaw);
                var particlePos = eyePos.add(transform);
                var velocity = player.getLookAngle().scale(speed);

                velocity = SimpleProjectileEntity.makeVelocity(velocity.x, velocity.y, velocity.z, (float) speed, 0.2f, random)
                        .add(player.getDeltaMovement());

                var spreadVec = new Vec3(spread, spread, spread);
                ParticleSpawnS2C.send((ServerPlayer) player, new PressureWashParticleType.Parameters(NMParticles.PRESSURE_WASH_BEAM,
                                1, 0xffffff, variant.getFluid()),
                        particlePos, velocity, spreadVec, 2);

                player.playNotifySound(NMSounds.PRESSURE_WASHER_SQUIRT.get(), SoundSource.PLAYERS, 1f,
                        (float) random.triangle(1, 0.1));

                // Create the entity that breaks blocks
                if (world.getGameTime() % 5 == 0)
                {
                    var projectile = NMEntities.PRESSURE_WASH.create(world);
                    projectile.setPos(particlePos);
                    projectile.setOwner(player);
                    projectile.setPosRaw(particlePos.x, particlePos.y, particlePos.z);
                    projectile.shoot(velocity.x, velocity.y, velocity.z, (float) speed, 0.1f);
                    projectile.setPower(3, variant);
                    world.addFreshEntity(projectile);

                    component.setAmount(Math.max(0, amount - perEntity));
                }
            }
        }
    }

    @Override
    public int getBarColor(ItemStack stack)
    {
        var component = NMComponents.PRESSURE_WASHER.getNullable(stack);
        if (component != null)
            return MeatlibFluidRendering.getColor(component.getFluid(), true);

        return super.getBarColor(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack)
    {
        var component = NMComponents.PRESSURE_WASHER.getNullable(stack);
        if (component != null)
        {
            // When an item is first created, its damage is 0, which is misleading.
            // When an item is emptied in creative, damage goes to an unpredictable number. Not sure why.
            return !(stack.getDamageValue() != getMaxDamage() && component.getAmount() == 0);
        }

        return false;
    }

    @Override
    public int getBarWidth(ItemStack stack)
    {
        var component = NMComponents.PRESSURE_WASHER.getNullable(stack);
        if (component != null)
        {
            return (int) (13. * component.getAmount() / (double) component.getCapacity());
        }

        return 0;
    }

    @Override
    public Component getName(ItemStack stack)
    {
        var component = NMComponents.PRESSURE_WASHER.getNullable(stack);
        if (component != null)
        {
            if (component.getAmount() > 0)
            {
                return Component.translatable(getDescriptionId() + ".full", FluidVariantAttributes.getName(component.getFluid()),
                        MiscUtil.dropletsToMb(component.getAmount()));
            }
        }

        return super.getName(stack);
    }

    public void appendHoverText(ItemStack stack   , Level world, List<Component> tooltip, TooltipFlag tooltipContext)
    {
        var component = NMComponents.PRESSURE_WASHER.getNullable(stack);
        if (component != null)
        {
            var variant = component.getFluid();
            long mb = MiscUtil.dropletsToMb(component.getAmount());
            int col = FluidVariantRendering.getColor(variant);
            MutableComponent text = FluidVariantAttributes.getName(variant).copy();
            text.append(": " + mb + "mb");
            text.setStyle(text.getStyle().withColor(col));
            tooltip.add(text);
        }

        super.appendHoverText(stack, world, tooltip, tooltipContext);
    }

    @Override
    public void syncAnimation(Level world, LivingEntity player, ItemStack stack, String animation, boolean broadcast)
    {

    }

    public RandomSource getRandom()
    {
        return random;
    }

    public StackStorage getStorage(ItemStack stack, ContainerItemContext context)
    {
        return new StackStorage(context);
    }

    //    @Override
    //    public void onScroll(PlayerEntity player, ItemStack stack, Hand hand, double amount)
    //    {
    //        if (player.getWorld() instanceof ServerWorld serverWorld)
    //        {
    //            var component = NMComponents.PRESSURE_WASHER.get(stack);
    //            var inventory = player.getInventory();
    //            var wrapper = InventoryStorage.of(inventory, Direction.UP);
    //            for (int slot = 0; slot < inventory.size(); slot++)
    //            {
    //                var invStack = inventory.getStack(slot);
    //                var fluidStorage = FluidStorage.ITEM.find(invStack, ContainerItemContext.ofSingleSlot(wrapper.getSlot(slot)));
    //
    //                if (fluidStorage != null && fluidStorage.supportsExtraction())
    //                {
    //                    component.setSlot(slot);
    //                }
    //            }
    //        }
    //    }
    public record StackStorage(ContainerItemContext context) implements SingleSlotStorage<FluidVariant>
    {
        private ItemStack getStack()
        {
            return context.getItemVariant().toStack((int) context.getAmount());
        }

        public long insert(FluidVariant insertedResource, long maxAmount, TransactionContext transaction)
        {
            StoragePreconditions.notBlankNotNegative(insertedResource, maxAmount);

            long amount = getAmount();
            FluidVariant resource = getResource();
            if ((insertedResource.equals(resource) || resource.isBlank()))
            {
                long insertedAmount = Math.min(maxAmount, getCapacity() - amount);

                if (insertedAmount > 0)
                {
                    if (resource.isBlank())
                    {
                        setResource(insertedResource, transaction);
                        setAmount(insertedAmount, transaction);
                    }
                    else
                    {
                        setAmount(amount + insertedAmount, transaction);
                    }

                    return insertedAmount;
                }
            }
            return 0;
        }

        public long extract(FluidVariant extractedResource, long maxAmount, TransactionContext transaction)
        {
            StoragePreconditions.notBlankNotNegative(extractedResource, maxAmount);

            FluidVariant resource = getResource();
            long amount = getAmount();

            if (extractedResource.equals(resource))
            {
                long extractedAmount = Math.min(maxAmount, amount);

                if (extractedAmount > 0)
                {
                    setAmount(amount - extractedAmount, transaction);

                    if (amount - extractedAmount == 0)
                    {
                        setResource(FluidVariant.BLANK, transaction);
                    }

                    return extractedAmount;
                }
            }

            return 0;
        }

        public boolean isResourceBlank()
        {
            return getResource().isBlank();
        }

        public FluidVariant getResource()
        {
            return getResource(context.getItemVariant());
        }

        public static FluidVariant getResource(ItemVariant stack)
        {
            return NMComponents.PRESSURE_WASHER.get(stack.toStack()).getFluid();
        }

        public void setResource(FluidVariant resource, TransactionContext transaction)
        {
            ItemStack stack = getStack();
            var component = NMComponents.PRESSURE_WASHER.get(stack);
            component.setFluid(resource);

            context.exchange(ItemVariant.of(stack.copy()), 1, transaction);
        }

        public long getAmount()
        {
            return getAmount(context.getItemVariant());
        }

        public static long getAmount(ItemVariant stack)
        {
            return NMComponents.PRESSURE_WASHER.get(stack.toStack()).getAmount();
        }

        public void setAmount(long amount, TransactionContext transaction)
        {
            ItemStack stack = getStack();
            var component = NMComponents.PRESSURE_WASHER.get(stack);
            component.setAmount(amount);

            context.exchange(ItemVariant.of(stack.copy()), 1, transaction);
        }

        public long getCapacity()
        {
            return NMComponents.PRESSURE_WASHER.get(getStack())
                    .getCapacity();
        }
    }
}
