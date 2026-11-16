package me.cortex.voxy.server;

import java.util.ArrayList;
import java.util.List;

/**
 * Drives {@link RingScan} the way the dedicated-server walker will: nearest ring first,
 * a rejected send stays on that cell, and a noted hole does not block later columns.
 */
public final class RingHarness {
    public static void main(String[] args) throws Exception {
        ringsMatchWalkCursor();
        rejectedOfferIsRetried();
        notedHoleDoesNotStall();
        capSkipsOnce();
        backpressureHolds();
        pendingFrameGoesToOwnerFront();
        availabilityRoundTrip();
        lodsStartInsideTheVanillaCircle();
        oneChunkMoveKeepsTheOuterRing();
        heldColumnsAreSkippedBeforeTheNewOne();
        skipLoopStopsAtTheDeadline();
        sentMemoryForgetsAChangedChunk();
        clientHeldColumnIsNotVoxelized();
        coverageArrivingMidRingIsRespected();
        olderPeerWithoutCoverageStillSends();
        staleColumnStaysSkippedAfterAnotherReport();
        storedSectionBecomesChunkColumns();
        standStillReachesPastTheLoadedRings();
        oneTickSendsABurst();
        renderSwapDoesNotDropThePlayersWorld();
        coverageKeyMatchesTheWalker();
        System.out.println("RingHarness passed");
    }

    private static void ringsMatchWalkCursor() throws Exception {
        Class<?> type = Class.forName("me.cortex.voxy.server.VoxyServerGeneratedChunkWalker$WalkCursor");
        java.lang.reflect.Constructor<?> ctor = type.getDeclaredConstructor(int.class, int.class, int.class);
        ctor.setAccessible(true);
        Object legacy = ctor.newInstance(Integer.valueOf(100), Integer.valueOf(200), Integer.valueOf(3));
        java.lang.reflect.Method next = type.getDeclaredMethod("next", int.class, int.class);
        next.setAccessible(true);
        RingCursor cursor = new RingCursor(100, 200, 3);
        for (int radius = 3; radius <= 5; radius++) {
            int count = radius * 8;
            for (int i = 0; i < count; i++) {
                Object point = next.invoke(legacy, Integer.valueOf(3), Integer.valueOf(6));
                int lx = (Integer) point.getClass().getMethod("x").invoke(point);
                int lz = (Integer) point.getClass().getMethod("z").invoke(point);
                int[] cell = cursor.peek(3, 6);
                check(cell[0] == lx && cell[1] == lz, "ring cell " + radius + ":" + i + " was " + cell[0] + "," + cell[1] + " legacy " + lx + "," + lz);
                check(Math.max(Math.abs(cell[0] - 100), Math.abs(cell[1] - 200)) == radius, "distance");
                cursor.commit(3, 6);
            }
        }
        cursor.retargetIfMoved(400, -50, 3);
        int[] moved = cursor.peek(3, 8);
        check(Math.max(Math.abs(moved[0] - 400), Math.abs(moved[1] + 50)) == 3, "retarget returns to the inner ring");
        System.out.println("ok square ring matches WalkCursor");
    }

    private static void rejectedOfferIsRetried() {
        RingCursor cursor = new RingCursor(0, 0, 1);
        List<int[]> sent = new ArrayList<int[]>();
        boolean[] rejectOnce = new boolean[] {true};
        int[] result = RingScan.scan(cursor, 0, 0, 0, 4, new RingScan.Probe() {
            @Override
            public boolean known(int x, int z) {
                return false;
            }

            @Override
            public boolean noted(int x, int z) {
                return true;
            }

            @Override
            public boolean onDisk(int x, int z) {
                return true;
            }

            @Override
            public int attempt(int x, int z) {
                if (rejectOnce[0] && x == -1 && z == -1) {
                    rejectOnce[0] = false;
                    return RingScan.FAIL;
                }
                sent.add(new int[] {x, z});
                return RingScan.SENT;
            }
        }, 8, null, null);
        check(result == null, "a rejected first cell sends nothing");
        result = RingScan.scan(cursor, 0, 0, 0, 4, new RingScan.Probe() {
            @Override
            public boolean known(int x, int z) {
                return false;
            }

            @Override
            public boolean noted(int x, int z) {
                return true;
            }

            @Override
            public boolean onDisk(int x, int z) {
                return true;
            }

            @Override
            public int attempt(int x, int z) {
                sent.add(new int[] {x, z});
                return RingScan.SENT;
            }
        }, 8, null, null);
        check(result != null && result[0] == -1 && result[1] == -1, "retry sends the same nearest cell");
        check(sent.size() == 1 && sent.get(0)[0] == -1 && sent.get(0)[1] == -1, "no farther cell jumped the queue");
        System.out.println("ok rejected offer retries the same cell");
    }

    private static void notedHoleDoesNotStall() {
        RingCursor cursor = new RingCursor(0, 0, 11);
        int[] hole = new int[] {-11, -11};
        int[] sent = RingScan.scan(cursor, 0, 0, 10, 40, new RingScan.Probe() {
            @Override
            public boolean known(int x, int z) {
                return false;
            }

            @Override
            public boolean noted(int x, int z) {
                return x == hole[0] && z == hole[1];
            }

            @Override
            public boolean onDisk(int x, int z) {
                return !(x == hole[0] && z == hole[1]);
            }

            @Override
            public int attempt(int x, int z) {
                if (x == hole[0] && z == hole[1]) {
                    return RingScan.SKIP;
                }
                return RingScan.SENT;
            }
        }, 8, null, null);
        check(sent != null, "a saved column past the hole is sent");
        check(!(sent[0] == hole[0] && sent[1] == hole[1]), "the unreadable cell is not the send");
        int distance = Math.max(Math.abs(sent[0]), Math.abs(sent[1]));
        check(distance >= 11 && distance <= 12, "the send is the next saved column, not a restart");
        System.out.println("ok a noted hole does not stall the ring");
    }

    private static void capSkipsOnce() {
        RingCursor cursor = new RingCursor(0, 0, 1);
        List<int[]> abandoned = new ArrayList<int[]>();
        List<int[]> sent = new ArrayList<int[]>();
        RingScan.Probe probe = new RingScan.Probe() {
            @Override
            public boolean known(int x, int z) {
                return false;
            }

            @Override
            public boolean noted(int x, int z) {
                return true;
            }

            @Override
            public boolean onDisk(int x, int z) {
                return true;
            }

            @Override
            public int attempt(int x, int z) {
                if (x == -1 && z == -1) {
                    return RingScan.FAIL;
                }
                sent.add(new int[] {x, z});
                return RingScan.SENT;
            }
        };
        RingScan.Abandoned log = (x, z) -> abandoned.add(new int[] {x, z});
        int[] first = RingScan.scan(cursor, 0, 0, 0, 2, probe, 3, null, log);
        check(first == null && abandoned.isEmpty(), "still retrying inside the cap");
        int[] second = RingScan.scan(cursor, 0, 0, 0, 2, probe, 3, null, log);
        check(second == null && abandoned.isEmpty(), "second failure still holds");
        int[] third = RingScan.scan(cursor, 0, 0, 0, 2, probe, 3, null, log);
        check(third != null && !(third[0] == -1 && third[1] == -1), "cap skips the bad cell and sends the next");
        check(abandoned.size() == 1 && abandoned.get(0)[0] == -1 && abandoned.get(0)[1] == -1, "skip is logged once");
        System.out.println("ok retry cap skips once");
    }

    private static void backpressureHolds() {
        RingCursor cursor = new RingCursor(0, 0, 1);
        int[] held = RingScan.scan(cursor, 0, 0, 0, 2, new RingScan.Probe() {
            @Override
            public boolean known(int x, int z) {
                return false;
            }

            @Override
            public boolean noted(int x, int z) {
                return true;
            }

            @Override
            public boolean onDisk(int x, int z) {
                return true;
            }

            @Override
            public int attempt(int x, int z) {
                return RingScan.BACKPRESSURE;
            }
        }, 2, null, (x, z) -> {
            throw new AssertionError("backpressure must not count as a give-up");
        });
        check(held == null, "full queue sends nothing");
        int[] cell = cursor.peek(1, 2);
        check(cell[0] == -1 && cell[1] == -1, "full queue keeps the nearest cell");
        System.out.println("ok backpressure holds the cursor");
    }

    private static void pendingFrameGoesToOwnerFront() {
        java.util.LinkedHashMap<Long, Object> pending = new java.util.LinkedHashMap<Long, Object>();
        FakeFrame older = new FakeFrame(1L, "alice");
        FakeFrame newer = new FakeFrame(2L, "alice");
        FakeFrame bob = new FakeFrame(3L, "bob");
        pending.put(Long.valueOf(1L), older);
        pending.put(Long.valueOf(2L), newer);
        pending.put(Long.valueOf(3L), bob);
        FakeFrame retry = new FakeFrame(9L, "alice");
        PendingOrder.requeueFront(pending, retry);
        Long[] order = pending.keySet().toArray(new Long[0]);
        check(order.length == 4, "four frames");
        check(order[0].longValue() == 9L && order[1].longValue() == 1L && order[2].longValue() == 2L && order[3].longValue() == 3L, "alice retry is in front of alice and bob stays put");
        System.out.println("ok retried frame is at the front of that player");
    }

    private static void availabilityRoundTrip() {
        TerrainAvailability.clearAll();
        check(TerrainAvailability.levelId("minecraft:overworld|minecraft:overworld|5").equals("minecraft:overworld"), "level id");
        TerrainAvailability.note("minecraft:overworld", 4, -2);
        check(TerrainAvailability.contains("minecraft:overworld", 4, -2), "noted");
        check(!TerrainAvailability.contains("minecraft:the_nether", 4, -2), "other dimension");
        TerrainAvailability.clear("minecraft:overworld", 4, -2);
        check(!TerrainAvailability.contains("minecraft:overworld", 4, -2), "cleared");
        System.out.println("ok availability set");
    }

    private static void lodsStartInsideTheVanillaCircle() {
        check(TerrainStreamPolicy.lodStartChebyshev(10) == 10, "view 10 starts on ring 10");
        check(TerrainStreamPolicy.lodStartChebyshev(2) == 2, "view 2 starts on ring 2");
        check(TerrainStreamPolicy.lodStartChebyshev(0) == 1, "unknown view still starts at 1");
        check(TerrainStreamPolicy.lodExclusiveInner(10) == 9, "rings through 9 stay inside the square");
        RingCursor cursor = new RingCursor(0, 0, 1);
        int[] sent = RingScan.scan(cursor, 0, 0, 9, 20, new RingScan.Probe() {
            @Override
            public boolean known(int x, int z) {
                return false;
            }

            @Override
            public boolean noted(int x, int z) {
                return false;
            }

            @Override
            public boolean onDisk(int x, int z) {
                return true;
            }

            @Override
            public int attempt(int x, int z) {
                return RingScan.SENT;
            }
        }, 4, null, null);
        check(sent != null, "a column in the overlap ring is sent");
        check(Math.max(Math.abs(sent[0]), Math.abs(sent[1])) == 10, "first send is the outer vanilla ring, not the one past it");
        System.out.println("ok LOD ring overlaps the outer vanilla square");
    }

    private static void oneChunkMoveKeepsTheOuterRing() {
        RingCursor cursor = new RingCursor(0, 0, 9);
        for (int i = 0; i < 9 * 8; i++) {
            cursor.peek(9, 40);
            cursor.commit(9, 40);
        }
        check(cursor.radius() == 10, "the ring advanced");
        cursor.retargetIfMoved(1, 0, 9);
        check(cursor.radius() == 10, "one chunk of movement keeps the outward ring");
        cursor.retargetIfMoved(30, 0, 9);
        check(cursor.radius() == 9, "a long jump starts again at the inner ring");
        System.out.println("ok a short move does not restart the ring");
    }

    private static void heldColumnsAreSkippedBeforeTheNewOne() {
        RingCursor cursor = new RingCursor(0, 0, 1);
        int[] attempts = new int[1];
        int[] heldSkips = new int[1];
        int[] sent = RingScan.scan(cursor, 0, 0, 0, 25, new RingScan.Probe() {
            @Override
            public boolean alreadyHeld(int x, int z) {
                int distance = Math.max(Math.abs(x), Math.abs(z));
                if (distance < 20) {
                    heldSkips[0]++;
                    return true;
                }
                return false;
            }

            @Override
            public boolean known(int x, int z) {
                return false;
            }

            @Override
            public boolean noted(int x, int z) {
                return false;
            }

            @Override
            public boolean onDisk(int x, int z) {
                return true;
            }

            @Override
            public int attempt(int x, int z) {
                attempts[0]++;
                return RingScan.SENT;
            }
        }, 4, null, null);
        check(heldSkips[0] > 100, "the already-held area is crossed in this pass");
        check(attempts[0] == 1, "only the new column is voxelized");
        check(sent != null && Math.max(Math.abs(sent[0]), Math.abs(sent[1])) == 20, "the new column is sent");
        System.out.println("ok held columns are skipped before the new one");
    }

    private static void skipLoopStopsAtTheDeadline() {
        RingCursor cursor = new RingCursor(0, 0, 1);
        int[] checks = new int[1];
        int[] attempts = new int[1];
        int[] sent = RingScan.scan(cursor, 0, 0, 0, 400, new RingScan.Probe() {
            @Override
            public boolean alreadyHeld(int x, int z) {
                return true;
            }

            @Override
            public boolean known(int x, int z) {
                return false;
            }

            @Override
            public boolean noted(int x, int z) {
                return false;
            }

            @Override
            public boolean onDisk(int x, int z) {
                return true;
            }

            @Override
            public int attempt(int x, int z) {
                attempts[0]++;
                return RingScan.SENT;
            }
        }, 4, () -> ++checks[0] > 50, null);
        check(sent == null, "the deadline ends the pass with nothing sent");
        check(attempts[0] == 0, "held skips do not start a send");
        check(checks[0] > 50 && checks[0] < 80, "the loop stops at the time cap");
        System.out.println("ok skip loop stops at the deadline");
    }

    private static void sentMemoryForgetsAChangedChunk() {
        TerrainSentMemory.clearAll();
        TerrainAvailability.clearAll();
        TerrainSentMemory.mark("player", "minecraft:overworld#7", 12, -3);
        check(TerrainSentMemory.holds("player", "minecraft:overworld#7", 12, -3), "marked");
        check(!TerrainSentMemory.holds("player", "minecraft:overworld#8", 12, -3), "other world");
        TerrainAvailability.note("minecraft:overworld", 12, -3);
        check(!TerrainSentMemory.holds("player", "minecraft:overworld#7", 12, -3), "a chunk load drops the sent mark");
        TerrainSentMemory.mark("player", "minecraft:overworld", 1, 1);
        TerrainSentMemory.clearPlayer("player");
        check(TerrainSentMemory.size("player", "minecraft:overworld") == 0, "leaving clears the player");
        System.out.println("ok sent memory invalidates on chunk change");
    }

    private static void clientHeldColumnIsNotVoxelized() {
        TerrainSentMemory.clearAll();
        TerrainAvailability.clearAll();
        coverChunk("player", "world", -1, -1);
        int[] attempts = new int[1];
        int[] sent = RingScan.scan(new RingCursor(0, 0, 1), 0, 0, 0, 4, probe(attempts, "player", "world"), 4, null, null);
        check(attempts[0] == 1, "the client-held column is not voxelized");
        check(sent != null && !(sent[0] == -1 && sent[1] == -1), "the next column is still sent");
        System.out.println("ok a client-held column the server never sent is skipped");
    }

    private static void coverageArrivingMidRingIsRespected() {
        TerrainSentMemory.clearAll();
        int[] steps = new int[1];
        int[] attempts = new int[1];
        int[] sent = RingScan.scan(new RingCursor(0, 0, 1), 0, 0, 0, 4, new RingScan.Probe() {
            @Override
            public boolean alreadyHeld(int x, int z) {
                steps[0]++;
                if (steps[0] == 1) {
                    coverChunk("player", "world", 1, -1);
                }
                return TerrainSentMemory.holds("player", "world", x, z);
            }

            @Override
            public boolean known(int x, int z) {
                return false;
            }

            @Override
            public boolean noted(int x, int z) {
                return false;
            }

            @Override
            public boolean onDisk(int x, int z) {
                return x == 1;
            }

            @Override
            public int attempt(int x, int z) {
                attempts[0]++;
                if (x == 1 && z == -1) {
                    throw new AssertionError("coverage that arrived mid-ring was voxelized");
                }
                return RingScan.SENT;
            }
        }, 4, null, null);
        check(sent != null && !(sent[0] == 1 && sent[1] == -1), "the newly reported column is not the send");
        check(attempts[0] == 1, "only the column after the reported one is voxelized");
        System.out.println("ok coverage arriving mid-ring is respected");
    }

    private static void olderPeerWithoutCoverageStillSends() {
        TerrainSentMemory.clearAll();
        int[] attempts = new int[1];
        int[] sent = RingScan.scan(new RingCursor(0, 0, 1), 0, 0, 0, 2, probe(attempts, "player", "world"), 4, null, null);
        check(sent != null && sent[0] == -1 && sent[1] == -1, "no coverage report still sends the nearest column");
        check(attempts[0] == 1, "the older peer's column is voxelized");
        System.out.println("ok an older peer without coverage still streams");
    }

    private static void staleColumnStaysSkippedAfterAnotherReport() {
        TerrainSentMemory.clearAll();
        TerrainAvailability.clearAll();
        coverChunk("player", "minecraft:overworld#5", 4, 0);
        check(TerrainSentMemory.holdKind("player", "minecraft:overworld#5", 4, 0) == TerrainSentMemory.CLIENT, "reported");
        TerrainAvailability.note("minecraft:overworld", 4, 0);
        check(!TerrainSentMemory.holds("player", "minecraft:overworld#5", 4, 0), "a reload drops the client bit");
        coverChunk("player", "minecraft:overworld#5", 4, 0);
        check(!TerrainSentMemory.holds("player", "minecraft:overworld#5", 4, 0), "a later report cannot revive a changed column");
        System.out.println("ok a changed column stays stale");
    }

    private static void storedSectionBecomesChunkColumns() {
        it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<byte[]> regions = new it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<byte[]>();
        long section = me.cortex.voxy.common.world.WorldEngine.getWorldSectionId(0, 3, 2, -4);
        long coarse = me.cortex.voxy.common.world.WorldEngine.getWorldSectionId(1, 3, 2, -4);
        ColumnCoverageIndex.ingest(regions, coarse, 0, 0, 1000);
        check(regions.isEmpty(), "a coarse section is not a column the server would skip");
        ColumnCoverageIndex.ingest(regions, section, 0, 0, 1000);
        check(me.cortex.voxy.common.world.WorldEngine.getX(section) == 3, "section x");
        check(me.cortex.voxy.common.world.WorldEngine.getZ(section) == -4, "section z");
        byte[] bits = regions.get(ColumnCoverageIndex.regionKey(0, -1));
        check(ColumnCoverageIndex.test(bits, 6, -8), "chunk 6,-8");
        check(ColumnCoverageIndex.test(bits, 7, -8), "chunk 7,-8");
        check(ColumnCoverageIndex.test(bits, 6, -7), "chunk 6,-7");
        check(ColumnCoverageIndex.test(bits, 7, -7), "chunk 7,-7");
        check(ColumnCoverageIndex.count(bits) == 4, "one lod-0 section is four chunk columns");
        System.out.println("ok a stored lod-0 section covers its chunk columns");
    }

    /**
     * Player stands still. View distance 10, hello radius 512. Rings 10-12 are
     * already on the client. Farther saved columns mostly fail to read, and every
     * 20th one sends. ipa10n returned FAIL for a missed disk read and the radius
     * stayed on ring 13. The radius now keeps moving out, and a far column is sent.
     */
    private static void standStillReachesPastTheLoadedRings() {
        RingCursor cursor = new RingCursor(0, 0, 10);
        int previous = cursor.radius();
        int far = 0;
        for (int tick = 0; tick < 80; tick++) {
            int[] sent = RingScan.scan(cursor, 0, 0, 9, 512, new RingScan.Probe() {
                @Override
                public boolean known(int x, int z) {
                    return false;
                }

                @Override
                public boolean noted(int x, int z) {
                    return false;
                }

                @Override
                public boolean onDisk(int x, int z) {
                    return Math.max(Math.abs(x), Math.abs(z)) <= 200;
                }

                @Override
                public boolean alreadyHeld(int x, int z) {
                    int distance = Math.max(Math.abs(x), Math.abs(z));
                    return distance >= 10 && distance <= 12;
                }

                @Override
                public int attempt(int x, int z) {
                    int distance = Math.max(Math.abs(x), Math.abs(z));
                    if (x == -distance && z == -distance && distance % 20 == 0) {
                        return RingScan.SENT;
                    }
                    // Missed disk read. ipa10n returned FAIL and parked on ring 13.
                    return RingScan.SKIP;
                }
            }, 32, () -> false, null);
            check(cursor.radius() >= previous, "radius went backwards while standing still");
            previous = cursor.radius();
            if (sent != null && Math.max(Math.abs(sent[0]), Math.abs(sent[1])) >= 40) {
                far++;
            }
        }
        check(cursor.radius() >= 40, "standing still never left the loaded rings, radius " + cursor.radius());
        check(far > 0, "no column past the loaded rings was sent");
        System.out.println("ok standing still walks out to radius " + cursor.radius());
    }

    private static void oneTickSendsABurst() {
        check(TerrainDispatch.columnsThisTick(true) == 1, "a busy server sends one column");
        int budget = TerrainDispatch.columnsThisTick(false);
        check(budget > 1, "an idle server is not capped at one column per tick");
        RingCursor cursor = new RingCursor(0, 0, 10);
        int ringCells = 8 * 10;
        int sent = TerrainDispatch.burst(cursor, new RingScan.Probe() {
            @Override
            public boolean known(int x, int z) {
                return false;
            }

            @Override
            public boolean noted(int x, int z) {
                return false;
            }

            @Override
            public boolean onDisk(int x, int z) {
                return true;
            }

            @Override
            public int attempt(int x, int z) {
                return RingScan.SENT;
            }
        }, 0, 0, 9, 512, Math.max(budget, ringCells));
        check(sent == ringCells, "one pass sends the whole ring, sent " + sent);
        check(cursor.radius() > 10, "the burst walked past the first ring, radius " + cursor.radius());
    }

    private static void renderSwapDoesNotDropThePlayersWorld() {
        check(me.cortex.voxy.client.TerrainReceive.disposition(true, false) == me.cortex.voxy.client.TerrainReceive.APPLY, "the player's world is applied while the render level is swapped");
        check(me.cortex.voxy.client.TerrainReceive.disposition(false, false) == me.cortex.voxy.client.TerrainReceive.DROP, "a world the player is not in is dropped");
    }

    private static void coverageKeyMatchesTheWalker() {
        int before = TerrainSentMemory.coveragePackets();
        String world = "minecraft:overworld#-1065291115130035377";
        coverChunk("walker", world, 4, -2);
        check(TerrainSentMemory.holdKind("walker", world, 4, -2) == TerrainSentMemory.CLIENT, "the walker key sees the report");
        check(TerrainSentMemory.coveragePackets() == before + 1, "the server counts the coverage packet");
        check(world.equals(TerrainSentMemory.lastCoverageWorld()), "the counted world is the walker key");
    }

    private static void coverChunk(String player, String world, int chunkX, int chunkZ) {
        it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<byte[]> regions = new it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<byte[]>();
        ColumnCoverageIndex.markChunk(regions, chunkX, chunkZ);
        long key = ColumnCoverageIndex.regionKey(chunkX >> 5, chunkZ >> 5);
        TerrainSentMemory.cover(player, world, ColumnCoverageIndex.regionX(key), ColumnCoverageIndex.regionZ(key), regions.get(key));
    }

    private static RingScan.Probe probe(int[] attempts, String player, String world) {
        return new RingScan.Probe() {
            @Override
            public boolean alreadyHeld(int x, int z) {
                return TerrainSentMemory.holds(player, world, x, z);
            }

            @Override
            public boolean known(int x, int z) {
                return false;
            }

            @Override
            public boolean noted(int x, int z) {
                return false;
            }

            @Override
            public boolean onDisk(int x, int z) {
                return true;
            }

            @Override
            public int attempt(int x, int z) {
                attempts[0]++;
                return RingScan.SENT;
            }
        };
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static final class FakeFrame {
        public final long id;
        public final FakeKey key;

        FakeFrame(long id, String owner) {
            this.id = id;
            this.key = new FakeKey(owner);
        }
    }

    public static final class FakeKey {
        private final String owner;

        FakeKey(String owner) {
            this.owner = owner;
        }

        public String owner() {
            return this.owner;
        }
    }

    private RingHarness() {
    }
}
