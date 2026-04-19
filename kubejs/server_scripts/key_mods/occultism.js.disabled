// priority: 1
ServerEvents.recipes(event => {
    // unify the output
    event.replaceOutput({ id: "occultism:crushing/obsidian_dust" }, "occultism:obsidian_dust", "create:powdered_obsidian")

    // Remove unwanted ore miner ores
    event.remove({ id: "occultism:miner/ores/redstone_ore" })
    event.remove({ id: "occultism:miner/ores/aluminum_ore" })
    event.remove({ id: "occultism:miner/ores/tin_ore" })
    event.remove({ id: "occultism:miner/ores/silver_ore" })
    event.remove({ id: "occultism:miner/deeps/deepslate_redstone_ore" })
    event.remove({ id: "occultism:miner/deeps/deepslate_aluminum_ore" })
    event.remove({ id: "occultism:miner/deeps/deepslate_tin_ore" })
    event.remove({ id: "occultism:miner/deeps/deepslate_silver_ore" })

    // Silver replacements
    event.replaceInput({ id: "occultism:crafting/magic_lamp_empty" }, "#forge:ingots/silver", "#forge:ingots/iron")
    event.replaceInput({ id: "occultism:crafting/lens_frame" }, "#forge:ingots/silver", "#forge:ingots/zinc")

    event.replaceInput({ type: "occultism:ritual" }, "#forge:dusts/silver", "#forge:dusts/zinc")
    event.replaceInput({ type: "occultism:ritual" }, "#forge:ingots/silver", "#forge:ingots/zinc")
    event.replaceInput({ type: "occultism:ritual" }, "#forge:storage_blocks/silver", "#forge:storage_blocks/zinc")

    // use dust instead of raw ore for ritual recipes
    event.replaceInput({ type: "occultism:ritual" }, "#forge:raw_materials/silver", "#forge:dusts/zinc")
    event.replaceInput({ type: "occultism:ritual" }, "#forge:raw_materials/iron", "#forge:dusts/iron")
    event.replaceInput({ type: "occultism:ritual" }, "#forge:raw_materials/gold", "#forge:dusts/gold")
    event.replaceInput({ type: "occultism:ritual" }, "#forge:raw_materials/copper", "#forge:dusts/copper")


    // crushing unification
    event.replaceOutput({ type: "occultism:crushing" }, "occultism:iron_dust", "thermal:iron_dust")
    event.replaceOutput({ type: "occultism:crushing" }, "occultism:gold_dust", "thermal:gold_dust")
    event.replaceOutput({ type: "occultism:crushing" }, "occultism:copper_dust", "thermal:copper_dust")
    event.replaceOutput({ type: "occultism:crushing" }, "occultism:iron_dust", "thermal:iron_dust")
    event.replaceOutput({ type: "occultism:crushing" }, "occultism:gold_dust", "thermal:gold_dust")
    event.replaceOutput({ type: "occultism:crushing" }, "occultism:silver_dust", "thermal:silver_dust")
})

// PlayerEvents.loggedIn(event => {

//     if (!global.occultism_reload) {
//         global.occultism_reload = true
//         event.server.runCommandSilent(`reload`)
//     }

// })
