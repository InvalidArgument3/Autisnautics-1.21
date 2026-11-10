/*
 * Copyright (c) MeatWheeze 2025.
 *
 * This file is part of NEEPMeat.
 *
 * NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * NEEPMeat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with NEEPMeat. If not, see <https://www.gnu.org/licenses/>.
 */

package com.neep.neepmeat.plc.instruction;

import com.neep.meatlib.util.NeepAsmTokenView;
import com.neep.neepmeat.api.plc.PLC;
import com.neep.neepmeat.api.storage.LazyBlockApiCache;
import com.neep.meatlib.neepasm.NeepASM;
import com.neep.neepmeat.neepasm.compiler.NeepAsmParser;
import com.neep.neepmeat.neepasm.compiler.parser.PlcParsedInstruction;
import com.neep.neepmeat.plc.Instructions;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import com.neep.meatlib.transfer.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.neep.neepmeat.neepasm.compiler.NeepAsmParser.convertToRegex;
import static com.neep.neepmeat.neepasm.compiler.NeepAsmParser.isSimplePattern;

public class CountInstruction implements PlcInstruction
{
    private static final Map<ResourceLocation, BlockApiLookup<? extends Storage<? extends TransferVariant<?>>, Direction>> LOOKUP_MAP = Map.of(
            ItemStorage.SIDED.getId(), ItemStorage.SIDED,
            FluidStorage.SIDED.getId(), FluidStorage.SIDED
    );

    private final BlockApiLookup<? extends Storage<? extends TransferVariant<?>>, Direction> lookup;
    private final LazyBlockApiCache<? extends Storage<? extends TransferVariant<?>>, Direction> targetCache;
    private final Argument target;
    private final Pattern pattern;

    public CountInstruction(Supplier<Level> world, BlockApiLookup<? extends Storage<? extends TransferVariant<?>>, Direction> lookup, Argument target, String pattern)
    {
        this.target = target;
        this.pattern = Pattern.compile(pattern);
        this.lookup = lookup;
        this.targetCache = LazyBlockApiCache.of(lookup, world, target);
    }

    public CountInstruction(Supplier<Level> worldSupplier, CompoundTag nbt)
    {
        this(worldSupplier, LOOKUP_MAP.getOrDefault(ResourceLocation.tryParse(nbt.getString("lookup")), ItemStorage.SIDED), Argument.fromNbt(nbt.getCompound("target")), nbt.getString("pattern"));
    }

    @Override
    public CompoundTag writeNbt(CompoundTag nbt)
    {
        nbt.putString("lookup", lookup.getId().toString());
        nbt.put("target", target.toNbt());
        nbt.putString("pattern", pattern.pattern());
        return nbt;
    }

    @Override
    public void start(PLC plc) throws NeepASM.RuntimeException
    {
        Storage<? extends TransferVariant<?>> storage = targetCache.find();
        long found = 0;
        if (storage != null)
        {
            for (var view : storage)
            {
                if (matches(view.getResource()))
                {
                    found += view.getAmount();
                }
            }
        }
        plc.dataStack().push((int) found);
        plc.advanceCounter();
    }

    private <T extends TransferVariant<?>> boolean matches(T resource)
    {
        return pattern.asMatchPredicate().test(resourceToEntry(resource));
    }

    private <T extends TransferVariant<?>> String resourceToEntry(T resource)
    {
        // Urgh
        if (resource.getObject() instanceof Item item)
        {
            return item.builtInRegistryHolder().key().location().toString();
        }
        else if (resource.getObject() instanceof Fluid fluid)
        {
            return fluid.builtInRegistryHolder().key().location().toString();
        }
        return "";
    }

    @Override
    public @NotNull InstructionProvider getOpcode()
    {
        return Instructions.COUNT;
    }

    public static PlcParsedInstruction parser(NeepAsmTokenView view, NeepAsmParser parser) throws NeepASM.ParseException
    {
        view.fastForward();
        Argument argument = parser.parseWorldTarget(view);
        if (argument == null)
            throw new NeepASM.ParseException("expected storage world target");

        view.fastForward();

        Registry<?> registry;

        if (view.peek() != '\"' && !view.lineEnded())
        {
            // Read 'item' or 'fluid'

            String identifier = view.nextIdentifier();
            if (identifier.isEmpty())
                throw new NeepASM.ParseException("Expected 'item' or 'fluid'");

            registry = switch (identifier.toLowerCase(Locale.ROOT))
            {
                case "item" -> BuiltInRegistries.ITEM;
                case "fluid" -> BuiltInRegistries.FLUID;
                default -> null;
            };

            if (registry == null)
                throw new NeepASM.ParseException("Expected 'item' or 'fluid'");
        }
        else
        {
            registry = null;
        }

        view.fastForward();

        String regex;
        String checkRegistry;
        if (view.peek() == '\"')
        {
            // Read ID
            String notGlob = view.nextString();
            if (notGlob != null)
            {
                if (isSimplePattern(notGlob))
                {
                    checkRegistry = notGlob;
                }
                else
                {
                    checkRegistry = null;
                }
                regex = convertToRegex(notGlob);
            }
            else
            {
                throw new NeepASM.ParseException("unterminated string");
            }
        }
        else
        {
            regex = ".*";
            checkRegistry = null;
        }

        parser.assureLineEnd(view);

        return (world, parsedSource1, program) ->
        {
            BlockApiLookup<? extends Storage<? extends TransferVariant<?>>, Direction> lookup = findLookup(registry, world, argument, checkRegistry);
            program.addBack(new CountInstruction(() -> world, lookup, argument, regex));
        };
    }

    private static BlockApiLookup<? extends Storage<? extends TransferVariant<?>>, Direction> findLookup(@Nullable Registry<?> registry, Level world, Argument argument, @Nullable String checkRegistry) throws NeepASM.CompilationException
    {
        if (registry == BuiltInRegistries.ITEM)
        {
            checkRegistry(registry, checkRegistry);
            checkStorage(ItemStorage.SIDED, "item", world, argument);

            return ItemStorage.SIDED;
        }

        if (registry == BuiltInRegistries.FLUID)
        {
            checkRegistry(registry, checkRegistry);
            checkStorage(FluidStorage.SIDED, "fluid", world, argument);

            return FluidStorage.SIDED;
        }

        if (registry == null)
        {
            // Infer type if none specified
            var itemStorage = findStorage(ItemStorage.SIDED, world, argument);
            if (itemStorage != null)
            {
                checkRegistry(BuiltInRegistries.ITEM, checkRegistry);
                return ItemStorage.SIDED;
            }

            var fluidStorage = findStorage(FluidStorage.SIDED, world, argument);
            if (fluidStorage != null)
            {
                checkRegistry(BuiltInRegistries.FLUID, checkRegistry);
                return FluidStorage.SIDED;
            }

        }

        throw new NeepASM.CompilationException(String.format("no item or fluid storage at %s", argument));
    }

    @Nullable
    private static <T> T findStorage(BlockApiLookup<T, Direction> lookup, Level world, Argument argument)
    {
        return lookup.find(world, argument.pos(), argument.face());
    }

    private static <T> void checkStorage(BlockApiLookup<T, Direction> lookup, String type, Level world, Argument argument) throws NeepASM.CompilationException
    {
//        if (findStorage(lookup, world, argument) == null)
//            throw new NeepASM.CompilationException(String.format("no %s storage at %s", type, argument));
    }

    private static <T> void checkRegistry(Registry<T> registry, @Nullable String string) throws NeepASM.CompilationException
    {
        if (string == null)
            return;

        T object = registry.getOptional(ResourceLocation.tryParse(string)).orElse(null);
        if (object == null)
            throw new NeepASM.CompilationException(registry.key().location().getPath() + " '" + string + "' not known");
    }
}
