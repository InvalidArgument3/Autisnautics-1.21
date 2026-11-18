# Neodymium Particle Guard

Client-only companion. It does not edit Alex's Caves. The pack loads
`neodymiumguard-1.0.0.jar` from `.pakku/client-overrides/mods/` (Git LFS). It does
not go in `.pakku/overrides/mods`, because that folder is copied onto the server
and this fix is client rendering only.

Minecraft 1.21.1, NeoForge 21.1.265. Built with `build.sh` and Java 21. The
compile classpath is Alex's Caves `2.0.10` (sha1
`366ddaac40f14cd0102f026b4d999753fc1db2a4`) and a Mojmap remap of the official
1.21.1 client. The Alex's Caves jar is an audit input and is not copied into the
mod jar.

## Build

```
./build.sh
```

That compiles against the real jars, runs `ThrottleHarness` and `MixinAudit`, and
writes `.pakku/client-overrides/mods/neodymiumguard-1.0.0.jar`. The script finds
a JDK via `JAVA_HOME`, then the Prism `temurin-25`, then `PATH`.

## The problem

`dimension_of_caves:cave` includes the `alexscaves:magnetic_caves` biome, and
that biome places `alexscaves:scarlet_magnetic_node` and
`alexscaves:azure_magnetic_node` at `"count": 25` each — 50 node clusters per
chunk. Every node runs this in `NeodymiumNodeBlock.animateTick`:

```java
Vec3 center = Vec3.atCenterOf(pos);
if (random.nextInt(1) == 0) {
    ParticleOptions p = azure ? AZURE_MAGNETIC_ORBIT.get() : SCARLET_MAGNETIC_ORBIT.get();
    level.addParticle(p, center.x, center.y, center.z, center.x, center.y, center.z);
}
```

`random.nextInt(1)` is always `0`, so the guard is always true and the particle
spawns on every call. The particle is `MagneticOrbitParticle extends
AbstractTrailParticle`:

| Property | Value |
| --- | --- |
| Lifetime | `50 + random(30)` — 50 to 80 ticks |
| Trail buffer | 64 `Vec3` |
| `sampleCount()` | 20 trail samples drawn per particle |
| `getRenderType()` | `ParticleRenderType.CUSTOM` |

`ParticleRenderType.CUSTOM` cannot batch with other particles, so each one costs
its own texture bind and draw. A single node keeps 50-80 of them alive at once,
and each draws about 20 quads. Across a magnetic caves chunk that is tens of
thousands of unbatched quads in view, which is the frame time you feel there.

There is no config switch for it: `config/alexscaves-client.toml` exposes
`emissive_block_models` but nothing for particles.

## What it changes

`NeodymiumNodeBlockMixin` cancels `animateTick` at HEAD unless
`NeodymiumParticleGuard.shouldSpawn` allows it. The audit asserts that
`animateTick` has no side effect other than the particle spawn, so cancelling it
drops exactly the particle and the `Vec3.atCenterOf` that feeds it — nothing
else about the block changes.

`NeodymiumThrottle` gives each node a fixed phase from its packed block
position and lets it spawn only when `(gameTime + phase) % divisor == 0`. Over
any `divisor` ticks each node spawns exactly once, and on any one tick about
`1/divisor` of the nodes in view spawn. The particles in flight drop by the
divisor. The phase is stable per block, so a node pulses on a steady beat
instead of flickering, and the position is mixed with Murmur3's finalizer so
neighbouring blocks do not land on the same phase and pulse in rows.

Measured by `ThrottleHarness` at the default divisor of 4, over a 16x16x8 block
region: phases split 520/526/502/500, and a single tick spawned 500 of 2048
nodes (24.4%).

## Tuning

The divisor defaults to 4. Change it without rebuilding by setting the
`neodymiumguard.divisor` system property in the Prism JVM arguments:

| Value | Effect |
| --- | --- |
| `1` | Unmodified Alex's Caves behaviour |
| `4` | Default — a quarter of the particles |
| `8` | Half the default — an eighth of the particles |

Anything unparseable or below 1 falls back to the default, so a typo cannot
switch the guard off.

## Rollback

Delete `.pakku/client-overrides/mods/neodymiumguard-1.0.0.jar` (and the copy in
`mods/`), or set `neodymiumguard.divisor=1`. Either one restores the original
behaviour with no world changes.
