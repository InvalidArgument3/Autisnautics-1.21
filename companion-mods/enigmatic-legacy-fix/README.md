# Enigmatic Legacy setup fix

EnigmaticLegacy+ `1.21.1-1.1.2` calls `CommonProxy.init` directly from `onCommonSetup`. That method's first action is `DispenserBlock.registerBehavior` for the ichor spear. Common setup listeners run in parallel, and the dispenser map is an unsynchronized fastutil hash map, so a concurrent `put` throws `ArrayIndexOutOfBoundsException` in `rehash`.

`enigmaticlegacyfix` redirects that `init` call to `FMLCommonSetupEvent.enqueueWork`. NeoForge runs the queued work on the main thread after the parallel listeners finish. The init body is the same on the client and the dedicated server. The jar ships on both sides.
