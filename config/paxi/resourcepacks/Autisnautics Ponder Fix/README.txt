Autisnautics Ponder Fix
========================

WHY THIS EXISTS
---------------
Create Aeronautics: Custom propellers 0.0.61 (CurseForge 1718605) ships three
malformed Ponder schematics. Each one contains a spurious 0x0a (TAG_Compound)
byte before every LIST<Compound> element. Per the NBT spec, list elements are
bare payloads and must NOT carry a tag id.

Minecraft reads that stray 0x0a as a tag id, then reads the following two bytes
(0x08 0x00) as a string length of 2048, then tries to read 2048 bytes that do
not exist -> java.io.EOFException -> client crash.

Reproduction: open the Creative inventory and hover any of the three propeller
items (Custom / Big / Multiple Propeller). Create's Ponder tooltip loads the
scene schematic and the client dies.

Crash signature:
    Description: Loading NBT data
    java.io.EOFException
      at DataInputStream.readFully -> readUTF
      at CompoundTag.readNamedTagData
      at PonderSceneRegistry.loadSchematic
      at PonderTooltipHandler.addToTooltip
      at CreativeModeInventoryScreen.getTooltipFromContainerItem

WHAT THIS PACK DOES
-------------------
Overrides the three broken files with corrected NBT. The original block data was
recovered byte-for-byte from the malformed files (only the stray 0x0a bytes were
removed) -- nothing was invented:

    assets/create_aeronautics_propellers_plus/ponder/big_propeller/thrust.nbt
    assets/create_aeronautics_propellers_plus/ponder/custom_propeller/thrust.nbt
    assets/create_aeronautics_propellers_plus/ponder/multiple_propeller/thrust.nbt

Recovered scene contents (all three):
    size        = [3, 2, 3]
    DataVersion = 3955
    palette     = [ minecraft:andesite,
                    create_aeronautics_propellers_plus:<variant>_propeller
                      with Properties{facing="up"} ]
    blocks      = 10  (9x andesite at y=0 forming the 3x3 base,
                       propeller at [1,1,1] with state=1)

That matches what the scene code expects: PropellersPlusPonderScenes.thrust()
points its text overlay at BlockPos(1,1,1) and calls setKineticSpeed on
fromTo(1,1,1, 1,1,1).

Ponder loads schematics through Minecraft.getResourceManager(), so a resource
pack overrides them cleanly. This pack is enabled last in options.txt, giving it
the highest priority over mod_resources.

WHY A RESOURCE PACK AND NOT A JAR PATCH
---------------------------------------
`pakku fetch` would overwrite a patched mod jar. A resource pack survives
updates and is trivially reversible (just disable it).

IF IT EVER STOPS WORKING
------------------------
1. Check it is still enabled: Options -> Resource Packs, and that it sits at the
   TOP of the selected list.
2. If a future mod version fixes the schematics upstream, this pack becomes
   redundant -- disable and delete it.
3. To re-verify the pack's files are valid NBT, parse them strictly; each must
   consume exactly its full byte length with zero trailing bytes.
