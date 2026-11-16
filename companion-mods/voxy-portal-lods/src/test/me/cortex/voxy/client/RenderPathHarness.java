package me.cortex.voxy.client;

import me.cortex.voxy.client.core.rendering.RenderDistanceTracker;
import me.cortex.voxy.client.core.rendering.SectionUpdateRouter;
import me.cortex.voxy.common.world.WorldEngine;

/**
 * The real render-distance tracker and the real section router. A section is
 * meshed only after the tracker watches it. renderNotifies does not do that.
 */
public final class RenderPathHarness {
    public static void main(String[] args) {
        int[] requested = new int[] {0};
        int[] farthest = new int[] {0};
        SectionUpdateRouter router = new SectionUpdateRouter();
        router.setCallbacks(position -> {
            requested[0]++;
            int scale = 2 << WorldEngine.getLevel(position);
            int chunks = Math.max(Math.abs(WorldEngine.getX(position)), Math.abs(WorldEngine.getZ(position))) * scale;
            if (chunks > farthest[0]) {
                farthest[0] = chunks;
            }
        }, position -> {
        }, section -> {
        });
        RenderDistanceTracker tracker = new RenderDistanceTracker(100000, -1, 1, position -> router.watch(position, 1), position -> {
            if (router.get(position) != 0) {
                router.unwatch(position, 1);
            }
        });
        tracker.setRenderDistance(17);
        int steps = 0;
        while (tracker.setCenterAndProcess(80.0 * 16.0, -30.0 * 16.0) && steps < 100000) {
            steps++;
        }
        long near = WorldEngine.getWorldSectionId(4, 0, 0, 0);
        long far = WorldEngine.getWorldSectionId(4, 40, 0, 0);
        boolean nearWatched = router.get(near) != 0;
        boolean farWatched = router.get(far) != 0;
        System.out.println("render path requested=" + requested[0] + " farthestChunks=" + farthest[0]
                + " steps=" + steps + " nearWatched=" + nearWatched + " farOutside=" + farWatched);
        if (requested[0] < 100) {
            throw new AssertionError("tracker did not request meshes");
        }
        if (farthest[0] < 17 * 32) {
            throw new AssertionError("farthest watched ring is " + farthest[0] + " chunks");
        }
        if (farWatched) {
            throw new AssertionError("a level-4 node outside the render ring was watched");
        }
        if (router.get(WorldEngine.getWorldSectionId(0, 400, 0, 0)) != 0) {
            throw new AssertionError("an unwatched section was treated as meshed");
        }
        System.out.println("RenderPathHarness passed");
    }
}
