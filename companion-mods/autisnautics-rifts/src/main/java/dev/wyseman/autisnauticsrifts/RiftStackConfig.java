package dev.wyseman.autisnauticsrifts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RiftStackConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("AutisnauticsRifts");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<FaceEntry>>() {}.getType();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("autisnautics_rifts_stack.json");

    public static final class FaceEntry {
        public String dimension;
        public String face; // "floor" or "ceiling"
        public boolean enabled = true;
        public String boundary;
        public String neighborName;
        public int lo;
        public int hi;
        public int anchorY;
        public Double sentinel;
        public String fill; // "AIR", "WATER", "LAVA"
        public Integer fluidMaxY;
        public boolean sealFluids = false;

        public FaceEntry() {}

        public FaceEntry(String dimension, String face, boolean enabled, String boundary, String neighborName,
                         int lo, int hi, int anchorY, Double sentinel, String fill, Integer fluidMaxY, boolean sealFluids) {
            this.dimension = dimension;
            this.face = face;
            this.enabled = enabled;
            this.boundary = boundary;
            this.neighborName = neighborName;
            this.lo = lo;
            this.hi = hi;
            this.anchorY = anchorY;
            this.sentinel = sentinel;
            this.fill = fill;
            this.fluidMaxY = fluidMaxY;
            this.sealFluids = sealFluids;
        }
    }

    private static final Map<String, RiftBoundary.DimensionFaces> LOADED = new LinkedHashMap<>();

    private RiftStackConfig() {}

    public static void onServerStarted(ServerStartedEvent event) {
        loadAndPopulate(event.getServer());
    }

    public static synchronized void loadAndPopulate(MinecraftServer server) {
        List<FaceEntry> entries = new ArrayList<>();
        boolean dirty = false;

        if (Files.isRegularFile(CONFIG_PATH)) {
            try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                List<FaceEntry> parsed = GSON.fromJson(r, LIST_TYPE);
                if (parsed != null) entries.addAll(parsed);
            } catch (Exception e) {
                LOGGER.warn("[AutisnauticsRifts] Failed to read {}: {}", CONFIG_PATH, e.toString());
            }
        } else {
            dirty = true;
        }

        Map<String, FaceEntry> byKey = new LinkedHashMap<>();
        for (FaceEntry e : entries) {
            if (e != null && e.dimension != null && e.face != null) {
                byKey.put(e.dimension + "|" + e.face, e);
            }
        }

        for (ServerLevel level : server.getAllLevels()) {
            String dimId = level.dimension().location().toString();
            RiftBoundary.DimensionFaces detected = RiftBoundary.detectDefaultFaces(level);
            if (detected.floor() != null) {
                String k = dimId + "|floor";
                if (!byKey.containsKey(k)) {
                    byKey.put(k, toEntry(dimId, "floor", detected.floor()));
                    dirty = true;
                }
            }
            if (detected.ceiling() != null) {
                String k = dimId + "|ceiling";
                if (!byKey.containsKey(k)) {
                    byKey.put(k, toEntry(dimId, "ceiling", detected.ceiling()));
                    dirty = true;
                }
            }
        }

        if (dirty) {
            try {
                Files.createDirectories(CONFIG_PATH.getParent());
                try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
                    GSON.toJson(new ArrayList<>(byKey.values()), w);
                }
                LOGGER.info("[AutisnauticsRifts] Saved dimension stack config to {}", CONFIG_PATH);
            } catch (IOException e) {
                LOGGER.warn("[AutisnauticsRifts] Failed to write {}: {}", CONFIG_PATH, e.toString());
            }
        }

        LOADED.clear();
        Map<String, RiftBoundary.Face> floors = new LinkedHashMap<>();
        Map<String, RiftBoundary.Face> ceils = new LinkedHashMap<>();
        for (FaceEntry e : byKey.values()) {
            RiftBoundary.FillType fillType = RiftBoundary.FillType.parse(e.fill);
            double sent = e.sentinel != null ? e.sentinel : Double.NaN;
            long salt = Double.isNaN(sent) ? AdaptiveRiftMask.boundarySalt(e.dimension, e.neighborName != null ? e.neighborName : e.boundary) : 0L;
            int maxFluidY = e.fluidMaxY != null ? e.fluidMaxY : Integer.MAX_VALUE;
            RiftBoundary.Face f = new RiftBoundary.Face(
                e.boundary != null ? e.boundary : "custom",
                e.enabled,
                e.lo, e.hi, e.anchorY, sent, salt,
                e.lo, e.hi, fillType, maxFluidY, e.sealFluids,
                "ceiling".equalsIgnoreCase(e.face), false,
                e.neighborName != null ? e.neighborName : e.boundary
            );
            if ("ceiling".equalsIgnoreCase(e.face)) {
                ceils.put(e.dimension, f);
            } else {
                floors.put(e.dimension, f);
            }
        }
        for (String dim : new ArrayList<>(floors.keySet())) {
            LOADED.put(dim, new RiftBoundary.DimensionFaces(floors.get(dim), ceils.get(dim)));
        }
        for (String dim : ceils.keySet()) {
            if (!LOADED.containsKey(dim)) {
                LOADED.put(dim, new RiftBoundary.DimensionFaces(null, ceils.get(dim)));
            }
        }
    }

    public static RiftBoundary.DimensionFaces getConfigured(String dimId) {
        return LOADED.get(dimId);
    }

    private static FaceEntry toEntry(String dimId, String faceName, RiftBoundary.Face f) {
        return new FaceEntry(
            dimId,
            faceName,
            f.enabled(),
            f.boundary(),
            f.neighborName(),
            f.lo(),
            f.hi(),
            f.anchorY(),
            Double.isNaN(f.sentinel()) ? null : f.sentinel(),
            f.fillType().name(),
            f.fluidMaxY() == Integer.MAX_VALUE ? null : f.fluidMaxY(),
            f.sealFluids()
        );
    }
}
