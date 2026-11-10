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
import com.neep.meatweapons.client.model.LMGItemModel;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.meatweapons.particle.MWGraphicsEffects;
import com.neep.neepmeat.init.NMSounds;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.model.GeoModel;

public class LMGItem extends BaseGunItem
{
    public LMGItem()
    {
        super(MWItems.SMALL_BALLISTIC_MAGAZINE, 50, 1, false, new MeatlibItemSettings());
        this.sounds.put(GunSounds.FIRE_PRIMARY, NMSounds.LMG_FIRE.get());
        this.sounds.put(GunSounds.RELOAD, NMSounds.HAND_CANNON_RELOAD.get());
    }

    @Override
    protected GeoModel<? extends BaseGunItem> createModel()
    {
        return new LMGItemModel();
    }

    public int getUseDuration(ItemStack stack)
    {
        return 99999;
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks)
    {
        ItemStack itemStack = user.getItemInHand(InteractionHand.MAIN_HAND);
        trigger(world, (Player) user, itemStack, 0, user.getXRot(), user.getYRot(), MWAttackC2SPacket.HandType.MAIN, InteractionHand.MAIN_HAND);
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
        if (!player.getCooldowns().isOnCooldown(this))
        {
            if (stack.getDamageValue() != this.maxShots)
            {
                player.getCooldowns().addCooldown(this, 2);

                if (!world.isClientSide)
                {
                    fireBeam(world, player, stack);
                    if (!player.isCreative())
                        stack.setDamageValue(stack.getDamageValue() + 1);
                }
            }
            else // Weapon is out of ammunition.
            {
                if (world.isClientSide)
                {
                    // Play empty sound.
                }
                else
                {
                    // Try to reload
                    this.reload(player, stack, null);
                }
            }
        }
    }

    @Override
    public void tickTrigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        trigger(world, player, stack, id, pitch, yaw, handType, hand);
    }

    @Override
    public void syncBeamEffect(ServerLevel world, Vec3 pos, Vec3 end, float width, int maxTime, double showRadius, boolean full)
    {
        Vec3 col = new Vec3(214, 175, 32);
        for (ServerPlayer player : PlayerLookup.around(world, pos, showRadius))
        {
            MWGraphicsEffects.syncBeamEffect(player, MWGraphicsEffects.BULLET_TRAIL, world, pos, end, col, 0.1f, 1);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<>(this, controllerName, 0, this::fireController)
                .triggerableAnim("fire", RawAnimation.begin().thenPlay("animation.light_machine_gun.fire")));
    }
}
