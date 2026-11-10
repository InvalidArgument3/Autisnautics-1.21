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
import com.neep.meatweapons.client.model.PistolItemModel;
import com.neep.meatweapons.network.MWAttackC2SPacket;
import com.neep.neepmeat.init.NMSounds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.model.GeoModel;

public class MachinePistolItem extends BaseGunItem implements Aimable
{
    public String controllerName = "controller";

    public MachinePistolItem()
    {
        super(MWItems.SMALL_BALLISTIC_MAGAZINE, 24, 10, false, new MeatlibItemSettings());
        this.sounds.put(GunSounds.FIRE_PRIMARY, NMSounds.HAND_CANNON_FIRE);
        this.sounds.put(GunSounds.RELOAD, NMSounds.HAND_CANNON_RELOAD);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.NONE;
    }

    @Override
    public Vector3f getAimOffset()
    {
        return new Vector3f(0.0f, 0, 0);
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
            if (getShots(stack, 0) > 0)
            {
                player.getCooldowns().addCooldown(this, 1);

                if (!world.isClientSide)
                {
                    fireBeam(world, player, stack);
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

//    @Override
//    public void onAnimationSync(int id, int state)
//    {
//        if (state == ANIM_FIRE)
//        {
//            final AnimationController<?> controller = GeckoLibUtil.getControllerForID(this.factory, id, controllerName);
//                controller.markNeedsReload();
//                controller.setAnimation(new AnimationBuilder().addAnimation("animation.machine_pistol.fire"));
//        }
//        else if (state == ANIM_RELOAD)
//        {
//            final AnimationController<?> controller = GeckoLibUtil.getControllerForID(this.factory, id, controllerName);
//            controller.markNeedsReload();
//            controller.setAnimation(new AnimationBuilder().addAnimation("animation.machine_pistol.reload_r"));
//        }
//    }

    @Override
    protected GeoModel<? extends BaseGunItem> createModel()
    {
        return new PistolItemModel();
    }
}
