# Limb systems: Octo Arms, Let It Walk, Walking Steel

Decompiled with Vineflower 1.10.1 from `octo-arms-1.6.0+mc1.21.1.jar` (tentaworks), `let_it_walk-0.1.1.jar`, and `create_walking_steel-1.0.0.jar`. Class and method names below are from those jars. Octo Legs comparisons are against the 0.3.6 sources this release started from.

## Octo Arms — how a tentacle chooses a placement

Octo Arms is a worn four-slot rig, not a ship mount. The body point is the player AABB centre (`Grips.body`). Slots are a fixed diamond around that point, not block faces on a hull.

### Roots and rest targets

`TentacleGait.targets(body, yaw, reach, floor)` builds four world aims from the player's yaw:

- forward `(-sin yaw, 0, cos yaw)`, left `(cos yaw, 0, sin yaw)`
- radius `rho = clamp(0.5 * reach, 1.1, 2.8)`
- slots 0 and 1 are forward, 2 and 3 are back; even slots go left, odd go right
- each aim is `normalize(forward * sign + left * sign) * rho`, then lifted by `0.35 * reach` on the forward pair
- if a floor Y is known and the aim is below it, Y is snapped to the floor

That aim is the idle plant. While moving, `TentacleGait.tick` shifts it further: `aim = target + dir * (0.35 * reach)`, where `dir` is the unit of the per-tick motion vector. The search also looks at where the body will be: `ahead = body + motion * stepTicks`, with `stepTicks = clamp(round(1 / speed), 2, 7)` (7 when nearly still).

### Grip faces

`Grips.faces(level, centre, radius)` scans exposed block faces in the world and, separately, in every nearby unit-scale ship frame (`ShipFrames.near`, then `frame.toLocal(centre)`). A face is an AABB of the exposed side (`MIN_FACE` 0.5, clearance 0.05). `TentacleGait.best` converts body, ahead, and aim into that face's frame before scoring, then converts the chosen point back with `frame.toWorld`. So a grip on a ship is stored in plot space (`Grip` holds a point, a block, and a direction) and resolved to world with `Grip.world(level)`.

Scoring is distance to the aim, plus a penalty when the point is within 1.7 blocks of another slot's grip. The point must stay inside `reach + overshoot` of both the current body and the predicted ahead body, and inside `keep = reach + 0.68` of the previous body. `Grips.visible` is the line-of-sight check.

### When a slot steps

A slot steps only if its phase is already 1 (planted) and it was not planted on this same tick. The trigger is not "distance from the socket equals reach". It is how far the current grip sits behind the rest aim:

```
behind = -world(slot).subtract(target).dot(dir) - stride
```

with `stride = 0.35 * reach`. If `behind > 0`, a candidate is searched. The candidate is kept only when `cand.world.subtract(current).dot(dir) >= 0.5`, so the new grip must move forward along travel, not backward.

At most two slots swing (`active < 2`). A voluntary step also requires `plantedOk - 1 >= 2`, and the other swinging slot must be the diagonal partner (`PARTNER`, `pairOf`: slots 0 and 3 are one pair, 1 and 2 the other). The neediest legal slot is picked; the pair that did not step last time gets a small bonus. Phase then runs from 0 to 1 over `stepTicks`, one step of `1 / duration` per server tick.

### Client, server, packets

`TentacleGait.tick` runs on the server (`RigServer`). The result is `Tentacles`: four `Grip`s plus a phase byte per slot (`Tentacles.phaseByte`). `RigStatePacket` carries state and tentacles to clients. `RigMotionPacket` carries the client's movement wish. `RigClient` reads vanilla movement keys and sends that wish; it does not pick grips.

`StepPhase` on the client does not snap to the server byte. It keeps `phaseO` and eases toward the wire value (`tick` adds at least `1/7` or `0.25` when the server says the step finished). `at(partialTick)` is `phaseO + (phase - phaseO) * partialTick`. `RigVisuals` draws tips from the grip's world point through the ship frame's render pose, and `TentacleMath.liftAxis` bows the limb while the phase is in swing. The root of each visual slot is on the player model, not a kinetic block.

## Let It Walk — how a walker manages legs

A leg is not an item in a socket. `BuiltLegAssembler` walks blocks from a `WalkerCoreBlockEntity`, follows `JointChain` links, and produces a `BuiltLeg` (hip, ordered joints, foot, reach). `LegRuntime` holds the live state: `STANCE` or `SWING`, `anchor`, `swingFrom` / `swingTo`, `swingStart`, `swingTicks`, `grounded`.

### Gait

`WalkerBrain.plan` is the server tick. Command velocity comes from the pilot bitfield (`KEY_FORWARD` through `KEY_DOWN`, gears in `GEAR_SPEED`). For each stance leg it builds a world target:

- `neutral = pose.transformPosition(leg.neutralLocal(core))` — the rest point in plot space, pushed through the ship pose
- travel includes yaw: `commandVel + yawRate * (z, 0, -x)` of the offset from the core
- `lead = normalize(travel) * min(speed * 0.5, maxHalfStride)`
- `target = neutral + lead`

Need is `dist(anchor, target) / max(stride * 0.7, 1.5 * half)`. Need at least 1 starts a step. Swing count is capped: 1 for three or fewer legs, 1 or 2 for four depending on gear, `n / 2` above that. A neighbour already swinging blocks the step (`neighbourSwinging`), with a quadruped gallop exception.

Before a low-need step, if three or more feet remain, `SupportPolygon.margin` of those feet around the core must be at least `minMargin` (`-0.35` normally, `0.05` for six or more, `-1` when urgent or self-balancing). The hull is Andrew's monotone chain. Positive margin means the core is inside.

The landing search is `findFoothold`, aimed at `target + velocity * swingTime`, then retried at 0.7 and 0.4 of the way from the neutral, then at the neutral, then a short step from the current foot along the aim. Swing duration is `max(distance / footSpeed, 0.15)`, capped by size. The arc height is `max(stepHeight, 0.1 * distance, clearance)`.

`WalkerPhysics` applies the resulting body velocity and support to the Sable handle. Pilot keys set the command; with no keys the gait still settles idle feet whose anchor is more than 0.6 from the neutral. That autonomous settle is not what Octo Legs does: Octo Legs stays planted until a helm is held and the network is powered.

## Walking Steel (short)

`LegCurvePathfinder.findBestFoothold` samples a grid of offsets around `nominalLandPos` (`-1.5 … 1.5`) and scores solid ground by distance to the nominal point, a travel-direction bonus, and a stance-Y error. `findObstacleElevationDelta` walks down a column for a safe surface. `LegBaseBlockEntity` has a `GaitMode` and links a foot block to a base. Useful confirmation that the nominal landing point, not the hip, is what the search should be centred on. The grid is coarser than Octo Arms' exposed-face scan, which Octo Legs already resembles more closely.

## What 0.3.6 still got wrong

0.3.6 already stopped scoring footholds with the ship-local move vector (`OctoLeg` motion X/Z) and used `Footholds.travelDirection(vx, vz)`. The ideal point was only `root + velocity * stepDuration`, a short prediction of the socket. The search was still centred on that socket, with a mild ahead bonus (`1.6`) and a reject only for points more than 0.35 behind the socket. A closer face just behind the current foot could still win, which is the "reaching backward" report. The step trigger measured distance from that short prediction, so a foot could trail a long way before the horizontal gap exceeded `stepTriggerFraction * reach`.

Support was a count: at least half the feet stay down. That does not notice when the remaining feet are all on one side of the hull.

## Adopt into Octo Legs

Ranked by impact, and only where it fits powered mounts plus a helm:

1. Aim the next foothold ahead of the mount along world velocity by `0.35 * reach`, after predicting the socket at the end of the step (`Footholds.lead`). Same constant Octo Arms uses for stride.
2. Reject a candidate that does not advance at least 0.5 along world travel from the current foot (`TentacleGait`'s `dot(dir) < 0.5` test).
3. Keep the world-velocity direction for scoring, the configurable step trigger, the world-space swing arc, and the half-planted count.
4. If three or more other feet are down, refuse a non-emergency step whose removal would leave the ship origin outside their hull (`SupportMath`, the Let It Walk polygon test, margin `-0.35`).

Left out on purpose:

- Octo Arms' four worn slots, player-yaw diamond, and phase bytes. Octo Legs roots are the mount blocks.
- Let It Walk's joint assembler, RPM gears, jump, flight, grab, and autonomous idle stepping. A helm that is not held keeps planting.
- Walking Steel's fixed offset grid. The exposed-face scan already covers slopes and walls.
- Copying either physics solver. Octo Legs still sets Sable linear and yaw velocity from the helm.
