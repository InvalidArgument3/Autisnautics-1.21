// Shared seeded rift masks. Optional center geometry stays modular in rift/center_trigger.json (currently disabled).
// NEW chunks only: v4 markers resume this work; legacy v3 markers retain boundary-only behavior.
// Seal before draining; cooperative 2 ms slices, halo caching, three delayed safety passes.
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
    const spans = [{"boundary": "0_1", "dimension": "minecraft:overworld", "lo": -53, "hi": 640, "anchor": -52, "sentinel": -2000, "clearLo": -53, "clearHi": -27}, {"boundary": "0_1", "dimension": "infinite_abyss:first_layer", "lo": 40, "hi": 128, "anchor": 127, "sentinel": -2001, "clearLo": 103, "clearHi": 128}, {"boundary": "1_2", "dimension": "infinite_abyss:first_layer", "lo": 0, "hi": 80, "anchor": 0, "sentinel": -2002, "clearLo": 0, "clearHi": 25}, {"boundary": "1_2", "dimension": "deeperdarker:otherside", "lo": 40, "hi": 128, "anchor": 127, "sentinel": -2003, "clearLo": 103, "clearHi": 128}, {"boundary": "2_3", "dimension": "deeperdarker:otherside", "lo": 0, "hi": 80, "anchor": 0, "sentinel": -2004, "clearLo": 0, "clearHi": 25}, {"boundary": "2_3", "dimension": "oceanworld:deepsea", "lo": 160, "hi": 320, "anchor": 299, "sentinel": -2005, "clearLo": 275, "clearHi": 302}, {"boundary": "3_4", "dimension": "oceanworld:deepsea", "lo": -64, "hi": 180, "anchor": -64, "sentinel": -2006, "clearLo": -64, "clearHi": -39}, {"boundary": "3_4", "dimension": "dimension_of_caves:cave", "lo": 40, "hi": 128, "anchor": 127, "sentinel": -2007, "clearLo": 103, "clearHi": 128}, {"boundary": "4_5", "dimension": "dimension_of_caves:cave", "lo": -48, "hi": 64, "anchor": -48, "sentinel": -2008, "clearLo": -48, "clearHi": -23}, {"boundary": "4_5", "dimension": "infernalcross:dimension", "lo": 0, "hi": 64, "anchor": 63, "sentinel": -2009, "clearLo": 39, "clearHi": 64}, {"boundary": "5_6", "dimension": "infernalcross:dimension", "lo": -48, "hi": 16, "anchor": -48, "sentinel": -2010, "clearLo": -48, "clearHi": -23}, {"boundary": "5_6", "dimension": "minecraft:the_nether", "lo": 112, "hi": 256, "anchor": 255, "sentinel": -2011, "clearLo": 231, "clearHi": 256}, {"boundary": "6_7", "dimension": "minecraft:the_nether", "lo": 0, "hi": 144, "anchor": 0, "sentinel": -2012, "clearLo": 0, "clearHi": 25}, {"boundary": "6_7", "dimension": "undergarden:undergarden", "lo": 40, "hi": 128, "anchor": 127, "sentinel": -2013, "clearLo": 103, "clearHi": 128}, {"boundary": "7_8", "dimension": "undergarden:undergarden", "lo": -64, "hi": 64, "anchor": -64, "sentinel": -2014, "clearLo": -64, "clearHi": -39}, {"boundary": "7_8", "dimension": "minecraft:the_end", "lo": 80, "hi": 384, "anchor": 383, "sentinel": -2015, "clearLo": 359, "clearHi": 384}]
    const wallIds = {
        'minecraft:overworld':'minecraft:stone',
        'infinite_abyss:first_layer':'minecraft:deepslate',
        'deeperdarker:otherside':'deeperdarker:sculk_stone',
        'oceanworld:deepsea':'minecraft:stone',
        'dimension_of_caves:cave':'minecraft:deepslate',
        'infernalcross:dimension':'minecraft:blackstone',
        'minecraft:the_nether':'minecraft:netherrack',
        'undergarden:undergarden':'undergarden:depthrock',
        'minecraft:the_end':'minecraft:end_stone'
    }
    // Euclidean two-block outer ring: 4 axial points at distance 2 and their nearer neighbours.
    const offsets = [[0,0],[1,0],[-1,0],[0,1],[0,-1],[1,1],[1,-1],[-1,1],[-1,-1],[2,0],[-2,0],[0,2],[0,-2]]
    let walls = {}, clock = 0, pending = {}, queue = [], head = 0, depth = 0
    let recent = {}, recentOrder = [], lastTickWall = Date.now(), lastLog = Date.now()
    let trimAvailable = true
    function blankStats() {
        return {slices:0,workMs:0,maxSliceMs:0,passes:0,lastPassMs:0,maxPassMs:0,lastPassWallMs:0,maskEvals:0,maskHits:0,checks:0,airSkipped:0,removed:0,fluidsCleared:0,sealed:0,trimBoxes:0,fluidTicksRemoved:0,maxTrimMs:0,trimErrors:0,maxTickGapMs:0}
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
        task.phase=0;task.face=0;task.col=0;task.offset=0;task.y=null
        task.trimFace=0;task.trimRow=0;task.trimX=0;task.passMs=0;task.passStarted=Date.now()
    }
    function request(level,dim,x,z,legacy) {
        let key=keyOf(dim,x,z), task=pending[key]
        if (task) {task.rescan=true;task.due=Math.min(task.due,clock+2);return}
        const previous=recent[key]
        // Neighbour rescans keep the original mode; never upgrade old v3 work to fluid sealing.
        if (previous) legacy=previous.legacy
        level.persistentData.putBoolean(markerOf(x,z,legacy),true)
        task={key:key,level:level,dim:dim,x:x,z:z,due:clock+2,passes:3,legacy:legacy,halo:previous?previous.halo:[],kinds:previous?previous.kinds:[],touched:false,rescan:false,minY:level.getMinBuildHeight(),maxY:level.getMaxBuildHeight()}
        task.faces=spans.filter(s => s.dimension===dim)
        resetPass(task);pending[key]=task;queue.push(task);depth++
        if (!previous) recentOrder.push(key)
        recent[key]=task
        while (recentOrder.length>4096) {const expired=recentOrder.shift();delete recent[expired]}
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
            const previous=recent[keyOf(dim,x,z)]
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
            const s=task.faces[f]
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
        if (((y&15)===0||y===lo)&&chunk.getSection(chunk.getSectionIndex(y)).hasOnlyAir()) {
            task.y=Math.min(hi,(Math.floor(y/16)+1)*16);stats.airSkipped+=task.y-y;return
        }
        const pos=new Pos(x,y,z),state=chunk.getBlockState(pos)
        stats.checks++
        if (state.isAir()) return
        const wet=!state.getFluidState().isEmpty()
        if (task.legacy) {
            const id=String(state.getBlock().id)
            if (id==='minecraft:water'||id==='minecraft:lava') return
        } else if (task.phase===1) {
            if (wet) {task.level.setBlock(pos,wallFor(task.dim,y),QUIET);stats.sealed++}
            return
        } else if (!wet&&(y<s.clearLo||y>=s.clearHi)) return
        task.level.setBlock(pos,Blocks.AIR.defaultBlockState(),QUIET)
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
    // Return 0 unloaded, 1 yielded, 2 completed full pass.
    function sweep(task,deadline) {
        const source=task.level.getChunkSource(),chunk=source.getChunkNow(task.x,task.z)
        if (chunk===null) return 0
        let density=source.randomState().router().finalDensity(),units=0
        while (Date.now()<deadline&&units++<512) {
            if (task.phase===0) prepareUnit(task,density)
            else if (task.phase===1||task.phase===2) blockUnit(task,chunk)
            else if (task.phase===3) trimUnit(task,chunk)
            else return 2
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
        let deadline=started+budgetMs,probes=0
        while (head<queue.length&&Date.now()<deadline&&probes++<64) {
            const task=queue[head++]
            if (pending[task.key]!==task) continue
            if (task.due>clock) {queue.push(task);continue}
            const before=Date.now(),result=sweep(task,deadline),spent=Date.now()-before
            task.passMs+=spent;stats.workMs+=spent;stats.slices++
            if (result===0) {delete pending[task.key];depth--;continue}
            if (result===2) {
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
            if (stats.slices>0||depth>0||stats.maxTickGapMs>250) console.info('[RiftPerf] budgetMs='+budgetMs+' queue='+depth+' slices='+stats.slices+' workMs='+stats.workMs+' maxSliceMs='+stats.maxSliceMs+' passes='+stats.passes+' lastPassMs='+stats.lastPassMs+' maxPassMs='+stats.maxPassMs+' lastPassWallMs='+stats.lastPassWallMs+' maskEvals='+stats.maskEvals+' maskHits='+stats.maskHits+' checks='+stats.checks+' airSkipped='+stats.airSkipped+' lateBlocks='+stats.removed+' fluidsCleared='+stats.fluidsCleared+' sealed='+stats.sealed+' trimBoxes='+stats.trimBoxes+' fluidTicksRemoved='+stats.fluidTicksRemoved+' maxTrimMs='+stats.maxTrimMs+' trimErrors='+stats.trimErrors+' avgMspt='+averageMspt(event.server)+' maxWallTickGapMs='+stats.maxTickGapMs)
            lastLog=Date.now();stats=blankStats()
        }
    })
})()
