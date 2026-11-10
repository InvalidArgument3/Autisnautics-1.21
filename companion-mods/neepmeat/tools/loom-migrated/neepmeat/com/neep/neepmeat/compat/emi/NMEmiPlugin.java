package com.neep.neepmeat.compat.emi;

import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import com.neep.meatlib.recipe.MeatlibRecipes;
import com.neep.neepmeat.NeepMeat;
import com.neep.neepmeat.api.processing.BlockCrushingRegistry;
import com.neep.neepmeat.api.processing.OreFatRegistry;
import com.neep.neepmeat.api.processing.ball_mill.BallMillRecipeGenerator;
import com.neep.neepmeat.client.screen.ImplantManagerScreen;
import com.neep.neepmeat.client.screen.tablet.GuideMainScreen;
import com.neep.neepmeat.compat.emi.helper.LazyEmiRecipeCategory;
import com.neep.neepmeat.compat.emi.recipe.*;
import com.neep.neepmeat.datagen.tag.NMTags;
import com.neep.neepmeat.init.NMBlocks;
import com.neep.neepmeat.init.NMItems;
import com.neep.neepmeat.init.NMScreenHandlers;
import com.neep.neepmeat.init.NMrecipeTypes;
import com.neep.neepmeat.machine.live_machine.LivingMachines;
import com.neep.neepmeat.plc.PLCBlocks;
import com.neep.neepmeat.plc.recipe.ItemManufactureRecipe;
import com.neep.neepmeat.plc.recipe.ItemToEntityRecipe;
import com.neep.neepmeat.plc.recipe.PLCRecipes;
import com.neep.neepmeat.plc.recipe.TransformingToolRecipe;
import com.neep.neepmeat.recipe.AdvancedBlockCrushingRecipe;
import com.neep.neepmeat.recipe.BlockCrushingRecipe;
import com.neep.neepmeat.transport.FluidTransport;
import com.neep.neepmeat.transport.client.screen.AdvancedEjectorScreen;
import com.neep.neepmeat.transport.client.screen.ProcessCardScreen;
import com.neep.neepmeat.transport.client.screen.ProcessCardEmiScreenDragDropHandler;
import com.neep.neepmeat.transport.client.screen.RequesterScreen;
import com.neep.neepmeat.transport.client.screen.filter.FilterEmiDragDropHandler;
import com.neep.neepmeat.transport.client.screen.filter.FilterScreen;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeManager;

public class NMEmiPlugin implements EmiPlugin
{
    public static final EmiStack ALLOY_SMELTING_WORKSTATION = EmiStack.of(NMBlocks.ALLOY_KILN);
    public static final EmiStack COMPACTING_WORKSTATION = EmiStack.of(NMBlocks.CHARNEL_COMPACTOR);
    public static final EmiStack ENLIGHTENING_WORKSTATION = EmiStack.of(NMBlocks.PEDESTAL);
    public static final EmiStack INTEGRATOR_WORKSTATION = EmiStack.of(NMBlocks.INTEGRATOR_EGG);
    public static final EmiStack ADV_INTEGRATOR_WORKSTATION = EmiStack.of(NMBlocks.ADVANCED_INTEGRATOR);
    public static final EmiStack GRINDING_WORKSTATION = EmiStack.of(NMBlocks.CRUSHER);
//    public static final EmiStack LARGE_CRUSHER_WORKSTATION = EmiStack.of(NMBlocks.LARGE_CRUSHER);
    public static final EmiStack CRUSHER_SEGMENT_WORKSTATION = EmiStack.of(LivingMachines.CRUSHER_SEGMENT);
    public static final EmiStack VIVISECTION_WORKSTATION = EmiStack.of(NMItems.TISSUE_OPERATOR);
    public static final EmiStack HEATING_WORKSTATION = EmiStack.of(FluidTransport.MULTI_TANK);
    public static final EmiStack MANUFACTURE_WORKSTATION = EmiStack.of(PLCBlocks.PLC);
    public static final EmiStack MIXING_WORKSTATION = EmiStack.of(NMBlocks.MIXER);
    public static final EmiStack PRESSING_WORKSTATION = EmiStack.of(NMBlocks.HYDRAULIC_PRESS);
    public static final EmiStack CRUCIBLE_WORKSTATION = EmiStack.of(NMBlocks.CRUCIBLE);
    public static final EmiStack SURGERY_WORKSTATION = EmiStack.of(PLCBlocks.PLC);
    public static final EmiStack TROMMEL_WORKSTATION = EmiStack.of(NMBlocks.SMALL_TROMMEL);
    public static final EmiStack LARGE_TROMMEL_WORKSTATION = EmiStack.of(LivingMachines.LARGE_TROMMEL);
    public static final EmiStack BIOREACTOR_WORKSTATION = EmiStack.of(LivingMachines.STOMACH);
    public static final EmiStack BALL_MILL_WORKSTATION = EmiStack.of(LivingMachines.BALL_MILL);
    public static final EmiStack RENDERING_WORKSTATION = EmiStack.of(NMBlocks.CRUCIBLE);

    public static final EmiRecipeCategory ALLOY_SMELTING = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/alloy_smelting"), ALLOY_SMELTING_WORKSTATION);
    public static final EmiRecipeCategory COMPACTING = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/compacting"), COMPACTING_WORKSTATION);
    public static final EmiRecipeCategory ENLIGHTENING = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/enlightening"), ENLIGHTENING_WORKSTATION);
    public static final EmiRecipeCategory GRINDING = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/grinding"), GRINDING_WORKSTATION);
    public static final EmiRecipeCategory ADVANCED_CRUSHING = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/advanced_crushing"), CRUSHER_SEGMENT_WORKSTATION);
    public static final EmiRecipeCategory BLOCK_CRUSHING = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/block_crushing"), GRINDING_WORKSTATION);
    public static final EmiRecipeCategory ADVANCED_BLOCK_CRUSHING = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/advanced_block_crushing"), CRUSHER_SEGMENT_WORKSTATION);
    public static final EmiRecipeCategory ORE_FAT = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/ore_fat"), CRUCIBLE_WORKSTATION);
    public static final EmiRecipeCategory VIVISECTION = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/vivisection"), VIVISECTION_WORKSTATION);
    public static final EmiRecipeCategory HEATING = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/heating"), HEATING_WORKSTATION);
    public static final EmiRecipeCategory ITEM_MANUFACTURE = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/manufacture"), MANUFACTURE_WORKSTATION);
    public static final EmiRecipeCategory ENTITY_TO_ITEM_MANUFACTURE = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/entity_to_item_manufacture"), MANUFACTURE_WORKSTATION);
    public static final EmiRecipeCategory TRANSFORMING_TOOL = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/transforming_tool"), MANUFACTURE_WORKSTATION);
    public static final EmiRecipeCategory MIXING = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/mixing"), MIXING_WORKSTATION);
    public static final EmiRecipeCategory PRESSING = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/pressing"), PRESSING_WORKSTATION);
    public static final EmiRecipeCategory SURGERY = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/surgery"), SURGERY_WORKSTATION);
    public static final EmiRecipeCategory TROMMEL = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/trommel"), TROMMEL_WORKSTATION);
//    public static final EmiRecipeCategory BIOREACTOR = new LazyEmiRecipeCategory(new Identifier(NeepMeat.NAMESPACE, "plugins/bioreactor"), BIOREACTOR_WORKSTATION);
    public static final EmiRecipeCategory BALL_MILL = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/ball_mill"), BALL_MILL_WORKSTATION);
    public static final EmiRecipeCategory RENDERING = new LazyEmiRecipeCategory(new ResourceLocation(NeepMeat.NAMESPACE, "plugins/rendering"), RENDERING_WORKSTATION);

    @Override
    public void register(EmiRegistry registry)
    {
        registry.addCategory(ALLOY_SMELTING);
        registry.addCategory(COMPACTING);
        registry.addCategory(ENLIGHTENING);
        registry.addCategory(GRINDING);
        registry.addCategory(ADVANCED_CRUSHING);
        registry.addCategory(BLOCK_CRUSHING);
        registry.addCategory(ADVANCED_BLOCK_CRUSHING);
        registry.addCategory(ORE_FAT);
        registry.addCategory(VIVISECTION);
        registry.addCategory(HEATING);
        registry.addCategory(ITEM_MANUFACTURE);
        registry.addCategory(ENTITY_TO_ITEM_MANUFACTURE);
        registry.addCategory(MIXING);
        registry.addCategory(PRESSING);
        registry.addCategory(SURGERY);
        registry.addCategory(TRANSFORMING_TOOL);
        registry.addCategory(TROMMEL);
//        registry.addCategory(BIOREACTOR);
        registry.addCategory(BALL_MILL);
        registry.addCategory(RENDERING);

        registry.addWorkstation(ALLOY_SMELTING, ALLOY_SMELTING_WORKSTATION);
        registry.addWorkstation(COMPACTING, COMPACTING_WORKSTATION);
        registry.addWorkstation(ENLIGHTENING, ENLIGHTENING_WORKSTATION);
        registry.addWorkstation(ENLIGHTENING, INTEGRATOR_WORKSTATION);
        registry.addWorkstation(ENLIGHTENING, ADV_INTEGRATOR_WORKSTATION);
        registry.addWorkstation(GRINDING, GRINDING_WORKSTATION);
        registry.addWorkstation(GRINDING, CRUSHER_SEGMENT_WORKSTATION);
        registry.addWorkstation(ADVANCED_CRUSHING, CRUSHER_SEGMENT_WORKSTATION);
        registry.addWorkstation(BLOCK_CRUSHING, CRUSHER_SEGMENT_WORKSTATION);
        registry.addWorkstation(BLOCK_CRUSHING, GRINDING_WORKSTATION);
        registry.addWorkstation(ADVANCED_BLOCK_CRUSHING, CRUSHER_SEGMENT_WORKSTATION);
        registry.addWorkstation(ORE_FAT, PRESSING_WORKSTATION);
        registry.addWorkstation(ORE_FAT, CRUCIBLE_WORKSTATION);
        registry.addWorkstation(ORE_FAT, TROMMEL_WORKSTATION);
        registry.addWorkstation(ORE_FAT, LARGE_TROMMEL_WORKSTATION);
        registry.addWorkstation(VIVISECTION, VIVISECTION_WORKSTATION);
        registry.addWorkstation(HEATING, HEATING_WORKSTATION);
        registry.addWorkstation(ITEM_MANUFACTURE, MANUFACTURE_WORKSTATION);
        registry.addWorkstation(ENTITY_TO_ITEM_MANUFACTURE, MANUFACTURE_WORKSTATION);
        registry.addWorkstation(TRANSFORMING_TOOL, MANUFACTURE_WORKSTATION);
        registry.addWorkstation(MIXING, MIXING_WORKSTATION);
        registry.addWorkstation(PRESSING, PRESSING_WORKSTATION);
        registry.addWorkstation(SURGERY, SURGERY_WORKSTATION);
        registry.addWorkstation(TROMMEL, TROMMEL_WORKSTATION);
        registry.addWorkstation(TROMMEL, LARGE_TROMMEL_WORKSTATION);
//        registry.addWorkstation(BIOREACTOR, BIOREACTOR_WORKSTATION);
        registry.addWorkstation(BALL_MILL, BALL_MILL_WORKSTATION);
        registry.addWorkstation(RENDERING, RENDERING_WORKSTATION);

        RecipeManager manager = registry.getRecipeManager();
        manager.getAllRecipesFor(NMrecipeTypes.ENLIGHTENING)
                .stream()
                .map(EnlighteningEmiRecipe::new)
                .forEach(registry::addRecipe);
        manager.getAllRecipesFor(NMrecipeTypes.MIXING)
                .stream()
                .map(MixingEmiRecipe::new)
                .forEach(registry::addRecipe);
        manager.getAllRecipesFor(NMrecipeTypes.PRESSING)
                .stream()
                .map(PressingEmiRecipe::new)
                .forEach(registry::addRecipe);

        MeatlibRecipes instance = MeatlibRecipes.getInstance();

        instance.getAllValuesOfType(NMrecipeTypes.ALLOY_SMELTING)
                .map(AlloySmeltingEmiRecipe::new)
                .forEach(registry::addRecipe);
        instance.getAllValuesOfType(NMrecipeTypes.GRINDING)
                .filter(r -> !r.destroy())
                .filter(r -> !(r instanceof BlockCrushingRecipe))
                .map(r -> new GrindingEmiRecipe(GRINDING, r))
                .forEach(registry::addRecipe);
        instance.getAllValuesOfType(NMrecipeTypes.ADVANCED_CRUSHING)
                .filter(r -> !r.destroy())
                .filter(r -> !(r instanceof BlockCrushingRecipe))
                .map(r -> new GrindingEmiRecipe(ADVANCED_CRUSHING, r))
                .forEach(registry::addRecipe);
        instance.getAllValuesOfType(NMrecipeTypes.HEATING)
                .map(HeatingEmiRecipe::new)
                .forEach(registry::addRecipe);
        instance.getAllValuesOfType(NMrecipeTypes.RENDERING)
                .map(RenderingEmiRecipe::new)
                .forEach(registry::addRecipe);
        instance.getAllValuesOfType(PLCRecipes.MANUFACTURE)
                .map(r -> r instanceof ItemManufactureRecipe r1 ? r1 : null)
                .filter(Objects::nonNull)
                .map(ItemManufactureEmiRecipe::new)
                .forEach(registry::addRecipe);
        instance.getAllValuesOfType(PLCRecipes.MANUFACTURE)
                .map(r -> r instanceof ItemToEntityRecipe r1 ? r1 : null)
                .filter(Objects::nonNull)
                .map(ItemToEntityEmiRecipe::new)
                .forEach(registry::addRecipe);
        instance.getAllValuesOfType(PLCRecipes.ENTITY_TO_ITEM)
                .map(EntityToItemManufactureEmiRecipe::new)
                .forEach(registry::addRecipe);
        instance.getAllValuesOfType(NMrecipeTypes.TROMMEL)
                .map(TrommelEmiRecipe::new)
                .forEach(registry::addRecipe);
//        instance.getAllValuesOfType(NMrecipeTypes.BIOREACTOR)
//                .map(BioreactorEmiRecipe::new)
//                .forEach(registry::addRecipe);
        instance.getAllValuesOfType(NMrecipeTypes.VIVISECTION)
                .map(VivisectionEmiRecipe::new)
                .forEach(registry::addRecipe);

        // Ball Mill
        instance.getAllValuesOfType(NMrecipeTypes.BALL_MILL)
                .map(BallMillEmiRecipe::new)
                .forEach(registry::addRecipe);
        registry.addDeferredRecipes(consumer ->
                BallMillRecipeGenerator.INSTANCE.getSynthetic().stream().map(BallMillEmiRecipe::new).forEach(consumer));

        registry.addRecipe(new ToolManufactureEmiRecipe(TransformingToolRecipe.getInstance()));

        // Charnel Compactor recipes
        int page = 0;
        UnmodifiableIterator<List<Item>> iterator = Iterators.partition(BuiltInRegistries.ITEM.getTag(NMTags.CHARNEL_COMPACTOR).orElseThrow().stream().map(Holder::value).iterator(), 35);
        while (iterator.hasNext())
        {
            List<Item> entries = iterator.next();
            registry.addRecipe(new CompactingEmiRecipe(entries, NMItems.CRUDE_INTEGRATION_CHARGE, page++));
        }

        registry.addRecipe(new VivisectionEmiRecipe(NMBlocks.INTEGRATOR_EGG.asItem(), NMItems.CHRYSALIS));

        registry.addDeferredRecipes(consumer ->
        {
            @Nullable BlockCrushingRecipe crushingRecipe = BlockCrushingRecipe.INSTANCE;
            if (crushingRecipe != null)
                addBlockCrushingRecipes(crushingRecipe, GRINDING, BlockCrushingRegistry.INSTANCE.getBasicEntries(), consumer);

            @Nullable AdvancedBlockCrushingRecipe advancedCrushingRecipe = AdvancedBlockCrushingRecipe.INSTANCE;
            if (advancedCrushingRecipe != null)
                addBlockCrushingRecipes(advancedCrushingRecipe, ADVANCED_CRUSHING, BlockCrushingRegistry.INSTANCE.getAdvancedEntries(), consumer);

            addOreFatRecipes(ORE_FAT, consumer);
        });

        // Recipe Handlers
        registry.addRecipeHandler(NMScreenHandlers.FABRICATOR, new FabricatorRecipeHandler());


        registry.addDragDropHandler(ProcessCardScreen.class, new ProcessCardEmiScreenDragDropHandler());
        registry.addDragDropHandler(ImplantManagerScreen.class, new FilterEmiDragDropHandler<>());
        registry.addDragDropHandler(FilterScreen.class, new FilterEmiDragDropHandler<>());
        registry.addDragDropHandler(AdvancedEjectorScreen.class, new FilterEmiDragDropHandler<>());
        registry.addDragDropHandler(RequesterScreen.class, new FilterEmiDragDropHandler<>());

//        registry.addExclusionArea(GuideMainScreen.class,
    }

    private static void addOreFatRecipes(EmiRecipeCategory category, Consumer<? super EmiRecipe> consumer)
    {
        for (var entry : OreFatRegistry.INSTANCE.getCollectedInputs().entrySet())
        {
            OreFatProcessingEmiRecipe recipe = OreFatProcessingEmiRecipe.ofItemVariants(category, entry.getValue(), entry.getKey());
            consumer.accept(recipe);
        }
    }

    private static void addBlockCrushingRecipes(BlockCrushingRecipe recipe, EmiRecipeCategory category, Collection<BlockCrushingRegistry.Entry> entries, Consumer<? super EmiRecipe> consumer)
    {
        int index = 0;
        for (var entry : entries)
        {
            consumer.accept(new BlockCrushingEmiRecipe(index, category, recipe, entry));
            index++;
        }
    }
}
