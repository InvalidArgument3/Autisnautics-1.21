package com.neep.meatweapons;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/// Associates an entity type with a method to determine whether a bullet made a critical hit.
public class EntityCritRegistry
{
    @FunctionalInterface
    public interface CritProvider<T extends Entity>
    {
        float apply(T entity, Vec3 hitPos, Vec3 direction);
    }

    public static final float DEFAULT_CRIT_MULTIPLIER = 2;

    public static final CritProvider<Entity> FALLBACK = (entity, hitPos, direction) ->
    {
        var eyePos = entity.getEyePosition();

        float w;
        if (entity.getBbWidth() < entity.getBbHeight())
            w = entity.getBbWidth(); // Assume this mob is tall and has a head on the top
        else
            w = Math.min(0.63f, entity.getBbWidth()); // 10 / 16

        float penetration = 1.5f;
        AABB headBox = AABB.ofSize(eyePos, w, w, w);
        Vec3 endPos = hitPos.add(direction.scale(penetration));
        boolean hitHead = headBox.contains(hitPos) || headBox.clip(hitPos, endPos).isPresent();
        return hitHead ? DEFAULT_CRIT_MULTIPLIER : 1;
    };

    public static <T extends Entity> CritProvider<T> none() { return (entity, hitPos, direction) -> 1; }
    public static <T extends Entity> CritProvider<T> fallback() { return (CritProvider<T>) FALLBACK; }

    private static final Map<EntityType<?>, CritProvider<?>> MAP = new IdentityHashMap<>();

    public static <T extends Entity> void register(EntityType<T> type, CritProvider<T> provider)
    {
        MAP.put(type, provider);
    }

    public static <T extends Entity> CritProvider<T> get(T entity)
    {
        return get((EntityType<T>) entity.getType());
    }

    public static <T extends Entity> CritProvider<T> get(EntityType<T> type)
    {
        return (CritProvider<T>) MAP.getOrDefault(type, FALLBACK);
    }

    public static void initDefaults()
    {
//        register(EntityType.ALLAY, fallback());
//        register(EntityType.AXOLOTL, none());
//        register(EntityType.BAT, none());
//        register(EntityType.BEE, fallback());
//        register(EntityType.BLAZE, fallback());
//        register(EntityType.CAT, ());
//        register(EntityType.PIG, none());
//        register(EntityType.COW, none());
    }
}
