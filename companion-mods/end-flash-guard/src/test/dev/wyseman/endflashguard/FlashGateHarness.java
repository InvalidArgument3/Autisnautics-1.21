package dev.wyseman.endflashguard;

/**
 * A background End {@code ClientLevel} is not the one the player is standing in.
 * Immersive Portals sets {@code Minecraft.level} to that background level for the
 * remote tick, so identity with {@code Minecraft.level} would still play the sound.
 * This drives {@link EndFlashPolicy} through that frame and through a real End frame.
 */
public final class FlashGateHarness {
    public static void main(String[] args) {
        Level overworld = new Level("overworld", false);
        Level end = new Level("end", true);
        Level secondaryEnd = new Level("end-secondary", true);
        EndFlashPolicy policy = new EndFlashPolicy();

        // Player tick in the overworld. No end flash state, so nothing plays.
        tick(policy, overworld, overworld, false, true);
        expect(!overworld.endSky, "overworld has no end sky");
        expect(policy.playerLevel() == overworld, "player level is the overworld");

        // Hidden End tick. Minecraft.level has been swapped onto it and the flash would start.
        tick(policy, end, end, true, true);
        expect(policy.playerLevel() == overworld, "switched tick keeps the overworld");
        expect(end.intensity == 0f, "hidden end state does not advance");
        expect(!end.sound, "hidden end plays no weather.end_flash");
        expect(!visualSky(policy, end), "hidden end draws no sky flash");
        expect(visualLight(policy, end) == 0f, "hidden end changes no light");
        expect(!visualSky(policy, overworld), "overworld draws no sky flash");
        expect(visualLight(policy, overworld) == 0f, "overworld changes no light");

        // The same hidden tick with only an identity check would play the sound.
        boolean swappedIdentity = end == end;
        expect(swappedIdentity, "Minecraft.level identity matches the swapped end");
        expect(!policy.allowsTick(end, true), "policy rejects the swapped identity");

        // A non-current level ticked without the switch flag is still not the player level.
        tick(policy, overworld, secondaryEnd, false, true);
        expect(secondaryEnd.intensity == 0f, "non-current level does not advance");
        expect(!secondaryEnd.sound, "non-current level plays no sound");
        expect(!visualSky(policy, secondaryEnd), "non-current level draws no sky");
        expect(visualLight(policy, secondaryEnd) == 0f, "non-current level changes no light");

        // The player is in the End. The unswitched tick is the real level.
        tick(policy, end, end, false, true);
        expect(policy.allowsTick(end, false), "real end tick is allowed");
        expect(end.intensity == 1f, "real end state advances");
        expect(end.sound, "real end plays weather.end_flash");
        expect(visualSky(policy, end), "real end draws the sky flash");
        expect(visualLight(policy, end) == 1f, "real end changes the light");

        // A second End client level, ticked while the player is already in the End.
        tick(policy, end, secondaryEnd, false, true);
        expect(!secondaryEnd.sound, "second end level stays quiet");
        expect(visualLight(policy, end) == 1f, "real end light stays");
        expect(!visualSky(policy, secondaryEnd), "second end level draws nothing");

        // Portal render points Minecraft.level at the secondary End. The player is still in the real End.
        expect(!visualSky(policy, secondaryEnd), "portal view of the secondary end draws no sky");
        expect(visualLight(policy, secondaryEnd) == 0f, "portal view of the secondary end changes no light");
        expect(visualSky(policy, end), "the player's end still draws");

        System.out.println("flash gate ok");
    }

    private static void tick(EndFlashPolicy policy, Level minecraftLevel, Level ticking, boolean worldSwitched, boolean flashStarted) {
        policy.observe(minecraftLevel, worldSwitched);
        if (!ticking.endSky || !policy.allowsTick(ticking, worldSwitched)) {
            return;
        }
        float previous = ticking.intensity;
        ticking.intensity = flashStarted ? 1f : 0f;
        ticking.sound = ticking.intensity > 0f && previous <= 0f;
    }

    private static boolean visualSky(EndFlashPolicy policy, Level minecraftLevel) {
        return policy.allowsVisual(minecraftLevel) && minecraftLevel.endSky && minecraftLevel.intensity > 0f;
    }

    private static float visualLight(EndFlashPolicy policy, Level minecraftLevel) {
        if (!policy.allowsVisual(minecraftLevel) || !minecraftLevel.endSky) {
            return 0f;
        }
        return minecraftLevel.intensity;
    }

    private static void expect(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
        System.out.println("  ok " + label);
    }

    private static final class Level {
        final String name;
        final boolean endSky;
        float intensity;
        boolean sound;

        Level(String name, boolean endSky) {
            this.name = name;
            this.endSky = endSky;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
