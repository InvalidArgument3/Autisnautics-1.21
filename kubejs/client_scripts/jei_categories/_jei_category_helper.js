// priority: 100

// Java imports
// Mysterious Conversion recipe type
const ConversionRecipe = Java.loadClass("com.simibubi.create.compat.jei.ConversionRecipe")
// Used for creating Drawable graphics for JEI
const ResourceLocation = Java.loadClass("net.minecraft.resources.ResourceLocation");
const ChatFormatting = Java.loadClass("net.minecraft.ChatFormatting");
const JEIIDrawableAnimated = Java.loadClass("mezz.jei.api.gui.drawable.IDrawableAnimated");
// Used to make the FE energy tooltip when hovering over the energy bar
let ThermalStringHelper = null;
try {
    ThermalStringHelper = Java.loadClass("cofh.lib.util.helpers.StringHelper");
} catch (e) {
    ThermalStringHelper = {
        getTextComponent: (key) => Text.translatable(key),
        format: (value) => String(value),
        localize: (key) => key === "info.cofh.unit_rf" ? "RF" : key
    };
}
// Used for to draw block models in jei categories
const JavaBoolean = Java.loadClass("java.lang.Boolean");
const Axis = Java.loadClass("com.mojang.math.Axis");
const BlockStateProperties = Java.loadClass("net.minecraft.world.level.block.state.properties.BlockStateProperties");
const AnimatedKinetics = Java.loadClass("com.simibubi.create.compat.jei.category.animations.AnimatedKinetics");

const AttachFace = Java.loadClass("net.minecraft.world.level.block.state.properties.AttachFace")

// Category state is initialized lazily in JEI event scripts when JEIAddedEvents exists.
