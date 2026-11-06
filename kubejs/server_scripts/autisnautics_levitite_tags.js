ServerEvents.tags('block', event => {
    event.add('minecraft:mineable/pickaxe', 'aeronautics:levitite');
    event.add('minecraft:needs_iron_tool', 'aeronautics:levitite');
    event.remove('minecraft:needs_diamond_tool', 'aeronautics:levitite');
    event.remove('minecraft:incorrect_for_iron_tool', 'aeronautics:levitite');
    event.remove('minecraft:incorrect_for_diamond_tool', 'aeronautics:levitite');
    event.remove('minecraft:incorrect_for_netherite_tool', 'aeronautics:levitite');
    event.add('minecraft:incorrect_for_wooden_tool', 'aeronautics:levitite');
    event.add('minecraft:incorrect_for_stone_tool', 'aeronautics:levitite');
    event.add('minecraft:incorrect_for_gold_tool', 'aeronautics:levitite');
});
