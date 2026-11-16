package me.cortex.voxy.client;

import me.cortex.voxy.common.network.SectionSyncBatchPayload;
import me.cortex.voxy.common.network.SectionWire;
import me.cortex.voxy.common.network.TerrainWireLimits;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Network-thread entry for section batches. A bad batch is logged and dropped
 * here, before {@code enqueueWork} hops to the render thread.
 */
public final class ClientIngress {
    private ClientIngress() {
    }

    public static void handle(SectionSyncBatchPayload payload, IPayloadContext context) {
        try {
            SectionSyncBatchPayload ready = SectionWire.expand(payload);
            int count = ready == null || ready.sectionDatas() == null ? 0 : ready.sectionDatas().size();
            if (count < 1 || count > TerrainWireLimits.MAX_BATCH_SECTIONS) {
                ClientSectionApply.logOnce("rejecting terrain batch with " + count + " sections");
                ClientSectionApply.ingest(emptySafe(ready, payload));
                return;
            }
            SectionSyncBatchPayload safe = ready;
            context.enqueueWork(() -> ClientSectionApply.ingest(safe));
        } catch (Throwable failure) {
            ClientSectionApply.logOnce("terrain batch failed before apply: " + failure);
        }
    }

    /** Keep a non-null payload so a reject can still nack by transfer id. */
    private static SectionSyncBatchPayload emptySafe(SectionSyncBatchPayload ready, SectionSyncBatchPayload original) {
        if (ready != null) {
            return ready;
        }
        return original;
    }
}
