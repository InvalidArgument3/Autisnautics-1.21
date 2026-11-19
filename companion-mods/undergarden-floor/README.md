# Undergarden Floor

Server-side companion for Autisnautics. On each `undergarden:undergarden` chunk load, once, it replaces `minecraft:bedrock` and `undergarden:depthrock` in the bottom five layers (Y=-64 through Y=-60) with obsidian and stores a chunk attachment so the chunk is never scanned again.

New chunks get the same floor from the Paxi datapack `Autisnautics Undergarden Floor`, which overrides the `minecraft:bedrock_floor` surface rule. The roof rule stays bedrock, and Immersive Portals still turns that roof into depthrock.
