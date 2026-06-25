ServerEvents.recipes(event => {
    if (Item.exists("createdeco:cast_iron_nugget")) {
        event.replaceInput({id: "createdeco:cast_iron_ingot"}, "createdeco:cast_iron_nugget","#forge:nuggets/cast_iron")
    }
    if (Item.exists("createdeco:cast_iron_block")) {
        event.replaceInput({id: "createdeco:cast_iron_ingot_from_cast_iron_block"}, "createdeco:cast_iron_block","#forge:storage_blocks/cast_iron")
        event.replaceInput([{id: "createdeco:cast_iron_block"}, {id: "createdeco:cast_iron_nugget_from_cast_iron_ingot"}], "createdeco:cast_iron_ingot","#forge:ingots/cast_iron")
    }
    event.remove({ id: "minecraft:pressing/cast_iron_sheet" })
    event.remove({ id: "createdeco:pressing/cast_iron_sheet" })
    if (Item.exists("createdeco:industrial_iron_sheet")) {
        event.recipes.create.pressing("createdeco:industrial_iron_sheet", "createdeco:industrial_iron_ingot")
    }
})
