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

import com.neep.meatlib.registry.RegistrationContext;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.client.model.GenericModel;
import com.neep.neepmeat.client.renderer.item.SwordRenderer;
import com.neep.neepmeat.compat.forge.GeoItemFabric;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

public class SlasherItem extends AnimatedSword implements GeoItem
{
    public static String CONTROLLER_NAME = "controller";
    private final AnimatableInstanceCache instanceCache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> rendererProvider = GeoItemFabric.makeRenderer(this);

    public SlasherItem(RegistrationContext ctx, Properties settings)
    {
        super(ctx, Tiers.DIAMOND, 0, -1.2f, settings);
    }

    private PlayState predicate(AnimationState<SlasherItem> event)
    {
        return PlayState.CONTINUE;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker)
    {
        super.hurtEnemy(stack, target, attacker);
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        if (world instanceof ServerLevel serverWorld)
        {
            long id = GeoItem.getOrAssignId(user.getItemInHand(hand), serverWorld);
//            triggerAnim(user, id, );

//            if (user.isSprinting())
//            {
//                GeckoLibNetwork.syncAnimation(user, this, id, ANIM_STAB);
//            }
//            else
//            {
//                GeckoLibNetwork.syncAnimation(user, this, id, ANIM_SWING);
//            }
        }
        return super.use(world, user, hand);
    }

//    @Override
//    public void onAnimationSync(int id, int state)
//    {
//        final AnimationController controller = GeckoLibUtil.getControllerForID(this.factory, id, CONTROLLER_NAME);
//
//        controller.transitionLengthTicks = 1;
//        switch (state)
//        {
//            case ANIM_SWING:
//                controller.markNeedsReload();
//                controller.setAnimation(new AnimationBuilder().addAnimation("animation.slasher.swing"));
//                break;
//            case ANIM_STAB:
//                controller.markNeedsReload();
//                controller.setAnimation(new AnimationBuilder().addAnimation("animation.slasher.stab"));
//                break;
//            default:
//
//
//        }
//    }

    @Override
    public AnimationQueue getQueue()
    {
        return null;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer)
    {
        consumer.accept(new RenderProvider()
        {
            private SwordRenderer<SlasherItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer()
            {
                if (this.renderer == null)
                    this.renderer = new SwordRenderer<>(
                            new GenericModel<>(
                                    NeepMeat.NAMESPACE,
                                    "geo/slasher.geo.json",
                                    "textures/item/slasher.png",
                                    "animations/slasher.animation.json"

                            )
                    );

                return this.renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider()
    {
        return rendererProvider;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<>(this, CONTROLLER_NAME, 20, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache()
    {
        return instanceCache;
    }
}
