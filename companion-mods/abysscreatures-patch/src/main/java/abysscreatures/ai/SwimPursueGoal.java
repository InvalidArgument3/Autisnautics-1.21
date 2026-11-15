package abysscreatures.ai;

import abysscreatures.entity.ChainFish;
import abysscreatures.entity.CorpseAnglerEntity;
import abysscreatures.entity.GhidruthEntity;
import abysscreatures.entity.SpineWormHeadEntity;
import abysscreatures.entity.WaterAbyssCreature;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

/** Keep the existing attack goals. This only closes distance through the water until one of them can fire. */
public class SwimPursueGoal extends Goal {
    private final WaterAbyssCreature fish;

    public SwimPursueGoal(WaterAbyssCreature fish) {
        this.fish = fish;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.ready() && this.fish.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    private boolean ready() {
        if (!this.fish.canMove() || !this.fish.isInWater()) {
            return false;
        }
        if (this.fish instanceof CorpseAnglerEntity angler && angler.isBurrow()) {
            return false;
        }
        if (this.fish instanceof GhidruthEntity ghidruth && (ghidruth.isCharge() || ghidruth.isStunned())) {
            return false;
        }
        if (this.fish instanceof SpineWormHeadEntity) {
            return false;
        }
        if (this.fish instanceof ChainFish chain && !chain.isLead()) {
            return false;
        }
        LivingEntity target = this.fish.getTarget();
        return target instanceof Player player && player.isAlive() && player.isInWater() && !player.isCreative() && !player.isSpectator();
    }

    @Override
    public void tick() {
        LivingEntity target = this.fish.getTarget();
        if (target == null) {
            return;
        }
        this.fish.getNavigation().moveTo(target.getX(), target.getY(0.5), target.getZ(), 1.15);
    }

    @Override
    public void stop() {
        this.fish.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
