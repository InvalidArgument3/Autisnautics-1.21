// Manual exit-band diagnostic for chunks that already exist. Nothing here runs on chunk load.
// Preview never changes blocks and never loads or generates a chunk.
// Apply is refused unless an operator passes the preview token, an explicit block-id
// allowlist, a block cap, and the literal word confirm. Block entities are never removed.
// Replacing this file does not restore blocks an apply already changed. Back up the world first.
//
// /autisnautics_rift preview <dimension> <chunkX> <chunkZ> <boundary|all>
// /autisnautics_rift apply <dimension> <chunkX> <chunkZ> <boundary> <id,id> <maxBlocks> <token> confirm
//
// chunkX and chunkZ are chunk coordinates, not block coordinates.
// Only the interior of that boundary's clear band is considered. The next boundary is not opened.
// Y is clamped to the dimension build limits. The 2048 block cap stays; preview again and apply
// the next batch. Do not raise the cap to clear a whole chunk in one call.
;(function () {
    const Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const hardCap = 2048
    let serial = 0, previews = [], previewOrder = []
    function tell(ctx, text) {
        console.info('[RiftRepair] '+text)
        try {
            let player=ctx.source.player
            if (player && player.tell) player.tell(text)
        } catch (error) { /* Server console has no player. The log line is the record. */ }
    }
    function api() {
        if (typeof global==='undefined' || !global || !global.AutisnauticsRift) return null
        return global.AutisnauticsRift
    }
    function findLevel(server, dim) {
        try {
            let it=server.getAllLevels().iterator()
            while (it.hasNext()) {
                let level=it.next()
                if (String(level.dimension)===dim) return level
            }
        } catch (error) { console.warn('[RiftRepair] level lookup failed: '+error) }
        return null
    }
    function chunkOf(level, x, z) {
        try { return level.getChunkSource().getChunkNow(x, z) } catch (error) { return null }
    }
    function hasEntity(chunk, pos, state) {
        let saw=false
        try {
            if (state.hasBlockEntity && state.hasBlockEntity()) return true
            saw=true
        } catch (error) { /* Try the chunk lookup before treating the block as protected. */ }
        try {
            let entity=chunk.getBlockEntity(pos)
            if (entity!==null && entity!==undefined) return true
            saw=true
        } catch (error) { /* Both lookups failed. */ }
        return !saw
    }
    function remember(snapshot) {
        serial++
        let token=String(serial)
        snapshot.token=token
        snapshot.at=Date.now()
        previews[token]=snapshot
        previewOrder.push(token)
        while (previewOrder.length>8) {
            let expired=previewOrder.shift()
            delete previews[expired]
        }
        return token
    }
    function collect(level, chunk, dim, cx, cz, boundary) {
        let rift=api()
        if (!rift) return {error:'Rift cleanup script has not published its span table.'}
        let faces=rift.spans.filter(function (s) { return s.dimension===dim && (boundary==='all' || s.boundary===boundary) })
        if (!faces.length) return {error:'No '+boundary+' span for '+dim+'.'}
        let density=null
        try { density=level.getChunkSource().randomState().router().finalDensity() } catch (error) {
            return {error:'final_density is not readable: '+error}
        }
        let rows=[], counts={}, protectedCounts={}, i=0
        let minY=level.getMinBuildHeight(), maxY=level.getMaxBuildHeight()
        while (i<faces.length) {
            let face=faces[i], lx=0
            i++
            while (lx<16) {
                let lz=0
                while (lz<16) {
                    let x=cx*16+lx, z=cz*16+lz
                    if (rift.interior(density,x,face.anchor,z,face.sentinel)) {
                        let lo=Math.max(face.clearLo,minY), hi=Math.min(face.clearHi,maxY), y=lo
                        while (y<hi) {
                            let pos=new Pos(x,y,z), state=chunk.getBlockState(pos), id=String(state.getBlock().id)
                            if (!state.isAir()) {
                                let row={x:x,y:y,z:z,id:id,boundary:face.boundary,anchor:face.anchor,entity:hasEntity(chunk,pos,state)}
                                rows.push(row)
                                if (row.entity) protectedCounts[id]=(protectedCounts[id]||0)+1
                                else counts[id]=(counts[id]||0)+1
                            }
                            y++
                        }
                    }
                    lz++
                }
                lx++
            }
        }
        rows.sort(function (a,b) {
            let da=Math.abs(a.y-a.anchor), db=Math.abs(b.y-b.anchor)
            if (da!==db) return da-db
            if (a.y!==b.y) return a.y-b.y
            if (a.x!==b.x) return a.x-b.x
            return a.z-b.z
        })
        return {rows:rows, counts:counts, protectedCounts:protectedCounts, faces:faces.length}
    }
    function formatCounts(counts) {
        let parts=[], id
        for (id in counts) parts.push(id+'='+counts[id])
        parts.sort()
        if (!parts.length) return 'none'
        if (parts.length>24) return parts.slice(0,24).join(', ')+' …'
        return parts.join(', ')
    }
    function preview(ctx, dim, cx, cz, boundary) {
        let level=findLevel(ctx.source.server, dim)
        if (!level) { tell(ctx,'No loaded dimension '+dim+'.'); return 0 }
        let chunk=chunkOf(level, cx, cz)
        if (!chunk) { tell(ctx,'Chunk '+cx+','+cz+' is not loaded. This command does not load or generate it.'); return 0 }
        let found=collect(level, chunk, dim, cx, cz, boundary)
        if (found.error) { tell(ctx,found.error); return 0 }
        let token=remember({dim:dim,cx:cx,cz:cz,boundary:boundary,rows:found.rows})
        tell(ctx,'preview token '+token+' '+dim+' chunk '+cx+','+cz+' boundary '+boundary+' non-air='+found.rows.length+' replaceable '+formatCounts(found.counts)+' protected '+formatCounts(found.protectedCounts))
        if (boundary==='all') tell(ctx,'Preview one boundary before apply. all does not authorize a change.')
        else tell(ctx,'Apply only ids you accept, with this token. Block entities stay. Example: /autisnautics_rift apply '+dim+' '+cx+' '+cz+' '+boundary+' minecraft:bedrock 256 '+token+' confirm')
        return 1
    }
    function apply(ctx, dim, cx, cz, boundary, allowText, maxText, token) {
        if (boundary==='all') { tell(ctx,'Apply one boundary. all is preview-only.'); return 0 }
        let snapshot=previews[token]
        if (!snapshot || Date.now()-snapshot.at>600000) { tell(ctx,'Preview token is missing or older than 10 minutes.'); return 0 }
        if (snapshot.dim!==dim || snapshot.cx!==cx || snapshot.cz!==cz || snapshot.boundary!==boundary) {
            tell(ctx,'Token does not match this dimension, chunk, and boundary.'); return 0
        }
        let maxBlocks=parseInt(maxText,10)
        if (!(maxBlocks>0) || maxBlocks>hardCap) { tell(ctx,'maxBlocks must be from 1 to '+hardCap+'.'); return 0 }
        let allow={}, piece, list=String(allowText).split(','), n=0
        while (n<list.length) {
            piece=list[n].replace(/^\s+|\s+$/g,'')
            n++
            if (!piece || piece==='*' || piece.indexOf(':')<0) { tell(ctx,'Allowlist needs explicit block ids, comma-separated.'); return 0 }
            allow[piece]=true
        }
        let level=findLevel(ctx.source.server, dim)
        if (!level) { tell(ctx,'No loaded dimension '+dim+'.'); return 0 }
        let chunk=chunkOf(level, cx, cz)
        if (!chunk) { tell(ctx,'Chunk is not loaded. Nothing was changed.'); return 0 }
        let rift=api(), changed=0, skipped=0, i=0
        if (!rift) { tell(ctx,'Rift cleanup script has not published its span table.'); return 0 }
        let air=Java.loadClass('net.minecraft.world.level.block.Blocks').AIR.defaultBlockState()
        while (i<snapshot.rows.length && changed<maxBlocks) {
            let row=snapshot.rows[i]
            i++
            if (row.boundary!==boundary || !allow[row.id]) { skipped++; continue }
            let pos=new Pos(row.x,row.y,row.z), state=chunk.getBlockState(pos), id=String(state.getBlock().id)
            if (id!==row.id || state.isAir()) { skipped++; continue }
            if (hasEntity(chunk,pos,state)) { skipped++; continue }
            try { level.setBlock(pos, air, rift.quiet); changed++ } catch (error) { skipped++ }
        }
        delete previews[token]
        console.info('[RiftRepair] apply '+dim+' chunk '+cx+','+cz+' '+boundary+' changed='+changed+' skipped='+skipped+' cap='+maxBlocks+' allow='+allowText)
        tell(ctx,'Changed '+changed+' blocks. Skipped '+skipped+'. Token consumed. Back up before relying on this; reverting the script will not restore these blocks.')
        return changed
    }
    function register(event) {
        let Commands=event.commands, Arguments=event.arguments
        function word(ctx, name) { return String(Arguments.STRING.getResult(ctx, name)) }
        function chunkNum(ctx, name) {
            let value=parseInt(word(ctx, name), 10)
            return value
        }
        event.register(Commands.literal('autisnautics_rift')
            .requires(function (src) {
                try { return src.hasPermission(2) } catch (error) {
                    try { return src.hasPermissions(2) } catch (error2) { return false }
                }
            })
            .then(Commands.literal('preview')
                .then(Commands.argument('dim', Arguments.STRING.create(event))
                    .then(Commands.argument('chunkX', Arguments.STRING.create(event))
                        .then(Commands.argument('chunkZ', Arguments.STRING.create(event))
                            .then(Commands.argument('boundary', Arguments.STRING.create(event))
                                .executes(function (ctx) {
                                    let x=chunkNum(ctx,'chunkX'), z=chunkNum(ctx,'chunkZ')
                                    if (!isFinite(x)||!isFinite(z)) { tell(ctx,'chunkX and chunkZ must be integers.'); return 0 }
                                    return preview(ctx, word(ctx,'dim'), x, z, word(ctx,'boundary'))
                                }))))))
            .then(Commands.literal('apply')
                .then(Commands.argument('dim', Arguments.STRING.create(event))
                    .then(Commands.argument('chunkX', Arguments.STRING.create(event))
                        .then(Commands.argument('chunkZ', Arguments.STRING.create(event))
                            .then(Commands.argument('boundary', Arguments.STRING.create(event))
                                .then(Commands.argument('allow', Arguments.STRING.create(event))
                                    .then(Commands.argument('maxBlocks', Arguments.STRING.create(event))
                                        .then(Commands.argument('token', Arguments.STRING.create(event))
                                            .then(Commands.literal('confirm')
                                                .executes(function (ctx) {
                                                    let x=chunkNum(ctx,'chunkX'), z=chunkNum(ctx,'chunkZ')
                                                    if (!isFinite(x)||!isFinite(z)) { tell(ctx,'chunkX and chunkZ must be integers.'); return 0 }
                                                    return apply(ctx, word(ctx,'dim'), x, z, word(ctx,'boundary'), word(ctx,'allow'), word(ctx,'maxBlocks'), word(ctx,'token'))
                                                })))))))))))
    }
    try {
        ServerEvents.commandRegistry(function (event) {
            try { register(event) } catch (error) { console.warn('[RiftRepair] command registration failed: '+error) }
        })
    } catch (error) {
        console.warn('[RiftRepair] command hook unavailable: '+error)
    }
})()
