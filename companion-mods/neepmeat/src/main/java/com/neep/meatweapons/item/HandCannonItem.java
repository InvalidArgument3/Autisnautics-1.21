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
import com.neep.meatweapons.client.model.HandCannonItemModel;
import com.neep.meatweapons.entity.CannonBulletEntity;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.neepmeat.init.NMSounds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.model.GeoModel;

public class HandCannonItem extends BaseGunItem implements Aimable
{
    public String controllerName = "controller";

    public HandCannonItem()
    {
        super(Items.DIRT, 8, 10, false, new MeatlibItemSettings());
        this.sounds.put(GunSounds.FIRE_PRIMARY, NMSounds.HAND_CANNON_FIRE.get());
        this.sounds.put(GunSounds.RELOAD, NMSounds.HAND_CANNON_RELOAD.get());
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.NONE;
    }

    @Override
    public Vector3f getAimOffset()
    {
        return new Vector3f(0.46f, 0, 0);
    }

    @Override
    public Vec3 getMuzzleOffset(LivingEntity entity, ItemStack stack)
    {
        return new Vec3(
                entity.getMainHandItem().equals(stack) ? -0.2 : 0.2,
                entity.isShiftKeyDown() ? -0.15 : 0.1,
                0);
    }

    @Override
    public void trigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        fire(world, player, stack, pitch, yaw);
    }

    public void fire(Level world, Player player, ItemStack stack, double pitch, double yaw)
    {
        {
            if (!player.getCooldowns().isOnCooldown(this))
            {
                if (stack.getDamageValue() != this.maxShots)
                {
                    player.getCooldowns().addCooldown(this, cooldown);

                    if (!world.isClientSide)
                    {
//                        double yaw = Math.toRadians(player.getHeadYaw());
//                        double pitch = Math.toRadians(player.getPitch(0.1f));

                        double mult = 5; // Multiplier for bullet speed.
                        double vx = mult * -Math.sin(yaw) * Math.cos(pitch) + player.getDeltaMovement().x();
                        double vy = mult * -Math.sin(pitch) + player.getDeltaMovement().y();
                        double vz = mult * Math.cos(yaw) * Math.cos(pitch) + player.getDeltaMovement().z();

                        Vec3 pos = new Vec3(player.getX(), player.getY() + 1.4, player.getZ());
                        if (!player.isShiftKeyDown())
                        {
                            Vec3 transform = getMuzzleOffset(player, stack).yRot((float) -yaw);
                            pos = pos.add(transform);
                        }

                        CannonBulletEntity bullet = new CannonBulletEntity(world, pos.x, pos.y, pos.z, vx, vy, vz);
                        bullet.setOwner(player);
                        world.addFreshEntity(bullet);

                        playSound(world, player, GunSounds.FIRE_PRIMARY);

                        stack.setDamageValue(stack.getDamageValue() + 1);

                        // TODO
//                        final int id = GeckoLibUtil.guaranteeIDForStack(stack, (ServerWorld) world);
//                        GeckoLibNetwork.syncAnimation(player, this, id, ANIM_FIRE);
//                        for (PlayerEntity otherPlayer : PlayerLookup.tracking(player))
//                        {
//                            GeckoLibNetwork.syncAnimation(otherPlayer, this, id, ANIM_FIRE);
//                        }
                    }
                }
                else // Weapon is out of ammunition.
                {
                    if (!world.isClientSide)
                    {
                        this.reload(player, stack, null);
                    }
                }
            }
        }
    }

//    @Override
//    public void onAnimationSync(int id, int state)
//    {
//        if (state == ANIM_FIRE)
//        {
//            final AnimationController<?> controller = GeckoLibUtil.getControllerForID(this.factory, id, controllerName);
//            controller.markNeedsReload();
//            controller.setAnimation(new AnimationBuilder().addAnimation("animation.hand_cannon.fire"));
//        }
//        else if (state == ANIM_RELOAD)
//        {
//            final AnimationController<?> controller = GeckoLibUtil.getControllerForID(this.factory, id, controllerName);
//            controller.markNeedsReload();
//            controller.setAnimation(new AnimationBuilder().addAnimation("animation.hand_cannon.reload_r"));
//        }
//    }

    @Override
    protected GeoModel<? extends BaseGunItem> createModel()
    {
        return new HandCannonItemModel();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController(this, controllerName, 1, this::fireController));
    }

}
