package me.cortex.voxy.common.network;
import java.util.*;
import java.util.function.*;
/** Main-thread owned, bounded v2 reliable batches. No networking/world access; used directly in production. */
public final class ReliableTerrainQueue<T> {
 public record Key(String owner,String world,int x,int z) {}
 public record Part<T>(T payload,int bytes,int sections) {}
 public static final class Frame<T> {
  public final long id; public final Key key; public final Part<T> part; public final long created;
  private long sent; private int attempts;
  Frame(long id,Key key,Part<T> part,long now){this.id=id;this.key=key;this.part=part;this.created=now;}
 }
 private static final class Column {long revision;int remaining;Column(long r,int n){revision=r;remaining=n;}}
 private record Completed(long revision,long until) {}
 public final int maxEntries,maxBytes,ownerEntries,ownerBytes,windowEntries,windowBytes;
 private final LinkedHashMap<Long,Frame<T>> pending=new LinkedHashMap<>(),flight=new LinkedHashMap<>();
 private final Map<Key,Column> columns=new HashMap<>();
 private final LinkedHashMap<Key,Completed> completed=new LinkedHashMap<>();
 private final ArrayDeque<String> roundRobin=new ArrayDeque<>();
 private long nextId=1;private int bytes;
 public long offered,sent,retried,applied,completedColumns,rejected,cancelled,expired;
 public ReliableTerrainQueue(int entries,int bytes,int perOwnerEntries,int perOwnerBytes,int windowEntries,int windowBytes){
  if(entries<1||bytes<1||perOwnerEntries<1||perOwnerBytes<1||windowEntries<1||windowBytes<1)throw new IllegalArgumentException();
  this.maxEntries=entries;this.maxBytes=bytes;this.ownerEntries=perOwnerEntries;this.ownerBytes=perOwnerBytes;this.windowEntries=windowEntries;this.windowBytes=windowBytes;
 }
 private List<Frame<T>> all(){ArrayList<Frame<T>> l=new ArrayList<>(pending.values());l.addAll(flight.values());return l;}
 public boolean known(Key k,long now){Completed c=completed.get(k);return columns.containsKey(k)||(c!=null&&c.until>now);}
 public boolean offer(Key key,long revision,List<Part<T>> parts,long now){
  if(parts.isEmpty())return false;
  Completed done=completed.get(key);if(done!=null&&done.until>now&&done.revision==revision)return true;
  Column old=columns.get(key);if(old!=null&&old.revision==revision)return true;
  long newBytes=0;for(Part<T> p:parts){if(p.bytes<1||p.bytes>windowBytes||p.sections<1||p.sections>TerrainWireLimits.MAX_BATCH_SECTIONS){rejected++;return false;}newBytes+=p.bytes;}
  int ownerN=0,ownerB=0,oldN=0,oldB=0;for(Frame<T> f:all()){if(f.key.owner.equals(key.owner)){ownerN++;ownerB+=f.part.bytes;}if(f.key.equals(key)){oldN++;oldB+=f.part.bytes;}}
  if(parts.size()>maxEntries||newBytes>maxBytes||pending.size()+flight.size()-oldN+parts.size()>maxEntries||(long)bytes-oldB+newBytes>maxBytes||ownerN-oldN+parts.size()>ownerEntries||(long)ownerB-oldB+newBytes>ownerBytes){rejected++;return false;}
  cancel(key);completed.remove(key);columns.put(key,new Column(revision,parts.size()));
  for(Part<T> p:parts){Frame<T> f=new Frame<>(nextId++,key,p,now);pending.put(f.id,f);bytes+=p.bytes;offered++;}
  if(!roundRobin.contains(key.owner))roundRobin.add(key.owner);return true;
 }
 public void cancel(Key key){for(Iterator<Frame<T>> i=pending.values().iterator();i.hasNext();){Frame<T> f=i.next();if(f.key.equals(key)){bytes-=f.part.bytes;i.remove();cancelled++;}}for(Iterator<Frame<T>> i=flight.values().iterator();i.hasNext();){Frame<T> f=i.next();if(f.key.equals(key)){bytes-=f.part.bytes;i.remove();cancelled++;}}columns.remove(key);}
 public void cancelOversized(int sectionLimit,long byteLimit){for(Frame<T> f:all())if((sectionLimit>0&&f.part.sections>sectionLimit)||(byteLimit>0&&f.part.bytes>byteLimit)){rejected++;cancel(f.key);}}
 public void clearOwner(String owner){for(Key k:new ArrayList<>(columns.keySet()))if(k.owner.equals(owner))cancel(k);completed.keySet().removeIf(k->k.owner.equals(owner));roundRobin.removeIf(owner::equals);}
 public void clear(){pending.clear();flight.clear();columns.clear();completed.clear();roundRobin.clear();bytes=0;}
 public boolean ack(String owner,long id,int status,long now){if(status<0||status>2)throw new IllegalArgumentException("ACK status");Frame<T> f=flight.get(id);if(f==null||!f.key.owner.equals(owner))return false;
  if(status==2){cancel(f.key);return true;}
  flight.remove(id);
  if(status==1){if(f.attempts>=4){expired++;cancel(f.key);bytes-=f.part.bytes;}else pending.put(id,f);return true;}
  bytes-=f.part.bytes;applied+=f.part.sections;Column c=columns.get(f.key);if(c!=null&&--c.remaining==0){columns.remove(f.key);completed.put(f.key,new Completed(c.revision,now+30000));completedColumns++;while(completed.size()>1024)completed.remove(completed.keySet().iterator().next());}return true;
 }
 /** Bounded scans: global cap 512 frames; at most 64 send attempts/tick in production. */
 public void tick(long now,Predicate<Key> valid,BiPredicate<Integer,Integer> budget,Consumer<Frame<T>> send,int maxWork){tick(now,valid,budget,send,k->true,maxWork);}
 public void tick(long now,Predicate<Key> valid,BiPredicate<Integer,Integer> budget,Consumer<Frame<T>> send,Predicate<Key> writable,int maxWork){
  for(Frame<T> f:new ArrayList<>(flight.values())){if(!flight.containsKey(f.id))continue;if(!valid.test(f.key)){cancel(f.key);continue;}if(now-f.sent>=5000){if(f.attempts>=4){expired++;cancel(f.key);}else{flight.remove(f.id);pending.put(f.id,f);}}}
  int attempts=Math.min(maxWork,Math.max(1,roundRobin.size())*2);
  for(int n=0;n<attempts&&!roundRobin.isEmpty();n++){
   String owner=roundRobin.removeFirst();Frame<T> chosen=null;for(Frame<T> f:pending.values())if(f.key.owner.equals(owner)){chosen=f;break;}
   if(chosen==null){boolean active=false;for(Frame<T> f:flight.values())if(f.key.owner.equals(owner)){active=true;break;}if(active)roundRobin.addLast(owner);continue;}
   roundRobin.addLast(owner);Frame<T> f=chosen;
   if(!valid.test(f.key)){cancel(f.key);continue;}
   if(!writable.test(f.key))continue;
   int nFlight=0,bFlight=0;for(Frame<T> g:flight.values())if(g.key.owner.equals(owner)){nFlight++;bFlight+=g.part.bytes;}
   if(nFlight>=windowEntries||(long)bFlight+f.part.bytes>windowBytes)continue;
   if(!budget.test(f.part.sections,f.part.bytes))continue;
   pending.remove(f.id);flight.put(f.id,f);f.sent=now;if(f.attempts++>0)retried++;
   try{send.accept(f);sent++;}catch(RuntimeException e){flight.remove(f.id);pending.put(f.id,f);if(f.attempts>=4){expired++;cancel(f.key);}throw e;}
  }
  int cleaned=0;for(Iterator<Map.Entry<Key,Completed>> i=completed.entrySet().iterator();i.hasNext()&&cleaned++<64;)if(i.next().getValue().until<=now)i.remove();
 }
 public int entries(){return pending.size()+flight.size();}public int bytes(){return bytes;}public int inFlight(){return flight.size();}
 public long oldestAge(long now){long oldest=now;for(Frame<T> f:all())oldest=Math.min(oldest,f.created);return now-oldest;}
 public String status(long now){return "queued="+entries()+" bytes="+bytes+" flight="+inFlight()+" ageMs="+oldestAge(now)+" offered="+offered+" sent="+sent+" retried="+retried+" appliedSections="+applied+" completedColumns="+completedColumns+" rejected="+rejected+" cancelled="+cancelled+" expired="+expired;}
}
