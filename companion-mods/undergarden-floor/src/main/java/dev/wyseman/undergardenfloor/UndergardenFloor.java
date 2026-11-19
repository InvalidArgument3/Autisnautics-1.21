package dev.wyseman.undergardenfloor;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

@Mod(UndergardenFloor.ID)
public class UndergardenFloor {
    public static final String ID = "undergardenfloor";
    public static final Logger LOG = LogUtils.getLogger();

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ID);

    /**
     * Saved on the chunk. A converted chunk is never scanned again, so a
     * depthrock block a player places in the band afterwards stays.
     */
    public static final Supplier<AttachmentType<Boolean>> CONVERTED = ATTACHMENTS.register(
            "converted",
            () -> AttachmentType.builder(() -> Boolean.FALSE).serialize(Codec.BOOL).build());

    private static final ResourceKey<Level> UNDERGARDEN =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(FloorBand.DIMENSION));

    public UndergardenFloor(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(this::onChunkLoad);
    }

    private void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !UNDERGARDEN.equals(level.dimension())) {
            return;
        }
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        if (Boolean.TRUE.equals(chunk.getData(CONVERTED.get()))) {
            return;
        }

        Optional<Block> depthrock = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(FloorBand.DEPTHROCK));
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        int minY = chunk.getMinBuildHeight();
        int bedrock = 0;
        int depth = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int x0 = chunk.getPos().getMinBlockX();
        int z0 = chunk.getPos().getMinBlockZ();
        BlockState floorSample = chunk.getBlockState(pos.set(x0, minY, z0));
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < minY + FloorBand.LAYERS; y++) {
                    pos.set(x0 + x, y, z0 + z);
                    Block block = chunk.getBlockState(pos).getBlock();
                    if (block == Blocks.BEDROCK) {
                        chunk.setBlockState(pos, obsidian, false);
                        bedrock++;
                    } else if (depthrock.isPresent() && block == depthrock.get()) {
                        chunk.setBlockState(pos, obsidian, false);
                        depth++;
                    }
                }
            }
        }
        chunk.setData(CONVERTED.get(), Boolean.TRUE);
        chunk.setUnsaved(true);
        LOG.info(
                "Undergarden floor chunk {} sample at y={} was {}; replaced {} bedrock and {} depthrock",
                chunk.getPos(),
                minY,
                BuiltInRegistries.BLOCK.getKey(floorSample.getBlock()),
                bedrock,
                depth);
    }
}
