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

package com.neep.neepbus.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public final class PortMap
{
    public static final PortMap EMPTY = new PortMap(Map.of(), Map.of(), new String[0], new WritePort[0], new String[0], new ReadPort[0]);

    private static final String[] EMPTY_STRINGS = new String[0];
    private static final WritePort[] EMPTY_WRITE = new WritePort[0];
    private static final ReadPort[] EMPTY_READ = new ReadPort[0];

    private final Map<String, WritePort> writeMap;
    private final Map<String, ReadPort> readMap;

    private final String[] writeAddresses;
    private final WritePort[] writePorts;
    private final String[] readAddresses;
    private final ReadPort[] readPorts;

    public PortMap(Map<String, WritePort> writeMap, Map<String, ReadPort> readMap,
                   String[] writeAddresses, WritePort[] writePorts,
                   String[] readAddresses, ReadPort[] readPorts
    )
    {
        this.writeMap = writeMap;
        this.readMap = readMap;
        this.writeAddresses = writeAddresses;
        this.writePorts = writePorts;
        this.readAddresses = readAddresses;
        this.readPorts = readPorts;
    }

    public static PortMap arrayMap(String[] writeAddresses, WritePort[] writePorts, String[] readAddresses, ReadPort[] readPorts)
    {
        Map<String, WritePort> writePortMap = new Object2ObjectArrayMap<>(writeAddresses, writePorts);
        Map<String, ReadPort> readPortMap = new Object2ObjectArrayMap<>(readAddresses, readPorts);

        return new PortMap(writePortMap, readPortMap, writeAddresses, writePorts, readAddresses, readPorts);
    }

    public PortMap delegate(int i)
    {
        String writeAddress = writeAddresses[i];
        String readAddress = readAddresses[i];
        WritePort writePort = writePorts[i];
        ReadPort readPort = readPorts[i];

        return new PortMap(Object2ObjectMaps.singleton(writeAddress, writePort), Object2ObjectMaps.singleton(readAddress, readPort),
                // It's slightly janky, but we really don't need to store these things in the delegate.
                EMPTY_STRINGS, EMPTY_WRITE, EMPTY_STRINGS, EMPTY_READ);
    }

    public Map<String, WritePort> writeMap()
    {
        return writeMap;
    }

    public Map<String, ReadPort> readMap()
    {
        return readMap;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PortMap) obj;
        return Objects.equals(this.writeMap, that.writeMap) &&
                Objects.equals(this.readMap, that.readMap) &&
                Arrays.equals(this.writeAddresses, that.writeAddresses) &&
                Arrays.equals(this.writePorts, that.writePorts) &&
                Arrays.equals(this.readAddresses, that.readAddresses) &&
                Arrays.equals(this.readPorts, that.readPorts);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(writeMap, readMap, writeAddresses, writePorts, readAddresses, readPorts);
    }

    @Override
    public String toString()
    {
        return "PortMap[" +
                "writeMap=" + writeMap + ", " +
                "readMap=" + readMap + ", " +
                "writeAddresses=" + writeAddresses + ", " +
                "writePorts=" + writePorts + ", " +
                "readAddresses=" + readAddresses + ", " +
                "readPorts=" + readPorts + ']';
    }

}
