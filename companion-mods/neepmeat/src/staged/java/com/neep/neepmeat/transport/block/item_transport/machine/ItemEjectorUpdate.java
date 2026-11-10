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

package com.neep.neepmeat.transport.block.item_transport.machine;

import com.neep.meatlib.api.network.ChannelFormat;
import com.neep.meatlib.api.network.ParamCodec;
import com.neep.meatlib.util.RedstoneMode;

public interface ItemEjectorUpdate
{
    ChannelFormat<ItemEjectorUpdate> CHANNEL_FORMAT = ChannelFormat.builder(ItemEjectorUpdate.class)
            .param(ParamCodec.BOOLEAN).param(ParamCodec.INT).param(ParamCodec.INT).param(RedstoneMode.PARAM_CODEC).build();

    void update(boolean roundRobin, int amount, int period, RedstoneMode redstoneMode);
}
