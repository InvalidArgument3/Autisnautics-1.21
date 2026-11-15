package abysscreatures.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** A water cell near the mob. The point is taken from the mob's current position, never a home or spawn anchor. */
public final class RoamPoint {
    private RoamPoint() {}

    public static Vec3 pick(Mob mob, double minDistance, double maxDistance) {
        Level level = mob.level();
        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = mob.getRandom().nextDouble() * Math.PI * 2.0;
            double distance = minDistance + mob.getRandom().nextDouble() * (maxDistance - minDistance);
            double dy = (mob.getRandom().nextDouble() - 0.5) * 8.0;
            BlockPos pos = BlockPos.containing(
                    mob.getX() + Math.cos(angle) * distance,
                    mob.getY() + dy,
                    mob.getZ() + Math.sin(angle) * distance);
            if (level.getFluidState(pos).is(FluidTags.WATER)) {
                return Vec3.atCenterOf(pos);
            }
        }
        double angle = mob.getRandom().nextDouble() * Math.PI * 2.0;
        return mob.position().add(Math.cos(angle) * minDistance, 0.0, Math.sin(angle) * minDistance);
    }
}
