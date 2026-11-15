package abysscreatures.ai;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * One drifting water point shared by a school.
 * Cohesion still pulls the fish together. The shared point moves, so the school does not orbit a fixed centre.
 */
public final class SchoolRoam {
    private static final double SHARE = 14.0;
    private static final List<Mark> MARKS = new ArrayList<>();

    private SchoolRoam() {}

    public static Vec3 point(Mob mob) {
        int tick = mob.tickCount;
        Mark found = null;
        double best = SHARE * SHARE;
        for (int i = MARKS.size() - 1; i >= 0; i--) {
            Mark mark = MARKS.get(i);
            if (tick > mark.until + 40) {
                MARKS.remove(i);
                continue;
            }
            double distance = mob.position().distanceToSqr(mark.pos);
            if (distance < best) {
                best = distance;
                found = mark;
            }
        }
        if (found != null && tick <= found.until) {
            return found.pos;
        }
        Vec3 next = RoamPoint.pick(mob, 8.0, 16.0);
        if (found == null) {
            found = new Mark();
            MARKS.add(found);
        }
        found.pos = next;
        found.until = tick + 80 + mob.getRandom().nextInt(40);
        return next;
    }

    private static final class Mark {
        Vec3 pos = Vec3.ZERO;
        int until;
    }
}
