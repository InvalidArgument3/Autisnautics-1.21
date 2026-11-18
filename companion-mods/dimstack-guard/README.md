# Dimension Stack Guard

Client-only companion. It does not edit Immersive Portals. The pack loads
`dimstackguard-1.0.0.jar` from `.pakku/client-overrides/mods/` (Git LFS). It does
not go in `.pakku/overrides/mods`, because that folder is copied onto the server
and the crash is in a client GUI.

Minecraft 1.21.1, NeoForge 21.1.265. Built with `build.sh` and Java 21. The
audit reads `ip_aeronautics-2.8.17.jar` (sha1
`aac253b7d2f04b514c041ed1197866a16ca57828`). The mod reaches the mod loader by
reflection, so Mixin is the whole compile classpath and no Minecraft jar is
downloaded.

## Build

```
./build.sh
```

That compiles, runs `GuardHarness` and `MixinAudit`, and writes
`.pakku/client-overrides/mods/dimstackguard-1.0.0.jar`. The script finds a JDK
via `JAVA_HOME`, then the Prism `temurin-25`, then `PATH`.

## The problem

`/portal dimension_stack` opens a GUI on the client. Building the first entry
widget runs:

```
DimEntryWidget.<init>
  -> McHelper.getDimensionName(ResourceKey)
       -> O_O.getModName(namespace)
```

`O_O.getModName` is:

```java
FMLLoader.getLoadingModList().getModFileById(modId).getMods()...
```

with **no null check**. `getModFileById` returns null for any namespace that is
not a mod id, so the call throws `NullPointerException` before it can return
anything. The client reports *"The client failed to process a packet from
server"* and the screen never opens.

`getDimensionName` only reaches that call when the dimension has no translation
key, which is true for every datapack dimension. This pack's stack contains five
of them:

| namespace | source |
| --- | --- |
| `oceanworld` | `Autisnautics Ocean` datapack |
| `dimension_of_caves` | `Autisnautics Abyssal Stack` datapack |
| `infinite_abyss` | `Autisnautics Abyssal Stack` datapack |
| `infernalcross` | `Autisnautics Abyssal Stack` datapack |
| `autisnautics` | `Autisnautics Abyssal Stack` datapack |

The live crash was on `oceanworld`, the first entry, right after
`Cannot load texture oceanworld:textures/dimension/deepsea.png`.

## What it changes

`OOModNameMixin` injects at the head of `O_O.getModName` and returns null when
the namespace has no mod behind it, so the method is never entered for a
datapack dimension. Real mods are untouched.

Returning null is the behaviour the caller was already written for.
`McHelper.getDimensionName` guards the result:

```java
String modName = O_O.getModName(namespace);
return Component.translatable("imm_ptl.a_dimension_of",
    modName != null ? modName : namespace)
    .append(" (" + dimension.location() + ")");
```

So a datapack dimension now reads as *"A dimension of oceanworld
(oceanworld:deepsea)"* instead of taking the screen down.

## Why returning null is safe

`MixinAudit` scans all 1322 classes in the Immersive Portals jar and asserts
that both methods are display-only:

```
TARGET getModName dereferences getModFileById with no null check
TARGET McHelper.getDimensionName null-checks the getModName result
  scanned 1322 classes
TARGET O_O.getModName is called only by McHelper
TARGET McHelper.getDimensionName is called only by DimEntryWidget
```

`getModName` has exactly one caller and `getDimensionName` has exactly one
caller, both on the GUI path. If a future build gives either one a caller that
does real work, the audit fails rather than shipping a guard that changes
behaviour somewhere it should not.

The audit also fails if `getModName` ever grows a null check of its own, since
the guard would then be dead code. The build is hash-pinned to
`ip_aeronautics-2.8.17.jar`.

## Rollback

Delete `.pakku/client-overrides/mods/dimstackguard-1.0.0.jar` (and the copy in
`mods/`). The dimension-stack screen goes back to crashing on datapack
dimensions; nothing else changes.
