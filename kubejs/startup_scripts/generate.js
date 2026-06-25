// priority: 0

const BlockStateProperties = Java.loadClass("net.minecraft.world.level.block.state.properties.BlockStateProperties");

const applyItemModel = (builder, modelPath) => {
    if (builder.modelJson) {
        return builder.modelJson({ parent: modelPath })
    }
    if (builder.parentModel) {
        return builder.parentModel(modelPath)
    }
    return builder
}
// deprecated
const applyBlockModel = (builder, modelPath) => {
    if (builder.model) {
        return builder.model(modelPath)
    }
    if (builder.blockModel) {
        return builder.blockModel(modelPath)
    }
    return builder
}

// Textures must use the 'cabin' namespace to avoid a bug involving kubejs loading textures from resource packs.
// Textures must also be stored in a resource pack since the kubejs assets folder cannot be overridden using resource packs for whatever reason
StartupEvents.registry("item", event => {
    event.create("alchemical_laser").parentModel("cabin:item/alchemical_laser").displayName("Alchemical Laser (Ponder Entry)").unstackable()

    let types = ["Certus", "Fluix"]
    types.forEach(e => {
        let id = e.toLowerCase()
        event.create(id + "_crystal_seed").texture("cabin:item/crystal_seed_" + id).displayName(e + " Quartz Seed")
        event.create("growing_" + id + "_seed", "create:sequenced_assembly").texture("cabin:item/crystal_seed_" + id).displayName(e + " Quartz Seed")
        event.create("tiny_" + id + "_crystal").texture("cabin:item/crystal_seed_" + id + "2").displayName("Tiny " + e + " Quartz Crystal")
        event.create("growing_tiny_" + id + "_crystal", "create:sequenced_assembly").texture("cabin:item/crystal_seed_" + id + "2").displayName("Tiny " + e + " Quartz Crystal")
        event.create("small_" + id + "_crystal").texture("cabin:item/crystal_seed_" + id + "3").displayName("Small " + e + " Quartz Crystal")
        event.create("growing_small_" + id + "_crystal", "create:sequenced_assembly").texture("cabin:item/crystal_seed_" + id + "3").displayName("Small " + e + " Quartz Crystal")
    });

    let processors = ["Calculation", "Logic", "Engineering"]
    processors.forEach(name => {
        let e = name.toLowerCase()
        event.create("incomplete_" + e + "_processor", "create:sequenced_assembly").texture("cabin:item/incomplete_" + e + "_processor").displayName("Incomplete " + name + " Processor")
    })

    applyItemModel(event.create("incomplete_flight_anchor", "create:sequenced_assembly"), "minecraft:block/beacon").displayName("Incomplete Flight Anchor")
    applyItemModel(event.create("incomplete_gravity_normalizer", "create:sequenced_assembly"), "ad_astra:block/gravity_normalizer").displayName("Incomplete Gravity Normalizer")

    let number = (name) => {
        let id = name.toLowerCase()
        event.create(id).texture("cabin:item/" + id).glow(true).displayName(name)
    }

    number("Zero")
    number("One")
    number("Two")
    number("Three")
    number("Four")
    number("Five")
    number("Six")
    number("Seven")
    number("Eight")
    number("Nine")
    number("Plus")
    number("Minus")
    number("Multiply")
    number("Divide")

    let mechanism = (name, rarity) => {
        let id = name.toLowerCase()
        event.create(id + "_mechanism").texture("cabin:item/" + id + "_mechanism").displayName(name + " Mechanism").rarity(rarity ? rarity : "common")
        event.create("incomplete_" + id + "_mechanism", "create:sequenced_assembly").texture("cabin:item/incomplete_" + id + "_mechanism").displayName("Incomplete " + name + " Mechanism")
    }

    event.create("radiant_coil").glow(true).texture("cabin:item/radiant_coil").displayName("Radiant Induction Coil")
    event.create("radiant_sheet").glow(true).texture("cabin:item/radiant_sheet").displayName("Radiant Sheet")

    mechanism("Kinetic")
    mechanism("Sealed")
    mechanism("Reinforced")
    mechanism("Infernal", "uncommon")
    mechanism("Logistic", "uncommon")
    mechanism("Refined", "uncommon") // 1.21: chapter 2c petroleum
    mechanism("Inductive", "uncommon")
    mechanism("Abstruse", "rare")
    mechanism("Calculation", "rare")

    /* 1.21: no more tconstruct slimy ferns — kubejs items still used in chapter 3 */
    let slime_types = ["Earth", "Sky", "Ender"]
    let slime_colours = [0x8FDB84, 0x00F9DE, 0xAC2EFC]

    for (let i = 0; i < slime_types.length; i++) {
        let name = slime_types[i]
        let id = name.toLowerCase()
        event.create(id + "_slimy_fern_leaf")
            .color(0, slime_colours[i])
            .texture("cabin:item/slimy_fern_leaf")
            .displayName("Slimy Fern Leaf")
        event.create(id + "_slime_fern_paste")
            .color(0, slime_colours[i])
            .texture("cabin:item/ground_slimy_fern")
            .displayName("Slimy Fern Blend")
        event.create(id + "_slime_fern")
            .color(0, slime_colours[i])
            .texture("cabin:item/slimy_fern_leaf")
            .displayName(name + " Slime Fern")
    }

    // Misc / Integration
    event.create("pipe_module_utility").texture("cabin:item/pipe_module_utility").displayName("Utility Pipe Module")
    event.create("pipe_module_tier_1").texture("cabin:item/pipe_module_tier_1").displayName("Brass Pipe Module")
    event.create("pipe_module_tier_2").texture("cabin:item/pipe_module_tier_2").displayName("Invar Pipe Module")
    event.create("pipe_module_tier_3").texture("cabin:item/pipe_module_tier_3").displayName("Enderium Pipe Module")

    event.create("circuit_scrap").texture("cabin:item/circuit_scrap").displayName("Circuit Scrap")
    // event.create('charged_calculator').texture("cabin:item/charged_calculator").displayName('Calculator').maxDamage(64)
    event.create("missingno").texture("cabin:item/missingno").displayName("∄")
    event.create("zinc_dust").texture("cabin:item/zinc_dust").displayName("Zinc Dust").tag("forge:dusts/zinc").tag("forge:dusts")
    // event.create('creosote_pellet').texture("cabin:item/creosote_pellet").displayName('Creosote Pellet')
    event.create("sand_ball").texture("cabin:item/sand_ball").displayName("Ball of Sand").unstackable()
    event.create("rough_sand").texture("cabin:item/rough_sand").displayName("Sand Chunks")
    event.create("purified_sand").texture("cabin:item/purified_sand").displayName("Purified Sand")
    event.create("silicon_compound").texture("cabin:item/silicon_compound").displayName("Silicon Compound")
    event.create("incomplete_coke_chunk", "create:sequenced_assembly").texture("cabin:item/incomplete_coke_chunk").displayName("Cut Coke")
    event.create("coke_chunk").texture("cabin:item/coke_chunk").displayName("Coke Chunks")
    // event.create('smoke_mote').texture("cabin:item/smoke_mote").displayName('Tiny Smoke Cloud')

    event.create("matter_plastics").texture("cabin:item/matter_plastics").displayName("Matter Plastics")
    event.create("nickel_compound").texture("cabin:item/nickel_compound").displayName("Nickel Compound").unstackable()
    event.create("invar_compound", "create:sequenced_assembly").texture("cabin:item/invar_compound").displayName("Unprocessed Invar Ingot")
    event.create("dye_entangled_singularity").texture("cabin:item/dye_entangled_singularity").unstackable().displayName("Chromatic Singularity")

    // event.create("strainer_filter").texture("waterstrainer:items/strainer_survivalist").displayName("Strainer Filter").maxDamage(384)
    event.create("chromatic_resonator").texture("cabin:item/chromatic_resonator").displayName("Chromatic Resonator").maxDamage(512)
    event.create("flash_drive").texture("cabin:item/boot_medium").displayName("Flash Drive").maxDamage(512)

    event.create("thermal_cast").texture("cabin:item/thermal_cast").displayName("Thermal Cast").unstackable()
    event.create("three_cast").texture("cabin:item/three_cast").displayName("Integer Cast (3)").unstackable()
    event.create("eight_cast").texture("cabin:item/eight_cast").displayName("Integer Cast (8)").unstackable()
    event.create("plus_cast").texture("cabin:item/plus_cast").displayName("Operator Cast (+)").unstackable()
    event.create("minus_cast").texture("cabin:item/minus_cast").displayName("Operator Cast (-)").unstackable()
    event.create("multiply_cast").texture("cabin:item/multiply_cast").displayName("Operator Cast (x)").unstackable()
    event.create("divide_cast").texture("cabin:item/divide_cast").displayName("Operator Cast (/)").unstackable()
    event.create("attachment_base").texture("cabin:item/attachment_base").displayName("Attachment Base")
    event.create("silver_coin").texture("cabin:item/silver_coin").displayName("Silver Coin Stack Icon")
    event.create("gold_coin").texture("cabin:item/gold_coin").displayName("Gold Coin Stack Icon")

    event.create("mystic_pottery_sherd").texture("cabin:item/mystic_pottery_sherd").displayName("Mystic Pottery Sherd").tag("minecraft:decorated_pot_ingredients").tag("minecraft:decorated_pot_sherds")
    event.create("cozy_pottery_sherd").texture("cabin:item/cozy_pottery_sherd").displayName("Cozy Pottery Sherd").tag("minecraft:decorated_pot_ingredients").tag("minecraft:decorated_pot_sherds")
    event.create("circuit_pottery_sherd").texture("cabin:item/circuit_pottery_sherd").displayName("Circuit Pottery Sherd").tag("minecraft:decorated_pot_ingredients").tag("minecraft:decorated_pot_sherds")
})

StartupEvents.registry("block", event => {

    event.create("trial_copper_block").texture("minecraft:block/copper_block").soundType("metal").tagBlock("mineable/pickaxe").tagBlock("needs_stone_tool").requiresTool(true).hardness(3.0).resistance(6.0)
    event.create("trial_cut_copper").texture("minecraft:block/cut_copper").soundType("metal").tagBlock("mineable/pickaxe").tagBlock("needs_stone_tool").requiresTool(true).hardness(3.0).resistance(6.0)
    event.create("trial_chiseled_copper").texture("minecraft:block/chiseled_copper").soundType("metal").tagBlock("mineable/pickaxe").tagBlock("needs_stone_tool").requiresTool(true).hardness(3.0).resistance(6.0)
    event.create("trial_copper_grate").texture("minecraft:block/copper_grate").soundType("metal").tagBlock("mineable/pickaxe").tagBlock("needs_stone_tool").requiresTool(true).hardness(3.0).resistance(6.0).notSolid().waterlogged()
    event.create("trial_cut_copper_stairs", "stairs").texture("minecraft:block/cut_copper").soundType("metal").tagBlock("mineable/pickaxe").tagBlock("needs_stone_tool").requiresTool(true).hardness(3.0).resistance(6.0)
    event.create("trial_cut_copper_slab", "slab").texture("minecraft:block/cut_copper").soundType("metal").tagBlock("mineable/pickaxe").tagBlock("needs_stone_tool").requiresTool(true).hardness(3.0).resistance(6.0)

    event.create("trial_oxidized_copper").texture("minecraft:block/oxidized_copper").soundType("metal").tagBlock("mineable/pickaxe").tagBlock("needs_stone_tool").requiresTool(true).hardness(3.0).resistance(6.0)
    event.create("trial_oxidized_cut_copper").texture("minecraft:block/oxidized_cut_copper").soundType("metal").tagBlock("mineable/pickaxe").tagBlock("needs_stone_tool").requiresTool(true).hardness(3.0).resistance(6.0)
    event.create("trial_oxidized_chiseled_copper").texture("minecraft:block/oxidized_chiseled_copper").soundType("metal").tagBlock("mineable/pickaxe").tagBlock("needs_stone_tool").requiresTool(true).hardness(3.0).resistance(6.0)
    event.create("trial_oxidized_copper_grate").texture("minecraft:block/oxidized_copper_grate").soundType("metal").tagBlock("mineable/pickaxe").tagBlock("needs_stone_tool").requiresTool(true).hardness(3.0).resistance(6.0).notSolid().waterlogged()
    event.create("trial_oxidized_cut_copper_stairs", "stairs").texture("minecraft:block/oxidized_cut_copper").soundType("metal").tagBlock("mineable/pickaxe").tagBlock("needs_stone_tool").requiresTool(true).hardness(3.0).resistance(6.0)
    event.create("trial_oxidized_cut_copper_slab", "slab").texture("minecraft:block/oxidized_cut_copper").soundType("metal").tagBlock("mineable/pickaxe").tagBlock("needs_stone_tool").requiresTool(true).hardness(3.0).resistance(6.0)

    event.create("enderium_casing").parentModel("cabin:block/enderium_casing").soundType("metal").tagBlock("mineable/pickaxe").tagBlock("create:wrench_pickup").requiresTool(true).hardness(4.0).displayName("Ender Casing")
    event.create("lead_casing").texture("cabin:block/lead_casing").soundType("metal").tagBlock("mineable/pickaxe").tagBlock("create:wrench_pickup").requiresTool(true).hardness(3.0).displayName("Lead Casing")
    event.create("zinc_casing").texture("cabin:block/zinc_casing").soundType("metal").tagBlock("mineable/pickaxe").tagBlock("create:wrench_pickup").requiresTool(true).hardness(3.0).displayName("Zinc Casing")
    event.create("constantan_casing").texture("cabin:block/constantan_casing").soundType("metal").tagBlock("mineable/pickaxe").tagBlock("create:wrench_pickup").requiresTool(true).hardness(3.0).displayName("Constantan Casing")
    event.create("fluix_casing").texture("cabin:block/fluix_casing").soundType("metal").tagBlock("mineable/pickaxe").tagBlock("create:wrench_pickup").requiresTool(true).hardness(3.0).displayName("Fluix Casing")

    event.create("computation_matrix").modelGenerator(model => { model.parent("cabin:block/computation_matrix") }).soundType("lantern").hardness(0.1).displayName("Computation Matrix").fullBlock(false).notSolid().box(1, 1, 1, 15, 15, 15).waterlogged().opaque(false).lightLevel(7).renderType("translucent")
        .item(i => { i.rarity("uncommon") })

    // event.create("ponder_laser_lamp").model("cabin:block/ponder_laser_lamp").notSolid().renderType("translucent").displayName("Laser Lamp (For Ponder)")
    // event.create("ponder_laser_lamp_on").model("cabin:block/ponder_laser_lamp_on").notSolid().lightLevel(15).renderType("translucent").displayName("Laser Lamp (For Ponder)")
    event.create("navigation_computer", "cardinal").parentModel("cabin:block/navigation_computer").soundType("metal").tagBlock("mineable/pickaxe").hardness(3.0).requiresTool(true).displayName("Navigation Computer")
    event.create("lander_deployer", "cardinal").parentModel("cabin:block/lander_deployer").soundType("metal").tagBlock("mineable/pickaxe").hardness(3.0).requiresTool(true).displayName("Lander Deployer")

    let machine = (name, layer) => {
        let id = name.toLowerCase()
        return event.create(id + "_machine", "cardinal").parentModel("cabin:block/" + id + "_machine")
            .soundType("lantern")
            .hardness(3.0)
            .tagBlock("mineable/pickaxe")
            .requiresTool(true)
            .displayName(name + " Machine")
            .notSolid()
            .renderType(layer)
            .redstoneConductor(false)
            .tagBlock("create:wrench_pickup")
            .defaultState(blockState => {
                blockState.set(BlockStateProperties.HORIZONTAL_FACING, "south")
            })
    }

    machine("Andesite", "solid").tagBlock("mineable/axe").box(0, 0, 3, 16, 16, 16).box(3, 14, 3, 13, 18, 17)
    machine("Brass", "translucent").tagBlock("mineable/axe").box(0, 0, 0, 16, 4, 16).box(0, 0, 3, 16, 10, 13).box(8, 3, 4, 16, 16, 16).box(1, 10, 5, 7, 21, 11)
    machine("Copper", "cutout").tagBlock("mineable/axe").box(0, 0, 0, 16, 4, 16).box(1.9, 2, -2, 14.9, 10, 10).box(6, 4, 6, 16, 20, 16).box(0, 4, 6, 10, 24, 16)
    machine("Gold", "solid").tagBlock("mineable/axe").box(0, 0, 0, 16, 3, 16).box(0, 3, 0, 12, 16, 8).box(0, 3, 8, 10, 14, 16).box(12, 3, 1, 16, 10, 16)
    machine("Lead", "cutout").box(0, 0, 1, 16, 2, 15).box(1, 2, 7, 14, 14, 15).box(0, 2, 0, 7, 9, 12).box(10, 0, 8, 16, 11, 16).box(7.5, 14, 8.5, 12.5, 21, 13.5)
    machine("Steel", "cutout").box(0, 0, 0, 16, 2, 16).box(4, 2, 4, 14, 11, 14).box(1, 2, 5, 4, 9, 12)
    machine("Constantan", "cutout")
    machine("Zinc", "cutout")
    machine("Enderium", "cutout")

    let pot = function (name) {
        let id = name.toLowerCase().split(" ").join("_")
        return event.create(id, "cardinal").parentModel(`cabin:block/${id}`)
            .notSolid()
            .renderType("translucent")
            .displayName(name)
            .hardness(0)
            // .material("COLOR_ORANGE") // Set a material (affects the sounds and some properties)
            .mapColor("COLOR_ORANGE")
            .soundType("glass")
            .waterlogged()
    }

    pot("Treasure Pot").box(4, 0, 4, 12, 10, 12)
    pot("Tall Treasure Pot").box(5, 0, 5, 11, 12, 11)
    pot("Small Treasure Pot").box(5, 0, 5, 11, 8, 11)
    pot("Small Quartz Treasure Pot").box(5, 0, 5, 11, 8, 11)
    pot("Tall Quartz Treasure Pot").box(5, 0, 5, 11, 12, 11)

    let alchemyBlockBase = (c1, c2, id, name, model) => {
        let block = event.create(id).modelGenerator(m => { m.parent(model)})
            .soundType("glass")
            .color(0, c1)
            .color(1, c2)
            .hardness(0.1)
            .displayName(name)
            .renderType("cutout")
            // .material("glass")
            .mapColor("NONE")// same as glass
            .waterlogged()
        block.item(e => e
            .color(0, c1)
            .color(1, c2)
        )
        return block
    }

    let substrateblock = (c1, c2, id, name, model) => {
        return alchemyBlockBase(c1, c2, id, name, model).box(.25, 0, .25, .75, 14.0 / 16.0, .75, false)
    }

    let acceleratorBlock = (c1, id, name, model) => {
        return alchemyBlockBase(c1, 0, id, name, model).box(.125, 0, .125, .875, 10.0 / 16.0, .875, false)
    }

    for (let i = 0; i < 15; i++)
        substrateblock(0x394867, 0x14274E, `failed_alchemy_${i}`, "Mundane Alchemic Blend", "cabin:block/mundane_substrate")

    global.substrates = []
    global.substrate_mapping = {}
    let current_category = []
    let category_index = 0
    let substrate_index = 0

    let category = () => {
        global.substrates.push(current_category)
        current_category = []
        category_index++
        substrate_index = 0
    }
    // substrates[current_category[id, ingredient, outputitem]]
    let createSubstrate = (c1, c2, id, name, model, ingredient, outputItem) => {
        global.substrate_mapping[id] = {
            category: category_index,
            index: substrate_index,
            name: name.replace(" Reagent", "").replace(" Catalyst", "")
        }
        current_category.push({
            id: `kubejs:substrate_${id}`,
            ingredient: ingredient,
            outputItem: outputItem
        })
        let substrate = substrateblock(c1, c2, `substrate_${id}`, name, "cabin:block/" + model)

        substrate_index++
        return substrate
    }

    let reagent = (c1, c2, id, prefix, ingredient, outputItem) => {
        return createSubstrate(c1, c2, id, `${prefix} Reagent`, "substrate", ingredient, outputItem)
    }
    let catalyst = (c1, c2, id, prefix, ingredient) => {
        let substrate = createSubstrate(c1, c2, id, `${prefix} Catalyst`, "catalyst", ingredient)
        substrate.item(item => { item.rarity("uncommon") })
        return substrate
    }

    /*
    Void            Stone    Lapis       Slime       EnderDust   Oil         Chocolate
    Valkyriad       Wool     Iron        Rope        Bait        Lead        Cuckoo
    Pandemoniac     Nthrrck  Scoria      Zinc        Sulfur      Niter       GhastTear
    Lycanomorph     Egg      Bonemeal    Rubber      Jack-O      Coral       Blaze
    Fractal         Granite  Quartz      Copper      Emerald     Burger      Prismarine
    Sloplike        Dirt     ConcreteP   Aluminum    Tofu        Cardboard   Beer
    Memorial        Calcite  DeadBush    Gold        Computer    Flywheel    Nickel
    */

    reagent(0x5F5F5F, 0x8E8E8E, "stone", "Stone", "minecraft:stone", "minecraft:stone")
    reagent(0x335DC1, 0x7395E7, "lapis", "Lapis Lazuli", "minecraft:lapis_lazuli", "minecraft:lapis_lazuli")
    reagent(0x4F7E48, 0x8AD480, "slime", "Slime", "minecraft:slime_ball", "minecraft:slime_ball")
    reagent(0x17F9AB, 0x1CB7B9, "ender", "Ender", "ae2:ender_dust", "ae2:ender_dust")
    reagent(0x22232D, 0x4C4C4C, "oil", "Petroleum", "kubejs:crystallized_oil", "kubejs:crystallized_oil")
    reagent(0x7D6B5A, 0x8AD480, "chocolate", "Chocolate", "create:bar_of_chocolate", "create:bar_of_chocolate")
    category()
    reagent(0x7F7F7F, 0xD4D4D4, "wool", "Woollen", "minecraft:white_wool", "minecraft:white_wool")
    reagent(0xA6A6A6, 0xD5D5D5, "iron", "Iron", "immersiveengineering:dust_iron", "immersiveengineering:dust_iron")
    reagent(0xDDD1C1, 0xDBA520, "rope", "Rope", "simulated:rope_coupling", "simulated:rope_coupling")
    reagent(0xD30000, 0x708238, "bait", "Bait", "tide:bait", "tide:bait")
    reagent(0x232456, 0x7C95A4, "lead", "Leaden", "immersiveengineering:dust_lead", "immersiveengineering:dust_lead")
    reagent(0xDD7E5D, 0xD0D2C5, "cuckoo", "Cuckoo", "create:cuckoo_clock", "create:cuckoo_clock")
    category()
    reagent(0x5B151A, 0xBC3E49, "netherrack", "Netherrack", "minecraft:netherrack", "minecraft:netherrack")
    reagent(0x6B5D4F, 0x7D6B5A, "scoria", "Scoria", "create:scoria", "create:scoria")
    reagent(0x616A60, 0xD0D2C5, "zinc", "Zinc", "kubejs:zinc_dust", "kubejs:zinc_dust")
    reagent(0xC7A94A, 0xEEF071, "sulfur", "Sulfuric", "immersiveengineering:dust_sulfur", "immersiveengineering:dust_sulfur")
    reagent(0x735A65, 0xB8AFAF, "niter", "Nitric", "immersiveengineering:dust_saltpeter", "immersiveengineering:dust_saltpeter")
    reagent(0x91C5FC, 0xA7CBCF, "ghasttear", "Tears", "minecraft:ghast_tear", "minecraft:ghast_tear")
    category()
    reagent(0xFFF200, 0xD4D4C3, "egg", "Egg", "minecraft:egg", "minecraft:egg")
    reagent(0xEEE2D2, 0xDDD1C1, "bonemeal", "Bone", "minecraft:bone_meal", "minecraft:bone_meal")
    reagent(0x13121A, 0x20253C, "rubber", "Rubber", "kubejs:rubber", "kubejs:rubber")
    reagent(0xFC6600, 0xFFF200, "jackolantern", "Jack O'", "minecraft:jack_o_lantern", "minecraft:jack_o_lantern")
    reagent(0xFCCED0, 0xDD7E5D, "coral", "Coral", "minecraft:brain_coral", "minecraft:brain_coral")
    reagent(0xAC3B00, 0xD5AC26, "blaze", "Blaze", "minecraft:blaze_powder", "minecraft:blaze_powder")
    category()
    reagent(0x783A2F, 0xBC6C5B, "granite", "Granite", "minecraft:granite", "minecraft:granite")
    reagent(0xB19E8F, 0xE7E2DB, "quartz", "Quartz", "minecraft:quartz", "minecraft:quartz")
    reagent(0xDD7E5D, 0x17E9AB, "copper", "Copper", "immersiveengineering:dust_copper", "immersiveengineering:dust_copper")
    reagent(0x00A82B, 0xADFACB, "emerald", "Emerald", "minecraft:emerald", "minecraft:emerald")
    reagent(0xFC7781, 0xFCCED0, "burger", "Burger", "farmersdelight:hamburger", "farmersdelight:hamburger")
    reagent(0x529680, 0xA2CFC0, "prismarine", "Prismarine", "minecraft:prismarine_crystals", "minecraft:prismarine_crystals")
    category()
    reagent(0x6B5D4F, 0x8E8E8E, "dirt", "Dirt", "minecraft:dirt", "minecraft:dirt")
    reagent(0x585858, 0x646363, "concrete", "Concrete", "clayworks:concrete_powder", "clayworks:concrete_powder")
    reagent(0xD5EDC3, 0xE9F4E5, "aluminum", "Aluminum", "immersiveengineering:dust_aluminum", "immersiveengineering:dust_aluminum")
    reagent(0xD5D5C3, 0xE4FFD3, "tofu", "Tofu", "youkaisfeasts:tofu", "youkaisfeasts:tofu")
    reagent(0xEBEDEF, 0x7D6B5A, "cardboard", "Cardboard", "create:cardboard", "create:cardboard")
    reagent(0xC5B55A, 0xE4E4B3, "beer", "Beer", "brewinandchewin:beer", "brewinandchewin:beer")
    category()
    reagent(0xB8AFAF, 0xD4D4D4, "calcite", "Calcified", "minecraft:calcite", "minecraft:calcite")
    reagent(0x1B1D1F, 0x7D6B5A, "deadbush", "Dead", "minecraft:dead_bush", "minecraft:dead_bush")
    reagent(0xD99413, 0xFAF25E, "gold", "Golden", "immersiveengineering:dust_gold", "immersiveengineering:dust_gold")
    reagent(0xb200ed, 0x05030A, "computer", "Computer", "computercraft:computer_normal", "computercraft:computer_normal")
    reagent(0xFAF25E, 0x22232D, "flywheel", "Flywheel", "create:flywheel", "create:flywheel")
    reagent(0x977756, 0xE4D196, "nickel", "Nickel", "immersiveengineering:dust_nickel", "immersiveengineering:dust_nickel")
    category()
    catalyst(0x22232D, 0x1CB7B9, "void", "Void")
    catalyst(0xDBA520, 0xFAF25E, "valkyriad", "Valkyriad")
    catalyst(0x5B151A, 0xEEF071, "pandemoniac", "Pandemoniac")
    catalyst(0x03020A, 0xD5AC26, "lycanomorph", "Lycanomorph")
    catalyst(0xB19E8F, 0x00A82B, "fractal", "Fractal")
    catalyst(0x777777, 0x666666, "sloplike", "Sloplike")
    catalyst(0xDEADBE, 0xEEEEEF, "memorial", "Memorial")
    category()

    substrateblock(0xb200ed, 0xff66cc, "substrate_chaos", "Chaos Catalyst", "cabin:block/chaos_catalyst")
        .item(item => item.rarity("rare"))

    substrateblock(0x474449, 0x967DA0, "substrate_silicon", "Silicon Reagent", "cabin:block/substrate")
        .item(item => item.rarity("rare"))

    substrateblock(0x9FADB4, 0xBECCD2, "substrate_silver", "Silver Reagent", "cabin:block/substrate")
        .item(item => item.rarity("rare"))

    substrateblock(0xFC7781, 0xFCCED0, "substrate_cinnabar", "Cinnabar Reagent", "cabin:block/substrate")
    substrateblock(0x735A65, 0xB8AFAF, "substrate_ruby", "Ruby Reagent", "cabin:block/substrate")
    substrateblock(0x335DC1, 0x7395E7, "substrate_sapphire", "Sapphire Reagent", "cabin:block/substrate")

    acceleratorBlock(0xFFBC5E, "accelerator_glowstone", "Glowstone Accelerator", "cabin:block/accellerator")

    acceleratorBlock(0xAA0F01, "accelerator_redstone", "Redstone Accelerator", "cabin:block/accellerator")
})

StartupEvents.registry("fluid", event => {
    let colors = [0xCBE827, 0xAEE827, 0x68E827, 0x27E86E, 0x27E8B1, 0x27DEE8, 0x27B5E8, 0x2798E8, 0x2778E8, 0x2748E8]
    event.create("raw_logic").displayName("Liquified Logic (Unprocessed)").stillTexture("cabin:fluid/number_still").flowingTexture("cabin:fluid/number_flow")
    for (let i = 0; i < 10; i++)
        event.create("number_" + i).displayName(`Liquified Logic (${i})`).stillTexture("cabin:fluid/number_still").flowingTexture("cabin:fluid/number_flow")
    event.create("matrix").displayName("Liquified Computation Matrix").stillTexture("cabin:fluid/matrix_still").flowingTexture("cabin:fluid/matrix_flow")
    event.create("fine_sand").displayName("Fine Sand").stillTexture("cabin:fluid/fine_sand_still").flowingTexture("cabin:fluid/fine_sand_flow")
    // event.create("crude_oil").displayName("Crude Oil").stillTexture("thermal:block/fluids/crude_oil_still").flowingTexture("thermal:block/fluids/crude_oil_flow")
    event.create("volatile_sky_solution").displayName("Volatile Sky Solution").stillTexture("blazinghot:fluid/molten_sturdy_alloy_still").flowingTexture("blazinghot:fluid/molten_sturdy_alloy_flow")
    event.create("chromatic_waste").displayName("Chromatic Waste").stillTexture("supplementaries:block/lumisene").flowingTexture("supplementaries:block/lumisene_flowing")
    event.create("liquid_pulp").displayName("Liquid Pulp").stillTexture("nuclearcraftneohaul:block/fluid/molten_still").flowingTexture("nuclearcraftneohaul:block/fluid/molten_flow").tint(0xb4a498)
    //	event.create('liquid_smoke').displayName(`Liquid Smoke`).stillTexture('advancedrocketry:blocks/fluid/oxygen_still').flowingTexture('advancedrocketry:blocks/fluid/oxygen_flow').bucketColor(0xEBEBEB)
    // A2: liquid plastic (tfmg replacement)
    event.create("liquid_plastic").displayName("Liquid Plastic").stillTexture("oritech:block/fluid/fluid_strange_pale_2").flowingTexture("oritech:block/fluid/fluid_strange_pale_2")
})

ItemEvents.modification(event => {
    let colors = ["red", "yellow", "green", "blue", "magenta", "black"]
    colors.forEach(element => {
        event.modify("ae2:" + element + "_paint_ball", item => {
            item.maxStackSize = 1
        })
    });

    if (Item.exists("projectred_core:screwdriver")) {
        event.modify("projectred_core:screwdriver", item => {
            item.maxDamage = 512
        })
    }
})

StartupEvents.registry("potion", (event) => {
    let createCustomPotion = (name, effect, duration, long_duration, strong_duration) => {
        event.create(name).effect(
            effect,
            20 * duration,
            0
        )

        event.create("long_" + name).effect(
            effect,
            20 * long_duration,
            0
        )

        if (strong_duration != undefined)
            event.create("strong_" + name).effect(
                effect,
                20 * strong_duration,
                1
            )
    }

    // createCustomPotion("haste", "minecraft:haste", 180, 480, 90) // unified under alexscaves:haste instead
})
