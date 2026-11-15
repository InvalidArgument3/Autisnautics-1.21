package abysscreatures.ai;

import abysscreatures.entity.CorpseAnglerEntity;
import abysscreatures.entity.FulgastraEntity;
import abysscreatures.entity.GhidruthEntity;
import abysscreatures.entity.GnasherEntity;
import abysscreatures.entity.MutavoreEntity;
import abysscreatures.entity.NecroshellEntity;
import abysscreatures.entity.SiamserpentHeadEntity;
import abysscreatures.entity.SpineWormHeadEntity;
import abysscreatures.entity.SplittedFulgastraEntity;
import abysscreatures.entity.WaterAbyssCreature;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/** Test log. Quiet unless the dedicated AI test (or abysscreatures.aiLog) is on. */
public final class AbyssAiLog {
    public static final boolean ON = Boolean.getBoolean("abysscreatures.aiTest") || Boolean.getBoolean("abysscreatures.aiLog");
    private static final Set<String> SEEN = new HashSet<>();

    private AbyssAiLog() {}

    public static boolean saw(WaterAbyssCreature mob, int state) {
        return SEEN.contains(mob.getId() + ":" + state);
    }

    public static void line(String message) {
        if (ON) {
            System.out.println("[AbyssAI] " + message);
        }
    }

    public static void animation(WaterAbyssCreature mob, int state) {
        if (!ON || state == 0 || mob.level().isClientSide()) {
            return;
        }
        SEEN.add(mob.getId() + ":" + state);
        line("ANIM " + id(mob) + " entity=" + mob.getId() + " state=" + state + " attack=" + attackName(mob, state));
    }

    public static void damage(WaterAbyssCreature attacker, LivingEntity victim, float amount, DamageSource source) {
        if (!ON || attacker.level().isClientSide()) {
            return;
        }
        line("DAMAGE attacker=" + id(attacker)
                + " entity=" + attacker.getId()
                + " anim=" + attacker.getAnimationState()
                + " attack=" + attackName(attacker, attacker.getAnimationState())
                + " amount=" + amount
                + " victim=" + victim.getName().getString()
                + " health=" + victim.getHealth()
                + " source=" + source.getMsgId());
    }

    private static String id(WaterAbyssCreature mob) {
        return String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()));
    }

    public static String attackName(WaterAbyssCreature mob, int state) {
        if (mob instanceof CorpseAnglerEntity) {
            return switch (state) {
                case 1 -> "ambush";
                case 2 -> "burrow";
                case 3 -> "unburrow";
                case 4 -> "open_mouth";
                default -> "idle";
            };
        }
        if (mob instanceof GhidruthEntity) {
            return switch (state) {
                case 1 -> "bite_right";
                case 2 -> "bite_left";
                case 3 -> "tail_right";
                case 4 -> "tail_left";
                case 5 -> "charge";
                case 6 -> "stun";
                case 7 -> "stun_end";
                default -> "idle";
            };
        }
        if (mob instanceof SiamserpentHeadEntity) {
            return switch (state) {
                case 2 -> "ray_start";
                case 3 -> "beam";
                case 5 -> "slash_right";
                case 6 -> "blaster_shot";
                case 7 -> "slasher_charge_start";
                case 8 -> "slasher_charging";
                case 9 -> "slash_left";
                case 10 -> "ray_charge";
                default -> "idle";
            };
        }
        if (mob instanceof NecroshellEntity) {
            return switch (state) {
                case 1 -> "claw";
                case 2 -> "hide";
                case 3 -> "unhide";
                default -> "idle";
            };
        }
        if (mob instanceof FulgastraEntity) {
            return switch (state) {
                case 1 -> "reform";
                case 2 -> "split";
                default -> "idle";
            };
        }
        if (mob instanceof SplittedFulgastraEntity) {
            return switch (state) {
                case 2 -> "shock";
                case 3 -> "closed";
                default -> "idle";
            };
        }
        if (mob instanceof MutavoreEntity) {
            return switch (state) {
                case 3 -> "tongue_start";
                case 4 -> "tongue";
                case 5 -> "tongue_end";
                case 6 -> "bubble";
                case 7 -> "bubble_end";
                default -> "idle";
            };
        }
        if (mob instanceof GnasherEntity) {
            return state == 1 ? "bite" : "idle";
        }
        if (mob instanceof SpineWormHeadEntity) {
            return "grab";
        }
        return "state_" + state;
    }
}
