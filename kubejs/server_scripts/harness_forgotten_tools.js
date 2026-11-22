// Adventurer's Harness mining calls ItemStack.isCorrectToolForDrops. A diamond
// harness uses a diamond pickaxe, and a pickaxe in the tool slot is checked the
// same way. The first matching tool rule wins, and a diamond or netherite
// pickaxe's first rule is incorrect_for_*_tool with correct_for_drops false.
// Undergarden adds #undergarden:needs_forgotten_tool to both of those tags, so
// the harness refuses dreadrock even though the block is mineable/pickaxe.
// Depthrock, shiverstone, and tremblecrust are not in that tag. Wood, stone,
// iron, and gold stay incorrect.
ServerEvents.tags('block', event => {
    const forgotten = '#undergarden:needs_forgotten_tool'
    event.remove('minecraft:incorrect_for_diamond_tool', forgotten)
    event.remove('minecraft:incorrect_for_netherite_tool', forgotten)
})
