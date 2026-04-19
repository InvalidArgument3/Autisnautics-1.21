// priority: 100

let colours = ["white", "orange", "magenta", "light_blue", "lime", "pink", "purple", "light_gray", "gray", "cyan", "brown", "green", "blue", "red", "black", "yellow"]
let native_metals = ["iron", "zinc", "lead", "copper", "nickel", "gold",
    // A2: more metals
    "tin", "aluminum", "boron", "calorite", "cobalt", "desh", "lithium", "magnesium", "ostrum", "platinum", "silver", "thorium", "anthralite", "uranium"]

let wood_types = ["minecraft:oak", "minecraft:spruce", "minecraft:birch", "minecraft:jungle", "minecraft:acacia", "minecraft:dark_oak", "minecraft:mangrove", "minecraft:cherry", "minecraft:crimson", "minecraft:warped"]

let unregistered_axes = []
global.hasThermal = Platform.isLoaded("thermal")

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


let invarMachine = (event, outputIngredient, inputIngredient) => {
    return createMachine("thermal:machine_frame", event, outputIngredient, inputIngredient)
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
    if (!global.hasThermal) {
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
    if (global.hasThermal && Ingredient.of(`#forge:ingots/${metalName}`).first && !Ingredient.of(`#forge:ingots/${metalName}`).first.isEmpty()) {
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
    if (global.hasThermal && Ingredient.of(`#forge:ingots/${metalName}`).first && !Ingredient.of(`#forge:ingots/${metalName}`).first.isEmpty()) {
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
    event.addJson(`${namespace}:worldgen/configured_feature/${identifier}`, {
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

    // A2: shorthand for <1 vein per chunk
    let rarityFilter = 1
    if (veinCount < 1) {
        rarityFilter = Math.max(1, Math.floor(1 / veinCount))
    }

    event.addJson(`${namespace}:worldgen/configured_feature/${identifier}`, {
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
                    "state": { "Name": `${blockNamespace}:deepslate_${blockIdentifier}` },
                    "target": { "predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables" }
                }
            ]
        }
    })
    let minInclusive = { "absolute": heightMin }
    let maxInclusive = { "absolute": heightMax }
    // EMI Oregen will display more useful information if we change to using Above Bottom where it makes sense to
    if (heightMin < -64) {
        minInclusive = { "above_bottom": heightMin + 64 }
        maxInclusive = { "above_bottom": heightMax + 64 }
    } else if (heightMax > 512) {
        minInclusive = { "below_top": -(heightMin - 512) }
        maxInclusive = { "below_top": -(heightMax - 512) }
    }

    if (rarityFilter == 1) {
        event.addJson(`${namespace}:worldgen/placed_feature/${identifier}`, {
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
        event.addJson(`${namespace}:worldgen/placed_feature/${identifier}`, {
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


    event.addJson(`${namespace}:forge/biome_modifier/${identifier}`, {
        "type": "forge:add_features",
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
    const preferredMods = ["minecraft", "kubejs", "create", "createdeco", "createaddition", "thermal", "tfmg", "tconstruct", "immersiveengineering", "ae2", "createaddition", "botania", "ad_astra", "scguns", "nuclearcraft", "embers"]
    const tagItems = Ingredient.of("#" + tag).itemIds;
    for (let i = 0; i < preferredMods.length; ++i) {
        let modId = preferredMods[i];
        for (let j = 0; j < tagItems.length; ++j) {
            let itemId = tagItems[j];
            if (itemId.split(":")[0] === modId) {
                return itemId;
            }
        }
    }
    if (tagItems.length > 0) {
        return tagItems[0];
    }
    return "minecraft:air";
}
