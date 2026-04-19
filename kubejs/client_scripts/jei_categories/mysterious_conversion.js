// We have to brute force things since the kubejs create guide doesn't work at all
const __RV = typeof JEIEvents !== "undefined" ? JEIEvents : (typeof RecipeViewerEvents !== "undefined" ? RecipeViewerEvents : null)

if (__RV && __RV.removeRecipes) __RV.removeRecipes(event => {
    if (!global.jeiRuntime || !global.jeiRuntime.jeiHelpers || !global.jeiRuntime.recipeManager) return
    const mysteryConversion = global.jeiRuntime.jeiHelpers.getRecipeType("create:mystery_conversion").get()
    global.jeiRuntime.recipeManager.addRecipes(mysteryConversion, [
        // Refined radiance mysterious conversion
        new ConversionRecipe.create("create:chromatic_compound", "create:refined_radiance"),
        new ConversionRecipe.create("create:chromatic_compound", "create:shadow_steel")
    ]);
})
