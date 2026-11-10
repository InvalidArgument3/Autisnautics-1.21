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

import com.neep.meatlib.item.BaseSwordItem;
import com.neep.meatlib.item.TooltipSupplier;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.meatlib.registry.RegistrationContext;
import com.neep.meatweapons.Util;
import com.neep.meatweapons.item.WeakTwoHanded;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.item.OverrideSwingItem;
import com.neep.neepmeat.config.NMConfig;
import com.neep.neepmeat.init.*;
import com.neep.neepmeat.recipe.VivisectionRecipe;
import com.neep.neepmeat.transport.block.fluid_transport.entity.FluidDrainBlockEntity;
import com.neep.meatlib.transfer.FluidVariant;
import com.neep.meatlib.transfer.Transaction;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class TissueOperatorItem extends BaseSwordItem implements OverrideSwingItem, WeakTwoHanded
{
    private final TooltipSupplier tooltipSupplier;

    public TissueOperatorItem(RegistrationContext ctx, Properties settings)
    {
        super(Tiers.DIAMOND, 3, -2.4f, settings);
        tooltipSupplier = TooltipSupplier.hidden(1);
    }

    public static void spawnBloodParticles(Vec3 entityPos, ServerLevel world)
    {
//        world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, NMFluids.BLOOD.getDefaultState()),
        world.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.NETHER_WART_BLOCK.defaultBlockState()),
                entityPos.x, entityPos.y, entityPos.z,
                30,
                0.1, 1, 0.1,
                0.1);
    }

    public static boolean tryRecipe(Level world, Vec3 pos, LivingEntity entity, boolean client)
    {
        var ctx = new VivisectionRecipe.VivisectionContext(world, entity);
        VivisectionRecipe recipe = MeatlibRecipes.getInstance().getFirstMatch(NMrecipeTypes.VIVISECTION, ctx).orElse(null);
        if (recipe != null)
        {
            recipe.ejectOutputs(ctx, null);
            world.playSound(null, pos.x, pos.y, pos.z, NMSounds.VIVISECTION_COMPLETE.get(), SoundSource.PLAYERS, 1, 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean displayArmFirstPerson(ItemStack stack, InteractionHand hand)
    {
        return false;
    }

    @Override
    public ArmPose getThirdPersonArmPose(AbstractClientPlayer player, ItemStack stackInHand, InteractionHand hand)
    {
        return ArmPose.BOW_AND_ARROW;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return false;
    }

    @Override
    public boolean onSwing(ItemStack stack, Player player)
    {
        player.attackAnim = 0;
        player.swingTime = 0;
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext world, List<Component> tooltip, TooltipFlag context)
    {
        super.appendHoverText(stack, world, tooltip, context);
        tooltipSupplier.apply(stack, tooltip);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker)
    {
        super.hurtEnemy(stack, target, attacker);

        Level world = target.getCommandSenderWorld();

        if (!world.isClientSide())
        {
            BlockPos pos = target.blockPosition();
            if (world.getBlockEntity(pos.below()) instanceof FluidDrainBlockEntity be)
            {
                Vec3 soundPos = target.position();
                world.playSound(null, soundPos.x, soundPos.y, soundPos.z, NMSounds.SCALPEL_HIT.get(), SoundSource.PLAYERS, 1, 1);

                if (target.isDeadOrDying())
                {
                    try (Transaction transaction = Transaction.openOuter())
                    {
                        var fluid = BuiltInRegistries.FLUID.get(ResourceLocation.tryParse(NeepMeat.CONFIG.drain.fluidId.getRealValue()));
                        be.getBuffer(Direction.UP).insert(FluidVariant.of(fluid), NeepMeat.CONFIG.drain.bloodPerMob.getRealValue(), transaction);
                        transaction.commit();
                    }
                }
            }
        }

        spawnBloodParticles(target.position(), (ServerLevel) world);

        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner)
    {
        if (state.is(NMBlocks.INTEGRATOR_EGG))
        {

        }
        return super.mineBlock(stack, world, state, pos, miner);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.NONE;
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks)
    {
        if (world.isClientSide())
        {
            if (remainingUseTicks % 10 == 0)
            {
                user.playSound(SoundEvents.SHEEP_SHEAR, 1F, (float) world.getRandom().triangle(1, 0.6f));
                user.playSound(SoundEvents.HONEYCOMB_WAX_ON, 1, 1);
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity)
    {
        return 40;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user)
    {
        if (user instanceof Player player && !world.isClientSide())
        {
            Optional<LivingEntity> optional = getEntity(player);
            BlockPos pos = getBlock(player);
            if (optional.isPresent())
            {
                LivingEntity entity = optional.get();
                entity.hurt(world.damageSources().playerAttack(player), 10);

                Vec3 entityPos = entity.position();
                if (world instanceof ServerLevel serverWorld)
                {
                    serverWorld.sendParticles(
                            NMParticles.BLOOD_SHOWER,
                            entityPos.x, entityPos.y + entity.getBbHeight() / 2, entityPos.z, 20,
                            0.05, 0.05, 0.05, 0.1);
                }

                if (tryRecipe(world, entity.position(), entity, world.isClientSide()))
                {
                    if (player instanceof ServerPlayer serverPlayerEntity)
                    {
                        NMCriteria.VIVISECTION.trigger(serverPlayerEntity, entity);
                    }
                }

                hurtEnemy(stack, entity, player);
//                stack.damage(1, player, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
            }
            else if (pos != null && world.getBlockState(pos).is(NMBlocks.INTEGRATOR_EGG))
            {
                world.destroyBlock(pos, false);
                ItemStack stack1 = NMItems.CHRYSALIS.getDefaultInstance();
                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack1);
                world.addFreshEntity(itemEntity);
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), NMSounds.VIVISECTION_COMPLETE.get(), SoundSource.PLAYERS, 1, 1);
            }
            return stack;
        }
        return super.finishUsingItem(stack, world, user);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        ItemStack itemStack = user.getItemInHand(hand);
        if (getEntity(user).isPresent() || getBlock(user) != null)
        {
            user.startUsingItem(hand);
            return InteractionResultHolder.consume(itemStack);
        }
        return InteractionResultHolder.fail(itemStack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks)
    {
        super.releaseUsing(stack, world, user, remainingUseTicks);
    }

    protected Optional<LivingEntity> getEntity(Player user)
    {
        Vec3 pos = user.getEyePosition();
        Vec3 end = pos.add(user.getViewVector(0.5f).scale(20));
        return getTarget(user, user.getEyePosition(), end, 4);
    }

    public Optional<LivingEntity> getTarget(Player caster, Vec3 start, Vec3 end, double distance)
    {
        Level world = caster.level();
        if (!world.isClientSide)
        {
            // Find where the ray hits a block
            ClipContext ctx = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster);
            BlockHitResult blockResult = world.clip(ctx);

            Predicate<Entity> entityFilter = entity -> !entity.isSpectator() && entity.isPickable() && entity instanceof LivingEntity;

            double minDistance = distance;
            Entity entity = null;
            for (var it = Util.getRayTargets(caster, caster.getBoundingBox(), start, blockResult.getLocation(), entityFilter, 0.1).iterator(); it.hasNext(); )
            {
                EntityHitResult result = it.next();
                if (result.getLocation().distanceTo(start) < minDistance)
                {
                    minDistance = result.getLocation().distanceTo(start);
                    entity = result.getEntity();
                }
            }

            return Optional.ofNullable((LivingEntity) entity);
        }
        return Optional.empty();
    }

    @Nullable
    protected BlockPos getBlock(Player user)
    {
        Vec3 pos = user.getEyePosition();
        Vec3 end = pos.add(user.getViewVector(0.5f).scale(4));
        return getBlockTarget(pos, end, user.level(), user);
    }

    @Nullable
    protected BlockPos getBlockTarget(Vec3 start, Vec3 end, Level world, Entity entity)
    {
        ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
        var result = world.clip(context);

        // Jank!
        if (result.getType() != HitResult.Type.MISS && world.getBlockState(result.getBlockPos()).is(NMBlocks.INTEGRATOR_EGG))
        {
            return result.getBlockPos();
        }
        return null;
    }
}
