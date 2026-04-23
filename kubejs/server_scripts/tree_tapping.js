// scarring trees to make resin

// check if a block belongs to a tree
// tree: at least one log cardinally adjacent to a leaf with blockdata persistent=false, in any direction but down, or any log directly below such a log
let checkTree = (e, log) => {
    let isTree = false
    // max check height 32 blocks
    for (let i = 0; i < 32; i++) {
        let adjacents = []
        adjacents.push(log.getUp(), log.getNorth(), log.getSouth(), log.getEast(), log.getWest())// don't check down
        for (let a = 0; a < 5; a++) {
            // check for a natural leaf block, not placed by a player (persistent=false)
            if (adjacents[a].tags.toString().includes("minecraft:leaves") && adjacents[a].getProperties().get("persistent") == "false") {
                isTree = true
                break
            }
        }
        // end the check if it's already succeeded or failed
        if (isTree || !(adjacents[0].tags.toString().includes("minecraft:logs"))) {
            break
        }
        // otherwise, move up one log and try again
        log = log.getUp()
    }
    return isTree
}

BlockEvents.rightClicked(event => {
    // right-clicking a log with a knife
    if (event.item.tags.toString().includes("c:tools/knife") && event.block.tags.toString().includes("minecraft:logs")) {
        if (checkTree(event, event.block) == true) {
            let hitResult = event.getHitResult().location
            let entityPos = event.player.position()
            let hit = hitResult.multiply(49, 49, 49).add(entityPos).multiply(0.02, 0.02, 0.02)
            let spawn = hitResult.multiply(3, 3, 3).add(entityPos).multiply(0.25, 0.25, 0.25)
            // spawn particles on that face and play a sound to tell the player they tapped
            event.server.runCommandSilent(`/execute positioned ${hit.x} ${hit.y} ${hit.z} run particle minecraft:dripping_honey`)
            event.server.runCommandSilent(`/execute positioned ${hit.x} ${hit.y} ${hit.z} run playsound minecraft:block.wood.break block @a`)
            // check low random chance (encourages automating)
            if (Math.random() <= 0.05) {
                // drop a resin clump
                event.server.runCommandSilent(`/execute positioned ${hit.x} ${hit.y} ${hit.z} run playsound minecraft:block.resin.break master @a`)
                event.server.runCommandSilent(`/summon minecraft:item ${spawn.x} ${spawn.y} ${spawn.z} {Item:{id:"minecraft:resin_clump",Count:1b}}`)
            }
        }
    }
})
