// priority: 100
// Installed KubeJS 2101.7.2 / NeoForge 21.1.265. Full game restart required.
const $AutisFieldUtils = Java.loadClass('org.apache.commons.lang3.reflect.FieldUtils');
const $AutisItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity');
const $AutisBuiltInRegistries = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries');
const $AutisRegistries = Java.loadClass('net.minecraft.core.registries.Registries');
const $AutisResourceKey = Java.loadClass('net.minecraft.resources.ResourceKey');
const $AutisLocation = Java.loadClass('net.minecraft.resources.ResourceLocation');
const $AutisObjects = Java.loadClass('java.util.Objects');

let autisItemsResolved = false;
let autisLevititeItem = null;
let autisPearlescentLevititeItem = null;

function isAutisFloatingLevitite(entity) {
    if (!(entity instanceof $AutisItemEntity)) return false;
    const stack = entity.getItem();
    if (stack.isEmpty()) return false;
    if (!autisItemsResolved) {
        const air = $AutisBuiltInRegistries.ITEM.get($AutisLocation.fromNamespaceAndPath('minecraft', 'air'));
        const lev = $AutisBuiltInRegistries.ITEM.get($AutisLocation.fromNamespaceAndPath('aeronautics', 'levitite'));
        const pearl = $AutisBuiltInRegistries.ITEM.get($AutisLocation.fromNamespaceAndPath('aeronautics', 'pearlescent_levitite'));
        autisLevititeItem = $AutisObjects.equals(lev, air) ? null : lev;
        autisPearlescentLevititeItem = $AutisObjects.equals(pearl, air) ? null : pearl;
        autisItemsResolved = true;
    }
    const item = stack.getItem();
    return (autisLevititeItem !== null && $AutisObjects.equals(item, autisLevititeItem)) ||
           (autisPearlescentLevititeItem !== null && $AutisObjects.equals(item, autisPearlescentLevititeItem));
}

BlockEvents.modification(event => {
    // NeoForge 21.1.265 BlockBehaviour caches Properties.drops (BuiltInLootTables.EMPTY from noLootTable())
    // inside lootTableSupplier, so setting drops = null reverts to minecraft:empty on getLootTable().
    // Writing a non-null ResourceKey<LootTable> directly to drops bypasses lootTableSupplier cleanly.
    const levititeLootKey = $AutisResourceKey.create(
        $AutisRegistries.LOOT_TABLE,
        $AutisLocation.fromNamespaceAndPath('aeronautics', 'blocks/levitite')
    );
    event.modify('aeronautics:levitite', block => {
        block.requiresTool = true;
        block.destroySpeed = 5.0;
        $AutisFieldUtils.writeField(block.block(), 'drops', levititeLootKey, true);
        console.info('Autisnautics: Levitite native loot table = ' + block.block().getLootTable().location());
    });
});

// STARTUP native listeners run on BOTH logical sides.
// Avoid ItemStack.is(...) in Rhino: BlockItem triggers an overload ambiguity between
// ItemStack.is(Item) and KubeJS's TypeWrapper for ItemStack.is(HolderSet).
NativeEvents.onEvent(Java.loadClass('net.neoforged.neoforge.event.tick.EntityTickEvent$Post'), event => {
    const entity = event.getEntity();
    if (!isAutisFloatingLevitite(entity)) return;
    entity.setNoGravity(true);
    const v = entity.getDeltaMovement();
    // Aero drag .93 * vanilla/ItemPhysic .98 = .9114.
    // v_next = .9114*v + .0068 -> .07675 post-tick, .07138 at movement (~1.43 blocks/s upward).
    const vy = Math.min(0.080, v.y + 0.0068);
    entity.setDeltaMovement(v.x * 0.92, vy, v.z * 0.92);
    if (!entity.level().isClientSide() && entity.getY() > entity.level().getMaxBuildHeight() + 64) {
        entity.discard();
    }
});

// Mark newly spawned/tossed Levitite stacks before ItemPhysic's first tick can apply gravity.
NativeEvents.onEvent(Java.loadClass('net.neoforged.neoforge.event.entity.EntityJoinLevelEvent'), event => {
    const entity = event.getEntity();
    if (isAutisFloatingLevitite(entity)) {
        entity.setNoGravity(true);
    }
});
