# Autisnautics quest layout — design

Working design for how the FTB Quests book should feel and flow with the mods we ship.
Companion files: `progression-matrix.csv` (track status), `questline-planning.md` (workflow).

Status: **spine remapped + softlocks gated**. Overview Moonshot deps Taking Flight (Skyworthy) + rocket + Stage 5. Origin/economy chapters hidden. `chapters.js` Thermal/Tinkers/Ad Astra leftovers gated; Autis Create silicon + digit/matrix path live. Playtest Stage 1 → Flight → Moon.

---

## 1. Fantasy

Players wake on **sky islands**, pick an **Origin**, then climb from **Create kinetics** into **oil and heavy industry**, then **airships**, then **Stellaris space**. Combat and Psi are optional branches, not the main ladder.

One sentence: *build machines, fly ships, leave the sky.*

Tone: CABIN-style gated craftsmanship, Autisnautics fantasy (Aeronautics / Sable / Stellaris), not Thermal-era CABIN or Ad Astra.

---

## 2. Design principles

1. **One main spine.** Overview chapter is a short linear map of stages. Side chapters never softlock the spine.
2. **Gates are craftable items we actually ship.** Prefer `kubejs:*_mechanism` / machines, then IE / Aeronautics / Stellaris items. No Thermal, Ad Astra, Tinkers, or Project Red IDs.
3. **Each stage teaches one fantasy beat.** Kinetics → brass/AE seeds → power/oil → calculation → process industry → flight → space.
4. **Origins are flavor, not progression.** Origin chapters give toys and identity. They do not unlock Stage N.
5. **Dead economy is gone.** The Remote Trading group (The Market, Bulletin Board/professions, Shipments) is deleted from the quest book. Re-add only if we pick a live currency (Numismatics or none).
6. **Book mirrors recipes.** If a quest asks for X, KubeJS must craft X without missing mods.
7. **Fewer chapters, denser pages.** Prefer rewriting Stage 1–5 over adding ten tiny Autis chapters.

---

## 3. Book layout (chapter groups)

Proposed groups in the quest sidebar:

| Group | Chapters | Role |
|---|---|---|
| **Tutorial** | Welcome, Basic Info, Starting Gear, Origin Selector | Onboarding only |
| **Main** | Overview, Stage 1…5, Metallurgy | Spine + metal primer |
| **Sky & Space** | Taking Flight, Airship Core, Stellaris | Autis fantasy centerpiece |
| **Branches** | Munitions, Ghost in the Machine, FLAOT | Optional power spikes |
| **Origins** | Per-origin chapters that still have live mods | RP / bonuses |
| **Hidden / retired** | CABIN Thermal/Ad Astra stubs | Not shown |

Overview sits first in **Main** and links stage completion quests only.

---

## 4. Main spine

### Stage 0 — Tutorial + Origin

- **Mods:** NeoOrigins, Autis originpack, FTB Quests
- **Gate out:** pick an origin + acknowledge Basic Info
- **Intent:** explain sky world, quest book, no softlock from missing starting kits
- **Do:** strip Occultism / Thermal crumbs from Welcome rewards
- **Files:** `welcome`, `basic_info`, `starting_gear`, `origin_selector`

### Stage 0.5 — Metallurgy (primer)

- **Mods:** Create, Immersive Engineering (metals/plates), ore processing we keep
- **Gate:** first useful metal tools / plates (Create or IE, not Tinkers)
- **Intent:** “how metal works here” before machines get serious
- **Do:** keep `metallurgy` healthy; rewrite or hide `professions` until currency exists
- **Files:** `metallurgy` (keep), `professions` (retire or park)

### Stage 1 — Andesite / Kinetics

- **Mods:** Create, Create Diesel Generators (early tools), Architect’s Palette
- **Gate:** `kubejs:kinetic_mechanism` → `kubejs:andesite_machine`
- **Intent:** first factory heartbeat; andesite alloy on Autis recipe (algal brick path)
- **Must fix:** sealed/rubber path without Thermal (CDG / IE treated products / custom kubejs)
- **Files:** `stage_1` rewrite

### Stage 2 — Brass, logistics seeds, early AE

- **Mods:** Create (brass), AE2 (certus seeds / growth), NuclearCraft NeoHaul (first touch only if it fits)
- **Gate:** brass-tier mechanism or `logistic_mechanism` + first certus product
- **Intent:** precision crafting + “computers start as crystals”
- **Do:** replace old `nuclearcraft:` quest IDs; Tinkers tasks go away
- **Files:** `stage_2` rewrite

### Stage 3 — Power and oil

- **Mods:** Immersive Engineering, Immersive Petroleum, Create Addition, Create Diesel Generators, Powergrid (light touch)
- **Gate:** inductive / power mechanism, or a concrete IE milestone (LV capacitor bank / diesel generator / pumpjack)
- **Intent:** this is where the pack stops being “Create only” and becomes industrial
- **Do:** delete Thermal dynamo/machine quests; IE/IP are the stand-ins
- **Files:** `stage_3` rewrite

### Stage 4 — Calculation / AE2 midgame

- **Mods:** AE2, Create, optional ComputerCraft teaser
- **Gate:** `kubejs:calculation_mechanism` (and processors if we keep that line)
- **Intent:** digital logistics unlock; storage/autocrafting becomes viable
- **Do:** remove Thermal + Occultism tasks
- **Files:** `stage_4` rewrite

### Stage 4b — Process industry

- **Mods:** Immersive Petroleum distillation, CDG, NuclearCraft NeoHaul processing, Create fluids
- **Gate:** new Autis gate (e.g. “refinery suite” checkmark set: still + tower + one NC process) — **not** Thermal bottler/sawmill/centrifuge
- **Intent:** fuels, plastics-adjacent materials, nuke feedstock; feeds flight fuel and late power
- **Files:** `stage_4b` full remap

### Stage 5 — Taking Flight, then the Rocket

Same emotional arc as Create Above and Beyond: industry → flight toys/ships → **launch a rocket**.

1. **Taking Flight** (`angel`) — atmosphere: gliders, Immersive Aircraft, Aeronautics airships
2. **Rocket** (`stage_5`) — Stellaris launch pad + rocket (replaces Ad Astra tier-1 rocket finale)

- **Mods:** Create Aeronautics (+ avionics, propellers, flight control, logistics, toolgun), Sable, Immersive Aircraft / VC Gliders / Jetpack on-ramps, **Stellaris**
- **Gates:**
  - Flight: first flyable craft / Aeronautics milestone (Taking Flight chapter complete)
  - Pack complete: **launch (or craft+fuel+pad) a Stellaris rocket** — CAB-style end boss quest
- **Intent:** Ships are the Autis fantasy midgame climax; **rocket launch is the questline end**, not an optional side grade
- **Files:** expand `angel`; rewrite `stage_5` Ad Astra → Stellaris IDs; Airship Core folds into Taking Flight unless it gets too crowded

### NuclearCraft NeoHaul placement

Not its own mandatory stage. Touches:

- light intro in Stage 2 or 3 if uranium shows up naturally
- real commitment in Stage 4b / early Stage 5 as high-density ship/base power

Quest copy should say NeoHaul, never old NuclearCraft item IDs.

---

## 5. Side branches (optional)

| Chapter (player title) | File today | Mods | Opens after | Intent |
|---|---|---|---|---|
| Taking Flight | `angel` | Aircraft, gliders, Aeronautics, jetpack | Stage 3+ (gliders) / Stage 5 (ships) | Canonical flight on-ramp; keep |
| Munitions Plant | `cactoid` | Create Big Cannons, Create | Stage 3 | Ship guns / base defense; CBC not SCGuns |
| Ghost in the Machine | `daemon` | Psi, ComputerCraft | Stage 4 | Spellcasting + CC; drop MnA |
| FLAOT | `generic` | toys from live mods | Stage 2+ | Side doodads; trim dead IDs |
| Origins (each) | `blobbian`, `rat`, … | NeoOrigins + whatever that origin needs | Stage 0 | Keep only if every task item exists |

Branches may reward useful gear, never spine gates.

---

## 6. Explicit retire / do-not-use

Do not add these back for quest convenience:

- Thermal Series (machines, coins, rubber, fuels)
- Ad Astra (rockets, suits, fuel)
- Tinkers Construct / Tinkers Jewelry
- Project Red, Pretty Pipes
- Occultism, Rats, Mana and Artifice (unless mod returns)
- Biomes O’ Plenty quest refs (we use Regions Unexplored / Terralith / Aero)
- Market / Bulletin Board / Shipments chapters (deleted)

CABIN resourcepack / kubejs names (`andesite_machine`, mechanisms, etc.) may stay as **Autis craft items**; they are not permission to keep Thermal recipes.

---

## 7. Overview chapter shape

Left → right hexes on `overview`:

```
[Origin] → [S1 Kinetics] → [S2 Brass/AE] → [S3 Power] → [S4 Calc] → [S4b Process] → [Flight] → [Stellaris]
                \__________________________________________________________________/
                         optional: Munitions, Psi (off the main line)
```

Each overview node completes when its stage gate quest completes. No Ad Astra / Tinkers icons.

---

## 8. Implementation order

1. Freeze this doc’s spine (edit here if fantasy disagrees).
2. Retire Market / Shipments / professions (chapters deleted).
3. Fix Stage 1 rubber/sealed recipes + quests (unblocks everything).
4. Remap Stage 3 to IE/IP; Stage 4b off Thermal.
5. Rewrite Stage 5 + Taking Flight + new Airship / Stellaris pages.
6. Sweep origin chapters for missing mods.
7. Point Overview deps at new gate quest IDs.
8. Playtest Stage 1→3 in AutisTest before touching late game art.

Authoring: FTB Quests edit mode in-game (± Quest Enhance later). Bulk ID/text: VS Code / qbedit on `config/ftbquests/`.

---

## 9. Open decisions (fill before coding late stages)

- [x] Sealed/rubber: **kubejs raw_rubber → rubber** (chapter_1a). Done.
- [x] Pack complete = **Stellaris rocket launch** (CAB-style finale). Taking Flight is the pre-space milestone.
- [x] Airship Core = **fold into Taking Flight** (`angel`) unless the page overflows.
- [ ] Stage 3 concrete gate item (name one craftable block/item)?
- [ ] Currency: Numismatics, keep no market, or something else?
- [ ] How early may gliders / Immersive Aircraft appear (Stage 1 toy vs Stage 3)? Default lean: **Stage 3+** for aircraft, gliders can tease earlier.

### Done this pass
- Deleted: `market`, `professions` (Bulletin Board), `shipments` chapters and the `Remote Trading` chapter group, plus their lang keys and the dead locator-event block in `kubejs/server_scripts/quests.js`
- Stage 1 Thermal rubber line → raw rubber / rubber / belt / sealed / copper machine
- Stage 1 mechanism/machine tasks un-purpled; saw → CDG hammer
- Stage 1 itemfilters tags → oak log/plank/slab/stripped + sand
- Legacy `chapters.js` Thermal 1A path gated behind `hasThermal`

---

## 10. Success criteria

A new player can:

1. Finish Stage 1 without purple missing items.
2. Reach a powered IE/CDG setup using only quest guidance.
3. Build or board an Aeronautics-related craft as the emotional climax before space.
4. Touch Stellaris with quests that name real Stellaris items.
5. Ignore Munitions / Psi / most Origins and still finish the spine.
