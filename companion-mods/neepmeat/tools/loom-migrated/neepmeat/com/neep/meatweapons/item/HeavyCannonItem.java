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
import com.neep.meatweapons.client.model.HeavyCannonItemModel;
import com.neep.meatweapons.entity.ExplodingShellEntity;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.neepmeat.init.NMSounds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.model.GeoModel;

public class HeavyCannonItem extends BaseGunItem
{
    public String controllerName = "controller";

    public HeavyCannonItem()
    {
        super(MWItems.SMALL_BALLISTIC_MAGAZINE, 1, 15,false, new MeatlibItemSettings());
        this.sounds.put(GunSounds.FIRE_PRIMARY, NMSounds.HAND_CANNON_FIRE);
        this.sounds.put(GunSounds.RELOAD, NMSounds.HAND_CANNON_RELOAD);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.NONE;
    }

    @Override
    protected GeoModel<? extends BaseGunItem> createModel()
    {
        return new HeavyCannonItemModel();
    }

    @Override
    public Vec3 getMuzzleOffset(LivingEntity entity, ItemStack stack)
    {
        return new Vec3(entity.getMainHandItem().equals(stack) ? -0.2 : 0.2,
                entity.isShiftKeyDown() ? -0.15 : 0.1,
                0);
    }

    @Override
    public void trigger(Level world, Player player, ItemStack stack, int id, double pitch, double yaw, MWAttackC2SPacket.HandType handType, InteractionHand hand)
    {
        if (!player.getCooldowns().isOnCooldown(this))
        {
            if (stack.getDamageValue() != this.maxShots)
            {
                player.getCooldowns().addCooldown(this, cooldown);

                if (!world.isClientSide)
                {
//                    double yaw = Math.toRadians(player.getHeadYaw());
//                    double pitch = Math.toRadians(player.getPitch(0.1f));

                    double mult = 1.5; // Multiplier for bullet speed.
                    double vx = mult * -Math.sin(yaw) * Math.cos(pitch) + player.getDeltaMovement().x();
                    double vy = mult * -Math.sin(pitch) + player.getDeltaMovement().y();
                    double vz = mult * Math.cos(yaw) * Math.cos(pitch) + player.getDeltaMovement().z();

                    Vec3 pos = new Vec3(player.getX(), player.getY() + 1.4, player.getZ());
                    if (!player.isShiftKeyDown())
                    {
                        Vec3 transform = getMuzzleOffset(player, stack).yRot((float) -yaw);
                        pos = pos.add(transform);
                    }

                    ExplodingShellEntity shell = new ExplodingShellEntity(world, 2, true, pos.x, pos.y, pos.z, vx, vy, vz);
                    shell.setOwner(player);
                    world.addFreshEntity(shell);

                    playSound(world, player, GunSounds.FIRE_PRIMARY);

                    if (!player.isCreative()) stack.setDamageValue(stack.getDamageValue() + 1);

                    // TODO
//                    final int anim = GeckoLibUtil.guaranteeIDForStack(stack, (ServerWorld) world);
//                    GeckoLibNetwork.syncAnimation(player, this, anim, ANIM_FIRE);
//                    for (PlayerEntity otherPlayer : PlayerLookup.tracking(player))
//                    {
//                        GeckoLibNetwork.syncAnimation(otherPlayer, this, anim, ANIM_FIRE);
//                    }
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

//    @Override
//    public void onAnimationSync(int id, int state)
//    {
//        if (state == ANIM_FIRE)
//        {
//            final AnimationController<?> controller = GeckoLibUtil.getControllerForID(this.factory, id, controllerName);
//                controller.markNeedsReload();
//                controller.setAnimation(new AnimationBuilder().addAnimation("animation.heavy_cannon.fire"));
//        }
//        else if (state == ANIM_RELOAD)
//        {
//            final AnimationController<?> controller = GeckoLibUtil.getControllerForID(this.factory, id, controllerName);
//            controller.markNeedsReload();
//            controller.setAnimation(new AnimationBuilder().addAnimation("animation.heavy_cannon.reload"));
//        }
//    }
}
