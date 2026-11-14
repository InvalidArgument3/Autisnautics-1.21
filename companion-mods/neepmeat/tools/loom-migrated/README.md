Loom-migrated upstream sources. The generated tree is the source of truth for the port, and it is not committed.

`regenerate.sh` fetches Codeberg commit `0d36776`, checks out meatlib, pins Fabric Loom to 1.9.2, and runs `migrateMappings`. It writes `neepmeat/` and `meatlib/` next to this file. Those directories are gitignored.

Both outputs are Mojmap 1.20.1 names:

```
./gradlew migrateMappings --mappings "net.minecraft:mappings:1.20.1"
```

That was run on a scratch copy of Codeberg commit `0d36776` (`1.20/dev`). Overrides are renamed (`newBlockEntity`, `createBlockStateDefinition`, `getBlockPos`). These files are still the 1.20.1 API. `tools/loom-migrated/apply_121_drift.py` rewrites the common 1.21 signature changes onto a copy. Do not edit this tree to match 1.21. Regenerate it with Loom if upstream moves.

The Mojang mappings license forbids redistributing the mappings file itself. This directory is the project's own source after renaming Minecraft references. It is not the mappings file.
