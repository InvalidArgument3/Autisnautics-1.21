/**
 * One Akashic Tome, filled from kubejs/config/universal_tome_books.json.
 *
 * Storage is Akashic Tome 1.8-30's data component akashictome:tool_content:
 * a list of full item stacks (ItemStack.CODEC), not a mod-id map. Each stored
 * stack also gets akashictome:defined_mod. The picker sends that string back,
 * and the first book for a mod uses the plain mod id so sneak-use on that
 * mod's blocks still finds it. Later books from the same mod get mod_0, mod_1.
 *
 * The stack is built with the 1.21 item-component syntax Item.of already
 * parses. KubeJS stack.set / stack.override JSON-encodes the value first, and
 * a ResourceLocation is not JSON ("Unsure how to convert 'mod:book' to JSON").
 *
 * components that this script knows how to write:
 *   patchouli:book        resource location string
 *   modonomicon:book_id   resource location string
 *
 * Given once per player. The flag lives on KubeJS entity persistent data, so
 * relogs and deaths do not give another copy. Clear it, drop the tome, relog:
 *   /kubejs persistent-data entity @s remove received_universal_tome
 */

const TOME_FLAG = 'received_universal_tome'
const TOME_LIST = 'kubejs/config/universal_tome_books.json'

function field(entry, key) {
    if (entry == null) return null
    if (entry[key] != null && entry[key] !== '') return entry[key]
    if (typeof entry.get === 'function') {
        let value = entry.get(key)
        if (value != null && value !== '') return value
    }
    return null
}

function asArray(value) {
    if (!value) return []
    if (Array.isArray(value)) return value
    let out = []
    if (typeof value.size === 'function' && typeof value.get === 'function') {
        for (let i = 0; i < value.size(); i++) out.push(value.get(i))
        return out
    }
    if (typeof value.length === 'number') {
        for (let i = 0; i < value.length; i++) out.push(value[i])
    }
    return out
}

function eachComponent(components, visit) {
    if (!components) return
    if (typeof components.keySet === 'function') {
        let keys = components.keySet().iterator()
        while (keys.hasNext()) {
            let key = '' + keys.next()
            visit(key, components.get(key))
        }
        return
    }
    Object.keys(components).forEach(key => visit(key, components[key]))
}

function claimDefinedMod(used, mod) {
    // First book keeps the mod id. Later books get mod_0, mod_1, matching
    // AttachementRecipe. Zero is a real suffix, so this cannot use a truthiness check.
    if (used[mod] === undefined) {
        used[mod] = 0
        return mod
    }
    let key = mod + '_' + used[mod]
    used[mod] = used[mod] + 1
    return key
}

function snbtString(value) {
    return '"' + String(value).replace(/\\/g, '\\\\').replace(/"/g, '\\"') + '"'
}

function componentMapSnbt(components, definedMod) {
    let parts = []
    let known = {
        'patchouli:book': true,
        'modonomicon:book_id': true
    }
    eachComponent(components, (key, value) => {
        if (!known[key]) {
            console.warn(`[universal-tome] component ${key} is not applied by universal_tome.js`)
        }
        parts.push(snbtString(key) + ':' + snbtString(value))
    })
    parts.push(snbtString('akashictome:defined_mod') + ':' + snbtString(definedMod))
    return '{' + parts.join(',') + '}'
}

function storedBookSnbt(itemId, components, definedMod) {
    return '{id:' + snbtString(itemId) + ',count:1,components:' + componentMapSnbt(components, definedMod) + '}'
}

function textComponentSnbt(text) {
    // custom_name's command syntax wants a string, not a compound.
    // The string's contents are the JSON text component.
    return snbtString(JSON.stringify({ text: String(text) }))
}

function buildUniversalTome() {
    let spec = JsonIO.read(TOME_LIST)
    let books = asArray(field(spec, 'books'))
    if (!books.length) {
        console.error(`[universal-tome] ${TOME_LIST} is missing or has no books`)
        return null
    }

    let stored = []
    let used = {}
    let skipped = []

    books.forEach(entry => {
        let itemId = field(entry, 'item')
        let modId = field(entry, 'mod')
        if (!itemId || !modId) {
            skipped.push('(entry missing item or mod)')
            return
        }
        itemId = '' + itemId
        modId = '' + modId
        if (typeof Item === 'undefined' || !Item.exists(itemId)) {
            skipped.push(itemId)
            return
        }
        let explicit = field(entry, 'defined_mod')
        let defined = explicit ? ('' + explicit) : claimDefinedMod(used, modId)
        stored.push(storedBookSnbt(itemId, field(entry, 'components'), defined))
    })

    if (!stored.length) {
        console.error('[universal-tome] no guidebook items resolved; tome was not given')
        return null
    }
    if (!Item.exists('akashictome:tome')) {
        console.error('[universal-tome] akashictome:tome is not registered')
        return null
    }

    let label = field(spec, 'tome_name') || 'Autisnautics Guide'
    let specString = 'akashictome:tome['
        + 'akashictome:tool_content=[' + stored.join(',') + '],'
        + 'akashictome:is_morphed=false,'
        + 'minecraft:custom_name=' + textComponentSnbt(label) + ','
        + 'akashictome:custom_tome_name=' + textComponentSnbt(label)
        + ']'
    let tome = Item.of(specString)
    if (!tome || tome.isEmpty()) {
        console.error('[universal-tome] Item.of rejected the tome component string')
        return null
    }

    if (skipped.length) {
        console.warn(`[universal-tome] skipped ${skipped.length} missing book(s): ${skipped.join(', ')}`)
    }
    console.info(`[universal-tome] built tome with ${stored.length} book(s)`)
    return tome
}

PlayerEvents.loggedIn(event => {
    let data = event.player.persistentData
    if (data.getBoolean(TOME_FLAG)) return

    let tome = null
    try {
        tome = buildUniversalTome()
    } catch (error) {
        console.error(`[universal-tome] failed to build tome: ${error}`)
        return
    }
    if (!tome) return

    data.putBoolean(TOME_FLAG, true)
    event.player.give(tome)
    console.info(`[universal-tome] gave ${event.player.username} the guidebook tome`)
})
