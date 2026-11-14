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

public interface PIDController
{
    int getDt();
    void setDt(int dt);

    void setKP(float kp);
    float getKP();

    void setKI(float ki);
    float getKI();

    void setKD(float kd);
    float getKD();

    void setWindupLimit(float windupLimit);
    float getWindupLimit();

    void markBlockDirty();

    PIDController EMPTY = new PIDController()
    {
        @Override
        public int getDt()
        {
            return 0;
        }

        @Override
        public void setDt(int dt)
        {

        }

        @Override
        public void setKP(float kp)
        {

        }

        @Override
        public float getKP()
        {
            return 0;
        }

        @Override
        public void setKI(float ki)
        {

        }

        @Override
        public float getKI()
        {
            return 0;
        }

        @Override
        public void setKD(float kd)
        {

        }

        @Override
        public float getKD()
        {
            return 0;
        }

        @Override
        public void setWindupLimit(float windupLimit)
        {

        }

        @Override
        public float getWindupLimit()
        {
            return 0;
        }

        @Override
        public void markBlockDirty()
        {

        }
    };
}
