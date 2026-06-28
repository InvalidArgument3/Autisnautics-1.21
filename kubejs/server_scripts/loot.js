/* A2: dust thing is both undesired and broken
let metal_ores_drop_dust = (id, crushedId, dustId) => {
    return {
        "type": "minecraft:block",
        "pools": [
            {
                "rolls": 1,
                "entries": [
                    {
                        "type": "minecraft:alternatives",
                        "children": [
                            {
                                "type": "minecraft:item",
                                "name": dustId,
                                "functions": [
                                    {
                                        "function": "minecraft:set_count",
                                        "count": 9
                                    }
                                ],
                                "conditions": [
                                    {
                                        "condition": "tconstruct:has_modifier",
                                        "modifier": "tconstruct:melting"
                                    }
                                ]
                            },
                            {
                                "type": "minecraft:item",
                                "name": id,
                                "conditions": [
                                    {
                                        "condition": "minecraft:match_tool",
                                        "predicate": {
                                            "enchantments": [
                                                {
                                                    "enchantment": "minecraft:silk_touch",
                                                    "levels": {
                                                        "min": 1
                                                    }
                                                }
                                            ]
                                        }
                                    }
                                ]
                            },
                            {
                                "type": "minecraft:item",
                                "name": crushedId,
                                "functions": [
                                    {
                                        "function": "minecraft:set_count",
                                        "count":
                                        {
                                            "min": 2.0,
                                            "max": 3.0,
                                            "type": "minecraft:uniform"
                                        }
                                    },
                                    {
                                        "function": "minecraft:apply_bonus",
                                        "enchantment": "minecraft:fortune",
                                        "formula": "minecraft:uniform_bonus_count",
                                        "parameters": { "bonusMultiplier": 1 }
                                    },
                                    {
                                        "function": "minecraft:explosion_decay"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
    }
}
*/

let metal_ores_json = (id, crushedId) => {
    return {
        "type": "minecraft:block",
        "pools": [
            {
                "rolls": 1,
                "entries": [
                    {
                        "type": "minecraft:alternatives",
                        "children": [
                            {
                                "type": "minecraft:item",
                                "name": id,
                                "conditions": [
                                    {
                                        "condition": "minecraft:match_tool",
                                        "predicate": {
                                            "enchantments": [
                                                {
                                                    "enchantment": "minecraft:silk_touch",
                                                    "levels": {
                                                        "min": 1
                                                    }
                                                }
                                            ]
                                        }
                                    }
                                ]
                            },
                            {
                                "type": "minecraft:item",
                                "name": crushedId,
                                "functions": [
                                    {
                                        "function": "minecraft:set_count",
                                        "count": 1,
                                    },
                                    {
                                        "function": "minecraft:apply_bonus",
                                        "enchantment": "minecraft:fortune",
                                        "formula": "minecraft:uniform_bonus_count",
                                        "parameters": { "bonusMultiplier": 1 }
                                    },
                                    {
                                        "function": "minecraft:explosion_decay"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
    }
}
ServerEvents.blockLootTables(event => {

    event.addSimpleBlock("minecraft:twisting_vines", "minecraft:twisting_vines")
    event.addSimpleBlock("minecraft:weeping_vines", "minecraft:weeping_vines")
    /*
    let extra_ores = ["minecraft:", "minecraft:deepslate_"]

    extra_ores.forEach(e => {
        let iron = e + "iron_ore"
        event.addJson(iron, metal_ores_drop_dust(iron, "create:crushed_raw_iron", "thermal:iron_dust"))
        let gold = e + "gold_ore"
        event.addJson(gold, metal_ores_drop_dust(gold, "create:crushed_raw_gold", "thermal:gold_dust"))
    })
    event.addJson("minecraft:copper_ore", metal_ores_drop_dust("minecraft:copper_ore", "create:crushed_raw_copper", "thermal:copper_dust"))
    event.addJson("minecraft:deepslate_copper_ore", metal_ores_drop_dust("minecraft:deepslate_copper_ore", "create:crushed_raw_copper", "thermal:copper_dust"))
    event.addJson("create:zinc_ore", metal_ores_drop_dust("create:zinc_ore", "create:crushed_raw_zinc", "kubejs:zinc_dust"))
    event.addJson("create:deepslate_zinc_ore", metal_ores_drop_dust("create:deepslate_zinc_ore", "create:crushed_raw_zinc", "kubejs:zinc_dust"))
    event.addJson("thermal:nickel_ore", metal_ores_drop_dust("thermal:nickel_ore", "create:crushed_raw_nickel", "thermal:nickel_dust"))
    event.addJson("thermal:deepslate_nickel_ore", metal_ores_drop_dust("thermal:deepslate_nickel_ore", "create:crushed_raw_nickel", "thermal:nickel_dust"))
    event.addJson("thermal:lead_ore", metal_ores_drop_dust("thermal:lead_ore", "create:crushed_raw_lead", "thermal:lead_dust"))
    event.addJson("thermal:deepslate_lead_ore", metal_ores_drop_dust("thermal:deepslate_lead_ore", "create:crushed_raw_lead", "thermal:lead_dust"))
    event.addJson("ad_astra:moon_iron_ore", metal_ores_drop_dust("ad_astra:moon_iron_ore", "create:crushed_raw_iron", "thermal:iron_dust"))
*/
    let jaopca_crushed_ore_types = ["boron", "calorite", "cobalt", "desh", "lithium", "magnesium", "ostrum", "thorium"]
    let create_crushed_ore_types = ["iron", "gold", "copper", "zinc", "platinum", "silver", "tin", "lead", "aluminum", "uranium", "nickel"]

    jaopca_crushed_ore_types.forEach(metal => {
        let jaopcaOreList = new RegExp("^.*:(deepslate_|nether_|moon_|mars_|venus_|mercury_|glacio_)?" + metal + "_(deepslate_)?ore$")
        // console.log(`oreList for ` + metal + ` is: ` + Ingredient.of(jaopcaOreList).itemIds.toString())
        Ingredient.of(jaopcaOreList).itemIds.forEach(ore => {
            event.addJson(ore, metal_ores_json(ore, "jaopca:create_crushed." + metal))
        })
    })
    create_crushed_ore_types.forEach(metal => {
        let createOreList = new RegExp("^.*:(deepslate_|nether_|moon_|mars_|venus_|mercury_|glacio_)?" + metal + "_(deepslate_)?ore$")
        // console.log(`oreList for ` + metal + ` is: ` + Ingredient.of(createOreList).itemIds.toString())
        Ingredient.of(createOreList).itemIds.forEach(ore => {
            event.addJson(ore, metal_ores_json(ore, "create:crushed_raw_" + metal))
        })
    })
    event.addJson("scguns:anthralite_ore", metal_ores_json("scguns:anthralite_ore", "scguns:crushed_raw_anthralite"))
    event.addJson("scguns:deepslate_anthralite_ore", metal_ores_json("scguns:deepslate_anthralite_ore", "scguns:crushed_raw_anthralite"))


})
