package dev.wyseman.portallinker;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(PortalLinker.ID)
public final class PortalLinker {
    public static final String ID = "portal_linker";
    public static final Logger LOG = LogUtils.getLogger();
    public static final TagKey<Block> FRAME_BLOCKS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ID, "portal_frames"));

    public PortalLinker(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        ModItems.register(modBus);
        NeoForge.EVENT_BUS.addListener(PortalLinkerEvents::onNeighbor);
        NeoForge.EVENT_BUS.addListener(PortalLinkerEvents::onTick);
        LOG.info("Portal Linker loaded");
    }
}
