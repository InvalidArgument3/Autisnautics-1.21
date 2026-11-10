// One-shot registry dump for Autisnautics agent tooling.
// Writes docs/dumps/items.json + recipes-index.json when flag file exists,
// or always once per JVM if AUTIS_DUMP_ALWAYS (see below).
const FLAG = 'kubejs/exported/.dump_now'
const ALWAYS = false // touch kubejs/exported/.dump_now to dump on next ServerEvents.loaded

// KubeJS 2101 blocks java.nio.file.Files and has no Utils.fileExists.
// JsonIO.read returns null when the flag is absent, and throws if the touched file is empty.
function dumpFlagPresent() {
    if (typeof JsonIO === "undefined" || !JsonIO.read) return false
    try {
        return JsonIO.read(FLAG) != null
    } catch (e) {
        // JsonIO throws when the touched flag file is empty. That is the dump signal.
        return true
    }
}

function dumpRegistries(server) {
    const outDir = 'kubejs/exported'
    const docsDir = 'docs/dumps'

    const itemIds = []
    const itemMeta = []

    // Prefer Item.getTypeList / Ingredient.all depending on KubeJS build.
    let ids = null
    try {
        if (typeof Item !== 'undefined' && Item.getTypeList) {
            ids = Item.getTypeList()
        }
    } catch (e) { /* fall through */ }

    if (!ids) {
        try {
            ids = Ingredient.all.itemIds
        } catch (e) { /* fall through */ }
    }

    if (!ids) {
        try {
            const Registries = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries')
            ids = []
            Registries.ITEM.keySet().forEach(id => ids.push(String(id)))
        } catch (e) {
            console.error('[autis-dump] no item registry API: ' + e)
            return
        }
    }

    for (let id of ids) {
        let s = String(id)
        itemIds.push(s)
        try {
            let stack = Item.of(s)
            itemMeta.push({
                id: s,
                display: stack.getDisplayName ? String(stack.getDisplayName().getString()) : s,
                mod: s.includes(':') ? s.split(':')[0] : 'minecraft'
            })
        } catch (e) {
            itemMeta.push({ id: s, display: s, mod: s.split(':')[0] })
        }
    }

    itemIds.sort()
    itemMeta.sort((a, b) => a.id.localeCompare(b.id))

    const byMod = {}
    for (const row of itemMeta) {
        byMod[row.mod] = (byMod[row.mod] || 0) + 1
    }

    const payload = {
        generated_at: new Date().toISOString(),
        count: itemIds.length,
        by_mod: byMod,
        items: itemIds,
        items_with_names: itemMeta
    }

    JsonIO.write(`${outDir}/items.json`, payload)
    JsonIO.write(`${docsDir}/items.json`, payload)

    // Recipe id index (types + counts). Full recipe bodies are huge; keep an index.
    try {
        const recipes = server.recipeManager ? null : null
    } catch (e) { /* ignore */ }

    let recipeIndex = { generated_at: payload.generated_at, note: 'run /kubejs export for full datapack dump', counts_by_namespace: {} }
    try {
        // Collect recipe ids from reloadable server recipes if exposed
        const all = []
        if (server && server.getRecipeManager) {
            const rm = server.getRecipeManager()
            // NeoForge RecipeManager iteration varies; best-effort
            if (rm.getRecipes) {
                for (let holder of rm.getRecipes()) {
                    let id = holder.id ? String(holder.id) : String(holder)
                    all.push(id)
                    let ns = id.includes(':') ? id.split(':')[0] : 'unknown'
                    recipeIndex.counts_by_namespace[ns] = (recipeIndex.counts_by_namespace[ns] || 0) + 1
                }
            }
        }
        recipeIndex.count = all.length
        recipeIndex.recipe_ids = all.sort()
    } catch (e) {
        recipeIndex.error = String(e)
    }

    JsonIO.write(`${outDir}/recipes-index.json`, recipeIndex)
    JsonIO.write(`${docsDir}/recipes-index.json`, recipeIndex)

    console.info(`[autis-dump] wrote ${itemIds.length} items -> ${outDir}/items.json and ${docsDir}/items.json`)
}

ServerEvents.loaded(event => {
    if (!ALWAYS && !dumpFlagPresent()) return
    try {
        dumpRegistries(event.server)
    } catch (e) {
        console.error('[autis-dump] failed: ' + e)
    }
    try {
        JsonIO.write(FLAG, null)
    } catch (e) { /* ignore */ }
})
