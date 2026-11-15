package abysscreatures.ai;

import abysscreatures.entity.ChainFish;
import abysscreatures.entity.CorpseAnglerEntity;
import abysscreatures.entity.WaterAbyssCreature;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * Survival players in water, on a fixed interval, inside the follow range in 3D.
 * The vanilla goal this replaces only inflated the search box by 4 blocks on Y.
 */
public class HuntGoal extends NearestAttackableTargetGoal<Player> {
    private final WaterAbyssCreature fish;
    private long nextScan;
    private int seenTick;

    public HuntGoal(WaterAbyssCreature fish) {
        super(fish, Player.class, 10, false, false, null);
        this.fish = fish;
        this.setFlags(EnumSet.noneOf(net.minecraft.world.entity.ai.goal.Goal.Flag.class));
    }

    @Override
    public boolean canUse() {
        if (this.fish instanceof CorpseAnglerEntity angler && angler.isBurrow()) {
            return false;
        }
        if (this.fish instanceof ChainFish chain && !chain.isLead()) {
            return false;
        }
        if (this.fish.level().getGameTime() < this.nextScan) {
            return false;
        }
        this.nextScan = this.fish.level().getGameTime() + 10L;
        this.findTarget();
        return this.target != null;
    }

    @Override
    protected void findTarget() {
        double range = range();
        AABB box = this.fish.getBoundingBox().inflate(range, range, range);
        Player best = null;
        double bestDistance = range * range;
        for (Player player : this.fish.level().getEntitiesOfClass(Player.class, box, this::acceptable)) {
            if (!this.fish.getSensing().hasLineOfSight(player)) {
                continue;
            }
            double distance = this.fish.distanceToSqr(player);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = player;
            }
        }
        this.target = best;
    }

    @Override
    public void start() {
        this.fish.setTarget(this.target);
        this.seenTick = this.fish.tickCount;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity current = this.fish.getTarget();
        if (!(current instanceof Player player) || !this.acceptable(player)) {
            return false;
        }
        double range = range() * 1.25;
        if (this.fish.distanceToSqr(player) > range * range) {
            return false;
        }
        if (this.fish.getSensing().hasLineOfSight(player)) {
            this.seenTick = this.fish.tickCount;
            return true;
        }
        return this.fish.tickCount - this.seenTick < 40;
    }

    @Override
    public void stop() {
        this.fish.setTarget(null);
        this.target = null;
    }

    private boolean acceptable(Player player) {
        if (!player.isAlive() || player.isSpectator() || player.isCreative() || player.getAbilities().instabuild) {
            return false;
        }
        return player.isInWater();
    }

    private double range() {
        var attribute = this.fish.getAttribute(Attributes.FOLLOW_RANGE);
        if (attribute == null) {
            return 24.0;
        }
        return Math.max(8.0, attribute.getValue());
    }
}
