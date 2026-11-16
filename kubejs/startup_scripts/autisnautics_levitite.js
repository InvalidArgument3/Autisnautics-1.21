// priority: 100
// Installed KubeJS 2101.7.2 / NeoForge 21.1.265. Full game restart required.
//
// Aeronautics' levitite.ItemEntityMixin runs on the client and the server. It
// reads the stack's aeronautics:levitating component, not the item id. When that
// component is present it cancels getDefaultGravity to 0 and scales velocity by
// the component's drag just before ItemEntity.move. It does not add upward speed.
//
// Startup scripts are evaluated from the KubeJS constructor, which can be before
// data component registration. ItemEvents.modification runs later, from
// FMLLoadComplete, after registries exist. A type captured at evaluation time
// stays null on a side that has not registered it yet. item.remove(null) then
// leaves the prototype alone, and a fresh stack's get(null) still logs null.
// The lookup below is inside the callback, on both sides.
//
// A stripped server prototype syncs an empty component patch. The client rebuilds
// the stack from its own prototype, so a client that still has the default
// component puts aeronautics:levitating back. That client hovers (gravity 0, the
// drop's upward pop is never cancelled) while the server falls, and position
// packets yank it back down. Item entities of these two items lose the component
// before they tick. Motion and gravity are not written. End stone powder, music
// discs, and the cloud-skipper converter keep the component.
const $AutisFieldUtils = Java.loadClass('org.apache.commons.lang3.reflect.FieldUtils');
const $AutisBuiltInRegistries = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries');
const $AutisRegistries = Java.loadClass('net.minecraft.core.registries.Registries');
const $AutisResourceKey = Java.loadClass('net.minecraft.resources.ResourceKey');
const $AutisLocation = Java.loadClass('net.minecraft.resources.ResourceLocation');
const $AutisItemStack = Java.loadClass('net.minecraft.world.item.ItemStack');
const $AutisEntityType = Java.loadClass('net.minecraft.world.entity.EntityType');
const $AutisEntityJoin = Java.loadClass('net.neoforged.neoforge.event.entity.EntityJoinLevelEvent');
const $AutisEntityTickPre = Java.loadClass('net.neoforged.neoforge.event.tick.EntityTickEvent$Pre');

const AUTIS_LEVITITE_ITEMS = {
    'aeronautics:levitite': true,
    'aeronautics:pearlescent_levitite': true
};

let autisLevitatingType = null;
let autisLevititeEntityStripLogged = false;

function autisLevitatingComponent() {
    if (autisLevitatingType == null) {
        autisLevitatingType = $AutisBuiltInRegistries.DATA_COMPONENT_TYPE.get($AutisLocation.parse('aeronautics:levitating'));
    }
    return autisLevitatingType;
}

function autisStripLevititeStack(stack) {
    if (stack == null || stack.isEmpty()) return false;
    const type = autisLevitatingComponent();
    if (type == null) return false;
    const id = '' + $AutisBuiltInRegistries.ITEM.getKey(stack.getItem());
    if (!AUTIS_LEVITITE_ITEMS[id]) return false;
    if (!stack.has(type)) return false;
    stack.remove(type);
    return !stack.has(type);
}

function autisStripLevititeEntity(entity) {
    if (entity == null || entity.getType() != $AutisEntityType.ITEM) return;
    if (!autisStripLevititeStack(entity.getItem())) return;
    if (autisLevititeEntityStripLogged) return;
    autisLevititeEntityStripLogged = true;
    console.info('Autisnautics: removed aeronautics:levitating from a levitite item entity on ' + (Platform.isClientEnvironment() ? 'client' : 'server'));
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

ItemEvents.modification(event => {
    const stripLevitating = item => {
        const type = autisLevitatingComponent();
        if (type == null) {
            console.warn('Autisnautics: aeronautics:levitating is not registered; left the default component on ' + item.item());
            return;
        }
        item.remove(type);
        const stack = new $AutisItemStack(item.item());
        console.info('Autisnautics: ' + item.item() + ' fresh stack levitating = ' + stack.get(type)
            + ' on ' + (Platform.isClientEnvironment() ? 'client' : 'server')
            + ', component = ' + $AutisBuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type));
    };
    event.modify('aeronautics:levitite', stripLevitating);
    event.modify('aeronautics:pearlescent_levitite', stripLevitating);
});

NativeEvents.onEvent($AutisEntityJoin, event => {
    autisStripLevititeEntity(event.getEntity());
});

// Pre runs before ItemEntity.tick, so the mixin sees the component already gone.
// Join is not enough on the client: the spawn packet can replace the stack
// afterwards, and an empty patch from a stripped server prototype is filled back
// in from this side's prototype. The dedicated server does not need the per-entity
// tick; its prototype strip is what makes the drop fall, and the join listener
// covers a stack that still has the component. This listener does not write
// motion or gravity.
if (Platform.isClientEnvironment()) {
    NativeEvents.onEvent($AutisEntityTickPre, event => {
        autisStripLevititeEntity(event.getEntity());
    });
}
