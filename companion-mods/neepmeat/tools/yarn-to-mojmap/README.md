Prefer Fabric Loom when the upstream project can resolve. On a scratch copy of commit `0d36776` (`1.20/dev`):

```
./gradlew migrateMappings --mappings "net.minecraft:mappings:1.20.1"
```

Loom 1.9.2 ran that successfully. Remapped sources land in `remappedSrc` for NeepMeat and `subprojects/meatlib/remappedSrc` for meatlib. Overrides rename: `createBlockEntity` becomes `newBlockEntity`, `appendProperties` becomes `createBlockStateDefinition`, `getPos` becomes `getBlockPos`. That is path A.

Path B, if Loom cannot resolve, is Mercury against a Yarn-named 1.20.1 client jar. It misses some override declarations and a full-tree run can stop on an Eclipse null binding.

Yarn 1.20.1 sources to Mojmap 1.20.1, using Mercury and tiny-remapper.

`prepare_tiny.py` writes two tiny v2 files from the Yarn tiny, the intermediary tiny, and Mojang's client mappings:

- `official-to-yarn.tiny` is not used for the client jar. The client jar is remapped with the original intermediary and Yarn tiny files, which do not have the merged-map conflicts.
- `yarn-to-mojmap.tiny` is owner-aware: class, method descriptor, and field. 6,918 classes and 25,747 renamed methods.

`remap.sh` downloads nothing. It expects `/tmp/remap` to already contain `yarn.tiny`, `intermediary.tiny`, `client-1.20.1.txt`, and `client-1.20.1.jar`, and `/tmp/mercury` to contain Mercury 0.6.0, tiny-remapper 0.11.0, and mapping-io 0.7.1.

```
bash tools/yarn-to-mojmap/remap.sh <yarn-source-root> <output-dir>
```

Put meatlib's `src/main/java` tree at `/tmp/meatlib-src` when the sources need it to resolve types. Mercury renames resolved members and imports. A full-tree run can throw an Eclipse null binding and stop. Method overrides such as `createBlockEntity` are not always renamed even when the mapping contains `newBlockEntity`. This pass does not apply 1.20.1 to 1.21.1 API drift. Do not commit Mojang's client jar or mapping file.
