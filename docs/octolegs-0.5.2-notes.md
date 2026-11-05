# Octo Legs 0.5.2 — walk down a wall

## What blocked the descent

Two checks only looked sideways.

`Footholds.shouldStep` measured the horizontal gap between the foot and the aim. On a wall that gap stays small: the foot and the mount share the same face. Sneak lowers the hull until it is level with the grip, and the leg never starts a step. Pure descend also does not count as “locomoting,” so the parked rule (wait until 80% of reach) applied.

`Footholds.lead` only moved the aim up (`vy > 0.2`). Descending left the aim at mount height, level with the current grip, so a lower face on the same wall was farther from the aim and lost.

## What changed

The aim drops by `0.45 * reach` when `vy < -0.2`, the same distance it rises when climbing. The step trigger uses both horizontal and vertical separation from that aim, and a vertical drift counts as motion even with no WASD. The next grip on the wall is the face nearest the lowered aim.

Climbing up, the stagger, and the mount-height aim while walking forward are unchanged.

## How to try it

Grab a wall, then hold sneak (descend) with or without forward. Feet should step down the same face and the hull should follow. Jump should still step upward.

## Risks

A fast drop can ask for a grip near the bottom of reach in one step, so the foot may skip a band of the wall instead of walking it evenly. If the wall ends, the next search falls back to whatever face is nearest the lowered aim, including the ground.
