package com.neep.meatweapons.component;

import com.neep.meatweapons.init.MWComponents;
import com.neep.meatweapons.item.meatgun.Meatgun;
import com.neep.meatweapons.meatgun.RootModuleHolder;
import com.neep.neepmeat.entity.goal.Action;
import com.neep.neepmeat.util.Easing;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class AdsCounter
{
    private final LivingEntity entity;
    private int ticks;
    private final int animationTicksIn = 6;
    private float f = 1;
    private final ActionThing action = new ActionThing();

    private UUID prevUUID = RootModuleHolder.NULL_UUID;

    public AdsCounter(LivingEntity entity)
    {
        this.entity = entity;
    }

    public void tick()
    {
        action.tick();

        // Top check prevents instantiation of components for every item the player holds
        // Also reduces frequency of the CCA Item concurrency issue
        if (entity.getMainHandItem().getItem() instanceof Meatgun)
        {
            var component = MWComponents.MEATGUN.getNullable(entity.getMainHandItem());

            if (component == null)
            {
                prevUUID = RootModuleHolder.NULL_UUID;
            }
            else if (!prevUUID.equals(component.getUuid()))
            {
                prevUUID = component.getUuid();
                action.setSequence(action.idle);
            }
        }
    }

    public static boolean isADS(LivingEntity entity, ItemStack stack, InteractionHand hand)
    {
        if (hand != InteractionHand.MAIN_HAND)
            return false;

        var component = MWComponents.GUN_TICK_HANDLER.getNullable(entity);
        if (component != null)
        {
            return component.getAds().isADS(entity, entity.getMainHandItem(), stack);
        }

        return false;
    }

    /// For use in renderers where the stack that the player is holding may be momentarily different to the one that is being rendered
    public boolean isADS(LivingEntity entity, ItemStack mainHandStack, ItemStack stack)
    {
        return mainHandStack.equals(stack)
                && animationFinished()
                && entity.isShiftKeyDown()
                && !(entity.getOffhandItem().getItem() instanceof Meatgun);
    }

    public boolean animationFinished()
    {
        return ticks >= animationTicksIn;
    }

    public int getCounter()
    {
        return ticks;
    }

    private boolean canEnterAds()
    {
        return entity.isShiftKeyDown() && (entity.getMainHandItem().getItem() instanceof Meatgun meatgun && meatgun.supportsAds());
    }

    public float getProgress(float tickDelta)
    {
        if (Minecraft.getInstance().isPaused())
            tickDelta = 0;

        // jank
        var seq = action.getSequence();

        if (seq == action.idle)
            return 0;
        else if (seq == action.idleAds)
            return 1;
        else
            return Mth.clamp((ticks + (f * tickDelta)) / (float) animationTicksIn, 0f, 1f);
    }

    public float getAnimation(float tickDelta, boolean mainHand)
    {
        if (!mainHand)
            return 0;

        float progress = getProgress(tickDelta);
        return (float) Easing.easeInOutCubic(progress);
    }

    // A state machine is probably overkill
    private class ActionThing extends Action.Simple<ActionThing>
    {
        private ActionThing()
        {
            setSequence(idle);
        }

        private final Sequence<ActionThing> idle = (parent, counter) ->
        {
            ticks = 0;
            if (canEnterAds())
                setSequence(parent.in);
        };

        private final Sequence<ActionThing> idleAds = (parent, counter) ->
        {
            ticks = animationTicksIn;
            if (!canEnterAds())
                setSequence(parent.out);
        };

        private final Sequence<ActionThing> in = (parent, counter1) ->
        {
            if (canEnterAds())
            {
                f = 1;
                ticks = Math.min(animationTicksIn, ticks + 1);
                if (ticks == animationTicksIn)
                    setSequence(parent.idleAds);
            }
            else
            {
                f = -1;
                setSequence(parent.out);
            }
        };

        private final Sequence<ActionThing> out = (parent, counter1) ->
        {
            if (!canEnterAds())
            {
                f = -1;
                ticks = Math.max(0, ticks - 1);
                if (ticks == 0)
                    setSequence(parent.idle);
            }
            else
            {
                f = 1;
                setSequence(in);
            }
        };
    }
}
