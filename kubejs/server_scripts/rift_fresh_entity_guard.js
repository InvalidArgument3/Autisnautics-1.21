// Stops Immersive Portals from looping on a brand-new entity below a rift portal.
//
// Cause (imm_ptl in ip_aeronautics-2.8.17, ServerTeleportationManager):
// - manageGlobalPortalTeleportation() runs every server tick over level.getAllEntities() in every level,
//   including levels that have stopped ticking entities. Vanilla ServerLevel.tick stops ticking entities
//   300 ticks after the last player leaves, but block and fluid ticks keep running in spawn chunks.
// - shouldEntityTeleport() tests the segment getEyePosition(0) -> getEyePosition(1). Before an entity's
//   first tick, xo/yo/zo are still 0, so that segment runs from (0, eye, 0) to the entity. For an item
//   below a floor portal (the overworld rift water under bottomY -49), it crosses the portal plane.
// - startTeleportingRegularEntity() then refuses because xo == yo == zo == 0 and logs
//   "Trying to teleport a fresh new entity". The entity is never ticked, so this repeats every tick.
//
// Fix: when an entity joins a level with an unset previous position, set it to the current position.
// That is what vanilla does at the start of the entity's first tick (setOldPosAndRot). Entities that tick
// normally behave exactly as before, so rift crossing for items, mobs, and so on is unchanged.
EntityEvents.spawned(event => {
    const entity = event.getEntity()
    if (entity == null) return
    if (entity.xo != 0 || entity.yo != 0 || entity.zo != 0) return
    entity.xo = entity.getX()
    entity.yo = entity.getY()
    entity.zo = entity.getZ()
})
