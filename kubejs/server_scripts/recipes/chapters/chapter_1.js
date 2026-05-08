// priority: 2

ServerEvents.recipes(event => {
    // --- Chapter 1
    let transitional
    // -- Wood slab sawing line
    // - Remove conflicting saw recipes
    // Create: Sawing planks into various wooden blocks besides slabs
    event.remove({ id: /^create:cutting(?=.*_planks_to_)(?!.*slab$).*$/, input: /.*_planks$/ })
    // Chisel: Stonecutting planks into chisel blocks
    event.remove({ type: "minecraft:stonecutting", id: /chisel:planks_/})
    // Youkai's Feasts: Stonecutting planks into steamer_lid
    // Replacement: add a stick to a pressure plate
    event.remove({ id: "youkaisfeasts:steamer_lid_from_planks_stonecutting" })
    event.shapeless("youkaisfeasts:steamer_lid", ["minecraft:stick", "#minecraft:wooden_pressure_plates"])
    // Cuisine Delight: Stonecutting planks into plates
    // Replacement: cut a slab again to make 8 plates, and remove conflicting powergrid circuit board recipe
    event.remove({ id: "cuisinedelight:plate_from_planks_stonecutting" })
    event.recipes.create.cutting(Item.of("cuisinedelight:plate", 8), Ingredient.of("#minecraft:wooden_slabs")).processingTime(150).id(`kubejs:cutting/wooden_slab_to_plates`)
    event.remove({ id: "powergrid:cutting/empty_circuit_slabs" })
    // createbigcannons: all moulds are sawed from logs in one recipe, which has been widely regarded as a bad move
    let mouldTypes = ["very_small", "small", "medium", "large", "very_large", "cannon_end", "sliding_breech", "screw_breech", "autocannon_breech", "autocannon_recoil_spring", "autocannon_barrel"]
    let removeMould = (type) => {
        let originalMould = "createbigcannons:" + type + "_cast_mould"
        // if (Item.exists(originalMould)) {
        event.remove({ output: originalMould })
        // }
    }
    mouldTypes.forEach(removeMould)
    // all moulds now start from the very large cast mould, by deploying a chisel on any log
    event.recipes.create.deploying("createbigcannons:very_large_cast_mould", [Ingredient.of("#minecraft:logs"), Ingredient.of("#forge:chisels")])
    // smaller moulds are made by chiseling the very large mould down smaller and smaller
    event.recipes.create.deploying("createbigcannons:large_cast_mould", ["createbigcannons:very_large_cast_mould", Ingredient.of("#forge:chisels")])
    event.recipes.create.deploying("createbigcannons:medium_cast_mould", ["createbigcannons:large_cast_mould", Ingredient.of("#forge:chisels")])
    event.recipes.create.deploying("createbigcannons:small_cast_mould", ["createbigcannons:medium_cast_mould", Ingredient.of("#forge:chisels")])
    event.recipes.create.deploying("createbigcannons:very_small_cast_mould", ["createbigcannons:small_cast_mould", Ingredient.of("#forge:chisels")])
    event.recipes.create.deploying("createbigcannons:autocannon_breech_cast_mould", ["createbigcannons:very_small_cast_mould", Ingredient.of("#forge:chisels")])
    event.recipes.create.deploying("createbigcannons:autocannon_recoil_spring_cast_mould", ["createbigcannons:autocannon_breech_cast_mould", Ingredient.of("#forge:chisels")])
    event.recipes.create.deploying("createbigcannons:autocannon_barrel_cast_mould", ["createbigcannons:autocannon_recoil_spring_cast_mould", Ingredient.of("#forge:chisels")])
    // the other moulds branch off from the generic ones by chiseling or hammering
    event.recipes.create.deploying("createbigcannons:sliding_breech_cast_mould", ["createbigcannons:large_cast_mould", "createdieselgenerators:hammer"])
    event.recipes.create.deploying("createbigcannons:cannon_end_cast_mould", ["createbigcannons:medium_cast_mould", "createdieselgenerators:hammer"])
    event.recipes.create.deploying("createbigcannons:screw_breech_cast_mould", ["createbigcannons:cannon_end_cast_mould", Ingredient.of("#forge:chisels")])
    // - Compat recipes
    event.recipes.create.cutting(Item.of("quark:ancient_planks_slab", 2), Ingredient.of("quark:ancient_planks")).processingTime(150)
    event.recipes.create.cutting(Item.of("quark:azalea_planks_slab", 2), Ingredient.of("quark:azalea_planks")).processingTime(150)
    event.recipes.create.cutting(Item.of("quark:blossom_planks_slab", 2), Ingredient.of("quark:blossom_planks")).processingTime(150)

    // -- Andesite production
    // - Remove vanilla meme stone recipes
    event.remove({ id: "minecraft:diorite" })
    event.remove({ id: "minecraft:andesite" })
    event.remove({ id: "minecraft:granite" })
    // - Remove create meme stone recipes
    event.remove({ id: "create:compacting/andesite_from_flint" })
    event.remove({ id: "create:compacting/diorite_from_flint" })
    event.remove({ id: "create:compacting/granite_from_flint" })

    // -- Algal Blend - adding minecraft:seagrass as a valid ingredient
    event.replaceInput({ id: "architects_palette:algal_blend" }, "minecraft:kelp", ["minecraft:kelp", "minecraft:seagrass"])

    // -- Andesite Alloy
    // - Remove other recipes
    event.remove({ id: "create:crafting/materials/andesite_alloy" })
    event.remove({ id: "create:crafting/materials/andesite_alloy_from_zinc" })
    event.remove({ id: "create:mixing/andesite_alloy" })
    event.remove({ id: "create:mixing/andesite_alloy_from_zinc" })
    event.remove({ id: "blazinghot:mixing/molten_andesite" })
    // - Add new recipe
    event.recipes.create.mixing(Item.of("create:andesite_alloy", 2), ["architects_palette:algal_brick", "minecraft:andesite"])

    // -- Kinetic Mechanism
    // - createdieselgenerators:hammer simplified recipe
    event.remove({ id: "createdieselgenerators:crafting/hammer" })
    event.shaped("createdieselgenerators:hammer", [
        " I ",
        " SI",
        "S  "
    ], {
        I: "minecraft:iron_ingot",
        S: "#c:rods/wooden"
    })
    event.shaped("createdieselgenerators:hammer", [
        " I ",
        "IS ",
        "  S"
    ], {
        I: "minecraft:iron_ingot",
        S: "#c:rods/wooden"
    })
    // - Mechanism Deploying
    transitional = "kubejs:incomplete_kinetic_mechanism"
    event.recipes.create.sequenced_assembly([
        "kubejs:kinetic_mechanism",
    ], Ingredient.of("#minecraft:wooden_slabs"), [
        event.recipes.create.deploying(transitional, [transitional, "create:andesite_alloy"]),
        event.recipes.create.deploying(transitional, [transitional, "create:andesite_alloy"]),
        event.recipes.create.deploying(transitional, [transitional, "createdieselgenerators:hammer"])
    ]).transitionalItem(transitional)
        .loops(1)
        .id("kubejs:kinetic_mechanism")
    // Manual recipe
    event.shapeless("kubejs:kinetic_mechanism", ["createdieselgenerators:hammer", "create:cogwheel", "create:andesite_alloy", "#c:stripped_logs"]).id("kubejs:kinetic_mechanism_manual_only")

    // -- Andesite Machine
    donutCraft(event, "kubejs:andesite_machine", "create:andesite_casing", "kubejs:kinetic_mechanism")
    // - Usage
    // Create
    andesiteMachine(event, Item.of("create:encased_fan", 1), "create:propeller")
    andesiteMachine(event, Item.of("create:mechanical_press", 1), "minecraft:iron_block")
    andesiteMachine(event, Item.of("create:mechanical_mixer", 1), "create:whisk")
    andesiteMachine(event, Item.of("create:mechanical_drill", 1), "kubejs:drill_head")
    andesiteMachine(event, Item.of("create:mechanical_saw", 1), "kubejs:saw_blade")
    andesiteMachine(event, Item.of("create:deployer", 1), "create:brass_hand")
    if (Platform.isLoaded("createdeco")) {
        andesiteMachine(event, Item.of("create:mechanical_roller", 1), "createdeco:andesite_hull")
    }
    else {
        andesiteMachine(event, Item.of("create:mechanical_roller", 1), "create:andesite_alloy_block")
    }
    andesiteMachine(event, Item.of("create:portable_storage_interface", 2))
    andesiteMachine(event, Item.of("create:andesite_funnel", 4))
    andesiteMachine(event, Item.of("create:andesite_tunnel", 4))
    andesiteMachine(event, Item.of("create:mechanical_harvester", 2))
    andesiteMachine(event, Item.of("create:mechanical_plough", 2))
    andesiteMachine(event, Item.of("create:contraption_controls", 1))
    // Simulated/Aeronautics
    andesiteMachine(event, Item.of("simulated:physics_assembler", 1), "minecraft:lever")
    andesiteMachine(event, Item.of("simulated:steering_wheel", 1), "create:large_cogwheel")
    if (Platform.isLoaded("immersive_aircraft")) { 
        andesiteMachine(event, Item.of("aeronautics:andesite_propeller", 1), "immersive_aircraft:propeller") 
        // convenience handcraft recipe for IA large propeller - same cost
        event.shaped("immersive_aircraft:propeller", [
            "PPP",
            "PAP",
            "PPP"
        ], {
        A: "create:andesite_alloy",
        P: "create:iron_sheet"
        })
    }
    else { 
        andesiteMachine(event, Item.of("simulated:andesite_propeller", 1), "create:encased_fan") 
    }
    
    // AE2
    andesiteMachine(event, Item.of("ae2:meteorite_compass", 1), "minecraft:compass")
    andesiteMachine(event, Item.of("ae2:charger", 1), "ae2:certus_quartz_crystal")
    // createaddition
    if (Platform.isLoaded("createaddition")) { andesiteMachine(event, Item.of("createaddition:rolling_mill", 1), "create:shaft") }

    // -- Parts for Chapter 1 factory machines
    // - Deployer: Brass Hand -> Gold Hand
    event.replaceInput({ id: "create:crafting/kinetics/brass_hand" }, "#c:plates/brass", "create:golden_sheet")
    // - Mechanical Saw: kubejs sawblade
    event.shaped("kubejs:saw_blade", [
        "NPN",
        "PLP",
        "NPN"
    ], {
        N: "minecraft:iron_nugget",
        P: "create:iron_sheet",
        L: "immersiveengineering:ingot_lead"
    })
    // - Mechanical Drill: kubejs drill head
    event.shaped("kubejs:drill_head", [
        "NN ",
        "NLP",
        " PL"
    ], {
        N: "minecraft:iron_nugget",
        P: "create:iron_sheet",
        L: "immersiveengineering:ingot_lead"
    })
})
