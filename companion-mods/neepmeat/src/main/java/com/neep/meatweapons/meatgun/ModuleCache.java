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

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.neep.meatweapons.meatgun.module.AmmunitionStoringModule;
import com.neep.meatweapons.meatgun.module.MeatgunModule;
import com.neep.meatweapons.meatgun.module.MeatgunModules;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Enables retrieval of modules using a hash map instead of a silly recursive search with each query.
 * Call {@link ModuleCache#invalidate()} whenever the hierarchy changes.
 */
public class ModuleCache
{
    // I'm not sure why I've added so many duplicate null checks instead of making a function that does the check and then generates the cache.
    // I just felt like it. It's strange.

    private final MeatgunModule root;

    @Nullable private Map<UUID, MeatgunModule> uuidLookup = null;
    @Nullable private Map<AmmunitionType, List<AmmunitionStoringModule>> ammunition;
    @Nullable private Set<MeatgunModule.Type<?>> moduleTypes; // For querying whether a module type is present.
    @Nullable private Multimap<MeatgunModule.Type<?>, MeatgunModule> typeToModules;

    public ModuleCache(MeatgunModule root)
    {
        this.root = root;
    }

    @Nullable
    public MeatgunModule findModule(UUID uuid)
    {
        if (uuidLookup == null)
            uuidLookup = makeModuleCache();

        return uuidLookup.get(uuid);
    }

    @Nullable
    public MeatgunModule findModule(MeatgunModule.Type<?> type)
    {
        if (uuidLookup == null)
            uuidLookup = makeModuleCache();

        // Pathetic linear search
        for (var module : uuidLookup.values())
        {
            if (module.getType() == type)
                return module;
        }

        return null;
    }

    public int getAmmo(AmmunitionType type)
    {
        if (ammunition == null)
            ammunition = makeAmmoCache();

        return ammunition.getOrDefault(type, List.of()).stream().mapToInt(AmmunitionStoringModule::amount).sum();
    }

    public int getCapacity(AmmunitionType type)
    {
        if (ammunition == null)
            ammunition = makeAmmoCache();

        return ammunition.getOrDefault(type, List.of()).stream().mapToInt(AmmunitionStoringModule::capacity).sum();
    }

    public Map<AmmunitionType, List<AmmunitionStoringModule>> getAmmo()
    {
        if (ammunition == null)
            ammunition = makeAmmoCache();

        return ammunition;
    }

    public Collection<MeatgunModule> getModules()
    {
        if (uuidLookup == null)
            uuidLookup = makeModuleCache();

        return uuidLookup.values();
    }

    public Set<MeatgunModule.Type<?>> moduleTypes()
    {
        if (moduleTypes == null)
            moduleTypes = makeTypeCache();

        return moduleTypes;
    }

    public Collection<MeatgunModule> modulesOfType(MeatgunModule.Type<?> type)
    {
        if (typeToModules == null)
            typeToModules = makeTypeToModuleCache();

        return typeToModules.get(type);
    }

    private Multimap<MeatgunModule.Type<?>, MeatgunModule> makeTypeToModuleCache()
    {
        Multimap<MeatgunModule.Type<?>, MeatgunModule> map = MultimapBuilder.hashKeys().hashSetValues().build();

        if (uuidLookup == null)
            uuidLookup = makeModuleCache();

        uuidLookup.values().forEach(m -> map.put(m.getType(), m));

        return map;
    }

    private Map<UUID, MeatgunModule> makeModuleCache()
    {
        Map<UUID, MeatgunModule> map = new HashMap<>();

        addRecursive(root, map);

        // Invalidate dependent caches
        ammunition = null;
        moduleTypes = null;
        typeToModules = null;

        return map;
    }

    private Map<AmmunitionType, List<AmmunitionStoringModule>> makeAmmoCache()
    {
        if (uuidLookup == null)
            uuidLookup = makeModuleCache();

        Map<AmmunitionType, List<AmmunitionStoringModule>> map = new EnumMap<>(AmmunitionType.class);

        // Sort each module by the type it provides for quick(er than the alternative) access via an EnumMap
        map = uuidLookup.values().stream()
                .filter(m -> m instanceof AmmunitionStoringModule module && module.ammoType() != null)
                .map(m -> (AmmunitionStoringModule) m)
                .collect(Collectors.groupingBy(AmmunitionStoringModule::ammoType, () -> new EnumMap<>(AmmunitionType.class), Collectors.toList()));

        return map;
    }

    private Set<MeatgunModule.Type<?>> makeTypeCache()
    {
        if (uuidLookup == null)
            uuidLookup = makeModuleCache();

        Set<MeatgunModule.Type<?>> types = new HashSet<>();

        for (var module : uuidLookup.values())
            types.add(module.getType());

        return types;
    }

    private void addRecursive(MeatgunModule module, Map<UUID, MeatgunModule> collector)
    {
        collector.put(module.getUuid(), module);

        for (var slot : module.getChildren())
        {
            MeatgunModule child = slot.get();
            if (child == MeatgunModule.DEFAULT)
                continue;

            addRecursive(child, collector);
        }
    }

    @Nullable
    private MeatgunModule findRecursive(MeatgunModule module, UUID uuid)
    {
        if (module.getUuid().equals(uuid))
            return module;

        for (var slot : module.getChildren())
        {
            MeatgunModule child = slot.get();
            if (child == MeatgunModule.DEFAULT)
                continue;

            if (child.getUuid().equals(uuid))
                return child;

            MeatgunModule next = findRecursive(child, uuid);
            if (next != null)
                return next;
        }
        return null;
    }

    public void invalidate()
    {
        uuidLookup = null;
        ammunition = null;
        moduleTypes = null;
    }
}
