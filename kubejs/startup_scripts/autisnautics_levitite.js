// priority: 100
// Installed KubeJS 2101.7.2 / NeoForge 21.1.265. Full game restart required.
// Mined levitite stays aeronautics:levitite. It drops as a normal item entity.
// The float came from two places, and neither is a config switch:
//   1. This script set no-gravity and shoved the stack upward every tick.
//   2. Aeronautics ItemEntityMixin zeroes gravity when the item has aeronautics:levitating.
const $AutisFieldUtils = Java.loadClass('org.apache.commons.lang3.reflect.FieldUtils');
const $AutisBuiltInRegistries = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries');
const $AutisRegistries = Java.loadClass('net.minecraft.core.registries.Registries');
const $AutisResourceKey = Java.loadClass('net.minecraft.resources.ResourceKey');
const $AutisLocation = Java.loadClass('net.minecraft.resources.ResourceLocation');

const autisLevitatingType = $AutisBuiltInRegistries.DATA_COMPONENT_TYPE.get($AutisLocation.parse('aeronautics:levitating'));

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

ItemEvents.modification(event => {
    const stripLevitating = item => {
        item.remove(autisLevitatingType);
        console.info('Autisnautics: ' + item.item() + ' levitating component = ' + item.item().components().get(autisLevitatingType));
    };
    event.modify('aeronautics:levitite', stripLevitating);
    event.modify('aeronautics:pearlescent_levitite', stripLevitating);
});
