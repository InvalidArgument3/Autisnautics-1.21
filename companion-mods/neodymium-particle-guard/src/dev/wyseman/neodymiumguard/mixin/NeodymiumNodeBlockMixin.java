package dev.wyseman.neodymiumguard.mixin;

import dev.wyseman.neodymiumguard.NeodymiumParticleGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Throttles the magnetic orbit particle that Alex's Caves spawns from every
 * neodymium node.
 *
 * <p>{@code NeodymiumNodeBlock.animateTick} does nothing except spawn one
 * particle, and it spawns it unconditionally: the guard is
 * {@code random.nextInt(1) == 0}, which is always true. Cancelling at HEAD
 * therefore skips exactly the particle and the {@code Vec3.atCenterOf} that
 * feeds it, and leaves every other behaviour of the block alone.
 *
 * <p>The target is named rather than imported so the build does not need Alex's
 * Caves on the compile classpath, and {@code remap = false} is correct because
 * the target is a mod class compiled against Mojmap, not a Minecraft class.
 */
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.block.NeodymiumNodeBlock", remap = false)
public class NeodymiumNodeBlockMixin {
    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true, remap = false)
    private void neodymium$throttleOrbitParticle(
        BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci
    ) {
        if (!NeodymiumParticleGuard.shouldSpawn(level, pos)) {
            ci.cancel();
        }
    }
}
