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

package com.neep.neepmeat.entity.skyworm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SkywormAnimationTimes(int descend, int stay, int ascend, int intermediate) {
    public static final Codec<SkywormAnimationTimes> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("descend").forGetter(SkywormAnimationTimes::descend),
                    Codec.INT.fieldOf("stay").forGetter(SkywormAnimationTimes::stay),
                    Codec.INT.fieldOf("ascend").forGetter(SkywormAnimationTimes::ascend),
                    Codec.INT.fieldOf("intermediate").forGetter(SkywormAnimationTimes::intermediate))
                    .apply(instance, SkywormAnimationTimes::new));

    public long total() {
        return (long) descend + stay + ascend + intermediate;
    }

    public long receiverWormAppear() {
        return (long) ascend + intermediate;
    }
}
