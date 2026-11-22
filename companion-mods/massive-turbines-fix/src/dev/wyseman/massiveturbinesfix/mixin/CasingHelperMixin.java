package dev.wyseman.massiveturbinesfix.mixin;

import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Create: Massive Turbines 1.0.1 registers its casing placement helpers from
 * {@code MassiveTurbinesPlacement}'s static initializer, which loads
 * {@code CasingHelper}. That class's ghost render reads
 * {@code Minecraft.player}, a {@code LocalPlayer}. NeoForge refuses to load
 * {@code LocalPlayer} on a dedicated server, so the mod never constructs.
 *
 * <p>This replaces only that render on the dedicated server. Placement, fill,
 * and the rest of the helper stay. The client does not apply this mixin, so
 * the ghost blocks stay as the mod wrote them.
 */
@Mixin(targets = "com.kompot.massiveturbines.placement.MassiveTurbinesPlacement$CasingHelper", remap = false)
public class CasingHelperMixin {
    @Overwrite(remap = false)
    public void renderAt(BlockPos pos, BlockState state, BlockHitResult hit, PlacementOffset offset) {
    }
}
