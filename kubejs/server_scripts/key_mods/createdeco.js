ServerEvents.recipes(event => {
    // Create Deco 2.1.3 renamed cast iron to industrial iron.
    // The block is create:industrial_iron_block. Nugget, ingot, and sheet stay on createdeco.
    if (!Item.exists("createdeco:industrial_iron_nugget")) return
    event.replaceInput({ id: "createdeco:industrial_iron_ingot" }, "createdeco:industrial_iron_nugget", "#c:nuggets/industrial_iron")
    event.replaceInput({ id: "createdeco:industrial_iron_ingot_from_industrial_iron_block" }, "create:industrial_iron_block", "#c:storage_blocks/industrial_iron")
    event.replaceInput([{ id: "createdeco:industrial_iron_block" }, { id: "createdeco:industrial_iron_nugget_from_industrial_iron_ingot" }], "createdeco:industrial_iron_ingot", "#c:ingots/industrial_iron")
    event.replaceInput({ id: "createdeco:pressing/industrial_iron_sheet" }, "createdeco:industrial_iron_ingot", "#c:ingots/industrial_iron")
})
