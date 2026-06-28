// priority: 1
ServerEvents.recipes(event => {
    // Fix farmer's delight recipe conflict with rechiseled
    event.remove({ id: "farmersdelight:flint_knife" })
    event.remove({ id: "farmersdelight:iron_knife" })
    event.remove({ id: "farmersdelight:golden_knife" })
    event.remove({ id: "farmersdelight:diamond_knife" })
    event.shaped("farmersdelight:flint_knife", ["S ", " M"], { M: "minecraft:flint", S: "#forge:rods/wooden" })
    event.shaped("farmersdelight:iron_knife", ["S ", " M"], { M: "minecraft:iron_ingot", S: "#forge:rods/wooden" })
    event.shaped("farmersdelight:golden_knife", ["S ", " M"], { M: "minecraft:gold_ingot", S: "#forge:rods/wooden" })
    event.shaped("farmersdelight:diamond_knife", ["S ", " M"], { M: "minecraft:diamond", S: "#forge:rods/wooden" })

    // Modify farmer's delight log stripping
    event.remove({ input: "#minecraft:logs", type: "farmersdelight:cutting" })
    // laziness and its consequences have been a distaster for the human race
    wood_types.forEach(wood => {
        let log = wood + "_log"
        if (!Item.exists(log)) {
            log = wood + "_stem"
        }
        let woodLog = wood + "_wood"
        if (!Item.exists(woodLog)) {
            woodLog = wood + "_hyphae"
        }
        let strippedLog = log.replace(":",":stripped_")
        let strippedWood = woodLog.replace(":",":stripped_")
        if (Item.exists(log) && Item.exists(strippedLog)) {
            event.custom({
                "type": "farmersdelight:cutting",
                "ingredients": [{ "item": log }],
                "tool": { "tag": "forge:tools/axes" },
                "result": [{ "item": strippedLog }, { "item": "farmersdelight:tree_bark" }]
            })
        }
        if (Item.exists(woodLog) && Item.exists(strippedWood)) {
            event.custom({
                "type": "farmersdelight:cutting",
                "ingredients": [{ "item": woodLog }],
                "tool": { "tag": "forge:tools/axes" },
                "result": [{ "item": strippedWood }, { "item": "farmersdelight:tree_bark" }]
            })
        }
    })
})
