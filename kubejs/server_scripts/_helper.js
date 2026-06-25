// priority: 100

let colours = ["white", "orange", "magenta", "light_blue", "lime", "pink", "purple", "light_gray", "gray", "cyan", "brown", "green", "blue", "red", "black", "yellow"]
let native_metals = ["iron", "zinc", "lead", "copper", "nickel", "gold",
    // A2: more metals
    "tin", "aluminum", "boron", "calorite", "cobalt", "desh", "lithium", "magnesium", "ostrum", "platinum", "silver", "thorium", "anthralite", "uranium"]

let wood_types = ["minecraft:oak", "minecraft:spruce", "minecraft:birch", "minecraft:jungle", "minecraft:acacia", "minecraft:dark_oak", "minecraft:mangrove", "minecraft:cherry", "minecraft:crimson", "minecraft:warped"]

let unregistered_axes = []

/** Mod / registry guards — use instead of global.hasThermal (broken on server scripts). */
function modLoaded(modId) {
    return Platform.isLoaded(modId)
}

function itemOr(primary, fallback) {
    if (typeof primary === "string" && Item.exists(primary)) return primary
    if (typeof fallback === "string" && Item.exists(fallback)) return fallback
    if (fallback && String(fallback).charAt(0) === "#") return fallback
    return fallback || primary
}

function fluidOr(primary, fallback) {
    if (typeof primary === "string" && Fluid.exists(primary)) return primary
    if (typeof fallback === "string" && Fluid.exists(fallback)) return fallback
    return fallback || primary
}

function ifMod(modId, fn) {
    if (Platform.isLoaded(modId)) fn()
}

function ifItem(itemId, fn) {
    if (Item.exists(itemId)) fn()
}

function tagAddIfExists(event, tag, itemId) {
    if (Item.exists(itemId)) event.add(tag, itemId)
}

function firstExistingItem(ids) {
    for (let i = 0; i < ids.length; i++) {
        if (Item.exists(ids[i])) return ids[i]
    }
    return null
}

/** Create 1.21 deploying — Item.of(x, n) in the KubeJS API expands to n inputs (max 2). */
function createDeployingCount(event, output, heldItem, heldCount, tool) {
    let outJson = typeof output === "string" ? { id: output } : { id: output.id, count: output.count || 1 }
    let toolIng = Ingredient.of(tool)
    let toolJson = toolIng.isEmpty() ? { item: String(tool) } : toolIng.toJson()
    event.custom({
        type: "create:deploying",
        ingredients: [
            { item: heldItem, count: heldCount },
            toolJson
        ],
        results: [outJson]
    })
}

// helper for 3x3 shaped recipes with a center item
let donutCraft = (event, output, outer, inner) => {
    return event.shaped(output, [
        "AAA",
        "ABA",
        "AAA"
    ], {
        A: outer,
        B: inner
    })
}

let deployTool = (event, output, held, tool) => {
    let heldIng = Ingredient.of(held)
    let toolStr = String(tool)
    let toolIng
    if (toolStr.indexOf("kubejs:saws") >= 0) {
        toolIng = Ingredient.of("minecraft:iron_axe")
    } else {
        toolIng = Ingredient.of(tool)
        if (toolIng.isEmpty()) {
            toolIng = Ingredient.of("#c:tools/axes")
        }
        if (toolIng.isEmpty()) {
            toolIng = Ingredient.of("minecraft:iron_axe")
        }
    }
    let heldJson = heldIng.isEmpty() ? { "tag": String(held).replace(/^#/, "") } : heldIng.toJson()
    let toolJson = toolIng.toJson()
    event.custom({
        "type": "create:deploying",
        "ingredients": [heldJson, toolJson],
        "results": [{ "id": output }]
    })
}

/** Create 1.21 — tag strings in recipe helpers serialize as fluids; use raw JSON instead. */
let createCuttingTag = (event, results, tag, processingTime, id) => {
    let recipe = {
        "type": "create:cutting",
        "ingredients": [{ "tag": tag.replace(/^#/, "") }],
        "results": Array.isArray(results) ? results : [results],
        "processing_time": processingTime
    }
    if (id) {
        event.custom(recipe).id(id)
    } else {
        event.custom(recipe)
    }
}

let createFillingJson = (event, resultId, itemIngredient, fluidId, fluidAmount, id) => {
    let itemJson = typeof itemIngredient === "string"
        ? (itemIngredient.startsWith("#")
            ? { "tag": itemIngredient.replace(/^#/, "") }
            : { "item": itemIngredient })
        : itemIngredient
    let recipe = {
        "type": "create:filling",
        "ingredients": [
            itemJson,
            {
                "type": "neoforge:single",
                "amount": fluidAmount,
                "fluid": fluidId
            }
        ],
        "results": [{ "id": resultId }]
    }
    if (id) {
        event.custom(recipe).id(id)
    } else {
        event.custom(recipe)
    }
}

/**
 * Common helper function to register a "machine" recipe (Create Item Application or Stonecutting)
 * @param {ItemStackJS|string} machineItem
 * @param {RecipeEventJS} event
 * @param {ItemStackJS|string} outputIngredient
 * @param {ItemStackJS|string} inputIngredient
 */
let createMachine = (machineItem, event, outputIngredient, inputIngredient) => {
    console.log(`createMachine called with: machineItem=${machineItem}, outputIngredient=${outputIngredient}, inputIngredient=${inputIngredient}`)

    if (!event) {
        console.log("ERROR: event is null in createMachine")
        return
    }

    let machine = Ingredient.of(machineItem)
    let output = Item.of(outputIngredient)

    if (output.isEmpty()) {
        console.log(`ERROR: outputIngredient corresponds to empty item, skipping machine recipe`)
        return
    }

    let outputId = output.id; // KubeJS 6 standard
    if (!outputId) outputId = output.getId();

    try {
        event.remove({ output: outputId })
    } catch (e) {
        console.log(`Warn: could not remove recipes for ${outputId}: ${e}`)
    }

    if (inputIngredient) {
        let inputIng = Ingredient.of(inputIngredient)
        if (inputIng.isEmpty()) {
            console.log(`ERROR: inputIngredient is empty (${inputIngredient}), skipping machine recipe for ${outputId}`)
            return
        }

        let machineJson = machine.toJson()
        let inputJson = inputIng.toJson()
        if (!inputJson || (inputJson.item === "minecraft:air")) {
            console.log(`ERROR: inputIngredient serialized empty (${inputIngredient}), skipping machine recipe for ${outputId}`)
            return
        }

        let results;
        try {
            // Simplified results generation to avoid complex checks on ItemStack properties
            if (output.count > 1) {
                results = [
                    Item.of(output.id, 1).toJson(),
                    Item.of(output.id, output.count - 1).toJson()
                ]
            } else {
                results = [output.toJson()]
            }
        } catch (err) {
            results = [output.toJson()]
        }

        event.custom({
            "type": "create:item_application",
            "ingredients": [
                machineJson,
                inputJson
            ],
            "results": results
        })
    }
    else {
        event.stonecutting(output, machine)
    }
}

let andesiteMachine = (event, outputIngredient, inputIngredient) => {
    return createMachine("kubejs:andesite_machine", event, outputIngredient, inputIngredient)
}

let copperMachine = (event, outputIngredient, inputIngredient) => {
    return createMachine("kubejs:copper_machine", event, outputIngredient, inputIngredient)
}

let goldMachine = (event, outputIngredient, inputIngredient) => {
    return createMachine("kubejs:gold_machine", event, outputIngredient, inputIngredient)
}

let brassMachine = (event, outputIngredient, inputIngredient) => {
    return createMachine("kubejs:brass_machine", event, outputIngredient, inputIngredient)
}

let zincMachine = (event, outputIngredient, inputIngredient) => {
    return createMachine("kubejs:zinc_machine", event, outputIngredient, inputIngredient)
}

let leadMachine = (event, outputIngredient, inputIngredient) => {
    return createMachine("kubejs:lead_machine", event, outputIngredient, inputIngredient)
}

let steelMachine = (event, outputIngredient, inputIngredient) => {
    return createMachine("kubejs:steel_machine", event, outputIngredient, inputIngredient)
}

let constantanMachine = (event, outputIngredient, inputIngredient) => {
    return createMachine("kubejs:constantan_machine", event, outputIngredient, inputIngredient)
}

let invarMachine = (event, outputIngredient, inputIngredient) => {
    return constantanMachine(event, outputIngredient, inputIngredient)
}

let enderiumMachine = (event, outputIngredient, inputIngredient) => {
    return createMachine("kubejs:enderium_machine", event, outputIngredient, inputIngredient)
}

let fluixMachine = (event, outputIngredient, inputIngredient) => {
    return createMachine("ae2:controller", event, outputIngredient, inputIngredient)
}

let toThermalInputJson = (value) => {
    if (value == null) return {};
    let item = Item.of(value);
    if (!item.isEmpty()) {
        let json = Ingredient.of(item).toJson();
        if (item.count > 1) {
            return {
                value: json,
                count: item.count
            }
        }
        return json;
    }
    try {
        let fluid = Fluid.of(value);
        if (fluid && !fluid.isEmpty()) {
            return { fluid: fluid.id, amount: fluid.amount };
        }
    } catch (e) { }
    // Last resort, try as ingredient string
    try {
        return Ingredient.of(value).toJson();
    } catch (e) {
        return {};
    }
}

let toThermalOutputJson = (value) => {
    if (value == null) return {};
    let item = Item.of(value);
    if (!item.isEmpty()) {
        let out = {
            item: item.id,
            count: item.count
        }
        if (item.nbt) out.nbt = item.nbt.toString();
        if (typeof value === 'object' && value.chance) {
            out.chance = value.chance;
        }
        return out;
    }
    try {
        let fluid = Fluid.of(value);
        if (fluid && !fluid.isEmpty()) {
            return { fluid: fluid.id, amount: fluid.amount };
        }
    } catch (e) { }
    return {};
}

let addTreeOutput = (event, trunk, leaf, fluid) => {
    if (!Platform.isLoaded("thermal")) {
        return {
            id: () => {}
        }
    }
    return event.custom({
        type: "thermal:tree_extractor",
        trunk: {
            Name: trunk,
            Properties: {
                axis: "y"
            }
        },
        leaves: {
            Name: leaf,
            Properties: {
                persistent: "false"
            }
        },
        result: fluid ? toThermalOutputJson(fluid) : {
            fluid: "thermal:resin",
            amount: 25
        }
    })
}

/**
 * Creates smeltery casting recipes for nuggets, ingots, blocks, etc
 * Also creates a chilling recipe for the ingot
 * This helper function requires all the items and fluids involved to be tagged correctly with the nugget/ingot/block tags
 * @param {RecipeEventJS} event recipe event
 * @param {string} metalName the name of the metal to create casting recipes (ex: "forge:ingots/{metalName}")
 * @param {number} castingTime the time it takes to cast a block in a casting table (nugget and ingot casting times will be calculated based on that)
 */
let metalCasting = (event, metalName, castingTime) => {
    if (!Platform.isLoaded("tconstruct")) return
    let fluidTag = "forge:molten_" + metalName
    let blockTag = "forge:storage_blocks/" + metalName

    // block casting
    if (Ingredient.of(`#${blockTag}`).first && !Ingredient.of(`#${blockTag}`).first.isEmpty()) {
        event.custom({
            "type": "tconstruct:casting_basin",
            "fluid": {
                "tag": fluidTag,
                "amount": 810
            },
            "result": { "tag": blockTag },
            "cooling_time": castingTime
        }).id(`kubejs:smeltery/casting/metal/${metalName}/block`)
    }

    let castTypes = [
        { name: "gear", fluidCost: 360, cooldownMultiplier: 2 / 3 },
        { name: "ingot", fluidCost: 90, cooldownMultiplier: 1 / 3 },
        { name: "nugget", fluidCost: 10, cooldownMultiplier: 1 / 9 },
        { name: "plate", fluidCost: 90, cooldownMultiplier: 1 / 3 },
        { name: "rod", fluidCost: 45, cooldownMultiplier: 1 / (3 * Math.SQRT2) },
        { name: "wire", fluidCost: 45, cooldownMultiplier: 1 / (3 * Math.SQRT2) }
    ]

    // casting into casts
    castTypes.forEach(cast => {
        let tag = `forge:${cast.name}s/${metalName}`
        if (Ingredient.of(`#${tag}`).first && !Ingredient.of(`#${tag}`).first.isEmpty()) {
            event.custom({
                "type": "tconstruct:casting_table",
                "cast": {
                    "tag": `tconstruct:casts/multi_use/${cast.name}`
                },
                "fluid": {
                    "tag": fluidTag,
                    "amount": cast.fluidCost
                },
                "result": { "tag": tag },
                "cooling_time": Math.round(castingTime * cast.cooldownMultiplier)
            }).id(`kubejs:smeltery/casting/metal/${metalName}/${cast.name}_gold_cast`)

            event.custom({
                "type": "tconstruct:casting_table",
                "cast": {
                    "tag": `tconstruct:casts/single_use/${cast.name}`
                },
                "cast_consumed": true,
                "fluid": {
                    "tag": fluidTag,
                    "amount": cast.fluidCost
                },
                "result": { "tag": tag },
                "cooling_time": Math.round(castingTime * cast.cooldownMultiplier)
            }).id(`kubejs:smeltery/casting/metal/${metalName}/${cast.name}_sand_cast`)
        }
    })

    // ingot chilling
    if (Platform.isLoaded("thermal") && Item.exists("thermal:chiller_ingot_cast") && Ingredient.of(`#forge:ingots/${metalName}`).first && !Ingredient.of(`#forge:ingots/${metalName}`).first.isEmpty()) {
        event.custom({
            "type": "thermal:chiller",
            "ingredients": [{
                "fluid_tag": fluidTag,
                "amount": 90
            }, {
                "item": "thermal:chiller_ingot_cast"
            }],
            "result": [{
                "item": getPreferredItemFromTag("forge:ingots/" + metalName),
                "count": 1
            }],
            "energy": 5000
        }).id(`kubejs:crucible/kubejs/smeltery/casting/metal/${metalName}/ingot_gold_cast`)
    }
}
/**
 * Creates smeltery melting recipes for nuggets, ingots, blocks, etc
 * Also creates a magma crucible recipe for the ingot
 * @param {RecipeEventJS} event recipe event
 * @param {string} metalName the name of the metal to create melting recipes for
 * @param {number} meltingTime the time it takes to melt a block in the smeltery
 * @param {number} temperature the temperature required to melt a block in the smeltery
 */
let metalMelting = (event, metalName, outputFluid, meltingTime, temperature) => {
    if (!Platform.isLoaded("tconstruct")) return
    let blockTag = "forge:storage_blocks/" + metalName

    // block melting
    if (Ingredient.of(`#${blockTag}`).first && !Ingredient.of(`#${blockTag}`).first.isEmpty()) {
        event.custom({
            "type": "tconstruct:melting",
            "ingredient": { "tag": blockTag },
            "result": {
                "fluid": outputFluid,
                "amount": 810
            },
            "temperature": temperature,
            "time": meltingTime
        }).id(`kubejs:smeltery/melting/metal/${metalName}/block`)
    }

    let castTypes = [
        { name: "coin", fluidAmount: 30, timeMultiplier: 1 / (3 * Math.sqrt(3)) },
        { name: "gear", fluidAmount: 360, timeMultiplier: 2 / 3 },
        { name: "ingot", fluidAmount: 90, timeMultiplier: 1 / 3 },
        { name: "nugget", fluidAmount: 10, timeMultiplier: 1 / 9 },
        { name: "plate", fluidAmount: 90, timeMultiplier: 1 / 3 },
        { name: "rod", fluidAmount: 45, timeMultiplier: 1 / (3 * Math.SQRT2) },
        { name: "wire", fluidAmount: 45, timeMultiplier: 1 / (3 * Math.SQRT2) }
    ]

    // melting cast shapes
    castTypes.forEach(cast => {
        let tag = `forge:${cast.name}s/${metalName}`
        if (Ingredient.of(`#${tag}`).first && !Ingredient.of(`#${tag}`).first.isEmpty()) {
            event.custom({
                "type": "tconstruct:melting",
                "ingredient": { "tag": tag },
                "result": {
                    "fluid": outputFluid,
                    "amount": cast.fluidAmount
                },
                "temperature": temperature,
                "time": meltingTime * cast.timeMultiplier
            }).id(`kubejs:smeltery/melting/metal/${metalName}/${cast.name}`)
        }
    })

    // ingot crucible melting
    if (Platform.isLoaded("thermal") && Ingredient.of(`#forge:ingots/${metalName}`).first && !Ingredient.of(`#forge:ingots/${metalName}`).first.isEmpty()) {
        event.custom({
            type: "thermal:crucible",
            ingredient: {
                "tag": `forge:ingots/${metalName}`
            },
            result: [{
                fluid: outputFluid,
                amount: 90
            }],
            energy: Math.round(meltingTime / 3) * 50
        }).id(`kubejs:crucible/kubejs/smeltery/melting/metal/${metalName}/ingot`)
    }
}

/** Used in datapack events instead of recipe events */
let addChiselingRecipe = (event, id, items, overwrite) => {
    const json = {
        type: "rechiseled:chiseling",
        entries: [],
        overwrite: !!overwrite
    }
    items.forEach(item => {
        json.entries.push({
            item: item
        })
    })
    event.addJson(id, json)
}

/** Used in a datapack event to remove a configured feature by its resource location */
let removeFeature = function (event, featureName) {
    featureName = featureName.split(":")
    let namespace = featureName[0]
    let identifier = featureName[1]
    event.json(`${namespace}:worldgen/configured_feature/${identifier}`, {
        "type": "minecraft:no_op",
        "config": {}
    })
}

/** Used in a datapack event to add ore generation for an ore to the overworld
 * This function only works for ores with both a stone and deepslate variant
*/
let addOregenOverworld = function (event, featureName, blockName, heightType, heightMin, heightMax, veinCount, veinSize, discardChanceOnAirExposure, biomeTag) {
    featureName = featureName.split(":")
    let namespace = featureName[0]
    let identifier = featureName[1]

    blockName = blockName.split(":")
    let blockNamespace = blockName[0]
    let blockIdentifier = blockName[1]
    let deepslateIdentifier
    if (blockNamespace === "nuclearcraftneohaul") {
        deepslateIdentifier = blockIdentifier.replace(/_ore$/, "_deepslate_ore")
    } else if (blockIdentifier.startsWith("ore_")) {
        deepslateIdentifier = `deepslate_${blockIdentifier}`
    } else {
        deepslateIdentifier = `deepslate_${blockIdentifier}`
    }

    // A2: shorthand for <1 vein per chunk
    let rarityFilter = 1
    if (veinCount < 1) {
        rarityFilter = Math.max(1, Math.floor(1 / veinCount))
    }

    event.json(`${namespace}:worldgen/configured_feature/${identifier}`, {
        "type": "minecraft:ore",
        "config": {
            "discard_chance_on_air_exposure": discardChanceOnAirExposure,
            "size": veinSize,
            "targets": [
                {
                    "state": { "Name": `${blockNamespace}:${blockIdentifier}` },
                    "target": { "predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables" }
                },
                {
                    "state": { "Name": `${blockNamespace}:${deepslateIdentifier}` },
                    "target": { "predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables" }
                }
            ]
        }
    })
    let minInclusive = { "absolute": heightMin }
    let maxInclusive = { "absolute": heightMax }
    // Deepslate band: above_bottom reads clearer in EMI; sky-island crust uses absolute Y.
    if (heightMin < -64) {
        minInclusive = { "above_bottom": heightMin + 64 }
        maxInclusive = { "above_bottom": heightMax + 64 }
    }

    if (rarityFilter == 1) {
        event.json(`${namespace}:worldgen/placed_feature/${identifier}`, {
            "feature": `${namespace}:${identifier}`,
            "placement": [
                { "type": "minecraft:count", "count": veinCount },
                { "type": "minecraft:in_square" },
                {
                    "type": "minecraft:height_range",
                    "height": {
                        "type": heightType,
                        "min_inclusive": minInclusive,
                        "max_inclusive": maxInclusive
                    }
                },
                { "type": "minecraft:biome" }
            ]
        })
    }
    else {
        event.json(`${namespace}:worldgen/placed_feature/${identifier}`, {
            "feature": `${namespace}:${identifier}`,
            "placement": [
                { "type": "minecraft:rarity_filter", "chance": rarityFilter },
                { "type": "minecraft:in_square" },
                {
                    "type": "minecraft:height_range",
                    "height": {
                        "type": heightType,
                        "min_inclusive": minInclusive,
                        "max_inclusive": maxInclusive
                    }
                },
                { "type": "minecraft:biome" }
            ]
        })
    }


    event.json(`${namespace}:neoforge/biome_modifier/${identifier}`, {
        "type": "neoforge:add_features",
        "biomes": biomeTag,
        "features": `${namespace}:${identifier}`,
        "step": "underground_ores"
    })
}

/**
 * Gets the prefered item from a tag. Useful for porting Mantle recipes that use tags as outputs.
 * @param {string} tag Don't include a hashtag in the tag name
 */
var getPreferredItemFromTag = (tag) => {
    const preferredMods = ["minecraft", "kubejs", "nuclearcraftneohaul", "create", "createdeco", "createaddition", "thermal", "tfmg", "tconstruct", "immersiveengineering", "ae2", "createaddition", "botania", "ad_astra", "scguns", "embers"]
    const tagItems = Ingredient.of("#" + tag).itemIds;
    for (let i = 0; i < preferredMods.length; ++i) {
        let modId = preferredMods[i];
        for (let j = 0; j < tagItems.length; ++j) {
            let itemId = tagItems[j];
            if (itemId.split(":")[0] === modId && Item.exists(itemId) && itemId !== "minecraft:air" && itemId !== "minecraft:barrier") {
                return itemId;
            }
        }
    }
    for (let j = 0; j < tagItems.length; ++j) {
        let itemId = tagItems[j];
        if (Item.exists(itemId) && itemId !== "minecraft:air" && itemId !== "minecraft:barrier") {
            return itemId;
        }
    }
    return "minecraft:air";
}

const noopThermalBuilder = {
    id() { return this },
    energy() { return this }
}

/** No-op thermal recipe builder when Thermal is not loaded (Rhino has no Proxy). */
function getThermalRecipes(event) {
    if (Platform.isLoaded("thermal")) {
        return event.recipes.thermal
    }
    const noop = () => noopThermalBuilder
    return {
        pulverizer: noop,
        smelter: noop,
        crucible: noop,
        chiller: noop,
        pyrolyzer: noop,
        bottler: noop,
        centrifuge: noop,
        refinery: noop
    }
}

/** Nuclearcraft Neohaul recipe helpers (1.21 JSON format). Rhino-safe — no shorthand props, spread, or ?. */
function ncModifiers(power, radiation, time) {
    return {
        powerModifier: power != null ? power : 1.0,
        radiation: radiation != null ? radiation : 0.0,
        timeModifier: time != null ? time : 1.0
    }
}

function ncApplyModifiers(recipe, mods) {
    let m = ncModifiers(mods && mods.power, mods && mods.radiation, mods && mods.time)
    recipe.powerModifier = m.powerModifier
    recipe.radiation = m.radiation
    recipe.timeModifier = m.timeModifier
    return recipe
}

function ncItemIng(entry) {
    if (typeof entry === "string") {
        return { count: 1, ingredient: entry.startsWith("#") ? { tag: entry.slice(1) } : { item: entry } }
    }
    let count = entry.count != null ? entry.count : 1
    if (entry.tag) return { count: count, ingredient: { tag: entry.tag } }
    if (entry.item) return { count: count, ingredient: { item: entry.item } }
    return { count: 1, ingredient: { item: entry } }
}

function ncFluidIng(entry) {
    let amount = entry.amount
    if (entry.fluid) return { amount: amount, ingredient: { fluid: entry.fluid } }
    if (entry.tag) return { amount: amount, ingredient: { tag: entry.tag } }
    return { amount: amount, ingredient: { fluid: entry } }
}

function ncItemProd(entry) {
    let count = entry.count != null ? entry.count : (entry.amount != null ? entry.amount : 1)
    let item = entry.item != null ? entry.item : entry
    return { count: count, ingredient: { item: item } }
}

function ncManufactoryRecipe(inputs, outputs, mods) {
    return ncApplyModifiers({
        type: "nuclearcraftneohaul:manufactory_recipe",
        itemIngredients: inputs.map(ncItemIng),
        itemProducts: outputs.map(ncItemProd)
    }, mods)
}

function ncMelterRecipe(itemInputs, fluidOutputs, mods) {
    return ncApplyModifiers({
        type: "nuclearcraftneohaul:melter_recipe",
        itemIngredients: itemInputs.map(ncItemIng),
        fluidProducts: fluidOutputs.map(ncFluidIng)
    }, mods)
}

function ncCrystallizerRecipe(fluidInputs, itemOutputs, mods) {
    return ncApplyModifiers({
        type: "nuclearcraftneohaul:crystallizer_recipe",
        fluidIngredients: fluidInputs.map(ncFluidIng),
        itemProducts: itemOutputs.map(ncItemProd)
    }, mods)
}

function ncIngotFormerRecipe(fluidInputs, itemOutputs, mods) {
    return ncApplyModifiers({
        type: "nuclearcraftneohaul:ingot_former_recipe",
        fluidIngredients: fluidInputs.map(ncFluidIng),
        itemProducts: itemOutputs.map(ncItemProd)
    }, mods)
}

function ncInfuserRecipe(fluidInputs, itemInputs, itemOutputs, mods) {
    return ncApplyModifiers({
        type: "nuclearcraftneohaul:infuser_recipe",
        fluidIngredients: fluidInputs.map(ncFluidIng),
        itemIngredients: itemInputs.map(ncItemIng),
        itemProducts: itemOutputs.map(ncItemProd)
    }, mods)
}

function ncEnricherRecipe(itemInputs, fluidInputs, fluidOutputs, mods) {
    return ncApplyModifiers({
        type: "nuclearcraftneohaul:enricher_recipe",
        itemIngredients: itemInputs.map(ncItemIng),
        fluidIngredients: fluidInputs.map(ncFluidIng),
        fluidProducts: fluidOutputs.map(ncFluidIng)
    }, mods)
}

function ncAssemblerRecipe(itemInputs, itemOutputs, mods) {
    return ncApplyModifiers({
        type: "nuclearcraftneohaul:assembler_recipe",
        itemIngredients: itemInputs.map(ncItemIng),
        itemProducts: itemOutputs.map(ncItemProd)
    }, mods)
}

function ncAlloyFurnaceRecipe(itemInputs, itemOutputs, mods) {
    return ncApplyModifiers({
        type: "nuclearcraftneohaul:alloy_furnace_recipe",
        itemIngredients: itemInputs.map(ncItemIng),
        itemProducts: itemOutputs.map(ncItemProd)
    }, mods)
}

/** KubeJS 1.21 Create chance output (replaces Item.of(...).withChance). */
function chanceItem(item, chance) {
    if (chance == null || chance >= 1) {
        return Item.of(item)
    }
    return CreateItem.of(item, chance)
}

function blockLootTableId(blockId) {
    const sep = blockId.indexOf(":")
    return blockId.slice(0, sep) + ":blocks/" + blockId.slice(sep + 1)
}

function replaceBlockLootFromJson(event, blockId, json) {
    const tableId = blockLootTableId(blockId)
    if (event.hasLootTable(tableId)) {
        event.getLootTable(tableId).clear()
    } else {
        event.create(tableId, LootType.BLOCK)
    }
    const table = event.getLootTable(tableId)
    json.pools.forEach(poolData => {
        table.createPool(pool => {
            let rollCount = poolData.rolls != null ? poolData.rolls : 1
            pool.rolls(rollCount)
            poolData.entries.forEach(entry => pool.addCustomEntry(entry))
        })
    })
}
