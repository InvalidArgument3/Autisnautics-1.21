// Wasteland portal
let frameBlock = "kubejs:lead_casing"
let rodBlock = "alexscaves:uranium_rod"
const wastelandDimension = "nuclearcraftneohaul:nuclear_wasteland"
const wastelandPortal = "nuclearcraftneohaul:wasteland_portal"

// checking for interdimensional anomalies
let isValidForWastelandPortal = (e, block) => {
    if (
        (e.getLevel().dimension == "minecraft:overworld" || e.getLevel().dimension == wastelandDimension)
        && Math.abs(block.getX()) < 1000000
        && Math.abs(e.getEntity().getX()) < 1000000
    ) { return true }
    return false
}


// building
BlockEvents.placed(event => {
    let placedBlock = event.getBlock()
    if (placedBlock.getId() == "supplementaries:globe_sepia") {
        let nuclearBomb = placedBlock.getDown()
        let frameCount = 0
        let rodCount = 0

        if (nuclearBomb.getId() == "alexscaves:nuclear_bomb") {
            let donutBlocks = [nuclearBomb.getNorth(), nuclearBomb.getSouth(), nuclearBomb.getEast(), nuclearBomb.getWest(), nuclearBomb.getNorth().getEast(), nuclearBomb.getNorth().getWest(), nuclearBomb.getSouth().getEast(), nuclearBomb.getSouth().getWest()]
            donutBlocks.forEach(b => {
                if (b.getId() == frameBlock) {
                    frameCount++
                }
            })
        }

        if (frameCount == 8) {
            let rodBlocks = [placedBlock.getNorth().getWest(), placedBlock.getNorth().getEast(), placedBlock.getSouth().getWest(), placedBlock.getSouth().getEast()]
            rodBlocks.forEach(b => {
                if (b.getId() == rodBlock) {
                    rodCount++
                }
            })
        }

        if (rodCount == 4 && event.getEntity()) {
            let worldEnder = event.getEntity()
            let server = event.getLevel().getServer()
            if (isValidForWastelandPortal(event, placedBlock)) {
                server.runCommandSilent(`/execute at ${worldEnder.stringUUID} run particle alexscaves:mushroom_cloud ${placedBlock.getX()} ${placedBlock.getY()} ${placedBlock.getZ()}`)
                server.runCommandSilent(`/execute at ${worldEnder.stringUUID} run playsound nuclearcraftneohaul:player.geiger_tick master @a ~ ~ ~ 1 1`)
                server.scheduleInTicks(150, callback => {
                    server.runCommandSilent(`/execute at ${worldEnder.stringUUID} run playsound nuclearcraftneohaul:player.geiger_tick master @a ~ ~ ~ 1 0.8`)
                })
                server.scheduleInTicks(300, callback => {
                    server.runCommandSilent(`/execute at ${worldEnder.stringUUID} run playsound nuclearcraftneohaul:player.geiger_tick master @a ~ ~ ~ 1 0.6`)
                })
                server.scheduleInTicks(340, callback => {
                    server.runCommandSilent(`/execute at ${worldEnder.stringUUID} run playsound nuclearcraftneohaul:music_disc.end_of_the_world master @a ~ ~ ~ 1 1`)
                })
                placedBlock.set("minecraft:air")
                nuclearBomb.set(wastelandPortal)
            }
            else if (!isValidForWastelandPortal(event, placedBlock) && (event.getLevel().dimension != "minecraft:overworld" && event.getLevel().dimension != wastelandDimension)) {
                worldEnder.tell(Text.red(`This world's past is far too different from the Overworld's for that to work.`))
            }
            else {
                worldEnder.tell(Text.red(`The ship's Gellar Field protects the local fabric of spacetime.`))
            }
        }
    }
})

// breaking
BlockEvents.broken(event => {
    let brokenBlock = event.getBlock()
    if (brokenBlock.getId() == frameBlock) {
        let portalBreaker = event.getEntity()
        let server = event.getLevel().getServer()
        let donutBlocks = [brokenBlock.getNorth(), brokenBlock.getSouth(), brokenBlock.getEast(), brokenBlock.getWest(), brokenBlock.getNorth().getEast(), brokenBlock.getNorth().getWest(), brokenBlock.getSouth().getEast(), brokenBlock.getSouth().getWest()]
        donutBlocks.forEach(b => {
            if (b.getId() == wastelandPortal) {
                b.set("minecraft:air")
                server.runCommandSilent(`/execute at ${portalBreaker.stringUUID} run playsound minecraft:block.portal.travel master @a ~ ~ ~ 1 1`)
            }
        })
    }
    if (brokenBlock.getId() == rodBlock) {
        let portalBreaker = event.getEntity()
        let server = event.getLevel().getServer()
        let diagonalBlocks = [brokenBlock.getNorth().getWest().getDown(), brokenBlock.getSouth().getWest().getDown(), brokenBlock.getNorth().getEast().getDown(), brokenBlock.getSouth().getEast().getDown()]
        diagonalBlocks.forEach(b => {
            if (b.getId() == wastelandPortal) {
                b.set("minecraft:air")
                server.runCommandSilent(`/execute at ${portalBreaker.stringUUID} run playsound minecraft:block.portal.travel master @a ~ ~ ~ 1 1`)
            }
        })
    }
})
