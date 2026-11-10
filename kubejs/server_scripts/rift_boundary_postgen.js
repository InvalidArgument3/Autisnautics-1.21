// Shared seeded rift masks. Optional center geometry stays modular in rift/center_trigger.json (currently disabled).
// NEW chunks only: v4 markers resume this work; legacy v3 markers retain boundary-only behavior.
// Seal before draining; cooperative 2 ms slices, halo caching, three delayed safety passes.
// A pass is complete only after the exit band was observed clear. getChunkNow misses retry,
// then park with the marker kept. This script never loads or generates a chunk.
// Existing chunks are not swept from here. See rift_boundary_repair.js for the manual preview.
// Each boundary is one disc on the upper dimension's floor and the same disc on the lower dimension's ceiling.
// It is not a shaft through the stack. Order, top to bottom: overworld, deepsea, caves, abyss, otherside,
// undergarden, infernal, nether, molten_depths, the_end.
// Overworld floor 0_1 keeps lo/clearLo -153 and hi/clearHi 23. Loops clamp to min Y, so blocks run Y=-64 through Y=22.
// waterBelow 188 fills that column with water, because Skylands sea level is 188 and the whole column sits under it.
// Deepsea ceiling is now boundary 0_1: lo/clearLo 275, hi/clearHi 320. Deepsea min_y is -64 and height is 384, so the top block is Y=319.
// Molten depths faces are the solid obsidian caps only: ceiling Y=295..319 and floor Y=-64..-40.
// The continuous metal sea is Y=-39..294 and is not cleared here. The floor loop stops before Y=-39.
// Native setBlock/clearArea calls cannot be preempted; native overruns are measured, not hidden.
;(function () {
    const ServerLevel = Java.loadClass('net.minecraft.server.level.ServerLevel')
    const ChunkLoad = Java.loadClass('net.neoforged.neoforge.event.level.ChunkEvent$Load')
    const Point = Java.loadClass('net.minecraft.world.level.levelgen.DensityFunction$SinglePointContext')
    const Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const Blocks = Java.loadClass('net.minecraft.world.level.block.Blocks')
    const Registries = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries')
    const ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const Box = Java.loadClass('net.minecraft.world.level.levelgen.structure.BoundingBox')
    // No UPDATE_NEIGHBORS (1); UPDATE_CLIENTS (2) + UPDATE_KNOWN_SHAPE (16).
    const QUIET = 18, budgetMs = 2
    const spans = [{"boundary": "0_1", "dimension": "minecraft:overworld", "lo": -153, "hi": 23, "anchor": -52, "sentinel": -2000, "clearLo": -153, "clearHi": 23, "waterBelow": 188}, {"boundary": "0_1", "dimension": "oceanworld:deepsea", "lo": 275, "hi": 320, "anchor": 299, "sentinel": -2005, "clearLo": 275, "clearHi": 320}, {"boundary": "1_2", "dimension": "oceanworld:deepsea", "lo": -64, "hi": -39, "anchor": -64, "sentinel": -2006, "clearLo": -64, "clearHi": -39}, {"boundary": "1_2", "dimension": "dimension_of_caves:cave", "lo": 103, "hi": 128, "anchor": 127, "sentinel": -2007, "clearLo": 103, "clearHi": 128}, {"boundary": "2_3", "dimension": "dimension_of_caves:cave", "lo": -48, "hi": -23, "anchor": -48, "sentinel": -2008, "clearLo": -48, "clearHi": -23}, {"boundary": "2_3", "dimension": "infinite_abyss:first_layer", "lo": 103, "hi": 128, "anchor": 127, "sentinel": -2001, "clearLo": 103, "clearHi": 128}, {"boundary": "3_4", "dimension": "infinite_abyss:first_layer", "lo": 0, "hi": 25, "anchor": 0, "sentinel": -2002, "clearLo": 0, "clearHi": 25}, {"boundary": "3_4", "dimension": "deeperdarker:otherside", "lo": 103, "hi": 128, "anchor": 127, "sentinel": -2003, "clearLo": 103, "clearHi": 128}, {"boundary": "4_5", "dimension": "deeperdarker:otherside", "lo": 0, "hi": 25, "anchor": 0, "sentinel": -2004, "clearLo": 0, "clearHi": 25}, {"boundary": "4_5", "dimension": "undergarden:undergarden", "lo": 103, "hi": 128, "anchor": 127, "sentinel": -2013, "clearLo": 103, "clearHi": 128}, {"boundary": "5_6", "dimension": "undergarden:undergarden", "lo": -64, "hi": -39, "anchor": -64, "sentinel": -2014, "clearLo": -64, "clearHi": -39}, {"boundary": "5_6", "dimension": "infernalcross:dimension", "lo": 39, "hi": 64, "anchor": 63, "sentinel": -2009, "clearLo": 39, "clearHi": 64}, {"boundary": "6_7", "dimension": "infernalcross:dimension", "lo": -48, "hi": -23, "anchor": -48, "sentinel": -2010, "clearLo": -48, "clearHi": -23}, {"boundary": "6_7", "dimension": "minecraft:the_nether", "lo": 231, "hi": 256, "anchor": 255, "sentinel": -2011, "clearLo": 231, "clearHi": 256}, {"boundary": "7_8", "dimension": "minecraft:the_nether", "lo": 0, "hi": 25, "anchor": 0, "sentinel": -2012, "clearLo": 0, "clearHi": 25}, {"boundary": "7_8", "dimension": "autisnautics:molten_depths", "lo": 295, "hi": 320, "anchor": 319, "sentinel": -2016, "clearLo": 295, "clearHi": 320}, {"boundary": "8_9", "dimension": "autisnautics:molten_depths", "lo": -64, "hi": -39, "anchor": -64, "sentinel": -2017, "clearLo": -64, "clearHi": -39}, {"boundary": "8_9", "dimension": "minecraft:the_end", "lo": 359, "hi": 384, "anchor": 383, "sentinel": -2015, "clearLo": 359, "clearHi": 384}];
    const wallIds = {
        'minecraft:overworld':'minecraft:stone',
        'oceanworld:deepsea':'minecraft:stone',
        'dimension_of_caves:cave':'minecraft:deepslate',
        'infinite_abyss:first_layer':'minecraft:deepslate',
        'deeperdarker:otherside':'deeperdarker:sculk_stone',
        'undergarden:undergarden':'undergarden:depthrock',
        'infernalcross:dimension':'minecraft:blackstone',
        'minecraft:the_nether':'minecraft:netherrack',
        'autisnautics:molten_depths':'minecraft:obsidian',
        'minecraft:the_end':'minecraft:end_stone'
    }
    // Euclidean two-block outer ring: 4 axial points at distance 2 and their nearer neighbours.
    const offsets = [[0,0],[1,0],[-1,0],[0,1],[0,-1],[1,1],[1,-1],[-1,1],[-1,-1],[2,0],[-2,0],[0,2],[0,-2]]
    function wantsWater(span,y) { return span.waterBelow!==undefined && y<span.waterBelow }
    function fillState(span,y) {
        if (wantsWater(span,y)) return Blocks.WATER.defaultBlockState()
        return Blocks.AIR.defaultBlockState()
    }
    let walls = {}, clock = 0, pending = {}, queue = [], head = 0, depth = 0
    let recent = {}, recentOrder = [], lastTickWall = Date.now(), lastLog = Date.now()
    let trimAvailable = true, parked = [], parkOverflow = false
    function blankStats() {
        return {slices:0,workMs:0,maxSliceMs:0,passes:0,lastPassMs:0,maxPassMs:0,lastPassWallMs:0,maskEvals:0,maskHits:0,checks:0,airSkipped:0,removed:0,fluidsCleared:0,sealed:0,trimBoxes:0,fluidTicksRemoved:0,maxTrimMs:0,trimErrors:0,maxTickGapMs:0,misses:0,verifyRetries:0}
    }
    let stats = blankStats()
    function keyOf(dim,x,z) { return dim+'|'+x+'|'+z }
    function markerOf(x,z,legacy) { return 'autisnautics_rift_'+(legacy?'v3_':'v4_')+x+'_'+z }
    function wallFor(dim,y) {
        const id=(dim==='minecraft:overworld'||dim==='oceanworld:deepsea')&&y<0?'minecraft:deepslate':wallIds[dim]
        if (!walls[id]) {
            let state=Registries.BLOCK.get(ResourceLocation.parse(id)).defaultBlockState()
            if (state.isAir()) {console.warn('[RiftPerf] Missing wall '+id+'; using deepslate');state=Blocks.DEEPSLATE.defaultBlockState()}
            walls[id]=state
        }
        return walls[id]
    }
    function resetPass(task) {
        task.phase=0;task.face=0;task.col=0;task.offset=0;task.y=null;task.unresolved=0
        task.trimFace=0;task.trimRow=0;task.trimX=0;task.passMs=0;task.passStarted=Date.now()
    }
    function dropParked(key) {
        let kept=[], i=0
        while (i<parked.length) {
            if (parked[i].key!==key) kept.push(parked[i])
            i++
        }
        parked=kept
    }
    function missDelay(misses) {
        let delay=2, i=1
        while (i<misses && delay<200) { delay=delay*2; i++ }
        if (delay>200) delay=200
        return delay
    }
    function request(level,dim,x,z,legacy) {
        let key=keyOf(dim,x,z), task=pending[key]
        dropParked(key)
        if (task) {task.rescan=true;task.due=Math.min(task.due,clock+2);return}
        const previous=recent[key]
        // Neighbour rescans keep the original mode; never upgrade old v3 work to fluid sealing.
        if (previous) legacy=previous.legacy
        level.persistentData.putBoolean(markerOf(x,z,legacy),true)
        task={key:key,level:level,dim:dim,x:x,z:z,due:clock+2,passes:3,legacy:legacy,halo:previous?previous.halo:[],kinds:previous?previous.kinds:[],touched:false,rescan:false,misses:0,verifyFails:0,unresolved:0,minY:level.getMinBuildHeight(),maxY:level.getMaxBuildHeight()}
        task.faces=spans.filter(s => s.dimension===dim)
        resetPass(task);pending[key]=task;queue.push(task);depth++
        if (!previous) recentOrder.push(key)
        recent[key]=task
        // Rhino 2101 reinitializes const on every loop pass and throws "redeclaration of var".
        while (recentOrder.length>4096) {let expired=recentOrder.shift();delete recent[expired]}
    }
    NativeEvents.onEvent(ChunkLoad,event => {
        const level=event.getLevel()
        if (!(level instanceof ServerLevel)) return
        const dim=String(level.dimension)
        if (!spans.some(s => s.dimension===dim)) return
        const cp=event.getChunk().getPos(),fresh=event.isNewChunk()
        const marked=level.persistentData.getBoolean(markerOf(cp.x,cp.z,false))
        const oldMarker=level.persistentData.getBoolean(markerOf(cp.x,cp.z,true))
        if (!fresh && !marked && !oldMarker) return
        request(level,dim,cp.x,cp.z,!fresh&&!marked)
        if (fresh) for (let x=cp.x-1;x<=cp.x+1;x++) for (let z=cp.z-1;z<=cp.z+1;z++) {
            if (x===cp.x&&z===cp.z) continue
            let previous=recent[keyOf(dim,x,z)]
            if (previous) request(level,dim,x,z,previous.legacy)
        }
    })
    function insideAt(task,density,face,lx,lz) {
        let ci=face*400+(lx+2)*20+lz+2,active=task.halo[ci]
        if (active===undefined) {
            const s=task.faces[face]
            active=Math.abs(density.compute(new Point(task.x*16+lx,s.anchor,task.z*16+lz))-s.sentinel)<=0.1
            task.halo[ci]=active;stats.maskEvals++
        } else stats.maskHits++
        return active
    }
    // One halo sample per unit; expensive footprint work is resumable too.
    function prepareUnit(task,density) {
        if (task.face>=task.faces.length) {
            task.phase=task.touched?(task.legacy?2:1):4;task.face=0;task.col=0;return
        }
        if (task.col>=256) {task.face++;task.col=0;task.offset=0;return}
        const index=task.face*256+task.col,kind=task.kinds[index]
        if (kind!==undefined) {if(kind!==0)task.touched=true;task.col++;task.offset=0;return}
        const lx=task.col>>4,lz=task.col&15,offset=offsets[task.offset]
        if (insideAt(task,density,task.face,lx+offset[0],lz+offset[1])) {
            task.kinds[index]=task.offset===0?1:2;task.touched=true;task.col++;task.offset=0;return
        }
        task.offset++
        if (task.legacy || task.offset>=offsets.length) {task.kinds[index]=0;task.col++;task.offset=0}
    }
    function insideOtherFace(task,col,y) {
        for (let f=0;f<task.faces.length;f++) {
            let s=task.faces[f]
            if (task.kinds[f*256+col]===1&&y>=s.lo&&y<s.hi) return true
        }
        return false
    }
    function blockUnit(task,chunk) {
        if (task.face>=task.faces.length) {task.phase++;task.face=0;task.col=0;task.y=null;return}
        if (task.col>=256) {task.face++;task.col=0;task.y=null;return}
        const s=task.faces[task.face],kind=task.kinds[task.face*256+task.col]
        if ((task.phase===1&&kind!==2)||(task.phase===2&&kind!==1)) {task.col++;task.y=null;return}
        const lo=Math.max(task.legacy?s.clearLo:s.lo,task.minY),hi=Math.min(task.legacy?s.clearHi:s.hi,task.maxY)
        if (task.y===null) task.y=lo
        if (task.y>=hi) {task.col++;task.y=null;return}
        const y=task.y++,x=task.x*16+(task.col>>4),z=task.z*16+(task.col&15)
        // Overlapping incoming/outgoing rifts must never be walled off by the other's outer ring.
        if (task.phase===1&&insideOtherFace(task,task.col,y)) return
        // Fully air sections need no per-block Java/Rhino crossings. Never skip a wet/non-air section.
        // The overworld column below sea level is filled with water, so an all-air section still needs work.
        if (((y&15)===0||y===lo)&&chunk.getSection(chunk.getSectionIndex(y)).hasOnlyAir()) {
            if (task.legacy||task.phase!==2||!wantsWater(s,y)) {
                task.y=Math.min(hi,(Math.floor(y/16)+1)*16);stats.airSkipped+=task.y-y;return
            }
        }
        const pos=new Pos(x,y,z),state=chunk.getBlockState(pos)
        stats.checks++
        if (state.isAir()&&(task.legacy||task.phase!==2||!wantsWater(s,y))) return
        const wet=!state.getFluidState().isEmpty()
        if (task.legacy) {
            const id=String(state.getBlock().id)
            if (id==='minecraft:water'||id==='minecraft:lava') return
        } else if (task.phase===1) {
            if (wet) {task.level.setBlock(pos,wallFor(task.dim,y),QUIET);stats.sealed++}
            return
        } else if (!wet&&(y<s.clearLo||y>=s.clearHi)) return
        if (!task.legacy&&wantsWater(s,y)&&String(state.getBlock().id)==='minecraft:water') return
        task.level.setBlock(pos,task.legacy?Blocks.AIR.defaultBlockState():fillState(s,y),QUIET)
        stats.removed++;if(wet)stats.fluidsCleared++
    }
    // Public LevelTicks.clearArea also updates scheduler indexes and collected-this-tick queues.
    // Coalesce protected columns into one-row strips, without cancelling ticks outside the mask/ring.
    // One native strip clear per unit; unlike JS removeIf, no Rhino callback runs for every queued tick.
    function trimUnit(task,chunk) {
        if (task.legacy || !trimAvailable || task.trimFace>=task.faces.length) {task.phase=4;return}
        if (task.trimRow>=16) {task.trimFace++;task.trimRow=0;task.trimX=0;return}
        const row=task.trimRow,face=task.trimFace,s=task.faces[face]
        const lo=Math.max(s.lo,task.minY),hi=Math.min(s.hi,task.maxY)
        if (hi<=lo) {task.trimFace++;task.trimRow=0;task.trimX=0;return}
        while (task.trimX<16&&!task.kinds[face*256+(task.trimX<<4)+row]) task.trimX++
        if (task.trimX>=16) {task.trimRow++;task.trimX=0;return}
        const start=task.trimX++
        while (task.trimX<16&&task.kinds[face*256+(task.trimX<<4)+row]) task.trimX++
        const box=new Box(task.x*16+start,lo,task.z*16+row,task.x*16+task.trimX-1,hi-1,task.z*16+row)
        const before=Date.now()
        try {
            const count=Number(chunk.getFluidTicks().count())
            task.level.getFluidTicks().clearArea(box)
            stats.fluidTicksRemoved+=Math.max(0,count-Number(chunk.getFluidTicks().count()));stats.trimBoxes++
        } catch (error) {
            trimAvailable=false;stats.trimErrors++
            console.warn('[RiftPerf] Fluid tick clearArea unavailable; quiet clearing/sealing continues: '+error)
        }
        stats.maxTrimMs=Math.max(stats.maxTrimMs,Date.now()-before)
    }
    // Exit-band check. Same columns and Y range the clearer treats as the traversable opening.
    function verifyUnit(task,chunk) {
        if (task.face>=task.faces.length) {task.phase=5;return}
        if (task.col>=256) {task.face++;task.col=0;task.y=null;return}
        let s=task.faces[task.face], kind=task.kinds[task.face*256+task.col]
        if (kind!==1) {task.col++;task.y=null;return}
        let lo=Math.max(s.clearLo,task.minY), hi=Math.min(s.clearHi,task.maxY)
        if (task.y===null) task.y=lo
        if (task.y>=hi) {task.col++;task.y=null;return}
        let y=task.y++, pos=new Pos(task.x*16+(task.col>>4),y,task.z*16+(task.col&15))
        if (((y&15)===0||y===lo)&&chunk.getSection(chunk.getSectionIndex(y)).hasOnlyAir()) {
            if (task.legacy||!wantsWater(s,y)) {
                task.y=Math.min(hi,(Math.floor(y/16)+1)*16);return
            }
        }
        let state=chunk.getBlockState(pos)
        stats.checks++
        if (!task.legacy&&wantsWater(s,y)) {
            if (String(state.getBlock().id)==='minecraft:water') return
            task.unresolved++
            return
        }
        if (state.isAir()) return
        if (task.legacy) {
            let id=String(state.getBlock().id)
            if (id==='minecraft:water'||id==='minecraft:lava') return
        }
        task.unresolved++
    }
    // Return 0 not loaded, 1 yielded, 2 exit band observed clear, 3 exit band still blocked.
    function sweep(task,deadline) {
        let source=null, chunk=null
        try {
            source=task.level.getChunkSource()
            chunk=source.getChunkNow(task.x,task.z)
        } catch (error) { return 0 }
        if (chunk===null||source===null) return 0
        let density=null
        try { density=source.randomState().router().finalDensity() } catch (error) { return 0 }
        let units=0
        while (Date.now()<deadline&&units++<512) {
            if (task.phase===0) prepareUnit(task,density)
            else if (task.phase===1||task.phase===2) blockUnit(task,chunk)
            else if (task.phase===3) trimUnit(task,chunk)
            else if (task.phase===4) verifyUnit(task,chunk)
            else return (task.unresolved||0)>0?3:2
        }
        return 1
    }
    function averageMspt(server) {
        try {const first=Number(server.getAverageTickTimeNanos())/1000000;if(isFinite(first))return first.toFixed(2)} catch (ignored) { /* Optional mapped MSPT accessor; try the next form. */ }
        try {const second=Number(server.averageTickTimeNanos)/1000000;if(isFinite(second))return second.toFixed(2)} catch (ignored) { /* Optional mapped MSPT accessor; try the next form. */ }
        return 'unavailable'
    }
    ServerEvents.tick(event => {
        clock++
        const started=Date.now(),gap=started-lastTickWall;lastTickWall=started
        stats.maxTickGapMs=Math.max(stats.maxTickGapMs,gap)
        let deadline=started+budgetMs,probes=0,looked=0
        while (parked.length>0&&looked<2&&Date.now()<deadline) {
            let parkedTask=parked.shift()
            looked++
            let back=null
            try { back=parkedTask.level.getChunkSource().getChunkNow(parkedTask.x,parkedTask.z) } catch (error) { back=null }
            if (back!==null) {
                if (pending[parkedTask.key]!==parkedTask) {
                    parkedTask.misses=0;parkedTask.due=clock
                    pending[parkedTask.key]=parkedTask;queue.push(parkedTask);depth++
                }
            } else parked.push(parkedTask)
        }
        while (head<queue.length&&Date.now()<deadline&&probes++<64) {
            let task=queue[head++]
            if (pending[task.key]!==task) continue
            if (task.due>clock) {queue.push(task);continue}
            let before=Date.now(),result=sweep(task,deadline),spent=Date.now()-before
            task.passMs+=spent;stats.workMs+=spent;stats.slices++
            if (result===0) {
                task.misses=(task.misses||0)+1;stats.misses++
                if (task.misses>=8) {
                    delete pending[task.key];depth--
                    if (parked.length<2048) parked.push(task)
                    else if (!parkOverflow) { parkOverflow=true; console.warn('[RiftPerf] park list full; markers kept until the chunk loads again') }
                    if (!task.missLogged) { task.missLogged=true; console.warn('[RiftPerf] parked '+task.key+' after getChunkNow misses; marker kept') }
                    continue
                }
                task.due=clock+missDelay(task.misses);queue.push(task);continue
            }
            task.misses=0
            if (result===3) {
                stats.verifyRetries++;task.verifyFails=(task.verifyFails||0)+1
                resetPass(task)
                task.due=clock+(task.verifyFails>=4?200:20)
                if (task.verifyFails>=4) task.verifyFails=0
                if (!task.verifyLogged) { task.verifyLogged=true; console.warn('[RiftPerf] exit band still blocked '+task.key+'; marker kept') }
                queue.push(task);continue
            }
            if (result===2) {
                task.verifyLogged=false
                stats.passes++;stats.lastPassMs=task.passMs;stats.maxPassMs=Math.max(stats.maxPassMs,task.passMs);stats.lastPassWallMs=Date.now()-task.passStarted
                task.passes=task.rescan?3:task.passes-1;task.rescan=false
                if (task.passes<=0) {delete pending[task.key];depth--;task.level.persistentData.remove(markerOf(task.x,task.z,task.legacy));continue}
                resetPass(task);task.due=clock+(task.passes===2?40:task.passes===1?160:2)
            }
            queue.push(task)
        }
        if (head>256&&head*2>queue.length) {queue=queue.slice(head);head=0}
        stats.maxSliceMs=Math.max(stats.maxSliceMs,Date.now()-started)
        if (Date.now()-lastLog>=30000) {
            if (stats.slices>0||depth>0||parked.length>0||stats.maxTickGapMs>250) console.info('[RiftPerf] budgetMs='+budgetMs+' queue='+depth+' parked='+parked.length+' slices='+stats.slices+' workMs='+stats.workMs+' maxSliceMs='+stats.maxSliceMs+' passes='+stats.passes+' lastPassMs='+stats.lastPassMs+' maxPassMs='+stats.maxPassMs+' lastPassWallMs='+stats.lastPassWallMs+' maskEvals='+stats.maskEvals+' maskHits='+stats.maskHits+' checks='+stats.checks+' airSkipped='+stats.airSkipped+' lateBlocks='+stats.removed+' fluidsCleared='+stats.fluidsCleared+' sealed='+stats.sealed+' trimBoxes='+stats.trimBoxes+' fluidTicksRemoved='+stats.fluidTicksRemoved+' maxTrimMs='+stats.maxTrimMs+' trimErrors='+stats.trimErrors+' misses='+stats.misses+' verifyRetries='+stats.verifyRetries+' avgMspt='+averageMspt(event.server)+' maxWallTickGapMs='+stats.maxTickGapMs)
            lastLog=Date.now();stats=blankStats()
        }
    })
    // global is not assignable from a server script on this KubeJS build.
    // kubejs/startup_scripts/rift_api.js publishes the same spans for /autisnautics_rift.
})()
