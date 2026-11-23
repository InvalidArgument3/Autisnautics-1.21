# Licence

This port did not relicense NeepMeat.

## What upstream says

- `LICENSE` in the NeepMeat repository is the GNU Lesser General Public License, version 3. Copied here unchanged as `LICENSE`.
- `fabric.mod.json` says `"license": "LGPL-3.0"`.
- Java file headers say: "NEEPMeat is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version."
- `neoforge.mods.toml` in this port uses `LGPL-3.0` because that is the licence id in `fabric.mod.json` and the text of the shipped `LICENSE` file. The headers are preserved on ported and new files, and `GPL-3.0.txt` is the verbatim GNU GPL version 3 from the Free Software Foundation, included because the LGPL incorporates those terms and the source headers name the GPL.

Anyone shipping the jar should ship this source tree with it. The sources jar from `./gradlew build` is the corresponding source for this phase.

## Permission

The upstream README says: "Please don't port this mod to other versions or loaders without asking the author (MeatWheeze)."

The pack owner (Wyseman) reported that he has the author's permission. This port did not verify that claim with MeatWheeze. The LGPL/GPL texts themselves allow modification and redistribution if their conditions are met. The README request is an additional courtesy the owner says was granted.

## Subprojects

| Piece | Upstream | Licence | In this jar |
| --- | --- | --- | --- |
| NeepMeat | codeberg `0d36776` | LGPL-3.0 file, GPL-3.0-or-later headers | Phase-1 derivative |
| meatlib | directory inside the NeepMeat repo, its own `fabric.mod.json` id `meatlib` | same headers and LGPL file | not included; not a separate mod id yet |
| NEEPSpeak | submodule `d409f849` | LGPL-3.0 (`licenses/NEEPSpeak-LGPL-3.0.txt`) | source inlined, mod id remains `neepmeat` |
| Myron-NEEPMeat | submodule `aa49118` | MIT (`licenses/Myron-MIT.txt`) | code not copied |
| Flywheel | Maven `flywheel-fabric-1.20.1:1.0.6-278` | not vendored in this commit | not bundled |

Nothing in those licences was found that forbids redistributing the parts that were copied. Myron was left out because it is a Fabric OBJ loader with mixins, not because the MIT licence forbids it. Flywheel was left out so this jar does not ship a second Flywheel next to Create's.

## Credits kept

Authors in `neoforge.mods.toml` are MeatWheeze. Language files `en_us.json` and `zh_cn.json` are the upstream files, copied whole, including strings for content this phase does not register. NEEPSpeak sources keep their upstream headers (several files have none). The Myron README note from MeatWheeze and from BlueVista is in `licenses/Myron-README.md`.
