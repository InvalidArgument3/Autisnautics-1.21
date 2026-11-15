package abysscreatures.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.Level;
import abysscreatures.entity.WaterAbyssCreature;

/**
 * 3D swim steer.
 * WaterBoundPathNavigation keeps a single node and reports the path unfinished when the fish
 * circles it, so the stroll never picks another point. This stores the destination and faces it
 * directly. There is no home position.
 */
public class DirectSwimNavigation extends WaterBoundPathNavigation {
    private double tx;
    private double ty;
    private double tz;
    private boolean moving;
    private int age;

    public DirectSwimNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    public boolean moveTo(double x, double y, double z, double speed) {
        this.tx = x;
        this.ty = y;
        this.tz = z;
        this.speedModifier = speed;
        if (!this.moving) {
            this.age = 0;
        }
        this.moving = true;
        this.face();
        this.mob.getMoveControl().setWantedPosition(x, y, z, speed);
        return true;
    }

    @Override
    public boolean moveTo(Entity entity, double speed) {
        return this.moveTo(entity.getX(), entity.getY(0.5), entity.getZ(), speed);
    }

    @Override
    public void tick() {
        if (!this.moving) {
            return;
        }
        this.age++;
        double arrive = Math.max(1.5, this.mob.getBbWidth() * 0.6);
        if (this.age > 80 || this.mob.distanceToSqr(this.tx, this.ty, this.tz) < arrive * arrive) {
            this.stop();
            return;
        }
        this.face();
        this.mob.getMoveControl().setWantedPosition(this.tx, this.ty, this.tz, this.speedModifier);
    }

    private void face() {
        double dx = this.tx - this.mob.getX();
        double dz = this.tz - this.mob.getZ();
        if (Math.hypot(dx, dz) < 0.75) {
            return;
        }
        float turn = 10.0f;
        if (this.mob instanceof WaterAbyssCreature fish) {
            turn = fish.movement().swim.turn.y;
        }
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        this.mob.setYRot(Course.rotlerp(this.mob.getYRot(), yaw, turn));
        this.mob.yBodyRot = this.mob.getYRot();
    }

    @Override
    public boolean isDone() {
        return !this.moving;
    }

    @Override
    public boolean isInProgress() {
        return this.moving;
    }

    @Override
    public void stop() {
        this.moving = false;
        this.age = 0;
        super.stop();
    }

    @Override
    protected boolean canUpdatePath() {
        return true;
    }
}
