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

package com.neep.neepbus.block.entity;

import com.neep.neepbus.util.NeepBusConfig;
import it.unimi.dsi.fastutil.floats.FloatList;

public interface MovementControl extends ConfigProvider
{
    FloatList getValues();

    void setValues(FloatList list);

    MovementControl EMPTY = new MovementControl()
    {
        @Override
        public NeepBusConfig getConfig()
        {
            return NeepBusConfig.EMPTY;
        }

        @Override
        public FloatList getValues()
        {
            return FloatList.of();
        }

        @Override
        public void setValues(FloatList list)
        {

        }
    };
}
