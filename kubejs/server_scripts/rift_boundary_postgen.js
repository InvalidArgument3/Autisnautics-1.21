// Shared-mask late boundary cleanup. Center geometry lives only in rift/center_trigger.json.
// Cooperative 2 ms/tick budget, resumable inside a column; a single native call can overrun.
// Cached seeded final-density sentinel footprints survive all three safety passes.
// Unfinished v3 markers survive reload; completed/old unmarked chunks are not retrofitted.
;(function () {
    let ServerLevel = Java.loadClass('net.minecraft.server.level.ServerLevel')
    let ChunkLoad = Java.loadClass('net.neoforged.neoforge.event.level.ChunkEvent$Load')
    let Point = Java.loadClass('net.minecraft.world.level.levelgen.DensityFunction$SinglePointContext')
    let Pos = Java.loadClass('net.minecraft.core.BlockPos')
    let Blocks = Java.loadClass('net.minecraft.world.level.block.Blocks')
    let spans = [{"boundary": "0_1", "dimension": "minecraft:overworld", "lo": -53, "hi": 640, "anchor": -52, "sentinel": -2000, "clearLo": -53, "clearHi": -27}, {"boundary": "0_1", "dimension": "infinite_abyss:first_layer", "lo": 40, "hi": 128, "anchor": 127, "sentinel": -2001, "clearLo": 103, "clearHi": 128}, {"boundary": "1_2", "dimension": "infinite_abyss:first_layer", "lo": 0, "hi": 80, "anchor": 0, "sentinel": -2002, "clearLo": 0, "clearHi": 25}, {"boundary": "1_2", "dimension": "deeperdarker:otherside", "lo": 40, "hi": 128, "anchor": 127, "sentinel": -2003, "clearLo": 103, "clearHi": 128}, {"boundary": "2_3", "dimension": "deeperdarker:otherside", "lo": 0, "hi": 80, "anchor": 0, "sentinel": -2004, "clearLo": 0, "clearHi": 25}, {"boundary": "2_3", "dimension": "oceanworld:deepsea", "lo": 160, "hi": 320, "anchor": 299, "sentinel": -2005, "clearLo": 275, "clearHi": 302}, {"boundary": "3_4", "dimension": "oceanworld:deepsea", "lo": -64, "hi": 180, "anchor": -64, "sentinel": -2006, "clearLo": -64, "clearHi": -39}, {"boundary": "3_4", "dimension": "dimension_of_caves:cave", "lo": 40, "hi": 128, "anchor": 127, "sentinel": -2007, "clearLo": 103, "clearHi": 128}, {"boundary": "4_5", "dimension": "dimension_of_caves:cave", "lo": -48, "hi": 64, "anchor": -48, "sentinel": -2008, "clearLo": -48, "clearHi": -23}, {"boundary": "4_5", "dimension": "infernalcross:dimension", "lo": 0, "hi": 64, "anchor": 63, "sentinel": -2009, "clearLo": 39, "clearHi": 64}, {"boundary": "5_6", "dimension": "infernalcross:dimension", "lo": -48, "hi": 16, "anchor": -48, "sentinel": -2010, "clearLo": -48, "clearHi": -23}, {"boundary": "5_6", "dimension": "minecraft:the_nether", "lo": 112, "hi": 256, "anchor": 255, "sentinel": -2011, "clearLo": 231, "clearHi": 256}, {"boundary": "6_7", "dimension": "minecraft:the_nether", "lo": 0, "hi": 144, "anchor": 0, "sentinel": -2012, "clearLo": 0, "clearHi": 25}, {"boundary": "6_7", "dimension": "undergarden:undergarden", "lo": 40, "hi": 128, "anchor": 127, "sentinel": -2013, "clearLo": 103, "clearHi": 128}, {"boundary": "7_8", "dimension": "undergarden:undergarden", "lo": -64, "hi": 64, "anchor": -64, "sentinel": -2014, "clearLo": -64, "clearHi": -39}, {"boundary": "7_8", "dimension": "minecraft:the_end", "lo": 80, "hi": 384, "anchor": 383, "sentinel": -2015, "clearLo": 359, "clearHi": 384}]
    let budgetMs = 2, clock = 0, pending = {}, queue = [], head = 0, depth = 0
    let recent = {}, recentOrder = [], lastTickWall = Date.now(), lastLog = Date.now()
    let stats = {slices:0,workMs:0,maxSliceMs:0,passes:0,lastPassMs:0,maxPassMs:0,lastPassWallMs:0,maskEvals:0,maskHits:0,checks:0,removed:0,maxTickGapMs:0}
    function keyOf(dim,x,z) { return dim+'|'+x+'|'+z }
    function markerOf(x,z) { return 'autisnautics_rift_v3_'+x+'_'+z }
    function request(level,dim,x,z) {
        let key=keyOf(dim,x,z), task=pending[key]
        level.persistentData.putBoolean(markerOf(x,z),true)
        if (task) { task.rescan=true; task.due=Math.min(task.due,clock+2); return }
        let previous=recent[key]
        task={key:key,level:level,dim:dim,x:x,z:z,due:clock+2,passes:3,face:0,col:0,y:null,masks:previous?previous.masks:[],passMs:0,passStarted:Date.now(),removed:0,rescan:false}
        task.faces=spans.filter(s => s.dimension===dim)
        pending[key]=task;queue.push(task);depth++
        if (!previous) recentOrder.push(key)
        recent[key]=task
        while (recentOrder.length>4096) { let expired=recentOrder.shift(); delete recent[expired] }
    }
    NativeEvents.onEvent(ChunkLoad,event => {
        let level=event.getLevel()
        if (!(level instanceof ServerLevel)) return
        let dim=String(level.dimension)
        if (!spans.some(s => s.dimension===dim)) return
        let cp=event.getChunk().getPos()
        if (!event.isNewChunk() && !level.persistentData.getBoolean(markerOf(cp.x,cp.z))) return
        request(level,dim,cp.x,cp.z)
        if (event.isNewChunk()) for (let x=cp.x-1;x<=cp.x+1;x++) for (let z=cp.z-1;z<=cp.z+1;z++) {
            if (x===cp.x && z===cp.z) continue
            if (recent[keyOf(dim,x,z)]) request(level,dim,x,z)
        }
    })
    // Return 0 for unloaded, 1 for yielded, 2 for a completed full pass.
    function sweep(task,deadline) {
        let source=task.level.getChunkSource()
        let chunk=source.getChunkNow(task.x,task.z)
        if (chunk===null) return 0
        let density=source.randomState().router().finalDensity(), units=0
        while (Date.now()<deadline && units++<512) {
            if (task.face>=task.faces.length) return 2
            let s=task.faces[task.face]
            if (task.col>=256) {task.face++;task.col=0;task.y=null;continue}
            let x=task.x*16+(task.col>>4),z=task.z*16+(task.col&15)
            let ci=task.face*256+task.col,active=task.masks[ci]
            if (active===undefined) {
                active=Math.abs(density.compute(new Point(x,s.anchor,z))-s.sentinel)<=0.1
                task.masks[ci]=active;stats.maskEvals++
                if (Date.now()>=deadline) return 1
            } else if (task.y===null) stats.maskHits++
            if (!active) {task.col++;task.y=null;continue}
            if (task.y===null) task.y=s.clearLo
            if (task.y>=s.clearHi) {task.col++;task.y=null;continue}
            let pos=new Pos(x,task.y,z),state=chunk.getBlockState(pos)
            stats.checks++;task.y++
            if (state.isAir()) continue
            let id=String(state.getBlock().id)
            if (id==='minecraft:water' || id==='minecraft:lava') continue
            // AIR has a known shape: flag 18 keeps client notification but suppresses neighbour-shape fan-out.
            task.level.setBlock(pos,Blocks.AIR.defaultBlockState(),18)
            task.removed++;stats.removed++
        }
        return 1
    }
    function averageMspt(server) {
        try {let n=Number(server.getAverageTickTimeNanos())/1000000;if (isFinite(n)) return n.toFixed(2)} catch (ignored) { /* Older server APIs may not expose this accessor. */ }
        try {let n=Number(server.averageTickTimeNanos)/1000000;if (isFinite(n)) return n.toFixed(2)} catch (ignored) { /* Older server APIs may not expose this accessor. */ }
        return 'unavailable'
    }
    ServerEvents.tick(event => {
        clock++
        let started=Date.now(),gap=started-lastTickWall;lastTickWall=started
        stats.maxTickGapMs=Math.max(stats.maxTickGapMs,gap)
        let deadline=started+budgetMs,probes=0
        while (head<queue.length && Date.now()<deadline && probes++<64) {
            let task=queue[head++]
            if (pending[task.key]!==task) continue
            if (task.due>clock) {queue.push(task);continue}
            let before=Date.now(),result=sweep(task,deadline),spent=Date.now()-before
            task.passMs+=spent;stats.workMs+=spent;stats.slices++
            if (result===0) {delete pending[task.key];depth--;continue}
            if (result===2) {
                stats.passes++;stats.lastPassMs=task.passMs;stats.maxPassMs=Math.max(stats.maxPassMs,task.passMs);stats.lastPassWallMs=Date.now()-task.passStarted
                task.passes=task.rescan?3:task.passes-1;task.rescan=false
                if (task.passes<=0) {delete pending[task.key];depth--;task.level.persistentData.remove(markerOf(task.x,task.z));continue}
                task.face=0;task.col=0;task.y=null;task.passMs=0;task.passStarted=Date.now();task.removed=0
                task.due=clock+(task.passes===2?40:task.passes===1?160:2)
            }
            queue.push(task)
        }
        if (head>256 && head*2>queue.length) {queue=queue.slice(head);head=0}
        stats.maxSliceMs=Math.max(stats.maxSliceMs,Date.now()-started)
        if (Date.now()-lastLog>=30000) {
            if (stats.slices>0 || depth>0 || stats.maxTickGapMs>250) console.info('[RiftPerf] budgetMs='+budgetMs+' queue='+depth+' slices='+stats.slices+' workMs='+stats.workMs+' maxSliceMs='+stats.maxSliceMs+' passes='+stats.passes+' lastPassMs='+stats.lastPassMs+' maxPassMs='+stats.maxPassMs+' lastPassWallMs='+stats.lastPassWallMs+' maskEvals='+stats.maskEvals+' maskHits='+stats.maskHits+' checks='+stats.checks+' lateBlocks='+stats.removed+' avgMspt='+averageMspt(event.server)+' maxWallTickGapMs='+stats.maxTickGapMs)
            lastLog=Date.now();stats={slices:0,workMs:0,maxSliceMs:0,passes:0,lastPassMs:0,maxPassMs:0,lastPassWallMs:0,maskEvals:0,maskHits:0,checks:0,removed:0,maxTickGapMs:0}
        }
    })
})()
