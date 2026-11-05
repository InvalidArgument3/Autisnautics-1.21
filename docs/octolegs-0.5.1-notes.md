# Octo Legs 0.5.1 — wall and ceiling grips

## What blocked the climb

0.5.0 still aimed the next foothold **below** the mount (`lead` subtracted up to 2.5 blocks unless vertical velocity was already above 0.35). Scoring then added a floor-normal bonus. A floor under the ship was closer to that lowered aim than the wall in front of it, so the foot replanted on the ground. Holding jump was the only way to lift the aim, and even then the floor bonus fought the ceiling.

Octo Arms `TentacleGait.best` does not do that. It scores every exposed face by distance to an aim at body height. A wall or ceiling wins when it is the surface in front of the limb. No jump is required.

## What changed

`Footholds.lead` stays at mount height and still leads `0.35 * reach` along world velocity. It only rises when the ship is already moving up (`vy > 0.2`), so jump into a high ceiling still helps. Face score is distance to that aim, plus a small ahead bonus. The old `ny * 8` / floor bonus is gone. Floors still win on open ground because they are the only face near the aim. The 0.5-block forward rule still applies only to upward faces, so a wall or underside is not rejected for failing to “step ahead.”

## How to try it

Drive straight into a wall taller than the mounts. On the next step the foot should take the vertical face and the hull should climb with it. Fly or walk under a ceiling that is closer to the mounts than the ground is; the underside should catch. Hold jump if the ceiling is well above the hull. Slopes and flat ground should still plant downward.

## Risks

A tall wall with a ledge at mount height can grab the top of the ledge instead of the face you wanted. The support polygon can still delay a step until other feet cover the ship origin. Faces more than about 0.35 blocks behind the mount are ignored while you are moving forward, so a surface you have already passed will not be gripped.
