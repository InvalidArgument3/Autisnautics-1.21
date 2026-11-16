// priority: 100
// Installed KubeJS 2101.7.2 / NeoForge 21.1.265. Full game restart required.
// The pack no longer listens for item ticks or joins to cancel gravity and shove
// levitite stacks upward. Aeronautics keeps aeronautics:levitating on the items,
// and its own item-entity mixin is the float.
const $AutisFieldUtils = Java.loadClass('org.apache.commons.lang3.reflect.FieldUtils');
const $AutisRegistries = Java.loadClass('net.minecraft.core.registries.Registries');
const $AutisResourceKey = Java.loadClass('net.minecraft.resources.ResourceKey');
const $AutisLocation = Java.loadClass('net.minecraft.resources.ResourceLocation');

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
