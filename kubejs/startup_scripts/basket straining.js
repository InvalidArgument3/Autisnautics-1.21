//straining water with farmersdelight:basket
BlockEvents.modification(event => {
    event.modify("farmersdelight:basket", handler => {
        handler.randomTickCallback = (tick) => {
            if (tick.block.getId().startsWith("farmersdelight:basket") && tick.block.getProperties().get("waterlogged") == "true") {
                //if water in front and behind basket: 20% base chance per randomtick
                //+20% per water in front of basket, up to 4 blocks (100%)
                let facing = tick.block.getProperties().get("facing")
                let chance = 0
                
                switch (facing) {
                    case "north":
                        if (tick.block.getNorth().id == "minecraft:water" && tick.block.getSouth().id == "minecraft:water") {
                            chance += 0.2
                            let currentBlock = tick.block.getNorth()
                            for (let a = 0; a < 4; a++) {
                                if (currentBlock.getNorth().id == "minecraft:water") {
                                    chance += 0.2
                                    currentBlock = currentBlock.getNorth()
                                    continue
                                }
                                break
                            }
                        }
                        break
                    case "south":
                        if (tick.block.getNorth().id == "minecraft:water" && tick.block.getSouth().id == "minecraft:water") {
                            chance += 0.2
                            let currentBlock = tick.block.getSouth()
                            for (let a = 0; a < 4; a++) {
                                if (currentBlock.getSouth().id == "minecraft:water") {
                                    chance += 0.2
                                    currentBlock = currentBlock.getSouth()
                                    continue
                                }
                                break
                            }
                        }
                        break
                    case "east":
                        if (tick.block.getEast().id == "minecraft:water" && tick.block.getWest().id == "minecraft:water") {
                            chance += 0.2
                            let currentBlock = tick.block.getEast()
                            for (let a = 0; a < 4; a++) {
                                if (currentBlock.getEast().id == "minecraft:water") {
                                    chance += 0.2
                                    currentBlock = currentBlock.getEast()
                                    continue
                                }
                                break
                            }
                        }
                        break
                    case "west":
                        if (tick.block.getEast().id == "minecraft:water" && tick.block.getWest().id == "minecraft:water") {
                            chance += 0.2
                            let currentBlock = tick.block.getWest()
                            for (let a = 0; a < 4; a++) {
                                if (currentBlock.getWest().id == "minecraft:water") {
                                    chance += 0.2
                                    currentBlock = currentBlock.getWest()
                                    continue
                                }
                                break
                            }
                        }
                        break
                    case "up":
                        if (tick.block.getUp().id == "minecraft:water" && tick.block.getDown().id == "minecraft:water") {
                            chance += 0.2
                            let currentBlock = tick.block.getUp()
                            for (let a = 0; a < 4; a++) {
                                if (currentBlock.getUp().id == "minecraft:water") {
                                    chance += 0.2
                                    currentBlock = currentBlock.getUp()
                                    continue
                                }
                                break
                            }
                        }
                        break
                    // downward-facing baskets can't hold anything...
                }
                if (Math.random() < chance) {
                    let x = tick.block.getCenterX()
                    let y = tick.block.getCenterY()
                    let z = tick.block.getCenterZ()
                    // 25% to get a clay ball instead of sand
                    let item = (Math.random() <= 0.75) ? "minecraft:sand" : "minecraft:clay_ball"
                    tick.server.runCommandSilent(`/summon minecraft:item ${x.toString()} ${y.toString()} ${z.toString()} {Item:{id:"${item}",Count:1b}}`)
                }
            }
        }
    })
})