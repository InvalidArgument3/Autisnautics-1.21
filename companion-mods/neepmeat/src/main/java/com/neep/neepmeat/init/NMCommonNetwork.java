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

package com.neep.neepmeat.init;

import com.neep.neepmeat.network.ScreenPropertyC2SPacket;
import com.neep.neepmeat.network.plc.PLCRobotC2S;
import com.neep.neepmeat.network.plc.PLCRobotEnterS2C;

public class NMCommonNetwork
{
    public static void init()
    {
        PLCRobotC2S.init();
        PLCRobotEnterS2C.init();
//        PLCSyncThings.init();
        ScreenPropertyC2SPacket.init();
    }
}
