package dev.wyseman.moltensea;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(MoltenSea.ID)
public final class MoltenSea {
    public static final String ID = "moltensea";
    public static final Logger LOG = LogUtils.getLogger();

    public MoltenSea(ModContainer container, Dist dist) {
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        NeoForge.EVENT_BUS.addListener(MoltenSeaEvents::onTags);
        NeoForge.EVENT_BUS.addListener(MoltenSeaEvents::onCreateSource);
        if (dist.isClient()) {
            NeoForge.EVENT_BUS.addListener(MoltenSeaClient::onFog);
        }
        LOG.info("Molten Sea loaded");
    }
}
