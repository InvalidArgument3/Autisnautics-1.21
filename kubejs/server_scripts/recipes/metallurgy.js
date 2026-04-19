// priority: 10
// This script is important and needs to run early on.

// Redstone, silver and tin do not exist in A&B and we need a bit of scripting to remove them
// A2: never mind, tin is back
ServerEvents.recipes(event => {
    const hasThermal = Platform.isLoaded("thermal")
    const noopThermalBuilder = {
        id() { return this },
        energy() { return this }
    }
    const thermalRecipes = hasThermal
        ? event.recipes.thermal
        : new Proxy({}, { get: () => () => noopThermalBuilder })

    // event.remove({ output: "#forge:nuggets/tin" })
    // event.remove({ output: "#forge:ingots/tin" })
    // event.remove({ output: "#forge:storage_blocks/tin" })
    // A2: keeping these removed
    event.remove({ output: "#forge:plates/tin" })
    event.remove({ output: "#forge:gears/tin" })

    // metal replacements
    // A2: keeping these because it's thermal only and QoL
    const replacementFilter = [{ mod: "thermal", type: "minecraft:crafting_shaped" }, { mod: "thermal", type: "minecraft:crafting_shapeless" }, { mod: "exchangers", type: "minecraft:crafting_shaped" }]
    event.replaceInput(replacementFilter, "#forge:ingots/tin", "#forge:ingots/zinc")
    event.replaceInput(replacementFilter, "#forge:gears/tin", "#forge:gears/lead")

    event.replaceInput(replacementFilter, "#forge:plates/bronze", "#forge:plates/nickel")
    event.replaceInput(replacementFilter, "#forge:gears/bronze", "#forge:gears/nickel")

    event.replaceInput(replacementFilter, "#forge:plates/silver", "#forge:ingots/invar")
    event.replaceInput(replacementFilter, "#forge:gears/silver", "#forge:gears/invar")

    event.replaceInput(replacementFilter, "#forge:plates/constantan", "#forge:plates/signalum")
    event.replaceInput(replacementFilter, "#forge:gears/constantan", "#forge:gears/signalum")

    event.replaceInput(replacementFilter, "#forge:ingots/electrum", "#forge:ingots/constantan")
    event.replaceInput(replacementFilter, "#forge:plates/electrum", "#forge:plates/constantan")
    event.replaceInput(replacementFilter, "#forge:gears/electrum", "#forge:gears/constantan")

    event.replaceInput(replacementFilter, "#forge:plates/invar", "#forge:ingots/invar")

    // // fix recipes broken by replacement
    event.replaceInput({ id: "thermal:storage/electrum_nugget_from_ingot" }, "#forge:ingots/constantan", "#forge:ingots/electrum")
    event.replaceInput({ id: "thermal:storage/electrum_block" }, "#forge:ingots/constantan", "#forge:ingots/electrum")
    // A2: electrum gear is useless
    // event.replaceInput({ id: "thermal:parts/electrum_gear" }, "#forge:ingots/constantan", "#forge:ingots/electrum")

    event.replaceInput({ id: "thermal:storage/electrum_ingot_from_block" }, "thermal:electrum_block", "#forge:storage_blocks/electrum")

    // A2: remove useless parts while we're at it
    event.remove({ output: "#forge:gears/tin" })
    event.remove({ output: "#forge:gears/bronze" })
    event.remove({ output: "#forge:gears/silver" })
    event.remove({ output: "#forge:gears/electrum" })

    // IE support because we enjoy suffering
    event.replaceInput({ output: "immersiveengineering:heavy_engineering" }, "#forge:ingots/electrum", "#forge:ingots/constantan")
    event.replaceInput({ output: "immersiveengineering:connector_mv" }, "#forge:ingots/electrum", "#forge:ingots/constantan")
    event.replaceInput({ output: "immersiveengineering:connector_mv_relay" }, "#forge:ingots/electrum", "#forge:ingots/constantan")
    event.replaceInput({ output: "immersiveengineering:wirecoil_electrum" }, "#forge:wires/electrum", "#forge:wires/gold")

    // Redstone exists in jei to provide a tooltip, we want to remove all of its recipes
    // A2: this ore is obtainable despite not genning, so the recipes should be restored
    // event.remove({ input: "#forge:ores/redstone" })
})

// Tweaks for the metals that we actually want
ServerEvents.recipes(event => {


    // Thermal recipes for zinc
    // A2: JAOPCA handles this one
    // event.recipes.thermal.pulverizer(["kubejs:zinc_dust"], "#forge:ingots/zinc", 0, 2000)
    thermalRecipes.pulverizer(["kubejs:zinc_dust"], "#forge:plates/zinc", 0, 2000)
    thermalRecipes.smelter(["create:zinc_ingot"], "#forge:plates/zinc", 0, 1600)

    // Thermal's fire charge ingot crafting recipes. We don't want them
    event.remove({ id: "thermal:fire_charge/invar_ingot_3" })
    event.remove({ id: "thermal:fire_charge/enderium_ingot_2" })
    event.remove({ id: "thermal:fire_charge/constantan_ingot_2" })
    event.remove({ id: "thermal:fire_charge/bronze_ingot_4" })
    event.remove({ id: "thermal:fire_charge/electrum_ingot_2" })
    event.remove({ id: "thermal:fire_charge/lumium_ingot_4" })
    event.remove({ id: "thermal:fire_charge/signalum_ingot_4" })

    // Duplicate Recipes
    /* A2: JAOPCA handles these probably
    event.remove({ id: "thermal:storage/silver_block"})
    event.remove({ id: "thermal:storage/silver_ingot_from_block"})
    event.remove({ id: "thermal:storage/silver_ingot_from_nuggets"})
    event.remove({ id: "thermal:storage/silver_nugget_from_ingot"})
    event.remove({ id: "thermal:smelting/silver_ingot_from_dust_smelting"})
    event.remove({ id: "thermal:smelting/silver_ingot_from_dust_blasting"})

    event.remove({ id: "thermal:storage/copper_nugget_from_ingot"})
    event.remove({ id: "tconstruct:common/materials/copper_nugget_from_ingot"})
    event.remove({ id: "thermal:storage/copper_ingot_from_nuggets"})
    event.remove({ id: "tconstruct:common/materials/copper_ingot_from_nuggets"})

    event.remove({ id: "thermal:storage/netherite_nugget_from_ingot"})
    event.remove({ id: "tconstruct:common/materials/netherite_nugget_from_ingot"})
    event.remove({ id: "thermal:storage/netherite_ingot_from_nuggets"})
    event.remove({ id: "tconstruct:common/materials/netherite_ingot_from_nuggets"})
    */

    // Remove unwanted Alloying recipes
    event.remove({ id: "create:mixing/brass_ingot" })// this is mixing ingots without heat
    event.remove({ id: /centrifuge_bronze_dust/ })// does nothing if alchemy.js is used (repurposes centrifuge for reagents)
    // smeltery
    event.remove({ id: "tconstruct:smeltery/alloys/molten_bronze" })
    event.remove({ id: "tconstruct:smeltery/alloys/molten_brass" })
    event.remove({ id: "tconstruct:smeltery/alloys/molten_invar" })
    event.remove({ id: "tconstruct:smeltery/alloys/molten_electrum" })
    event.remove({ id: "tconstruct:smeltery/alloys/molten_constantan" })
    event.remove({ id: "tconstruct:smeltery/alloys/molten_rose_gold" })
    event.remove({ id: "tconstruct:smeltery/alloys/molten_enderium" })
    event.remove({ id: "tconstruct:smeltery/alloys/molten_lumium" })
    event.remove({ id: "tconstruct:smeltery/alloys/molten_signalum" })
    // alloy smelter
    event.remove({ id: "thermal:machines/smelter/smelter_alloy_signalum" })
    event.remove({ id: "thermal:machines/smelter/smelter_alloy_lumium" })
    event.remove({ id: "thermal:machines/smelter/smelter_alloy_enderium" })
    event.remove({ id: "thermal:machines/smelter/smelter_alloy_invar" })
    event.remove({ id: "thermal:machines/smelter/smelter_alloy_bronze" })
    event.remove({ id: "thermal:compat/create/smelter_create_alloy_brass" })
    event.remove({ id: "thermal:compat/tconstruct/smelter_alloy_tconstruct_rose_gold_ingot" })
    // thermal handcrafting
    event.remove({ type: "minecraft:crafting_shapeless", output: "thermal:constantan_dust" })
    event.remove({ type: "minecraft:crafting_shapeless", output: "thermal:electrum_dust" })
    event.remove({ type: "minecraft:crafting_shapeless", output: "thermal:lumium_dust" })
    event.remove({ type: "minecraft:crafting_shapeless", output: "thermal:signalum_dust" })
    event.remove({ type: "minecraft:crafting_shapeless", output: "thermal:enderium_dust" })
    event.remove({ type: "minecraft:crafting_shapeless", output: "thermal:bronze_dust" })
    event.remove({ type: "minecraft:crafting_shapeless", output: "thermal:invar_dust" })
    // A2: nuclearcraft grinds pearls into ae2:ender_dust, not enderium
    event.remove({ type: "nuclearcraft:manufactory", output: "thermal:enderium_dust" })
    event.remove({ type: "nuclearcraft:manufactory", output: "#forge:dusts/enderium" })
    event.custom({
        "type": "nuclearcraft:manufactory",
        "input": [
            {
                "count": 1,
                "item": "minecraft:ender_pearl"
            }
        ],
        "output": [
            {
                "amount": 1,
                "item": "ae2:ender_dust"
            }
        ],
        "powerModifier": 1.0,
        "radiation": 1.0,
        "timeModifier": 1.0
    })

    // Create new alloying recipes
    // Mixing Alloys
    let moltenAlloy = function (fluidAlloy, fluid1, fluid2) {
        // Recipe ids are actually important here since the id that comes later in alphabetical order is the one that is prioritized
        event.custom({
            "type": "create:mixing",
            "ingredients": [
                { "amount": 2, "fluid": fluid1 },
                { "amount": 2, "fluid": fluid2 }
            ],
            "results": [
                { "amount": 2, "fluid": "tconstruct:" + fluidAlloy }
            ],
            processingTime: 1
        }).id(`kubejs:mixing/${fluidAlloy}_2`)
    }
    moltenAlloy("molten_brass", "tconstruct:molten_copper", "tconstruct:molten_zinc")
    moltenAlloy("molten_constantan", "tconstruct:molten_copper", "tconstruct:molten_nickel")
    moltenAlloy("molten_rose_gold", "tconstruct:molten_copper", "tconstruct:molten_gold")
    moltenAlloy("molten_electrum", "tconstruct:molten_silver", "tconstruct:molten_gold")
    // remove existing smelter recipes because they accept dusts
    event.remove({ id: "thermal:machines/smelter/smelter_alloy_constantan" })
    event.remove({ id: "thermal:machines/smelter/smelter_alloy_electrum" })
    event.remove({ id: "thermal:machines/smelter/smelter_alloy_netherite" })
    // alloy smelter recipes
    thermalRecipes.smelter(Item.of("create:brass_ingot", 2), ["#forge:ingots/copper", "#forge:ingots/zinc"])
    thermalRecipes.smelter(Item.of("tconstruct:rose_gold_ingot", 2), ["#forge:ingots/copper", "#forge:ingots/gold"])
    thermalRecipes.smelter(Item.of("thermal:constantan_ingot", 2), ["#forge:ingots/copper", "#forge:ingots/nickel"])
    thermalRecipes.smelter(Item.of(getPreferredItemFromTag("forge:ingots/electrum"), 2), ["#forge:ingots/silver", "#forge:ingots/gold"])
    thermalRecipes.smelter(Item.of("minecraft:netherite_ingot", 1), [Item.of("#forge:ingots/netherite_scrap", 4), Item.of("#forge:ingots/gold", 4)])
    // bronze
    thermalRecipes.smelter("3x thermal:bronze_ingot", [Item.of("minecraft:copper_ingot", 3), "#forge:sand"])

    // Nickel Compound
    event.shapeless("kubejs:nickel_compound", ["thermal:nickel_ingot", "thermal:iron_dust", "thermal:iron_dust", "thermal:iron_dust", "thermal:iron_dust"])
    thermalRecipes.smelter(["kubejs:invar_compound", "kubejs:invar_compound"], ["thermal:nickel_ingot", "minecraft:iron_ingot"])
    // Invar Compound
    event.blasting("kubejs:invar_compound", "kubejs:nickel_compound")
    { // Invar ingots
        let s = "kubejs:invar_compound"
        event.recipes.create.sequenced_assembly([
            "thermal:invar_ingot",
        ], "kubejs:invar_compound", [
            event.recipes.create.pressing(s, s)
        ]).transitionalItem(s)
            .loops(16)
            .id("kubejs:invar")
    }

    // smeltery alloys
    event.custom({
        "type": "tconstruct:alloy",
        "inputs": [
            { "name": "tconstruct:molten_silver", "amount": 90 },
            { "name": "tconstruct:molten_copper", "amount": 90 },
            { "name": "thermal:redstone", "amount": 1000 }
        ],
        "result": {
            "fluid": "tconstruct:molten_signalum",
            "amount": 90
        },
        "temperature": 1000
    })
    event.custom({
        "type": "tconstruct:alloy",
        "inputs": [
            { "name": "tconstruct:molten_silver", "amount": 90 },
            { "name": "tconstruct:molten_copper", "amount": 90 },
            { "name": "thermal:glowstone", "amount": 1000 }
        ],
        "result": {
            "fluid": "tconstruct:molten_lumium",
            "amount": 90
        },
        "temperature": 1000
    })
    event.custom({
        "type": "tconstruct:alloy",
        "inputs": [
            // A2: bronze is 9:1 copper/tin, no glass
            { "name": "tconstruct:molten_copper", "amount": 9 },
            { "name": "tconstruct:molten_tin", "amount": 1 }
        ],
        "result": {
            "fluid": "tconstruct:molten_bronze",
            "amount": 10// 1 nugget
        },
        "temperature": 1000
    })

    // Thermal alloys
    if (hasThermal) {
        event.custom({
            "type": "thermal:refinery",
            "ingredient": {
                "fluid": "thermal:glowstone",
                "amount": 1000
            },
            "result": [
                {
                    "item": "thermal:lumium_ingot"
                }
            ],
            "energy": 2000
        })

        event.custom({
            "type": "thermal:refinery",
            "ingredient": {
                "fluid": "thermal:redstone",
                "amount": 1000
            },
            "result": [
                {
                    "item": "thermal:signalum_ingot"
                }
            ],
            "energy": 2000
        })
    }

    // Plates
    event.recipes.create.pressing(["thermal:lead_plate"], "thermal:lead_ingot")
    event.recipes.create.pressing(["thermal:constantan_plate"], "thermal:constantan_ingot")
    event.recipes.create.pressing(["thermal:nickel_plate"], "thermal:nickel_ingot")
    event.recipes.create.pressing(["thermal:signalum_plate"], "thermal:signalum_ingot")
    event.recipes.create.pressing(["thermal:lumium_plate"], "thermal:lumium_ingot")
    event.recipes.create.pressing(["thermal:enderium_plate"], "thermal:enderium_ingot")

    // dusts
    event.recipes.create.milling("thermal:iron_dust", "#forge:ingots/iron")
    event.recipes.create.milling("thermal:gold_dust", "#forge:ingots/gold")
    event.recipes.create.milling("thermal:nickel_dust", "#forge:ingots/nickel")
    event.recipes.create.milling("thermal:lead_dust", "#forge:ingots/lead")
    event.recipes.create.milling("thermal:copper_dust", "#forge:ingots/copper")
    // A2: nuclearcraft has zinc dust now, remove kubejs item
    // event.recipes.create.milling("kubejs:zinc_dust", "#forge:ingots/zinc")
    event.recipes.create.milling("nuclearcraft:zinc_dust", "#forge:ingots/zinc")

    // other metal unification
    /* A2: JAOPCA gets it?
    event.replaceOutput({}, "#forge:ingots/silver", "thermal:silver_ingot")
    event.replaceOutput({}, "#forge:ingots/bronze", "thermal:bronze_ingot")
    */
    // event.replaceOutput({ id:"occultism:crafting/silver_block"}, "#forge:storage_blocks/silver", "thermal:silver_block")


    // Ore processing
    event.remove({ id: /thermal:machines\/smelter\/.*dust/ })
    event.remove({ id: /tconstruct:smeltery\/.*\/ore/ })
    event.remove({ input: "#create:crushed_raw_materials" })

    native_metals.forEach(e => {// see _helper.js
        event.remove({ type: "minecraft:smelting", input: "#forge:dusts/" + e })
        event.remove({ type: "minecraft:blasting", input: "#forge:dusts/" + e })
        event.remove({ type: "tconstruct:melting", input: "#forge:dusts/" + e })
    })
    event.remove({ id: "thermal:smelting/silver_ingot_from_dust_smelting" })
    event.remove({ id: "thermal:smelting/silver_ingot_from_dust_blasting" })

    const stone = Item.of("minecraft:cobblestone", 1).withChance(.5)
    let experience = Item.of("create:experience_nugget", 1).withChance(0.75)

    let dust_process = (materialName, byproduct, ByproductName) => {
        let crushedOre = "create:crushed_raw_" + materialName
        let fluid = "tconstruct:molten_" + materialName
        switch (materialName) {
        case "anthralite":
            crushedOre = "scguns:crushed_raw_" + materialName
            fluid = "kubejs:molten_" + materialName// made in generate.js
            break
        case "boron":
        case "lithium":
        case "magnesium":
        case "thorium":
            crushedOre = "jaopca:create_crushed." + materialName
            fluid = "nuclearcraft:" + materialName
            break
        case "cobalt":
            crushedOre = "jaopca:create_crushed." + materialName
            fluid = "tconstruct:molten_" + materialName
            break
        case "calorite":
        case "desh":
        case "ostrum":
            crushedOre = "jaopca:create_crushed." + materialName
            fluid = "tcintegrations:molten_" + materialName
            break
        default:
            crushedOre = "create:crushed_raw_" + materialName
            fluid = "tconstruct:molten_" + materialName
        }

        let oreTag = ("#forge:ores/" + materialName)
        let crushedOreBlockTag = ("#forge:storage_blocks/raw_" + materialName)
        let dustTag = ("#forge:dusts/" + materialName)
        let fluidByproduct = "tconstruct:molten_" + ByproductName
        let rawOreTag = ("#forge:raw_materials/" + materialName)
        let fluidTag = ("#forge:molten_" + materialName)
        let ingotTag = ("#forge:ingots/" + materialName)

        // slightly slower than passing the name directly but it reduces how many parameters this function needs.
        let ingot = getPreferredItemFromTag("forge:ingots/" + materialName);
        let nugget = getPreferredItemFromTag("forge:nuggets/" + materialName);
        let nuggetByproduct = getPreferredItemFromTag("forge:nuggets/" + ByproductName);
        let dust = getPreferredItemFromTag("forge:dusts/" + materialName);

        // raw ore block compression and decompression
        event.replaceInput({ type: "minecraft:crafting_shaped" }, rawOreTag, crushedOre)
        event.replaceOutput({ type: "minecraft:crafting_shapeless" }, rawOreTag, crushedOre)

        event.remove([
            { type: "minecraft:smelting", input: rawOreTag },
            { type: "minecraft:blasting", input: rawOreTag },
            { type: "create:crushing", input: rawOreTag },
            // { type: "occultism:crushing", input: rawOreTag },
            { type: "tconstruct:ore_melting", input: rawOreTag },
            // A2
            { id: /^immersiveengineering:crafting\/raw_hammercrushing.*/ },
            { id: /^scguns:immersiveengineering.*hammercrushing/ },
            { type: "nuclearcraft:manufactory", input: rawOreTag },
            { type: "nuclearcraft:melter", input: rawOreTag }
        ])

        event.remove({ id: `thermal:machines/pulverizer/pulverizer_raw_${materialName}` })
        event.remove({ id: `thermal:machines/smelter/smelter_raw_${materialName}` })

        event.remove([
            { type: "thermal:smelter", input: oreTag },
            { type: "thermal:pulverizer", input: oreTag },
            { type: "minecraft:blasting", input: oreTag },
            { type: "minecraft:smelting", input: oreTag },
            { type: "create:crushing", input: oreTag },
            { type: "create:milling", input: oreTag },
            // { type: "occultism:crushing", input: oreTag },
            // A2
            { id: /^immersiveengineering:crafting\/hammercrushing.*/ },
            { id: /^jaopca:immersiveengineering.*dust.*hammer.*/ },
            { type: "nuclearcraft:melter", input: oreTag }
        ])

        event.remove({ id: `thermal:machines/pulverizer/pulverizer_${materialName}_ore` })
        event.remove({ id: `thermal:machines/smelter/smelter_${materialName}_ore` })

        event.remove([
            { type: "minecraft:smelting", input: crushedOreBlockTag },
            { type: "minecraft:blasting", input: crushedOreBlockTag },
            { type: "create:crushing", input: crushedOreBlockTag },
            // { type: "occultism:crushing", input: crushedOreBlockTag },
            { type: "tconstruct:ore_melting", input: crushedOreBlockTag },
            // A2: quark iron and gold dupe
            { id: /^quark:tweaks\/smelting\/raw.*/ },
            { id: /^quark:tweaks\/blasting\/raw.*/ }
        ])

        // 'concentrated ore' to crushed ore
        event.recipes.create.milling([Item.of(crushedOre, 5)], rawOreTag).id("kubejs:ore_processing/milling/raw_ore/" + materialName)
        event.recipes.create.crushing([Item.of(crushedOre, 5), Item.of(crushedOre, 2).withChance(0.5)], rawOreTag).id("kubejs:ore_processing/crushing/raw_ore/" + materialName)

        // ore to crushed ore
        event.recipes.create.crushing([Item.of(crushedOre, 3), Item.of(crushedOre, 1).withChance(0.5), experience, stone], oreTag).id("kubejs:ore_processing/crushing/ore/" + materialName)
        thermalRecipes.pulverizer([Item.of(crushedOre).withChance(4.5), Item.of("minecraft:gravel").withChance(0.2)], oreTag, 0.2).id("kubejs:ore_processing/pulverizing/ore/" + materialName)
        // event.recipes.occultism.crushing(Item.of(dust, 3), Item.of(crushedOre), 200, -1, false).id(`kubejs:occultism/crushing/${materialName}`)

        // crushed ore to nuggets
        event.smelting(Item.of(nugget, 3), crushedOre).id("kubejs:ore_processing/smelting/crushed/" + materialName)
        event.recipes.create.splashing([Item.of(nugget, 2), Item.of(nuggetByproduct, 1).withChance(0.85)], dustTag).id("kubejs:ore_processing/splashing/dust/" + materialName)

        // crushed ore to ore dust
        event.recipes.create.milling([Item.of(dust, 3)], crushedOre).id("kubejs:ore_processing/milling/crushed/" + materialName)
        event.recipes.create.crushing([Item.of(dust, 3), Item.of(dust, 3).withChance(0.5)], crushedOre).id("kubejs:ore_processing/crushing/crushed/" + materialName)
        thermalRecipes.pulverizer([Item.of(dust, 6)], crushedOre, 0.2, 6400).id("kubejs:ore_processing/pulverizing/crushed/" + materialName)
        // A2: nuclearcraft clean slurry to dust, tweak to even out the ore amounts
        let cleanSlurryTag = "forge:" + materialName + "_clean_slurry"
        event.remove({ type: "nuclearcraft:crystallizer", output: dust })
        event.custom({
            "type": "nuclearcraft:crystallizer",
            "inputFluids": [
                {
                    "amount": 400,
                    "tag": cleanSlurryTag
                }
            ],
            "output": [
                {
                    "count": 3,
                    "item": dust
                }
            ],
            "powerModifier": 1.0,
            "radiation": 1.0,
            "timeModifier": 1.0
        })

        // ore dust to nuggets
        event.smelting(Item.of(nugget, 1), dustTag).cookingTime(40).id("kubejs:ore_processing/smelting/dust/" + materialName)

        // ore dust to fluid
        thermalRecipes.crucible(Fluid.of(fluid, 30), dustTag, 0, 3000).id("kubejs:ore_processing/crucible/dust/" + materialName)
        event.recipes.create.mixing([Fluid.of(fluid, 180)], [Item.of(dust, 3), "ae2:matter_ball"]).superheated().id("kubejs:ore_processing/mixing/dust/" + materialName)
        // A2: NC melter
        event.remove({ type: "nuclearcraft:melter", input: dustTag })
        event.custom({
            "type": "nuclearcraft:melter",
            "input": [
                {
                    "tag": dustTag.slice(1)
                }
            ],
            "outputFluids": [
                {
                    "amount": 30,
                    "fluid": fluid
                }
            ],
            "powerModifier": 1.0,
            "radiation": 1.0,
            "timeModifier": 1.0
        })
        // NC melter direct ore melting
        event.custom({
            "type": "nuclearcraft:melter",
            "input": [
                {
                    "tag": oreTag.slice(1)
                }
            ],
            "outputFluids": [
                {
                    "amount": 180,
                    "fluid": fluid
                }
            ],
            "powerModifier": 1.0,
            "radiation": 1.0,
            "timeModifier": 1.0
        })
        // ingots too I guess
        event.remove({ type: "nuclearcraft:melter", input: ingotTag })
        event.custom({
            "type": "nuclearcraft:melter",
            "input": [
                {
                    "tag": ingotTag.slice(1)
                }
            ],
            "outputFluids": [
                {
                    "amount": 90,
                    "fluid": fluid
                }
            ],
            "powerModifier": 1.0,
            "radiation": 1.0,
            "timeModifier": 1.0
        })

        // ingots to fluid
        // event.recipes.thermal.crucible(Fluid.of(fluid, 90), ingot, 2000).id('kubejs:ore_processing/crucible/ingot/'+materialName) //now automatically ported

        // melting crushed ores to nuggets
        if (hasThermal) {
            event.custom({
                "type": "thermal:smelter",
                "ingredient": { "item": crushedOre },
                "result": [
                    { "item": nugget, "chance": 9.0 },
                    { "item": byproduct, "chance": (byproduct.endsWith("nugget") ? 1.8 : 0.2) },
                    { "item": "thermal:rich_slag", "chance": 0.2 }
                ],
                "experience": 0.2,
                "energy": 3200
            }).id("kubejs:ore_processing/induction_smelting/crushed/" + materialName)
        }

        // melting ore dusts to fluid
        event.custom({
            "type": "tconstruct:melting",
            "ingredient": { "tag": dustTag.slice(1) },
            "result": { "fluid": fluid, "amount": 30 },
            "temperature": 500,
            "time": 30,
            "byproducts": [{ "fluid": fluidByproduct, "amount": 10 }]
        }).id("kubejs:ore_processing/melting/dust/" + materialName);
    }

    //		 	  materialName, byproduct, ByproductName
    dust_process("nickel", "create:copper_nugget", "copper")
    dust_process("lead", "minecraft:iron_nugget", "iron")
    dust_process("iron", "thermal:nickel_nugget", "nickel")
    dust_process("gold", "thermal:cinnabar", "zinc")
    dust_process("copper", "minecraft:gold_nugget", "gold")
    dust_process("zinc", "thermal:sulfur", "lead")
    // A2
    dust_process("silver", "minecraft:gold_nugget", "gold")
    dust_process("platinum", "thermal:nickel_nugget", "nickel")
    dust_process("tin", "minecraft:iron_nugget", "iron")
    dust_process("aluminum", "minecraft:quartz", "quartz")
    dust_process("uranium", "thermal:lead_nugget", "lead")
    dust_process("anthralite", "thermal:sulfur", "lead")
    dust_process("boron", "immersiveengineering:nugget_aluminum", "aluminum")
    dust_process("thorium", "nuclearcraft:neodymium_dust", "uranium")
    dust_process("magnesium", "minecraft:iron_nugget", "iron")
    dust_process("lithium", "thermal:apatite", "zinc")
    dust_process("cobalt", "thermal:nickel_nugget", "nickel")
    dust_process("calorite", "tconstruct:debris_nugget", "gold")
    dust_process("desh", "nuclearcraft:titanium_dust", "quartz")
    dust_process("ostrum", "tconstruct:pig_iron_nugget", "pig_iron")

    /* A2: raw silver already accounted for
    event.remove([
        { type: "minecraft:crafting_shaped", input: "#forge:raw_materials/silver" },
        { type: "minecraft:crafting_shapeless", input: "#forge:raw_materials/silver" },
        { type: "minecraft:smelting", input: "#forge:raw_materials/silver" },
        { type: "minecraft:blasting", input: "#forge:raw_materials/silver" },
        { type: "create:crushing", input: "#forge:raw_materials/silver" },
       // { type: "occultism:crushing", input: "#forge:raw_materials/silver" },
        { type: "tconstruct:ore_melting", input: "#forge:raw_materials/silver" }
    ])
    event.remove({ id: "thermal:machines/pulverizer/pulverizer_raw_silver"})
    event.remove({ id: "thermal:machines/smelter/smelter_raw_silver"})
    */

    event.replaceInput({ id: "thermal:machine/smelter/smelter_iron_ore" }, "minecraft:iron_ore", "create:crushed_raw_iron")
    event.replaceInput({ id: "thermal:machine/smelter/smelter_gold_ore" }, "minecraft:gold_ore", "create:crushed_raw_gold")

    // Other Tweaks
    /* A2: not necessary?
    event.custom({
        "type": "tconstruct:ore_melting",
        "ingredient": {
            "tag": "forge:ores/cobalt"
        },
        "result": {
            "fluid": "tconstruct:molten_cobalt",
            "amount": 90
        },
        "temperature": 950,
        "time": 97,
        "rate": "metal",
        "byproducts": [
            {
                "fluid": "tconstruct:molten_iron",
                "amount": 30
            }
        ]
    })
    */

    // A2: netherite redone below
    /*
    event.custom({
        "type": "tconstruct:ore_melting",
        "ingredient": {
            "tag": "forge:ores/netherite_scrap"
        },
        "result": {
            "fluid": "tconstruct:molten_debris",
            "amount": 90
        },
        "temperature": 1175,
        "time": 143,
        "rate": "metal",
        "byproducts": [
            {
                "fluid": "tconstruct:molten_diamond",
                "amount": 25
            },
            {
                "fluid": "tconstruct:molten_gold",
                "amount": 90
            }
        ]
    })
    */

    // metal recycling
    event.custom({
        "type": "tconstruct:melting",
        "ingredient": { "tag": "kubejs:recycling/iron" },
        "result": {
            "fluid": "tconstruct:molten_iron",
            "amount": 30
        },
        "temperature": 500,
        "time": 40
    })

    event.custom({
        "type": "tconstruct:melting",
        "ingredient": { "tag": "kubejs:circuit_press" },
        "result": {
            "fluid": "tconstruct:molten_invar",
            "amount": 180
        },
        "temperature": 500,
        "time": 90
    })

    // A2: createaddition rods and wires
    let rodMats = ["brass", "copper", "electrum", "gold"]
    let wireMats = ["iron", "gold"]
    rodMats.forEach(e => {
        event.custom({
            "type": "tconstruct:casting_table",
            "cast": {
                "tag": "tconstruct:casts/multi_use/rod"
            },
            "fluid": {
                "tag": `forge:molten_${e}`,
                "amount": 45
            },
            "result": { "tag": `forge:rods/${e}` },
            "cooling_time": 1
        }).id(`kubejs:smeltery/casting/metal/${e}/rod_gold_cast`)

        event.custom({
            "type": "tconstruct:casting_table",
            "cast": {
                "tag": `tconstruct:casts/single_use/rod`
            },
            "cast_consumed": true,
            "fluid": {
                "tag": `forge:molten_${e}`,
                "amount": 45
            },
            "result": { "tag": `forge:rods/${e}` },
            "cooling_time": 1
        }).id(`kubejs:smeltery/casting/metal/${e}/rod_sand_cast`)
    })
    wireMats.forEach(e => {
        event.custom({
            "type": "tconstruct:casting_table",
            "cast": {
                "tag": "tconstruct:casts/multi_use/wire"
            },
            "fluid": {
                "tag": `forge:molten_${e}`,
                "amount": 45
            },
            "result": { "tag": `forge:wires/${e}` },
            "cooling_time": 1
        }).id(`kubejs:smeltery/casting/metal/${e}/wire_gold_cast`)

        event.custom({
            "type": "tconstruct:casting_table",
            "cast": {
                "tag": `tconstruct:casts/single_use/wire`
            },
            "cast_consumed": true,
            "fluid": {
                "tag": `forge:molten_${e}`,
                "amount": 45
            },
            "result": { "tag": `forge:wires/${e}` },
            "cooling_time": 1
        }).id(`kubejs:smeltery/casting/metal/${e}/wire_sand_cast`)
    })

    // A2: Alloys in NC melter
    let alloys = ["steel", "bronze", "constantan", "invar", "brass", "electrum"]
    alloys.forEach(metal => {
        let fluid = "tconstruct:molten_" + metal
        let dustTag = "#forge:dusts/" + metal
        let ingotTag = "#forge:ingots/" + metal
        // dust melting
        event.remove({ type: "nuclearcraft:melter", input: dustTag })
        event.custom({
            "type": "nuclearcraft:melter",
            "input": [
                {
                    "tag": dustTag.slice(1)
                }
            ],
            "outputFluids": [
                {
                    "amount": 30,
                    "fluid": fluid
                }
            ],
            "powerModifier": 1.0,
            "radiation": 1.0,
            "timeModifier": 1.0
        })
        // ingot melting
        event.remove({ type: "nuclearcraft:melter", input: ingotTag })
        event.custom({
            "type": "nuclearcraft:melter",
            "input": [
                {
                    "tag": ingotTag.slice(1)
                }
            ],
            "outputFluids": [
                {
                    "amount": 90,
                    "fluid": fluid
                }
            ],
            "powerModifier": 1.0,
            "radiation": 1.0,
            "timeModifier": 1.0
        })
    })
    // other alloys mostly unified in datapack: tfmg, createbigcannons
    // event.remove({type: "tfmg:casting"})// only works for steel, and fluid costs are both incorrect and hardcoded
    // event.remove({output: "tfmg:casting_basin"})
    // event.remove({output: "tfmg:casting_spout"})
    // event.remove({output: "tfmg:block_mold"})
    // event.remove({output: "tfmg:ingot_mold"})

    // //A2: ancient debris/netherite tweaks
    // configuring jaopca materials in kubejs because i'm a deranged maniac
    event.remove({ output: "jaopca:create_crushed.netherite_scrap" })
    event.remove({ output: "jaopca:dusts.netherite_scrap" })
    event.remove({ input: "jaopca:create_crushed.netherite_scrap" })
    event.remove({ input: "jaopca:dusts.netherite_scrap" })
    // delete jaopca "molten netherite scrap"
    event.remove({ id: /^jaopca:.*molten.*netherite_scrap$/ })

    // processing debris
    // blasting/smelting: 1 scrap
    // milling/grinding: 2 scrap
    event.recipes.create.milling(Item.of("minecraft:netherite_scrap", 2), "minecraft:ancient_debris")
    // crushing: 2 scrap + 50% chance of 3 + exp + gold byproduct
    event.recipes.create.crushing([Item.of("minecraft:netherite_scrap", 2), Item.of("minecraft:netherite_scrap", 1).withChance(0.5), experience, Item.of("minecraft:gold_nugget").withChance(0.5)], "minecraft:ancient_debris")
    // pulverizing: 3 scrap + gold byproduct
    thermalRecipes.pulverizer([Item.of("minecraft:netherite_scrap").withChance(3.0), Item.of("minecraft:gold_nugget").withChance(0.2)], "minecraft:ancient_debris", 0.2)
    // melting: 2.66 scrap or 2 scrap + byproducts
    event.custom({
        "type": "tconstruct:ore_melting",
        "ingredient": {
            "tag": "forge:ores/netherite_scrap"
        },
        "result": {
            "fluid": "tconstruct:molten_debris",
            "amount": 180
        },
        "temperature": 1175,
        "time": 143,
        "rate": "metal",
        "byproducts": [
            {
                "fluid": "tconstruct:molten_diamond",
                "amount": 75// actually 25mb
            },
            {
                "fluid": "tconstruct:molten_gold",
                "amount": 90// actually 30mb
            }
        ]
    })
    event.custom({
        "type": "nuclearcraft:melter",
        "input": [
            {
                "tag": "forge:ores/netherite_scrap"
            }
        ],
        "outputFluids": [
            {
                "amount": 240,
                "fluid": "tconstruct:molten_debris"
            }
        ],
        "powerModifier": 2.0,
        "radiation": 1.0,
        "timeModifier": 4.0
    })
    event.custom({
        "type": "embers:melting",
        "bonus": {
            "amount": 30,
            "fluid": "tconstruct:molten_gold"
        },
        "input": {
            "tag": "forge:ores/netherite_scrap"
        },
        "output": {
            "amount": 180,
            "fluid": "tconstruct:molten_debris"
        }
    })

    // processing storage blocks
    // block from casting basin
    event.custom({
        "type": "tconstruct:casting_basin",
        "cooling_time": 221,
        "fluid": {
            "amount": 810,
            "tag": "forge:molten_debris"
        },
        "result": {
            "tag": "forge:storage_blocks/netherite_scrap"
        }
    })
    // molten from block
    event.custom({
        "type": "tconstruct:melting",
        "ingredient": {
            "tag": "forge:storage_blocks/netherite_scrap"
        },
        "result": {
            "amount": 810,
            "fluid": "tconstruct:molten_debris"
        },
        "temperature": 1175,
        "time": 221
    })
    event.custom({
        "type": "embers:melting",
        "input": {
            "tag": "forge:storage_blocks/netherite_scrap"
        },
        "output": {
            "amount": 810,
            "fluid": "tconstruct:molten_debris"
        }
    })

    // processing nuggets
    // molten from nugget
    event.custom({
        "type": "embers:melting",
        "input": {
            "tag": "forge:nuggets/netherite_scrap"
        },
        "output": {
            "amount": 10,
            "fluid": "tconstruct:molten_debris"
        }
    })
    // nugget from molten
    event.custom({
        "type": "embers:stamping",
        "fluid": {
            "amount": 10,
            "fluid": "tconstruct:molten_debris"
        },
        "output": {
            "tag": "forge:nuggets/netherite_scrap"
        },
        "stamp": {
            "item": "embers:nugget_stamp"
        }
    })

    // processing scrap
    // molten from scrap
    event.custom({
        "type": "embers:melting",
        "input": {
            "tag": "forge:ingots/netherite_scrap"
        },
        "output": {
            "amount": 90,
            "fluid": "tconstruct:molten_debris"
        }
    })
    // scrap from molten
    event.custom({
        "type": "embers:stamping",
        "fluid": {
            "amount": 90,
            "fluid": "tconstruct:molten_debris"
        },
        "output": {
            "tag": "forge:ingots/netherite_scrap"
        },
        "stamp": {
            "item": "embers:ingot_stamp"
        }
    })

    // jaopca/embers melter output unification
    let offendingMetals = ["zinc", "platinum", "uranium", "brass", "constantan", "invar"]
    offendingMetals.forEach(metal => {
        let idRegex = new RegExp("jaopca:embers.*to_molten\." + metal + "$")
        event.remove({ id: idRegex })
    })

    // casting recipes for molten NC metals and kubejs:molten_anthralite
    let ncMetals = ["lithium", "thorium", "magnesium", "boron", "anthralite"]
    let recipeMap = new Map([
        ["nugget", 10],
        ["ingot", 90],
        ["plate", 90]
    ])

    ncMetals.forEach(metal => {
        function ncCast(value, key, map) {
            if (!(metal == "anthralite" && key == "plate")) {// doesn't exist
                event.custom({
                    "type": "tconstruct:casting_table",
                    "cast": {
                        "tag": "tconstruct:casts/multi_use/" + key
                    },
                    "fluid": {
                        "tag": "forge:molten_" + metal,
                        "amount": value
                    },
                    "result": { "tag": "forge:" + key + "s/" + metal },
                    "cooling_time": 1
                }).id("kubejs:smeltery/casting/metal/" + metal + "/" + key + "_gold_cast")
                event.custom({
                    "type": "tconstruct:casting_table",
                    "cast": {
                        "tag": "tconstruct:casts/single_use/" + key
                    },
                    "fluid": {
                        "tag": "forge:molten_" + metal,
                        "amount": value
                    },
                    "result": { "tag": "forge:" + key + "s/" + metal },
                    "cooling_time": 1
                }).id("kubejs:smeltery/casting/metal/" + metal + "/" + key + "_sand_cast")
            }
        }
        recipeMap.forEach(ncCast)
        event.custom({
            "type": "tconstruct:casting_basin",
            "cooling_time": 221,
            "fluid": {
                "amount": 810,
                "tag": "forge:molten_" + metal
            },
            "result": {
                "tag": "forge:storage_blocks/" + metal
            }
        })
    })
})
