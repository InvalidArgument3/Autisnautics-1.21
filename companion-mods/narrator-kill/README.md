# Narrator Kill

Client-only companion. It does not edit Minecraft or any other mod. The pack loads `narratorkill-1.0.0.jar` from `.pakku/client-overrides/mods/` (Git LFS). It does not go in `.pakku/overrides/mods`, because that folder is copied onto the server.

Minecraft 1.21.1, NeoForge 21.1.265. Built with `build.sh` and Java 21 against a Mojmap remap of the official 1.21.1 client and `com.mojang:text2speech:1.17.9`. The audit checks each injected method name against the real client jar before it packages anything.

## Build

```
./build.sh
```

Writes `.pakku/client-overrides/mods/narratorkill-1.0.0.jar`. Build inputs are cached in `build/deps` and are gitignored.

## What it changes

`GameNarrator` builds the text-to-speech engine in its constructor by calling `Narrator.getNarrator()`, which loads the native library and starts the narrator thread. `GameNarratorMixin` redirects that call to Mojang's own `Narrator.EMPTY`, so the engine is never created and there is nothing to speak with.

The rest of the mixin is redundancy: `say`, `sayChat`, both `sayNow` overloads and `updateNarratorStatus` cancel at HEAD, and `isActive` returns false. The last one is what makes the hotkey inert — `KeyboardHandler.keyPress` tests `isActive()` before it looks at `narratorHotkey` and `GLFW_KEY_B`, so no key combination reaches the toggle:

```
386: isActive() -> ifeq 525
393: Screen.hasControlDown() -> ifeq 525
399: key == 66 -> if_icmpne 525
425: options.narratorHotkey() -> ifeq 525
```

Removing the jar is the rollback. No world, config or server state is touched.
