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

import com.neep.meatlib.item.MeatlibItemSettings;
import com.neep.meatweapons.MWItems;
import com.neep.meatweapons.MeatWeapons;
import com.neep.meatweapons.client.model.BaseGunModel;
import com.neep.meatweapons.entity.ExplodingShellEntity;
import com.neep.meatweapons.entity.WeaponCooldownAttachment;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.meatweapons.particle.MWGraphicsEffects;
import com.neep.neepmeat.init.NMSounds;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.model.GeoModel;

public class MA75Item extends BaseGunItem implements WeakTwoHanded
{
    public String controllerName = "controller";

    public MA75Item()
    {
        super(MWItems.SMALL_BALLISTIC_MAGAZINE, 50, 1, false, new MeatlibItemSettings());
        this.sounds.put(GunSounds.FIRE_PRIMARY, NMSounds.AR_FIRE);
        this.sounds.put(GunSounds.FIRE_SECONDARY, NMSounds.HAND_CANNON_FIRE);
        this.sounds.put(GunSounds.RELOAD, NMSounds.HAND_CANNON_RELOAD);
    }

    @Override
    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack)
    {
        return false;
    }

    @Override
    public int getUseDuration(ItemStack stack)
    {
        return 99999;
    }

    @Override
    public Vec3 getMuzzleOffset(LivingEntity entity, ItemStack stack)
    {
        boolean sneak = entity.isShiftKeyDown();
        return new Vec3(
                sneak ? 0 : entity.getMainHandItem().equals(stack) ? -0.2 : 0.2,
                sneak ? -0.25 : 0.1,
                .2);
    }

    @Override
    public void trigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        WeaponCooldownAttachment manager = WeaponCooldownAttachment.get(player);

        if (stack.getDamageValue() >= stack.getMaxDamage())
        {
            reload(player, stack, null);
            return;
        }

        if (id == MWAttackC2SPacket.TRIGGER_PRIMARY
            && !manager.isCoolingDown(stack, 0)
            && stack.getDamageValue() < stack.getMaxDamage()
        )
        {
            fire(world, player, stack, pitch, yaw);
            manager.set(stack, 0, 2);
            if (!player.isCreative()) stack.setDamageValue(stack.getDamageValue() + 1);
        }
        if (id == MWAttackC2SPacket.TRIGGER_SECONDARY
                && !manager.isCoolingDown(stack, 1)
                && stack.getDamageValue() < stack.getMaxDamage()
        )
        {
            player.getCooldowns().addCooldown(this, 3);
            fireShell(world, player, stack, 1, ((world1, x, y, z, vx, vy, vz) -> new ExplodingShellEntity(world, 1, false, x, y, z, vx, vy, vz)));
            manager.set(stack, 1, 15);
            if (!player.isCreative()) stack.setDamageValue(stack.getDamageValue() + 1);
        }
    }

    @Override
    public void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        trigger(world, player, stack, id, pitch, yaw, handType, hand);
    }

    public void fire(Level world, Player player, ItemStack stack, double pitch, double yaw)
    {

        if (!world.isClientSide)
        {
            fireBeam(world, player, stack);
        }
    }

    @Override
    public int getShots(ItemStack stack, int trigger)
    {
        return switch (trigger)
        {
            case 1: yield maxShots - stack.getDamageValue();
            case 2: yield 10;
            default: yield -1;
        };
    }

    @Override
    protected GeoModel<? extends BaseGunItem> createModel()
    {
        return new BaseGunModel<>(
                new ResourceLocation(MeatWeapons.NAMESPACE, "geo/ma75.geo.json"),
                new ResourceLocation(MeatWeapons.NAMESPACE, "textures/general/ma75.png"),
                new ResourceLocation(MeatWeapons.NAMESPACE, "animations/ma75.animation.json"));
    }

    @Override
    public void syncBeamEffect(ServerLevel world, Vec3 pos, Vec3 end, float width, int maxTime, double showRadius, boolean full)
    {
        Vec3 col = new Vec3(214, 175, 32);
        for (ServerPlayer player : PlayerLookup.around(world, pos, showRadius))
        {
            MWGraphicsEffects.syncBeamEffect(player, MWGraphicsEffects.BULLET_TRAIL, world, pos, end, col,0.1f, 1);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<>(this, controllerName, this::fireController)
                .triggerableAnim("fire", RawAnimation.begin().thenPlay("animation.ma75.fire")));
    }
}
