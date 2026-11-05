# Octo Legs 0.5.0

## Walk cycle (from Let It Walk `WalkerBrain`)

`WalkerBrain.plan` does not start every ready leg on the same tick. It caps how many may be in `SWING` (`n <= 3` → 1, `n == 4` → 1 or 2 by gear, otherwise `n / 2`) and `neighbourSwinging` blocks a leg whose neighbour is already swinging. Steps are taken from a need-sorted list, so starts are serial.

Octo Legs keeps mount roots and the helm. `GaitMath.maxSimultaneousSwing` uses that cap (quadruped is 2, since there is no gear). `GaitMath.mayBeginSwing` gives each leg index one start window, at least 4 ticks apart, walking around the leg count with time. A step still requires the early step trigger, at least half the feet staying down, and the support-polygon check. No idle auto-step.

## Wall and ceiling grips (from Octo Arms `Grips` / `TentacleGait.best`)

Octo Arms scores every exposed face by distance to the aim. It does not require the face to point up. 0.4.0 scored `ny * 8` plus a bonus for being below the socket, so a floor always beat a wall or a ceiling, and the 0.5-block forward rule rejected an underside that did not step ahead.

0.5.0 scores distance to the aim. The aim sits below the mount while walking, and `0.45 * reach` above it when the ship is rising, so a ceiling underside wins while climbing and a floor still wins on flat ground. The forward-advance reject applies only to upward faces (`ny > 0.25`). Walls and ceilings in reach remain legal. Faces more than 0.35 behind the mount are still rejected while moving forward.
