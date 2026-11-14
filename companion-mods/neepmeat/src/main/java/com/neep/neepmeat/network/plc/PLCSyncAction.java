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

package com.neep.neepmeat.network.plc;

import com.neep.meatlib.api.network.ParamCodec;
import com.neep.neepmeat.plc.screen.PLCScreenHandler;
import net.minecraft.server.level.ServerPlayer;

public enum PLCSyncAction
{
    PROGRAM,
    RUN,
    PAUSE,
    STOP,
    COMPILE,
    COMPILE_RUN,
    STEP;

    public static void sendCompileStatus(ServerPlayer controller, String message, boolean b, int line)
    {
        if (controller.containerMenu instanceof PLCScreenHandler handler)
        {
            handler.compileMessageS2C.emitter().apply(message, b, line);
        }
    }

    public static final ParamCodec<PLCSyncAction> PARAM_CODEC = ParamCodec.of(PLCSyncAction.class, (o, b) -> b.writeVarInt(o.ordinal()), b -> PLCSyncAction.values()[b.readVarInt()]);
}
