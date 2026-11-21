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

package com.neep.meatweapons.item;

import com.neep.meatlib.item.MeatlibItem;
import com.neep.meatlib.item.MeatlibItemSettings;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.Util;
import com.neep.meatweapons.client.renderer.BaseGunRenderer;
import com.neep.meatweapons.entity.BulletDamageSource;
import com.neep.meatweapons.entity.HitScan;
import com.neep.neepmeat.api.item.OverrideSwingItem;
import com.neep.neepmeat.compat.forge.GeoItemFabric;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class BaseGunItem extends Item implements MeatlibItem, GunItem, GeoItem, OverrideSwingItem
{
    protected final AnimatableInstanceCache instanceCache = GeckoLibUtil.createInstanceCache(this);
    protected final Supplier<Object> renderProvider = GeoItemFabric.makeRenderer(this);

    protected Map<GunSounds, SoundEvent> sounds = new EnumMap<GunSounds, SoundEvent>(GunSounds.class);
    public Item ammunition;
    public boolean hasLore;
    public final int maxShots;
    public final int cooldown;
    protected final Random random = new Random(0);

    protected final String controllerName = "controller";

    public BaseGunItem(Item ammunition, int maxShots, int cooldown, boolean hasLore, MeatlibItemSettings settings)
    {
        super(settings.stacksTo(1).durability(maxShots).defaultDurability(maxShots).group(MeatWeapons.WEAPONS));
        this.ammunition = ammunition;
        this.maxShots = maxShots;
        this.hasLore = hasLore;
        this.cooldown = cooldown;

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public Random getRandom()
    {
        return random;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext world, List<Component> tooltip, TooltipFlag tooltipContext)
    {
        if (hasLore)
        {
            tooltip.add(Component.translatable(getDescriptionId() + ".lore"));
//            tooltip.add(Text.translatable("item." + MeatWeapons.NAMESPACE + "." + registryName + ".lore"));
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return false;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.NONE;
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        ItemStack itemStack = user.getItemInHand(hand);
//        user.setCurrentHand(hand);
//        fire(world, user, itemStack);
        return InteractionResultHolder.fail(itemStack);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference)
    {
        if (cursorStackReference.get().getItem().equals(ammunition) && stack.getDamageValue() != 0)
        {
            this.reload(player, stack, cursorStackReference.get());
            return true;
        }
        return false;
    }

    @Override
    public int getShots(ItemStack stack, int trigger)
    {
        return trigger == 0 ? this.maxShots - stack.getDamageValue() : -1;
    }

    // Should only be called on server.
    public void reload(Player user, ItemStack stack, @Nullable ItemStack ammoStack)
    {
        user.getCooldowns().addCooldown(this, 7);
        ammoStack = ammoStack == null ? GunItem.removeStack(this.ammunition, user) : ammoStack;
        if (ammoStack != null)
        {
            stack.setDamageValue(0);
            ammoStack.shrink(1);

            if (user.level() instanceof ServerLevel serverWorld)
            {
                // Do not sync reload animation with other players; it looks silly.
                final long id = GeoItem.getOrAssignId(stack, serverWorld);
                triggerAnim(user, id, "Activation", "activate");

                playSound(serverWorld, user, GunSounds.RELOAD);
            }
        }
    }

    protected void fireBeam(Level world, Player player, ItemStack stack)
    {
        Random random = getRandom();

        double yaw = Math.toRadians(player.getYHeadRot()) + 0.1 * (random.nextFloat() - 0.5);
        double pitch = Math.toRadians(player.getViewXRot(0.1f)) + 0.1 * (random.nextFloat() - 0.5);

        Vec3 pos = new Vec3(player.getX(), player.getY() + 1.4, player.getZ());
        Vec3 transform = getMuzzleOffset(player, stack).xRot((float) -pitch).yRot((float) -yaw);
        pos = pos.add(transform);

        double d = 0.2;
        Vec3 perturb = new Vec3(random.nextFloat() - 0.5, random.nextFloat() - 0.5, random.nextFloat() - 0.5)
                .scale(d);
        Vec3 end = pos.add(player.getViewVector(1)
                .add(perturb)
                .scale(40));
        Optional<EntityHitResult> target = hitScan(player, pos, end, 40, this);
        if (target.isPresent())
        {
            Entity entity = target.get().getEntity();
            entity.hurt(BulletDamageSource.create(world, player, 0.1f, false), 2);
            entity.invulnerableTime = 0;
        }

        playSound(world, player, GunSounds.FIRE_PRIMARY);

        syncAnimation(world, player, stack, "fire", true);
    }

    protected void fireShell(Level world, Player player, ItemStack stack, double speed, ProjectileFactory factory)
    {
        double yaw = Math.toRadians(player.getYHeadRot());
        double pitch = Math.toRadians(player.getViewXRot(0.1f));

        double vx = speed * -Math.sin(yaw) * Math.cos(pitch) + player.getDeltaMovement().x();
        double vy = speed * -Math.sin(pitch) + player.getDeltaMovement().y();
        double vz = speed * Math.cos(yaw) * Math.cos(pitch) + player.getDeltaMovement().z();

        Vec3 pos = new Vec3(player.getX(), player.getY() + 1.4, player.getZ());
        if (!player.isShiftKeyDown())
        {
            Vec3 transform = getMuzzleOffset(player, stack).yRot((float) -yaw);
            pos = pos.add(transform);
        }

        AbstractArrow shell = factory.create(world, pos.x, pos.y, pos.z, vx, vy, vz);
        shell.setOwner(player);
        world.addFreshEntity(shell);

        playSound(world, player, GunSounds.FIRE_SECONDARY);

        syncAnimation(world, player, stack, "fire", true);
    }

    public static Optional<EntityHitResult> hitScan(@NotNull Entity caster, Vec3 start, Vec3 end, double distance, BeamEffectProvider gunItem)
    {
        return hitScan(caster, start, end, distance, e -> true, gunItem, 0.1f);
    }

    /// Returns the where the ray hits a block and a list of all entiites that it hit, sorted by distance.
    public static HitScanResults hitScan2(@NotNull Entity caster, AABB originBox, Vec3 start, Vec3 end, Predicate<Entity> predicate, float margin)
    {
        Level world = caster.level();

        // Find where the ray hits a block
        ClipContext ctx = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster);
        BlockHitResult maxExtent = world.clip(ctx);

        Predicate<Entity> entityFilter = entity -> !entity.isSpectator() && entity.isAlive() && entity.isPickable() && predicate.test(entity);

        List<HitScan.HitScanResult> results = Util.getRayTargets(caster, originBox.move(start), start, maxExtent.getLocation(), entityFilter, margin)
                .sorted(Comparator.comparingDouble(h -> h.getLocation().distanceToSqr(start)))
                .toList();

        return new HitScanResults(maxExtent, results);
    }

    public record HitScanResults(BlockHitResult maxExtent, List<HitScan.HitScanResult> entities) {}

    public static Optional<EntityHitResult> hitScan(@NotNull Entity caster, Vec3 start, Vec3 end, double distance, Predicate<Entity> predicate, BeamEffectProvider gunItem, float margin)
    {
        // 1. Do a block raycast to find the maximum extent of the shot
        // 2. Expand the player's bounding box along the maximum extent ray
        // 3. Get all entities in this region
        // 4. Check which entities intersect the ray

        Level world = caster.level();
        if (!world.isClientSide)
        {
            // Find where the ray hits a block
            ClipContext ctx = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster);
            BlockHitResult blockResult = world.clip(ctx);

            Predicate<Entity> entityFilter = entity -> !entity.isSpectator() && entity.isPickable() && predicate.test(entity);
            var hits = Util.getRayTargets(caster, caster.getBoundingBox(), start, blockResult.getLocation(), entityFilter, margin);
            var entityResult = Util.closestHit(hits, start);

            Vec3 hitPos = Objects.requireNonNullElse(entityResult, blockResult).getLocation();
            gunItem.syncBeamEffect((ServerLevel) world, start, hitPos, 0.2f, 9, 100);

            return Optional.ofNullable(entityResult);
        }
        return Optional.empty();
    }

    @Override
    public void syncAnimation(Level world, LivingEntity player, ItemStack stack, String animation, boolean broadcast)
    {
        if (world instanceof ServerLevel serverWorld)
        {
            if (player instanceof Player playerEntity)
            {
                long id = GeoItem.getOrAssignId(stack, (ServerLevel) world);
                triggerAnim(player, id, controllerName, animation);

                if (broadcast)
                {
                    for (Player otherPlayer : PlayerLookup.tracking(player))
                    {
                        triggerAnim(otherPlayer, id, controllerName, animation);
                    }
                }
            }
        }
        else
        {
            triggerAnim(player, GeoItem.getId(stack), controllerName, animation);
        }
    }

    @Override
    public void playSound(Level world, LivingEntity entity, GunSounds sound)
    {
        if (sounds.containsKey(sound))
        {
            world.playSound(
                    null,
                    entity.blockPosition(),
                    sounds.get(sound),
                    SoundSource.PLAYERS,
                    1f,
                    1f
            );
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<>(this, controllerName, this::fireController));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache()
    {
        return instanceCache;
    }

    @Override
    public Supplier<Object> getRenderProvider()
    {
        return renderProvider;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer)
    {
        consumer.accept(new RenderProvider()
        {
            private BaseGunRenderer<? extends BaseGunItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer()
            {
                if (renderer == null)
                {
                    this.renderer = new BaseGunRenderer<>(createModel());
                }
                return renderer;
            }
        });
    }

    protected abstract GeoModel<? extends BaseGunItem> createModel();

    protected <P extends BaseGunItem> PlayState fireController(AnimationState<P> event)
    {
        return PlayState.CONTINUE;
    }

    public interface ProjectileFactory
    {
        AbstractArrow create(Level world, double x, double y, double z, double vx, double vy, double vz);
    }
}
