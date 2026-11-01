# Questline planning (Autisnautics)

## What pack authors usually use

1. **Progression matrix** (Google Sheets / Excel / CSV) — plan stages, gates, mods, and replacements *before* touching FTB Quests.
2. **Dependency sketch** (FigJam / Miro / draw.io / Excalidraw) — optional; only when branches get messy.
3. **FTB Quests in-game editor** — implementation. Quests live as `.snbt` under `config/ftbquests/`.
4. **KubeJS chapter recipes** — lock items behind the same stage gates as the book.

Almost nobody designs the full book only inside FTBQ. The sheet is the source of truth; FTBQ is the player-facing UI.

This folder’s `progression-matrix.csv` is that sheet. Open it in Google Sheets or Excel, edit columns, then implement.

## How to use the matrix

| Column | Meaning |
|---|---|
| Stage | Player-facing chapter / era |
| Gate item | Thing that “unlocks” the stage (usually a kubejs mechanism or machine) |
| Primary mods | Mods the player is *meant* to touch here |
| CABIN leftover | Old quest/recipe assumption still in the book |
| Autis replacement | What we use instead (or `drop`) |
| Quest chapter | Current `config/ftbquests/quests/chapters/*.snbt` file |
| Status | `keep` / `rewrite` / `retire` / `new` |

Fill **Status** as you decide. Do not implement a row until Gate item + Autis replacement are set.

## Suggested workflow

1. Edit the CSV until Stage 1→5 reads as one story (Create → power/oil → AE2 → flight → space/nuke).
2. Retire CABIN chapters marked `retire` (hide or delete in FTBQ).
3. Rewrite `rewrite` rows: swap `missing_item` / Thermal / Ad Astra / Tinkers IDs for live mods.
4. Mirror gates in `kubejs/server_scripts/recipes/chapters/`.
5. Playtest one stage at a time; do not open Stage N+1 until Stage N gate crafts cleanly.
