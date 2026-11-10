// priority: 2
// Chapter 4 semiconductor line. States are items, not custom data, so Create filters can route them.

ServerEvents.recipes(event => {
    let storage = Platform.isLoaded("refinedstorage") ? "refinedstorage" : (Platform.isLoaded("ae2") ? "ae2" : "")
    if (!storage || !Platform.isLoaded("theurgy") || !Platform.isLoaded("create_enchantment_industry")) return

    // RS raw tiers: basic, improved, advanced. AE2 has no raw items. Printed calculation, logic, and engineering fill those slots.
    let silicon = storage == "refinedstorage" ? "refinedstorage:silicon" : "ae2:silicon"
    let tiers = storage == "refinedstorage"
        ? {
            basic: "refinedstorage:raw_basic_processor",
            improved: "refinedstorage:raw_improved_processor",
            advanced: "refinedstorage:raw_advanced_processor"
        }
        : {
            basic: "ae2:printed_calculation_processor",
            improved: "ae2:printed_logic_processor",
            advanced: "ae2:printed_engineering_processor"
        }

    if (storage == "refinedstorage") {
        event.remove({ id: "refinedstorage:raw_basic_processor" })
        event.remove({ id: "refinedstorage:raw_improved_processor" })
        event.remove({ id: "refinedstorage:raw_advanced_processor" })
    } else {
        event.remove({ id: "ae2:inscriber/calculation_processor_print" })
        event.remove({ id: "ae2:inscriber/logic_processor_print" })
        event.remove({ id: "ae2:inscriber/engineering_processor_print" })
    }

    event.recipes.create.cutting(Item.of("kubejs:silicon_wafer", 6), silicon).id("kubejs:wafer_cut")

    event.recipes.create.mixing(Item.of("kubejs:spirit_of_lithography", 8), [
        Ingredient.of("#theurgy:alchemical_sulfurs"),
        "theurgy:mercury_shard",
        "minecraft:redstone",
        Fluid.of("create_enchantment_industry:experience", 250)
    ]).heated().id("kubejs:spirit_of_lithography")

    event.recipes.create.deploying("kubejs:latent_wafer", ["kubejs:silicon_wafer", "kubejs:spirit_of_lithography"]).id("kubejs:wafer_expose")
    event.recipes.create.deploying("kubejs:ruined_wafer", ["kubejs:latent_wafer", "kubejs:spirit_of_lithography"]).id("kubejs:wafer_overexpose")

    event.recipes.create.mixing("kubejs:annealed_wafer", ["kubejs:latent_wafer"]).superheated().id("kubejs:wafer_anneal")
    event.recipes.create.mixing("kubejs:ruined_wafer", ["kubejs:silicon_wafer"]).superheated().id("kubejs:wafer_anneal_blank")

    let solvents = ["theurgy:sal_ammoniac"]
    if (Platform.isLoaded("nuclearcraftneohaul")) solvents.push("nuclearcraftneohaul:sulfuric_acid")
    solvents.forEach(fluid => {
        let key = fluid.split(":")[1]
        event.recipes.create.filling("kubejs:washed_wafer", ["kubejs:annealed_wafer", Fluid.of(fluid, 250)]).id("kubejs:wafer_wash_" + key)
        event.recipes.create.filling("kubejs:ruined_wafer", ["kubejs:silicon_wafer", Fluid.of(fluid, 250)]).id("kubejs:wafer_etch_blank_" + key)
        event.recipes.create.filling("kubejs:ruined_wafer", ["kubejs:latent_wafer", Fluid.of(fluid, 250)]).id("kubejs:wafer_etch_exposed_" + key)
    })

    event.recipes.create.deploying("kubejs:polished_wafer", ["kubejs:washed_wafer", "create:sand_paper"]).id("kubejs:wafer_polish")

    event.recipes.create.mechanical_crafting("kubejs:insulated_die", [
        "WWW",
        " D "
    ], {
        W: "kubejs:washed_wafer",
        D: "minecraft:diamond"
    }).id("kubejs:wafer_stack")

    event.custom({
        type: "create:haunting",
        ingredients: [{ item: "kubejs:ruined_wafer" }],
        results: [{ id: "kubejs:silicon_wafer" }]
    }).id("kubejs:wafer_exorcism")

    if (Platform.isLoaded("nuclearcraftneohaul")) {
        event.recipes.create.filling("kubejs:silicon_wafer", ["kubejs:ruined_wafer", Fluid.of("nuclearcraftneohaul:soul", 250)]).id("kubejs:wafer_soul_bath")
    }

    let bins = [
        { tier: "advanced", id: tiers.advanced, chance: 4 },
        { tier: "improved", id: tiers.improved, chance: 18 },
        { tier: "basic", id: tiers.basic, chance: 48 },
        { id: "kubejs:dram", chance: 16 }
    ]
    if (Platform.isLoaded("psi")) bins.push({ id: "psi:cad_core_basic", chance: 8 })
    if (Platform.isLoaded("farmersdelight")) bins.push({ id: "farmersdelight:cooked_bacon", chance: 6 })

    let tune = table => bins.map(bin => {
        if (bin.tier && table[bin.tier] != null) return { id: bin.id, chance: table[bin.tier] }
        if (bin.id == "kubejs:dram") return { id: bin.id, chance: table.dram }
        return { id: bin.id, chance: Math.max(2, Math.floor(bin.chance / 2)) }
    })
    let better = tune({ advanced: 22, improved: 28, basic: 30, dram: 8 })
    let jackpot = tune({ advanced: 40, improved: 28, basic: 16, dram: 6 })

    let dice = (recipeId, input, results) => {
        event.custom({
            type: "create:sequenced_assembly",
            ingredient: { item: input },
            transitional_item: { id: "kubejs:incomplete_wafer_die" },
            loops: 1,
            results: results,
            sequence: [{
                type: "create:pressing",
                ingredients: [{ item: "kubejs:incomplete_wafer_die" }],
                results: [{ id: "kubejs:incomplete_wafer_die" }]
            }]
        }).id(recipeId)
    }
    dice("kubejs:wafer_dice", "kubejs:washed_wafer", bins)
    dice("kubejs:wafer_dice_polished", "kubejs:polished_wafer", better)
    dice("kubejs:wafer_dice_stacked", "kubejs:insulated_die", jackpot)

    event.shaped("kubejs:wafer_gate", [
        "QSQ",
        "SCS",
        "QSQ"
    ], {
        Q: "minecraft:quartz_block",
        S: silicon,
        C: "minecraft:crying_obsidian"
    }).id("kubejs:wafer_gate")
})
