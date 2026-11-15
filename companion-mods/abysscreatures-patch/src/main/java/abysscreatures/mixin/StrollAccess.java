package abysscreatures.mixin;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RandomStrollGoal.class)
public interface StrollAccess {
    @Accessor("mob")
    PathfinderMob abyss$mob();

    @Accessor("wantedX")
    void abyss$wantedX(double value);

    @Accessor("wantedY")
    void abyss$wantedY(double value);

    @Accessor("wantedZ")
    void abyss$wantedZ(double value);
}
