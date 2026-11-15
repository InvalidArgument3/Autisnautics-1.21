package abysscreatures.ai;

import abysscreatures.entity.CorpseAnglerEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Lure of the corpse angler.
 *
 * <p>min01's Lovecraft: Beyond the Abyss (branch {@code 1.20.1}) defines the trigger in
 * {@code CorpseAnglerEntity.hurt}: while animation state is 3 (the burrow animation), a player
 * attack whose ray clips the model part named {@code "Bait"} starts the unburrow animation.
 * The part is built in {@code CorpseAnglerModel} as the bulb at the end of the illicium, and its
 * child {@code "Gnasher"} is the corpse hung on that bulb. {@code CorpseAnglerAmbushGoal} is the
 * settled trigger: once {@code isBurrow()} is true, a target within 2.5 blocks of the
 * {@code "Gnasher"} model position starts the ambush. There is no right-click {@code mobInteract}.
 *
 * <p>This port has no {@code partBuilder}, so the bait is a box in front of and above the body,
 * the same place the straight illicium holds the bulb while the angler is buried in the seabed.
 * A player attack whose look ray hits that box is the Bait clip. Standing inside 2.5 blocks of
 * the same box is the ambush goal.
 */
public final class Bait {
    /** Original {@code CorpseAnglerAmbushGoal} distance to the Gnasher lure. */
    public static final double AMBUSH = 2.5;
    /** Original {@code partBuilder.clip} uses the player's attack reach. Five blocks covers that reach. */
    public static final double REACH = 5.0;

    private Bait() {}

    public static Vec3 center(CorpseAnglerEntity angler) {
        float yaw = angler.getYRot() * ((float) Math.PI / 180.0f);
        double forwardX = -Math.sin(yaw);
        double forwardZ = Math.cos(yaw);
        return new Vec3(
                angler.getX() + forwardX * 1.2,
                angler.getY() + angler.getBbHeight() + 2.2,
                angler.getZ() + forwardZ * 1.2);
    }

    public static AABB box(CorpseAnglerEntity angler) {
        Vec3 center = center(angler);
        return new AABB(center.x - 0.75, center.y - 0.75, center.z - 0.75, center.x + 0.75, center.y + 0.75, center.z + 0.75);
    }

    public static boolean hits(CorpseAnglerEntity angler, Player player) {
        if (!hunter(player)) {
            return false;
        }
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(REACH));
        return box(angler).clip(eye, end).isPresent();
    }

    public static boolean ambush(CorpseAnglerEntity angler, Player player) {
        return hunter(player) && player.isInWater() && player.distanceToSqr(center(angler)) <= AMBUSH * AMBUSH;
    }

    public static boolean hunter(Player player) {
        return player.isAlive() && !player.isSpectator() && !player.isCreative() && !player.getAbilities().instabuild;
    }
}
