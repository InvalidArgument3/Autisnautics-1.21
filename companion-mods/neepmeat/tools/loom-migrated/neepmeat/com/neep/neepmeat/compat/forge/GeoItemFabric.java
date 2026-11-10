package com.neep.neepmeat.compat.forge;

import com.google.common.base.Suppliers;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class GeoItemFabric {
	/**
	 * Safety wrapper to distance the client-side code from common code.<br>
	 * This should be cached in your {@link net.minecraft.world.item.Item Item} class
	 */
	public static Supplier<Object> makeRenderer(GeoItem item) {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
			return () -> null;

		return Suppliers.memoize(() -> {
			AtomicReference<Object> renderProvider = new AtomicReference<>();
			item.createRenderer(renderProvider::set);
			return renderProvider.get();
		});
	}
}
