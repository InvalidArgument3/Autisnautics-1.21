package dev.wyseman.dimstackguard;

import java.util.Set;

/**
 * Drives {@link DimStackGuard} without a mod loader. The lookup is injected, so
 * the decision can be checked against the namespaces this pack's dimension
 * stack actually contains.
 */
public final class GuardHarness {
    /** Mod ids the loader would know about. */
    private static final Set<String> MODS = Set.of(
        "minecraft", "neoforge", "ip_aeronautics", "immersive_portals",
        "deeperdarker", "undergarden", "alexscaves", "create"
    );

    /** Namespaces in the dimension stack that come from datapacks, not mods. */
    private static final Set<String> DATAPACK_NAMESPACES = Set.of(
        "oceanworld", "dimension_of_caves", "infinite_abyss", "infernalcross", "autisnautics"
    );

    public static void main(String[] args) {
        realModsAreNotMissing();
        datapackNamespacesAreMissing();
        nullAndEmptyAreMissing();
        throwingLookupFailsClosed();
        everyStackNamespaceDecided();
        System.out.println("guard ok");
    }

    private static void realModsAreNotMissing() {
        for (String mod : MODS) {
            expect(!DimStackGuard.isMissingMod(mod, MODS::contains),
                "real mod " + mod + " is not missing");
        }
    }

    /**
     * These are the namespaces that made /portal dimension_stack throw. Each one
     * must be reported missing so getModName is never entered.
     */
    private static void datapackNamespacesAreMissing() {
        for (String ns : DATAPACK_NAMESPACES) {
            expect(DimStackGuard.isMissingMod(ns, MODS::contains),
                "datapack namespace " + ns + " is missing");
        }
        System.out.println("  " + DATAPACK_NAMESPACES.size() + " datapack namespaces guarded");
    }

    private static void nullAndEmptyAreMissing() {
        expect(DimStackGuard.isMissingMod(null, MODS::contains), "null is missing");
        expect(DimStackGuard.isMissingMod("", MODS::contains), "empty is missing");
    }

    /** An unexpected loader failure must not take the screen down. */
    private static void throwingLookupFailsClosed() {
        expect(DimStackGuard.isMissingMod("minecraft", id -> {
            throw new IllegalStateException("loader exploded");
        }), "a throwing lookup reports missing");
        expect(DimStackGuard.isMissingMod("minecraft", id -> {
            throw new NullPointerException("the real bug");
        }), "the real NPE reports missing");
    }

    /** Every namespace in the live stack gets a decision, and the split is right. */
    private static void everyStackNamespaceDecided() {
        String[] stack = {
            "minecraft", "oceanworld", "dimension_of_caves", "infinite_abyss",
            "deeperdarker", "undergarden", "infernalcross", "autisnautics"
        };
        int guarded = 0;
        for (String ns : stack) {
            boolean missing = DimStackGuard.isMissingMod(ns, MODS::contains);
            if (missing) {
                guarded++;
            }
            System.out.println("    " + (missing ? "guarded " : "passes  ") + ns);
        }
        expect(guarded == 5, "five of the eight stack namespaces are datapacks, got " + guarded);
    }

    private static void expect(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
    }
}
