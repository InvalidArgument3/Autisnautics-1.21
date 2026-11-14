# Keybind defaults (0.0.91 layout)

`config/defaultoptions/keybindings.txt` is what DefaultOptions writes into a new client’s `options.txt`. It has one line per live key id from the 1.21.1 pack (34 vanilla lines, kept as they already were, then every live mod bind). Ids from mods that are no longer in the pack are gone.

A fresh install picks this up automatically. An existing client does not: DefaultOptions does not rewrite `options.txt` after the first run. Those clients load the same set from Raw’s Visual Keybinder (below).

## Keys that are not the audit’s recommendation

The audit’s KEEP / REMAP / BLANK plan is the base. These maintainer choices replace it:

| Action | Id | Key |
|---|---|---|
| Cobblemon throw | `key.cobblemon.throwpartypokemon` | R |
| Iron’s spell wheel | `key.irons_spellbooks.spell_wheel` | C |
| Iron’s cast | `key.irons_spellbooks.spellbook_cast` | G |
| Voice push-to-talk | `key.push_to_talk` | Caps Lock |
| Voice menu | `key.voice_chat` | J |
| Just Zoom | `justzoom.keybinds.keybind.zoom` | N |
| Harness menu | `key.adventurers_harness.radial_menu` | I |
| Harness grapple | `key.adventurers_harness.grapple` | Mouse 5 |
| Gun reload | `key.irons_artifice.reload` | O |
| Quests | `key.ftbquests.quests` | K |
| New waypoint | `gui.xaero_new_waypoint` | `;` |

NeoOrigins is unbound on every id, including the ones the audit would have moved to N, I, and O. Those three letters are the zoom, harness menu, and reload keys. Z and C were the obvious zoom keys; Z is the Ars spellbook previous-slot key (and walker / Octolegs suspension), and C is the spell wheel. X was the harness menu’s own default, and it is the Ars next-slot key plus walker and Octolegs controls. R is Cobblemon throw, so reload cannot stay there.

Harness climbing (`key.adventurers_harness.climbing`, default C) and harness inventory stay unbound. In `adventurers_harness` 1.2 the radial menu already sends climb-toggle and open-inventory (`RadialMenuScreen` → `C2S$ClimbToggle` / `C2S$OpenInventory`). Grapple is not on that menu, so it keeps Mouse 5, which nothing else uses. Just Zoom’s options key stays unbound (it was B, the backpack key). Zoom in / zoom out stay unbound. The gun modifier menu stays unbound (it was G, which is now cast).

Cobblemon’s four numpad party keys move to the arrow keys: Up party-shift up (was KP 8), Down party-shift down (was KP 5), Left hide the party overlay (was KP 7), Right open the summary (was KP 9). M is the world map, so summary cannot keep Cobblemon’s usual M. O is reload, so hide-overlay cannot keep Cobblemon’s usual O.

Three situational binds stay bound because they only mirror a key you are already holding in that state: Hypertube escape (Left Shift), Ornithopter boost (Space), Yo-Hooks hook jump (Space).

## Ultimine shape keys stay on Up / Down

`ftb-ultimine-neoforge-2101.1.15`, `FTBUltimineClient.onKeyPress`: a shape-next / shape-prev press changes shape only when `(pressed || !require_ultimine_key_for_cycling) && isMenuSneaking()`. `pressed` is `keyBindUltimine.isDown()`. Both config flags default to true (`FTBUltimineClientConfig` `addBoolean(..., true)`), and this pack does not ship an `ftbultimine-client.snbt` that changes them. The Ultimine key itself stays grave accent.

So Up and Down change the Cobblemon party in normal play and do not cycle Ultimine shapes unless grave is held. With the default sneak requirement they also do not cycle shapes unless Shift is held. On NeoForge, `ClientPlatformUtilImpl.doesKeybindMatch` treats a plain Up binding as unmatched while Shift is down (modifier bits must be 0). That is the bug in [FTB-Mods-Issues #1881](https://github.com/FTBTeam/FTB-Mods-Issues/issues/1881): with stock settings the arrow keys do not cycle shapes at all. If a player turns off “Require Sneak For Shape Menu”, Up / Down cycle shapes only while grave is held, and they also move the party. That overlap is the hold, not ordinary movement.

## Visual Keybinder preset

`visual_keybinder` 1.21.1-0.1.13 (`FileUtil`) reads presets from the instance folder `visual_keybinder/presets/<name>.preset.txt`. `savePreset` writes `keyId:saveString()` and appends `:CONTROL`, `:SHIFT`, or `:ALT` only when `KeyModifier` is not `NONE`. A first line of exactly `readonly` is skipped on load and blocks an in-game save from overwriting the file. Keys missing from a preset are set to unknown when that preset is loaded (`loadPreset` → `KeyMapping.setKeyModifierAndCode(NONE, UNKNOWN)`).

The pack file is `.pakku/client-overrides/visual_keybinder/presets/Autisnautics Default.preset.txt`. Pakku copies client-overrides onto the client instance, which is the directory the mod scans. The line list is the DefaultOptions file with the `key_` prefix removed and with `:NONE` omitted. The `readonly` header is the only extra line.

To load it: Esc → Options → Controls → Visual Keybinder (or F10) → Presets → Autisnautics Default. The five “switch to preset” binds are left unbound, so the list is the way to select it. The mod can also apply a preset on first Options init from `visual_keybinder/preset to load on first startup.txt` if `initialized.dat` is absent. That file is not shipped, so an existing client is not rewritten just by updating the pack.

## Clash check

A plain key still fires in-world while Ctrl, Shift, or Alt is held, so a modifier on one bind is not treated as separating it from another in-world bind on the same key. Pairs of always-on in-world binds on the same key: **none**.

What still shares a key:

- Left Shift, Left Ctrl, and Left Alt are shared on purpose (sneak / sprint / carry with the Create modifiers and the spell-bar scroll modifier).
- Up / Down: Cobblemon party shift, and Ultimine shape cycling only while grave is held (above). Quest-editor and JEI search arrows are screen-only.
- Vehicle and held-item keys reuse movement and a few letters (Octolegs and Let It Walk on WASD, Space, Q, R, F, G, V, Z, X, Tab, Alt; Ars spellbook Z / X / V; tool guns on Tab). They do not fire on foot with empty hands.
- Screen binds (JEI, quest GUI, quest editor, Refined Storage search) reuse keys that also do something in the world. They do not run while no screen is open.

## Source notes the audit could not see from names

- Ultimine shape next / prev are not always-on. See above.
- Harness keys are registered `KeyConflictContext.IN_GAME`. The menu key calls `consumeClick` even with no harness worn and, if the key is pressed in the world with no screen open, prints “no harness”. Grapple’s client tick also `consumeClick`s and sends `C2S$Grapple` with no worn-harness check on the client. Mouse 5 and I are otherwise free, so that does not add a second in-world action.
- Iron’s Artifice reload and the modifier menu use the plain `KeyMapping` constructor, which is `KeyConflictContext.UNIVERSAL`, not an item context. `tryInitiateReload` still returns immediately unless there is no screen open and the main hand is a `GunItem`. O has no other bind. The modifier menu stays unbound.
- Ponder’s “hold over item” bind was left on W as the audit classified it (GUI). That handler was not re-read here. If it actually runs with no screen open, it would share W with walking.
