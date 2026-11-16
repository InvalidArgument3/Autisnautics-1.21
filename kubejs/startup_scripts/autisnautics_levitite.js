// priority: 100
// Installed KubeJS 2101.7.2 / NeoForge 21.1.265. Full game restart required.
// The pack does not listen for item ticks or joins and does not change motion.
// Aeronautics registers aeronautics:levitating on these items. Removing that
// default component means a new stack has normal gravity: a block drop, a
// thrown stack, and the item entity that carries it. The mod mixin only
// floats a stack that still has the component.
const $AutisFieldUtils = Java.loadClass('org.apache.commons.lang3.reflect.FieldUtils');
const $AutisBuiltInRegistries = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries');
const $AutisRegistries = Java.loadClass('net.minecraft.core.registries.Registries');
const $AutisResourceKey = Java.loadClass('net.minecraft.resources.ResourceKey');
const $AutisLocation = Java.loadClass('net.minecraft.resources.ResourceLocation');
const $AutisItemStack = Java.loadClass('net.minecraft.world.item.ItemStack');

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
        const stack = new $AutisItemStack(item.item());
        console.info('Autisnautics: ' + item.item() + ' fresh stack levitating = ' + stack.get(autisLevitatingType));
    };
    event.modify('aeronautics:levitite', stripLevitating);
    event.modify('aeronautics:pearlescent_levitite', stripLevitating);
});
