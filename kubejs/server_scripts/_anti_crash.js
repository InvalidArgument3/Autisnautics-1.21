// i hate the antichrist
const BlockGetter = Java.loadClass("net.minecraft.world.level.BlockGetter")
const ClipContext = Java.loadClass("net.minecraft.world.level.ClipContext")
const LevelReader = Java.loadClass("net.minecraft.world.level.LevelReader")

// //hooking create_sa:grapplin_whisk to/from shipyard coordinates
// const unHookWhisk = function (event, whisk) {
//     let newStack = whisk.copy()
//     let data = newStack.nbt
//     data.tagHooked = false
//     newStack.setNbt(data)
//
//     event.player.drop(newStack, true, true)
//     whisk.setCount(0)
//     event.player.tell(Text.red(`Your Grapplin Whisk is repulsed by the ship's Gellar Field and falls out of your hand.`))
// }

// prevent hooking the shipyard, for real this time
ItemEvents.firstRightClicked(event => {
    // let jsItem = Item.of(event.item)

    // if (jsItem.getId() == "create_sa:grapplin_whisk") {
    //     let targetPos = event.player.level.clip(new ClipContext(event.player.getEyePosition(1.0), event.player.getEyePosition(1.0).add(event.player.getViewVector(1.0).scale(75.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, event.player)).getBlockPos()
    //     if (!event.player.level.isEmptyBlock(targetPos)) {
    //         let targetX = targetPos.getX()
    //         if ( Math.abs(targetX) > 1000000 ) {
    //             unHookWhisk(event, jsItem)
    //         }
    //     }
    // }
})

// unhooking create_sa:grapplin_whisk for a player that crashed out
PlayerEvents.loggedIn(event => {
    // let jsMainItem = Item.of(event.player.getMainHandItem())
    // let jsOffItem = Item.of(event.player.getOffhandItem())

    // if (jsMainItem.getId() == "create_sa:grapplin_whisk") {
    //     let origData = jsMainItem.nbt
    //     if (origData.tagHooked == true && origData.xPostion) {
    //         if (Math.abs(origData.xPostion) > 1000000) {
    //             unHookWhisk(event, jsMainItem)
    //         }
    //     }
    // }
    // if (jsOffItem.getId() == "create_sa:grapplin_whisk") {
    //     let origData = jsOffItem.nbt
    //     if (origData.tagHooked == true && origData.xPostion) {
    //         if (Math.abs(origData.xPostion) > 1000000) {
    //             unHookWhisk(event, jsOffItem)
    //         }
    //     }
    // }
})
