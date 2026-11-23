package com.neep.meatlib.mixin.resource_reload;

import com.neep.meatlib.api.event.DataPackEvents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadableServerResources.class)
public class DataPackContentsMixin
{
    @Inject(method = "refresh", at = @At("TAIL"))
    private void onRefresh(RegistryAccess dynamicRegistryManager, CallbackInfo ci)
    {
        DataPackEvents.IMMEDIATELY_AFTER_RELOAD.invoker().apply((ReloadableServerResources) (Object) this, dynamicRegistryManager);
    }
}
