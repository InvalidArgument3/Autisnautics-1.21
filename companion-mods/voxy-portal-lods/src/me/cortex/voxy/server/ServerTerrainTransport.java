package me.cortex.voxy.server;
import me.cortex.voxy.common.Logger;
import me.cortex.voxy.common.config.VoxyConfig;
import me.cortex.voxy.common.network.*;
import me.cortex.voxy.commonImpl.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;
import java.security.MessageDigest;
/** Server-thread owned v2 transport. Max 16 MiB/512 batches globally; 4 MiB/128 per player; two in flight. */
public final class ServerTerrainTransport {
 private record Body(ResourceLocation level,ResourceLocation type,long seed,List<byte[]> data){}
 private static final ReliableTerrainQueue<Body> QUEUE=new ReliableTerrainQueue<>(1024,32*1024*1024,256,12*1024*1024,24,6*1024*1024);
 private static final Map<String,ServerPlayer> PLAYERS=new HashMap<>();
 private static final boolean DEBUG=VoxyCommon.isVerificationFlagOn("terrainSyncDebug",false);private static long lastReport,sentBytes,oversized,handoffFailures,lastHandoffWarning;
 private ServerTerrainTransport(){}
 public static void register(){NeoForge.EVENT_BUS.addListener(ServerTerrainTransport::tick);}
 private static String owner(ServerPlayer p){return p.getUUID().toString();}
 private static String world(ServerLevel l){WorldIdentifier w=WorldIdentifier.of(l);return w==null?"":w.key.location()+"|"+w.dimension.location()+"|"+w.biomeSeed;}
 public static String coverageWorldKey(ServerLevel l){return l.dimension().location().toString()+"#"+l.getSeed();}
 private static ReliableTerrainQueue.Key key(ServerPlayer p,ServerLevel l,ChunkPos c){return new ReliableTerrainQueue.Key(owner(p),world(l),c.x,c.z);}
 public static boolean known(ServerPlayer p,ServerLevel l,ChunkPos c){return QUEUE.known(key(p,l,c),(System.nanoTime()/1_000_000L))||TerrainSentMemory.holds(owner(p),coverageWorldKey(l),c.x,c.z);}
 public static boolean offerColumn(ServerPlayer p,ServerLevel l,ChunkPos c,List<byte[]> data){
  WorldIdentifier w=WorldIdentifier.of(l);if(w==null||data.isEmpty()||!valid(key(p,l,c),p))return false;
  int maxSections=Math.max(1,Math.min(8,VoxyConfig.CONFIG.serverTerrainSyncMicroBatchEnabled?VoxyConfig.CONFIG.serverTerrainSyncMicroBatchMaxSections:1));
  int sectionBudget=VoxyConfig.CONFIG.serverTerrainSyncMaxSectionsPerTick;if(sectionBudget>0)maxSections=Math.min(maxSections,sectionBudget);
  int maxBytes=VoxyConfig.CONFIG.serverTerrainSyncMicroBatchMaxBytes;maxBytes=maxBytes<=0?TerrainWireLimits.MAX_BATCH_BYTES:Math.min(maxBytes,TerrainWireLimits.MAX_BATCH_BYTES);
  long byteBudget=Integer.toUnsignedLong(VoxyConfig.CONFIG.serverTerrainSyncMaxBytesPerTick);if(byteBudget>0)maxBytes=(int)Math.min(maxBytes,byteBudget);
  ArrayList<ReliableTerrainQueue.Part<Body>> parts=new ArrayList<>();ArrayList<byte[]> batch=new ArrayList<>();int bytes=0;MessageDigest digest;
  try{digest=MessageDigest.getInstance("SHA-256");}catch(Exception e){throw new IllegalStateException(e);}
  for(byte[] a:data){if(a.length<1||a.length>TerrainWireLimits.MAX_SECTION_BYTES||a.length>maxBytes){oversized++;return false;}digest.update((byte)(a.length>>>24));digest.update((byte)(a.length>>>16));digest.update((byte)(a.length>>>8));digest.update((byte)a.length);digest.update(a);
   if(!batch.isEmpty()&&(batch.size()>=maxSections||bytes+a.length>maxBytes)){parts.add(part(w,batch,bytes));batch=new ArrayList<>();bytes=0;}
   batch.add(a);bytes+=a.length;
  }
  if(!batch.isEmpty())parts.add(part(w,batch,bytes));byte[] hash=digest.digest();long revision=0;for(int i=0;i<8;i++)revision=(revision<<8)|(hash[i]&255);
  PLAYERS.put(owner(p),p);boolean accepted=QUEUE.offer(key(p,l,c),revision,parts,(System.nanoTime()/1_000_000L));
  if(accepted)TerrainSentMemory.mark(owner(p),coverageWorldKey(l),c.x,c.z);
  return accepted;
 }
 private static ReliableTerrainQueue.Part<Body> part(WorldIdentifier w,List<byte[]> list,int n){return new ReliableTerrainQueue.Part<>(new Body(w.key.location(),w.dimension.location(),w.biomeSeed,List.copyOf(list)),n,list.size());}
 static int effectiveInnerRadius(ServerPlayer p){
  int cfg=VoxyConfig.CONFIG.serverTerrainSyncInnerRadiusChunks;
  if(cfg>0)return cfg;
  int vd=p.server!=null?p.server.getPlayerList().getViewDistance():10;
  return Math.max(1,vd-1);
 }
 private static boolean valid(ReliableTerrainQueue.Key k,ServerPlayer p){
  if(p==null||!p.isAlive()||p.connection==null||!VoxyConfig.CONFIG.serverTerrainSyncBroadcast||!VoxySessionSupport.wantsTerrainPackets(p))return false;
  int max=VoxySessionSupport.getMaxTerrainDistanceChunks(p);
  if(max<=0)return false;
  int distance=new ChunkPos(k.x(),k.z()).getChessboardDistance(p.chunkPosition());
  if(world(p.serverLevel()).equals(k.world())){
   return distance>effectiveInnerRadius(p)&&distance<=max;
  }
  for(ServerLevel adj:ImmPortalsServerCompat.getAdjacentDimstackLevels(p)){
   if(world(adj).equals(k.world())){
    int adjMax=Math.min(max,ImmPortalsServerCompat.ADJACENT_DIM_MAX_RADIUS_CHUNKS);
    return distance<=adjMax;
   }
  }
  return false;
 }
 public static void ack(ServerPlayer p,long id,int status){QUEUE.ack(owner(p),id,status,(System.nanoTime()/1_000_000L));}
 public static void clearPlayer(ServerPlayer p){QUEUE.clearOwner(owner(p));PLAYERS.remove(owner(p));TerrainSentMemory.clearPlayer(owner(p));}
 public static void clear(){QUEUE.clear();PLAYERS.clear();TerrainSentMemory.clearAll();}
 static boolean prepareTick(boolean dedicated,boolean broadcast){if(!dedicated)return false;if(!broadcast){clear();return false;}return true;}
 private static final java.lang.invoke.MethodHandle CONN_CHANNEL;
 static{
  java.lang.invoke.MethodHandle mh=null;
  try{mh=java.lang.invoke.MethodHandles.publicLookup().findVirtual(net.minecraft.network.Connection.class,"channel",java.lang.invoke.MethodType.methodType(io.netty.channel.Channel.class));}catch(Throwable ignored){}
  CONN_CHANNEL=mh;
 }
 private static boolean isWritable(ServerPlayer p){
  if(p==null||p.connection==null)return false;
  var conn=((net.neoforged.neoforge.common.extensions.ICommonPacketListener)p.connection).getConnection();
  if(conn==null||!conn.isConnected())return false;
  if(CONN_CHANNEL!=null){
   try{io.netty.channel.Channel ch=(io.netty.channel.Channel)CONN_CHANNEL.invokeExact(conn);return ch!=null&&ch.isWritable();}catch(Throwable ignored){}
  }
  return true;
 }
 private static void tick(ServerTickEvent.Post e){if(!e.getServer().isDedicatedServer())return;if(!prepareTick(true,VoxyConfig.CONFIG.serverTerrainSyncBroadcast))return;QUEUE.cancelOversized(VoxyConfig.CONFIG.serverTerrainSyncMaxSectionsPerTick,Integer.toUnsignedLong(VoxyConfig.CONFIG.serverTerrainSyncMaxBytesPerTick));try{QUEUE.tick((System.nanoTime()/1_000_000L),k->valid(k,PLAYERS.get(k.owner())),(sections,bytes)->ServerTerrainSyncBudget.tryConsume(sections,bytes),f->{ServerPlayer p=PLAYERS.get(f.key.owner());Body b=f.part.payload();PacketDistributor.sendToPlayer(p,new SectionSyncBatchPayload(f.id,b.level,b.type,b.seed,b.data));sentBytes+=f.part.bytes();},k->isWritable(PLAYERS.get(k.owner())),64);}catch(RuntimeException failure){handoffFailures++;long warningNow=System.nanoTime()/1_000_000L;if(warningNow-lastHandoffWarning>=5000){lastHandoffWarning=warningNow;Logger.warn("[terrain-sync][server-v2] Network handoff failed; bounded retry retained (failures=",handoffFailures,")");}}long now=(System.nanoTime()/1_000_000L);if((DEBUG||VoxyConfig.CONFIG.serverTerrainSyncDebug)&&now-lastReport>=5000){lastReport=now;Logger.info("[terrain-sync][server-v2] ",status());}}
 public static String status(){return QUEUE.status((System.nanoTime()/1_000_000L))+" actualInnerBytesSent="+sentBytes+" oversized="+oversized+" handoffFailures="+handoffFailures+" covPkts="+TerrainSentMemory.coveragePackets();}
}
