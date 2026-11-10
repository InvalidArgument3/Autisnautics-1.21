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

package com.neep.neepmeat.entity.repeater;

import com.neep.neepmeat.entity.SimpleEntity;
import com.neep.neepmeat.entity.goal.Action;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class RepeaterEntity extends SimpleEntity implements GeoEntity
{
    private final AnimatableInstanceCache instanceCache = GeckoLibUtil.createInstanceCache(this);
    private final Speaker speaker;

    public RepeaterEntity(EntityType<?> type, Level world)
    {
        super(type, world);
        this.speaker = new Speaker(this);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (level().isClientSide())
        {
            speaker.tick();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(
                new AnimationController<>(this, "move", 5, this::moveController));
    }

    private PlayState moveController(AnimationState<RepeaterEntity> event)
    {
        event.setAndContinue(RawAnimation.begin().thenLoop("animation.repeater.idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache()
    {
        return instanceCache;
    }

    protected static class Speaker extends Action.Simple<Speaker>
    {
        private final RandomSource random = RandomSource.create();
        private final Entity parent;

        private Language language = Language.ENGLISH;
        private int numbersToPlay = 5;

        private final Sequence<Speaker> playNumbers = (t, counter) ->
        {
            int delay = 20;
            if (numbersToPlay > 0 && counter >= delay)
            {
                int i = random.nextInt(10);
                SoundEvent soundEvent = language.numbers.get(i);

                playSound(soundEvent, 0.1f);

                numbersToPlay = Math.max(0, numbersToPlay - 1);

                setSequence(t.playNumbers);
            }
            else if (numbersToPlay <= 0 && counter >= delay)
            {
                setSequence(t.playJingle);
            }
        };

        private final Sequence<Speaker> playJingle = (t, counter) ->
        {
            if (counter == 0)
            {
                playSound(NumbersSounds.JINGLE_1, 0);
            }

            if (counter >= 13 * 20)
            {
                setSequence(t.chooseNumbers);
            }
        };

        private final Sequence<Speaker> chooseNumbers = (t, counter) ->
        {
            language = nextLanguage();
            numbersToPlay = random.nextIntBetweenInclusive(10, 17);

            if (counter >= 10)
                setSequence(playNumbers);
        };

        protected Speaker(Entity parent)
        {
            this.parent = parent;
            setSequence(playJingle);
        }

        @Override
        public void tick()
        {
            super.tick();
        }

        private void playSound(SoundEvent soundEvent, float deviation)
        {
            // Use world#playSound as entity#playSound cancels on client
            parent.level().playLocalSound(
                    parent.getX(),
                    parent.getY(),
                    parent.getZ(),
                    soundEvent, SoundSource.NEUTRAL,
                    1, (float) random.triangle(1, deviation),
                    false);
        }

        private Language nextLanguage()
        {
            return Language.values()[random.nextInt(Language.values().length)];
        }
    }

    public enum Language
    {
        ENGLISH(NumbersSounds.ENGLISH),
        GERMAN(NumbersSounds.GERMAN);

        private final List<SoundEvent> numbers;

        Language(List<SoundEvent> numbers)
        {
            this.numbers = numbers;
        }
    }
}
