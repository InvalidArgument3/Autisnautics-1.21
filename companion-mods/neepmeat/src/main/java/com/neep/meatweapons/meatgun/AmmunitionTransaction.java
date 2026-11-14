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

package com.neep.meatweapons.meatgun;

public interface AmmunitionTransaction
{
    default boolean notEmpty()
    {
        return true;
    }

    void commit();

    default AmmunitionTransaction chain(AmmunitionTransaction other)
    {
        return new AmmunitionTransaction()
        {
            @Override
            public void commit()
            {
                AmmunitionTransaction.this.commit();
                other.commit();
            }

            @Override
            public boolean notEmpty()
            {
                return AmmunitionTransaction.this.notEmpty();
            }
        };
    }

    AmmunitionTransaction EMPTY = new AmmunitionTransaction()
    {
        @Override
        public boolean notEmpty()
        {
            return false;
        }

        @Override
        public void commit()
        {

        }
    };
}
