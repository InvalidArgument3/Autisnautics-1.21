package abysscreatures.ai;

import abysscreatures.entity.ChainFish;
import abysscreatures.entity.CorpseAnglerEntity;
import abysscreatures.entity.FulgastraEntity;
import abysscreatures.entity.GhidruthEntity;
import abysscreatures.entity.GloomfishEntity;
import abysscreatures.entity.GnasherEntity;
import abysscreatures.entity.LithoshrimpEntity;
import abysscreatures.entity.ModEntities;
import abysscreatures.entity.MutavoreEntity;
import abysscreatures.entity.NecroshellEntity;
import abysscreatures.entity.SiamserpentHeadEntity;
import abysscreatures.entity.SpineWormHeadEntity;
import abysscreatures.entity.SplittedFulgastraEntity;
import abysscreatures.entity.WaterAbyssCreature;
import com.mojang.authlib.GameProfile;
import abysscreatures.entity.SpineWormBodyEntity;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Headless proof. Started only with {@code -Dabysscreatures.aiTest=true}.
 * One survival probe in a closed tank of water, plus ServerCore and DoesPotatoTick from the pack configs.
 */
@EventBusSubscriber(modid = "abysscreatures")
public final class AiTest {
    private static Run run;

    private AiTest() {}

    @SubscribeEvent
    public static void started(ServerStartedEvent event) {
        if (!Boolean.getBoolean("abysscreatures.aiTest") || run != null) {
            return;
        }
        run = new Run(event.getServer());
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        if (run != null) {
            run.tick();
        }
    }

    public static final class Probe extends FakePlayer {
        /** FakePlayer.startRiding returns false, and invokespecial from this class resolves to that override. */
        private static final Field VEHICLE = entityField("vehicle");
        private static final Method COULD_ACCEPT = entityMethod("couldAcceptPassenger");
        private static final Method CAN_ADD = entityMethod("canAddPassenger", Entity.class);
        private static final Method ADD_PASSENGER = entityMethod("addPassenger", Entity.class);
        private static final Field SPAWN_INVULNERABLE = spawnField();

        public Probe(ServerLevel level, String name, boolean creative) {
            super(level, new GameProfile(UUID.randomUUID(), name));
            this.setGameMode(creative ? GameType.CREATIVE : GameType.SURVIVAL);
            this.getAbilities().instabuild = creative;
            this.getAbilities().invulnerable = creative;
            this.getAbilities().flying = false;
            this.onUpdateAbilities();
            try {
                SPAWN_INVULNERABLE.setInt(this, 0);
            } catch (IllegalAccessException error) {
                throw new IllegalStateException(error);
            }
        }

        @Override
        public boolean isInvulnerableTo(DamageSource source) {
            return this.isCreative() || this.isSpectator();
        }

        @Override
        public boolean hurt(DamageSource source, float amount) {
            float capped = Math.min(amount, Math.max(0.0f, this.getHealth() - 1.0f));
            if (capped <= 0.0f) {
                return false;
            }
            return super.hurt(source, capped);
        }

        /**
         * Same checks as Entity.startRiding, including the NeoForge mount event.
         * The vehicle field and addPassenger are Entity's; calling them here is what a real player mount does,
         * without FakePlayer's refusal.
         */
        @Override
        public boolean startRiding(Entity vehicle, boolean force) {
            if (vehicle == this.getVehicle()) {
                return false;
            }
            try {
                if (!(boolean) COULD_ACCEPT.invoke(vehicle)) {
                    return false;
                }
                for (Entity chain = vehicle; chain.getVehicle() != null; chain = chain.getVehicle()) {
                    if (chain.getVehicle() == this) {
                        return false;
                    }
                }
                if (!EventHooks.canMountEntity(this, vehicle, true)) {
                    return false;
                }
                if (!(force || this.canRide(vehicle) && (boolean) CAN_ADD.invoke(vehicle, this))) {
                    return false;
                }
                if (this.isPassenger()) {
                    this.stopRiding();
                }
                this.setPose(Pose.STANDING);
                VEHICLE.set(this, vehicle);
                ADD_PASSENGER.invoke(vehicle, this);
                return true;
            } catch (Throwable error) {
                throw new RuntimeException(error);
            }
        }

        @Override
        public void tick() {
            if (this.invulnerableTime > 0) {
                this.invulnerableTime--;
            }
            this.refreshFluids();
        }

        private static Field entityField(String name) {
            try {
                Field field = Entity.class.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException error) {
                throw new ExceptionInInitializerError(error);
            }
        }

        private static Method entityMethod(String name, Class<?>... parameters) {
            try {
                Method method = Entity.class.getDeclaredMethod(name, parameters);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException error) {
                throw new ExceptionInInitializerError(error);
            }
        }

        private static Field spawnField() {
            try {
                Field field = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException error) {
                throw new ExceptionInInitializerError(error);
            }
        }

        public void refreshFluids() {
            this.updateInWaterStateAndDoFluidPushing();
            this.setDeltaMovement(Vec3.ZERO);
            this.fallDistance = 0.0f;
        }
    }

    private static final class Run {
        private final MinecraftServer server;
        private final ServerLevel level;
        private final List<String> failures = new ArrayList<>();
        private Probe survival;
        private int phase;
        private int tick;
        private WaterAbyssCreature mob;
        private double startDistance;
        private double closest;
        private float healthAtStart;
        private boolean acquired;
        private boolean ridden;
        private final Map<Integer, List<Vec3>> tracks = new HashMap<>();
        private Vec3 anglerHome;
        private boolean lureRay;
        private String phaseName = "boot";

        Run(MinecraftServer server) {
            this.server = server;
            this.level = server.overworld();
            server.setDifficulty(Difficulty.NORMAL, true);
        }

        void tick() {
            try {
                switch (this.phase) {
                    case 0 -> boot();
                    case 1 -> creativeIgnored();
                    case 2 -> roam("gloomfish", GloomfishEntity.class, ModEntities.GLOOMFISH.get(), 3);
                    case 3 -> roam("lithoshrimp", LithoshrimpEntity.class, ModEntities.LITHOSHRIMP.get(), 1);
                    case 4 -> chase("gnasher", ModEntities.GNASHER.get(), 10.0, 220);
                    case 5 -> leaveWater();
                    case 6 -> chase("necroshell", ModEntities.NECROSHELL.get(), 6.0, 220);
                    case 7 -> chase("mutavore", ModEntities.MUTAVORE.get(), 4.0, 220);
                    case 8 -> chase("ghidruth", ModEntities.GHIDRUTH.get(), 12.0, 240);
                    case 9 -> anglerWait();
                    case 10 -> anglerBodyHit();
                    case 11 -> anglerLure();
                    case 12 -> fulgastra();
                    case 13 -> mine();
                    case 14 -> spine();
                    case 15 -> siam(SiamserpentHeadEntity.HeadType.SLASHER, "siamserpent_slasher");
                    case 16 -> siam(SiamserpentHeadEntity.HeadType.BLASTER, "siamserpent_blaster");
                    default -> finish();
                }
            } catch (RuntimeException error) {
                this.failures.add(this.phaseName + " threw " + error);
                error.printStackTrace(System.out);
                finish();
            }
        }

        private void boot() {
            this.phaseName = "boot";
            boolean servercore = ModList.get().isLoaded("servercore");
            boolean potato = ModList.get().isLoaded("doespotatotick");
            String version = ModList.get().getModContainerById("abysscreatures").map(container -> container.getModInfo().getVersion().toString()).orElse("missing");
            AbyssAiLog.line("MODS abysscreatures=" + version + " servercore=" + servercore + " doespotatotick=" + potato);
            if (!servercore || !potato || !"0.2.4".equals(version)) {
                this.failures.add("mod set version=" + version + " servercore=" + servercore + " potato=" + potato);
            }
            for (int cx = -2; cx <= 2; cx++) {
                for (int cz = -2; cz <= 2; cz++) {
                    this.level.setChunkForced(cx, cz, true);
                }
            }
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int x = -18; x <= 18; x++) {
                for (int z = -18; z <= 18; z++) {
                    for (int y = 64; y <= 90; y++) {
                        boolean shell = y == 64 || y == 90 || Math.abs(x) == 18 || Math.abs(z) == 18;
                        this.level.setBlock(cursor.set(x, y, z), shell ? Blocks.STONE.defaultBlockState() : (y < 88 ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState()), 2);
                    }
                }
            }
            this.survival = this.probe("abyss_survival", false);
            this.place(this.survival, 0, 76, 0);
            AbyssAiLog.line("TANK water y=65..87 stone shell, probe=" + this.survival.isInWater() + " creative=" + this.survival.isCreative());
            this.phase = 1;
            this.tick = 0;
        }

        private void creativeIgnored() {
            this.phaseName = "creative";
            if (this.tick == 0) {
                this.clearMobs();
                this.place(this.survival, 40, 76, 40);
                Probe creative = this.probe("abyss_creative", true);
                this.place(creative, 4, 76, 0);
                this.mob = this.summon(ModEntities.GNASHER.get(), 0, 76, 0, false);
                AbyssAiLog.line("CREATIVE probe instabuild=" + creative.getAbilities().instabuild + " game=" + creative.gameMode.getGameModeForPlayer());
            }
            this.tick++;
            if (this.mob.getTarget() != null) {
                this.failures.add("creative or out-of-range player was targeted: " + this.mob.getTarget().getName().getString());
                this.mob.setTarget(null);
            }
            if (this.tick >= 40) {
                this.clearMobs();
                this.place(this.survival, 0, 76, 0);
                this.phase = 2;
                this.tick = 0;
                this.tracks.clear();
            }
        }

        private void roam(String name, Class<? extends WaterAbyssCreature> type, EntityType<? extends WaterAbyssCreature> entityType, int count) {
            this.phaseName = "roam-" + name;
            if (this.tick == 0) {
                this.clearMobs();
                this.tracks.clear();
                this.place(this.survival, 40, 120, 40);
                for (int i = 0; i < count; i++) {
                    this.summon(entityType, i * 1.5, 76, 0, false);
                }
            }
            if (this.tick % 40 == 0) {
                for (WaterAbyssCreature fish : this.level.getEntitiesOfClass(type, new net.minecraft.world.phys.AABB(-20, 60, -20, 20, 92, 20), entity -> true)) {
                    this.tracks.computeIfAbsent(fish.getId(), id -> new ArrayList<>()).add(fish.position());
                    AbyssAiLog.line("ROAM " + name + " t=" + this.tick + " id=" + fish.getId() + " pos=" + fmt(fish.position()) + " yaw=" + fish.getYRot());
                }
            }
            this.tick++;
            if (this.tick < 280) {
                return;
            }
            double worstTravel = Double.MAX_VALUE;
            double worstSpread = Double.MAX_VALUE;
            if (this.tracks.size() < count) {
                this.failures.add(name + " roam tracked " + this.tracks.size() + " of " + count);
            }
            for (List<Vec3> samples : this.tracks.values()) {
                if (samples.size() < 4) {
                    worstTravel = 0.0;
                    worstSpread = 0.0;
                    continue;
                }
                Vec3 first = samples.get(0);
                Vec3 mean = centroid(samples);
                double travel = 0.0;
                double spread = 0.0;
                for (Vec3 sample : samples) {
                    travel = Math.max(travel, first.distanceTo(sample));
                    spread = Math.max(spread, mean.distanceTo(sample));
                }
                worstTravel = Math.min(worstTravel, travel);
                worstSpread = Math.min(worstSpread, spread);
                AbyssAiLog.line("ROAM " + name + " travel=" + travel + " spread=" + spread + " from=" + fmt(first) + " far=" + fmt(samples.get(samples.size() - 1)));
            }
            // A fixed-point orbit stays inside a block or two of one centre. Leaving the spawn by 4 blocks
            // and spreading at least 2 from the path centre is a roam, including a fish that later turns back.
            if (worstTravel < 4.0 || worstSpread < 2.0) {
                this.failures.add(name + " did not roam travel=" + worstTravel + " spread=" + worstSpread);
            }
            this.phase++;
            this.tick = 0;
            this.tracks.clear();
        }

        private void chase(String name, EntityType<? extends WaterAbyssCreature> type, double apart, int limit) {
            this.phaseName = name;
            if (this.tick == 0) {
                this.clearMobs();
                this.place(this.survival, apart, 76, 0);
                this.survival.setHealth(this.survival.getMaxHealth());
                this.healthAtStart = this.survival.getHealth();
                this.mob = this.summon(type, 0, 76, 0, true);
                this.startDistance = this.mob.distanceTo(this.survival);
                this.closest = this.startDistance;
                this.acquired = false;
                AbyssAiLog.line("HUNT start " + name + " dist=" + this.startDistance + " mob=" + fmt(this.mob.position()) + " player=" + fmt(this.survival.position()) + " playerWater=" + this.survival.isInWater());
            }
            this.observe();
            this.tick++;
            if (this.tick % 40 == 0) {
                AbyssAiLog.line("HUNT " + name + " t=" + this.tick + " dist=" + this.mob.distanceTo(this.survival) + " target=" + (this.mob.getTarget() == this.survival) + " anim=" + this.mob.getAnimationState() + " pos=" + fmt(this.mob.position()));
            }
            if (this.tick < limit && this.survival.getHealth() >= this.healthAtStart) {
                return;
            }
            boolean damaged = this.survival.getHealth() < this.healthAtStart;
            boolean pursued = this.closest < this.startDistance - 2.0 || damaged;
            AbyssAiLog.line("HUNT result " + name + " acquired=" + this.acquired + " pursued=" + pursued + " closest=" + this.closest + " start=" + this.startDistance + " damaged=" + damaged + " health=" + this.survival.getHealth() + " anim=" + this.mob.getAnimationState());
            if (!this.acquired) {
                this.failures.add(name + " never acquired the survival player");
            }
            if (!pursued) {
                this.failures.add(name + " did not close distance closest=" + this.closest + " start=" + this.startDistance);
            }
            if (!damaged) {
                this.failures.add(name + " did not damage the player");
            }
            this.phase++;
            this.tick = 0;
        }

        private void leaveWater() {
            this.phaseName = "leave-water";
            if (this.tick == 0) {
                this.place(this.survival, 0, 96, 0);
                AbyssAiLog.line("LEAVE water=" + this.survival.isInWater() + " target=" + (this.mob.getTarget() == this.survival));
            }
            this.tick++;
            if (this.tick < 30) {
                return;
            }
            if (this.mob.getTarget() != null) {
                this.failures.add("target was kept after the player left the water: " + this.mob.getTarget().getName().getString());
            } else {
                AbyssAiLog.line("LEAVE target cleared");
            }
            this.phase++;
            this.tick = 0;
        }

        private void anglerWait() {
            this.phaseName = "angler-wait";
            if (this.tick == 0) {
                this.clearMobs();
                this.mob = this.summon(ModEntities.CORPSE_ANGLER.get(), 0, 80, 0, true);
                if (!(this.mob instanceof CorpseAnglerEntity angler) || !angler.isBurrow()) {
                    this.failures.add("angler did not burrow on natural spawn burrow=" + (this.mob instanceof CorpseAnglerEntity a && a.isBurrow()));
                }
                this.anglerHome = this.mob.position();
                this.place(this.survival, 16, this.mob.getY(), 0);
                this.survival.setHealth(this.survival.getMaxHealth());
                this.healthAtStart = this.survival.getHealth();
                AbyssAiLog.line("ANGLER burrow=" + ((CorpseAnglerEntity) this.mob).isBurrow() + " home=" + fmt(this.anglerHome) + " lure=" + fmt(Bait.center((CorpseAnglerEntity) this.mob)) + " playerDist=" + this.mob.distanceTo(this.survival));
            }
            this.tick++;
            if (this.mob.getTarget() != null) {
                this.failures.add("burrowed angler took a target before the lure");
            }
            if (this.tick < 80) {
                return;
            }
            double drift = this.mob.position().distanceTo(this.anglerHome);
            AbyssAiLog.line("ANGLER waited drift=" + drift + " health=" + this.survival.getHealth() + " burrow=" + ((CorpseAnglerEntity) this.mob).isBurrow());
            if (drift > 0.2) {
                this.failures.add("burrowed angler moved drift=" + drift);
            }
            if (this.survival.getHealth() < this.healthAtStart) {
                this.failures.add("burrowed angler damaged the player before the lure");
            }
            this.phase++;
            this.tick = 0;
        }

        private void anglerBodyHit() {
            this.phaseName = "angler-body";
            CorpseAnglerEntity angler = (CorpseAnglerEntity) this.mob;
            this.place(this.survival, angler.getX() + 2.5, angler.getY(), angler.getZ());
            this.aim(angler.position());
            boolean ray = Bait.hits(angler, this.survival);
            AbyssAiLog.line("ANGLER body rayHitsLure=" + ray);
            if (ray) {
                this.failures.add("body-hit setup still clipped the lure");
            } else {
                angler.hurt(this.level.damageSources().playerAttack(this.survival), 1.0f);
                if (!angler.isBurrow()) {
                    this.failures.add("hitting the body unburrowed the angler");
                } else {
                    AbyssAiLog.line("ANGLER body hit kept the burrow");
                }
            }
            this.phase++;
            this.tick = 0;
        }

        private void anglerLure() {
            this.phaseName = "angler-lure";
            CorpseAnglerEntity angler = (CorpseAnglerEntity) this.mob;
            if (this.tick == 0) {
                Vec3 lure = Bait.center(angler);
                this.place(this.survival, lure.x, lure.y - 1.62, lure.z + 3.6);
                this.aim(lure);
                this.lureRay = Bait.hits(angler, this.survival);
                AbyssAiLog.line("ANGLER lure ray=" + this.lureRay + " eye=" + fmt(this.survival.getEyePosition()) + " lure=" + fmt(lure) + " dist=" + this.survival.getEyePosition().distanceTo(lure));
                if (!this.lureRay) {
                    this.failures.add("lure ray did not hit the bait box");
                } else {
                    angler.hurt(this.level.damageSources().playerAttack(this.survival), 1.0f);
                }
                this.healthAtStart = this.survival.getHealth();
            }
            this.tick++;
            if (this.tick < 80 && this.survival.getHealth() >= this.healthAtStart) {
                return;
            }
            boolean damaged = this.survival.getHealth() < this.healthAtStart;
            AbyssAiLog.line("ANGLER lure result burrow=" + angler.isBurrow() + " anim=" + angler.getAnimationState() + " damaged=" + damaged + " health=" + this.survival.getHealth() + " target=" + (angler.getTarget() == this.survival));
            if (angler.isBurrow()) {
                this.failures.add("lure hit left the angler burrowed");
            }
            if (!damaged) {
                this.failures.add("angler did not attack after the lure");
            }
            this.phase++;
            this.tick = 0;
        }

        private void fulgastra() {
            this.phaseName = "fulgastra";
            if (this.tick == 0) {
                this.clearMobs();
                this.place(this.survival, 8, 76, 0);
                this.survival.setHealth(this.survival.getMaxHealth());
                this.mob = this.summon(ModEntities.FULGASTRA.get(), 0, 76, 0, false);
                this.acquired = false;
            }
            if (this.mob.getTarget() == this.survival) {
                this.acquired = true;
            }
            this.tick++;
            if (this.tick < 160 && !AbyssAiLog.saw(this.mob, 2)) {
                return;
            }
            AbyssAiLog.line("FULGASTRA acquired=" + this.acquired + " split=" + AbyssAiLog.saw(this.mob, 2) + " anim=" + this.mob.getAnimationState());
            if (!this.acquired) {
                this.failures.add("fulgastra never acquired the player");
            }
            if (!AbyssAiLog.saw(this.mob, 2)) {
                this.failures.add("fulgastra did not fire split");
            }
            this.phase++;
            this.tick = 0;
        }

        private void mine() {
            this.phaseName = "splitted_fulgastra";
            if (this.tick == 0) {
                this.clearMobs();
                this.place(this.survival, 0, 76, 0);
                this.survival.setHealth(this.survival.getMaxHealth());
                this.healthAtStart = this.survival.getHealth();
                FulgastraEntity parent = (FulgastraEntity) this.summon(ModEntities.FULGASTRA.get(), 0, 76, 2, false);
                SplittedFulgastraEntity mine = (SplittedFulgastraEntity) ModEntities.SPLITTED_FULGASTRA.get().create(this.level);
                mine.setOwner(parent);
                mine.moveTo(0, 76, 0, 0, 0);
                mine.setStopMoveTick(80);
                this.level.addFreshEntity(mine);
                this.mob = mine;
            }
            this.tick++;
            if (this.tick < 30 && this.survival.getHealth() >= this.healthAtStart) {
                return;
            }
            boolean damaged = this.survival.getHealth() < this.healthAtStart;
            AbyssAiLog.line("MINE anim=" + this.mob.getAnimationState() + " attack=" + AbyssAiLog.attackName(this.mob, this.mob.getAnimationState()) + " damaged=" + damaged + " health=" + this.survival.getHealth());
            if (!damaged) {
                this.failures.add("splitted fulgastra shock did not damage the player");
            }
            this.phase++;
            this.tick = 0;
        }

        private void spine() {
            this.phaseName = "spine_worm";
            if (this.tick == 0) {
                this.clearMobs();
                this.place(this.survival, 8, 76, 0);
                this.survival.setHealth(this.survival.getMaxHealth());
                this.healthAtStart = this.survival.getHealth();
                this.level.setBlock(new BlockPos(-1, 76, 0), Blocks.STONE.defaultBlockState(), 2);
                this.mob = this.summon(ModEntities.SPINE_WORM_HEAD.get(), 0, 76, 0, true);
                this.startDistance = this.mob.distanceTo(this.survival);
                this.closest = this.startDistance;
                this.acquired = false;
                this.ridden = false;
                AbyssAiLog.line("SPINE start dist=" + this.startDistance + " bodies=" + ((SpineWormHeadEntity) this.mob).followers().size());
            }
            this.observe();
            this.tick++;
            if (this.mob.getFirstPassenger() == this.survival) {
                this.ridden = true;
            }
            if (this.tick % 20 == 0) {
                AbyssAiLog.line("SPINE t=" + this.tick + " dist=" + this.mob.distanceTo(this.survival) + " target=" + (this.mob.getTarget() == this.survival) + " passenger=" + this.ridden + " gap=" + chainGap((ChainFish) this.mob));
            }
            boolean damaged = this.survival.getHealth() < this.healthAtStart;
            // The grab is the original attack. doHurtTarget runs every 20 ticks while the player is riding, so wait for that hit.
            if (this.tick < 140 && !damaged) {
                return;
            }
            AbyssAiLog.line("SPINE result acquired=" + this.acquired + " closest=" + this.closest + " passenger=" + this.ridden + " damaged=" + damaged + " health=" + this.survival.getHealth() + " gap=" + chainGap((ChainFish) this.mob));
            if (!this.acquired) {
                this.failures.add("spine worm never acquired the player");
            }
            if (this.closest > this.startDistance - 1.0 && !this.ridden && !damaged) {
                this.failures.add("spine worm head did not reach toward the player");
            }
            if (!this.ridden) {
                this.failures.add("spine worm did not grab the player");
            }
            if (!damaged) {
                this.failures.add("spine worm grab did not damage the player");
            }
            double gap = chainGap((ChainFish) this.mob);
            if (gap > 2.5) {
                this.failures.add("spine worm chain gap " + gap);
            }
            this.level.setBlock(new BlockPos(-1, 76, 0), Blocks.WATER.defaultBlockState(), 2);
            this.phase++;
            this.tick = 0;
        }

        private void siam(SiamserpentHeadEntity.HeadType type, String name) {
            this.phaseName = name;
            if (this.tick == 0) {
                this.clearMobs();
                this.place(this.survival, 6, 76, 0);
                this.survival.setHealth(this.survival.getMaxHealth());
                this.healthAtStart = this.survival.getHealth();
                SiamserpentHeadEntity head = (SiamserpentHeadEntity) this.summon(ModEntities.SIAMSERPENT_HEAD.get(), 0, 76, 0, true);
                head.setHeadType(type);
                this.mob = head;
                this.startDistance = this.mob.distanceTo(this.survival);
                this.closest = this.startDistance;
                this.acquired = false;
                AbyssAiLog.line("SIAM " + name + " followers=" + head.followers().size() + " dist=" + this.startDistance);
            }
            this.observe();
            this.tick++;
            if (this.tick % 40 == 0) {
                AbyssAiLog.line("SIAM " + name + " t=" + this.tick + " dist=" + this.mob.distanceTo(this.survival) + " target=" + (this.mob.getTarget() == this.survival) + " anim=" + this.mob.getAnimationState() + " gap=" + chainGap((ChainFish) this.mob));
            }
            if (this.tick < 220 && this.survival.getHealth() >= this.healthAtStart) {
                return;
            }
            boolean damaged = this.survival.getHealth() < this.healthAtStart;
            double gap = chainGap((ChainFish) this.mob);
            AbyssAiLog.line("SIAM result " + name + " acquired=" + this.acquired + " closest=" + this.closest + " damaged=" + damaged + " anim=" + this.mob.getAnimationState() + " gap=" + gap);
            if (!this.acquired) {
                this.failures.add(name + " never acquired the player");
            }
            if (this.closest > this.startDistance - 1.5 && !damaged) {
                this.failures.add(name + " did not pursue closest=" + this.closest);
            }
            if (!damaged) {
                this.failures.add(name + " did not damage the player");
            }
            if (gap > 2.5) {
                this.failures.add(name + " chain gap " + gap);
            }
            this.phase++;
            this.tick = 0;
        }

        private void observe() {
            if (!this.mob.isAlive()) {
                return;
            }
            if (this.mob.getTarget() == this.survival) {
                this.acquired = true;
            }
            this.closest = Math.min(this.closest, this.mob.distanceTo(this.survival));
        }

        private void finish() {
            if (this.failures.isEmpty()) {
                AbyssAiLog.line("ABYSS_AI_TEST PASS");
            } else {
                for (String failure : this.failures) {
                    AbyssAiLog.line("FAIL " + failure);
                }
                AbyssAiLog.line("ABYSS_AI_TEST FAIL count=" + this.failures.size());
            }
            int code = this.failures.isEmpty() ? 0 : 1;
            this.server.halt(false);
            System.exit(code);
        }

        private void clearMobs() {
            List<Entity> present = new ArrayList<>();
            this.level.getAllEntities().forEach(present::add);
            for (Entity entity : present) {
                if (entity instanceof WaterAbyssCreature) {
                    entity.discard();
                }
                if (entity instanceof Probe probe && probe != this.survival) {
                    probe.discard();
                }
            }
        }

        private Probe probe(String name, boolean creative) {
            Probe probe = new Probe(this.level, name, creative);
            probe.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200.0);
            probe.setHealth(200.0f);
            if (!this.level.addFreshEntity(probe)) {
                throw new IllegalStateException("could not add " + name);
            }
            return probe;
        }

        private WaterAbyssCreature summon(EntityType<? extends WaterAbyssCreature> type, double x, double y, double z, boolean finalize) {
            WaterAbyssCreature mob = type.create(this.level);
            if (mob == null) {
                throw new IllegalStateException("null " + type);
            }
            mob.moveTo(x, y, z, 0.0f, 0.0f);
            if (finalize) {
                MobSpawnType reason = mob instanceof CorpseAnglerEntity ? MobSpawnType.NATURAL : MobSpawnType.MOB_SUMMONED;
                net.neoforged.neoforge.event.EventHooks.finalizeMobSpawn(
                    mob, this.level, this.level.getCurrentDifficultyAt(mob.blockPosition()), reason, null
                );
            }
            this.level.addFreshEntity(mob);
            return mob;
        }

        private void place(Probe probe, double x, double y, double z) {
            probe.moveTo(x, y, z, probe.getYRot(), probe.getXRot());
            probe.refreshFluids();
        }

        private void aim(Vec3 at) {
            Vec3 eye = this.survival.getEyePosition();
            Vec3 delta = at.subtract(eye);
            double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
            float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
            float pitch = (float) Math.toDegrees(-Math.atan2(delta.y, horizontal));
            this.survival.setYRot(yaw);
            this.survival.setYHeadRot(yaw);
            this.survival.setXRot(pitch);
        }

        private double chainGap(ChainFish lead) {
            List<Entity> parts = new ArrayList<>();
            for (UUID id : lead.followers()) {
                Entity entity = this.level.getEntity(id);
                if (entity == null) {
                    return 999.0;
                }
                parts.add(entity);
            }
            if (lead instanceof SpineWormHeadEntity) {
                parts.sort(Comparator.comparingInt(entity -> entity instanceof SpineWormBodyEntity body ? body.chainIndex() : 0));
                parts.add(lead);
            } else {
                parts.add(0, lead);
            }
            double worst = 0.0;
            Vec3 previous = null;
            for (Entity entity : parts) {
                if (previous != null) {
                    worst = Math.max(worst, previous.distanceTo(entity.position()));
                }
                previous = entity.position();
            }
            return worst;
        }

        private static Vec3 centroid(List<Vec3> points) {
            Vec3 sum = Vec3.ZERO;
            for (Vec3 point : points) {
                sum = sum.add(point);
            }
            return sum.scale(1.0 / points.size());
        }

        private static String fmt(Vec3 pos) {
            return String.format("%.2f,%.2f,%.2f", pos.x, pos.y, pos.z);
        }
    }
}
