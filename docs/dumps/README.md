# Autisnautics registry dumps

## Items (live game, KubeJS web API)

| File | Contents |
|------|----------|
| `items.json` | **18,529** item ids + per-mod counts |
| `items-named.json` | + display names (~17k) |
| `items.tsv` | id / name / mod (grep-friendly) |
| `items-by-mod/*.txt` | Per-namespace lists |
| `mods.json` | 465 loaded mods |
| `recipe_type-keys.json` | 180 recipe types |
| `recipe_serializer-keys.json` | 325 serializers |

## Recipes

`/kubejs export pack-folders` writes `local/kubejs/exported_packs/` — that is **KubeJS datapack overlays** (worldgen/loot/tags), **not** the full recipe book.

Full JSON recipe index from **356 mod jars** (+ kubejs/datapacks):

| File | Contents |
|------|----------|
| `recipes-index.json` | **26,044** recipes (id, type, source jar, path) |
| `recipes.tsv` | Greppable |

**Gap:** recipes added only in KubeJS `ServerEvents.recipes` (not written as datapack JSON) are not in the jar scan. Those need a live dump script or EMI export later.

Re-scan jars: `python docs/_scan_jar_recipes.py`
