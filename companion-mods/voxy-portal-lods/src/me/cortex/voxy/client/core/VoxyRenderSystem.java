package me.cortex.voxy.client.core;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import me.cortex.voxy.client.TimingStatistics;
import me.cortex.voxy.client.VoxyClient;
import me.cortex.voxy.client.compat.CrossDimPortalManager;
import me.cortex.voxy.client.compat.ImmPortalsCompat;
import me.cortex.voxy.client.compat.PortalPassState;
import me.cortex.voxy.client.core.model.ColourTintSafety;
import me.cortex.voxy.common.config.VoxyConfig;
import me.cortex.voxy.client.core.gl.Capabilities;
import me.cortex.voxy.client.core.gl.GlBuffer;
import me.cortex.voxy.client.core.gl.GlTexture;
import me.cortex.voxy.client.core.model.ModelBakerySubsystem;
import me.cortex.voxy.client.core.model.ModelStore;
import me.cortex.voxy.client.core.rendering.ChunkBoundRenderer;
import me.cortex.voxy.client.core.rendering.RenderDistanceTracker;
import me.cortex.voxy.client.core.rendering.Viewport;
import me.cortex.voxy.client.core.rendering.ViewportSelector;
import me.cortex.voxy.client.core.rendering.building.RenderGenerationService;
import me.cortex.voxy.client.core.rendering.hierachical.AsyncNodeManager;
import me.cortex.voxy.client.core.rendering.hierachical.HierarchicalOcclusionTraverser;
import me.cortex.voxy.client.core.rendering.hierachical.NodeCleaner;
import me.cortex.voxy.client.core.rendering.section.IUsesMeshlets;
import me.cortex.voxy.client.core.rendering.section.backend.AbstractSectionRenderer;
import me.cortex.voxy.client.core.rendering.section.backend.mdic.MDICSectionRenderer;
import me.cortex.voxy.client.core.rendering.section.geometry.BasicSectionGeometryData;
import me.cortex.voxy.client.core.rendering.section.geometry.IGeometryData;
import me.cortex.voxy.client.core.rendering.util.DownloadStream;
import me.cortex.voxy.client.core.rendering.util.PrintfDebugUtil;
import me.cortex.voxy.client.core.rendering.util.UploadStream;
import me.cortex.voxy.client.core.util.GPUTiming;
import me.cortex.voxy.client.core.util.IrisUtil;
import me.cortex.voxy.common.Logger;
import me.cortex.voxy.common.thread.ServiceManager;
import me.cortex.voxy.common.world.WorldEngine;
import me.cortex.voxy.commonImpl.VoxyCommon;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.glUseProgram;
import static org.lwjgl.opengl.GL30.glGetIntegeri;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL32C.GL_DEPTH_CLAMP;
import static org.lwjgl.opengl.GL33.glBindSampler;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER_BINDING;

public class VoxyRenderSystem {
    private static int RENDER_ENTRY_LOGS;
    private static int RENDER_SKIP_LOGS;
    private static int PORTAL_PASS_LOGS;
    //Counts main (non-portal) Voxy passes; used to age portal viewports
    private long mainFrameCounter;
    private final boolean secondary;
    private long lastSecondaryTickMainFrame = -1L;
    private final WorldEngine worldIn;
    private boolean cacheRoleAttached;


    private final ModelBakerySubsystem modelService;
    private final RenderGenerationService renderGen;
    private final IGeometryData geometryData;
    private final AsyncNodeManager nodeManager;
    private final NodeCleaner nodeCleaner;
    private final HierarchicalOcclusionTraverser traversal;


    private final RenderDistanceTracker renderDistanceTracker;
    public final ChunkBoundRenderer chunkBoundRenderer;

    private final ViewportSelector<?> viewportSelector;

    private final AbstractRenderPipeline pipeline;

    private static AbstractSectionRenderer.Factory<?,? extends IGeometryData> getRenderBackendFactory() {
        //TODO: need todo a thing where selects optimal section render based on if supports the pipeline and geometry data type
        return MDICSectionRenderer.FACTORY;
    }

    public VoxyRenderSystem(WorldEngine world, ServiceManager sm) {
        this(world, sm, Minecraft.getInstance().level, false);
    }

    public VoxyRenderSystem(WorldEngine world, ServiceManager sm, ClientLevel level, boolean secondary) {
        this.secondary = secondary;
        //Keep the world loaded, NOTE: this is done FIRST, to keep and ensure that even if the rest of loading takes more
        // than timeout, we keep the world acquired
        world.acquireRef();
        Logger.info(secondary ? "Creating secondary cross-dimension Voxy render system" : "Creating Voxy render system");

        if (!secondary) {
            System.gc();
        }

        if (!secondary && Minecraft.getInstance().options.renderDistance().get()<3) {
            String msg = "Voxy: Having a vanilla render distance of 2 can cause rare culling near the edge of your screen issues, please use 3 or more";
            Logger.warn(msg);
            Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal(msg), false);
        }

        //Fking HATE EVERYTHING AAAAAAAAAAAAAAAA
        int[] oldBufferBindings = new int[10];
        for (int i = 0; i < oldBufferBindings.length; i++) {
            oldBufferBindings[i] = glGetIntegeri(GL_SHADER_STORAGE_BUFFER_BINDING, i);
        }

        try {
            //wait for opengl to be finished, this should hopefully ensure all memory allocations are free
            glFinish();
            if (!secondary) {
                glFinish();
            }

            this.worldIn = world;

            var backendFactory = getRenderBackendFactory();

            {
                this.modelService = new ModelBakerySubsystem(world.getMapper());
                this.renderGen = new RenderGenerationService(world, this.modelService, sm, IUsesMeshlets.class.isAssignableFrom(backendFactory.clz()));

                if (secondary) {
                    this.geometryData = new BasicSectionGeometryData(1 << 18, CrossDimPortalManager.SECONDARY_GEOMETRY_BUDGET_BYTES);
                    this.nodeManager = new AsyncNodeManager(1 << 19, this.geometryData, this.renderGen);
                } else {
                    this.geometryData = new BasicSectionGeometryData(1 << 20, RenderResourceReuse.getOrCreateGeometryBuffer());
                    this.nodeManager = new AsyncNodeManager(1 << 21, this.geometryData, this.renderGen);
                }
                this.nodeCleaner = new NodeCleaner(this.nodeManager);
                this.traversal = new HierarchicalOcclusionTraverser(this.nodeManager, this.nodeCleaner, this.renderGen);

                world.setDirtyCallback(this.nodeManager::worldEvent);

                Arrays.stream(world.getMapper().getBiomeEntries()).forEach(this.modelService::addBiome);
                world.getMapper().setBiomeCallback(this.modelService::addBiome);

                this.nodeManager.start();
            }

            this.pipeline = RenderPipelineFactory.createPipeline(this.nodeManager, this.nodeCleaner, this.traversal, this::frexStillHasWork);
            this.pipeline.setupExtraModelBakeryData(this.modelService);//Configure the model service

            //Late stage traversal compile for shaders with taa
            this.traversal.lateStageCompile(this.pipeline);


            var sectionRenderer = backendFactory.create(this.pipeline, this.modelService.getStore(), this.geometryData);
            this.pipeline.setSectionRenderer(sectionRenderer);
            this.viewportSelector = new ViewportSelector<>(sectionRenderer::createViewport);

            {
                ClientLevel heightLevel = level != null ? level : Minecraft.getInstance().level;
                int minSec = heightLevel.getMinSection() >> 5;
                int maxSec = (heightLevel.getMaxSection() - 1) >> 5;

                //Do some very cheeky stuff for MiB
                if (VoxyCommon.IS_MINE_IN_ABYSS) {//TODO: make this somehow configurable
                    minSec = -8;
                    maxSec = 7;
                }

                this.renderDistanceTracker = new RenderDistanceTracker(40,
                        minSec,
                        maxSec,
                        this.nodeManager::addTopLevel,
                        this.nodeManager::removeTopLevel);

                this.setRenderDistance(secondary
                        ? CrossDimPortalManager.effectiveSecondaryRenderDistance()
                        : VoxyConfig.CONFIG.sectionRenderDistance);
            }

            this.chunkBoundRenderer = new ChunkBoundRenderer(this.pipeline);

            this.cacheRoleAttached = true;
            world.attachRendererCache();
            Logger.info("Voxy render system created with " + this.geometryData.getMaxCapacity() + " geometry capacity, using pipeline '" + this.pipeline.getClass().getSimpleName() + "' with renderer '" + sectionRenderer.getClass().getSimpleName() + "'");
        } catch (RuntimeException e) {
            if (this.cacheRoleAttached) {
                this.cacheRoleAttached = false;
                world.detachRendererCache();
            }
            world.releaseRef();//If something goes wrong, we must release the world first
            throw e;
        }

        for (int i = 0; i < oldBufferBindings.length; i++) {
            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, i, oldBufferBindings[i]);
        }

        for (int i = 0; i < 12; i++) {
            GlStateManager._activeTexture(GlConst.GL_TEXTURE0+i);
            GlStateManager._bindTexture(0);
            glBindSampler(i, 0);
        }
    }


    public Viewport<?> setupViewport(ChunkRenderMatrices matrices, double cameraX, double cameraY, double cameraZ) {
        var viewport = this.getViewport();
        if (viewport == null) {
            return null;
        }
        return this.setupViewport0(viewport, matrices, cameraX, cameraY, cameraZ);
    }

    /**
     * Immersive Portals portal pass (portal-LOD prototype): selects the per-portal viewport and sets it up with the
     * portal camera's matrices. Returns null if no portal key is available.
     */
    public Viewport<?> setupPortalViewport(ChunkRenderMatrices matrices, double cameraX, double cameraY, double cameraZ) {
        if (IrisUtil.irisShadowActive()) {
            return null;
        }
        Object key = ImmPortalsCompat.currentPortalKey();
        if (key == null) {
            return null;
        }
        long frameCounter = this.secondary ? CrossDimPortalManager.currentMainFrame() : this.mainFrameCounter;
        var viewport = this.viewportSelector.getPortalViewport(key, frameCounter);
        return this.setupViewport0(viewport, matrices, cameraX, cameraY, cameraZ);
    }

    private Viewport<?> setupViewport0(Viewport<?> viewport, ChunkRenderMatrices matrices, double cameraX, double cameraY, double cameraZ) {

        //Do some very cheeky stuff for MiB
        if (VoxyCommon.IS_MINE_IN_ABYSS) {
            int sector = (((int)Math.floor(cameraX)>>4)+512)>>10;
            cameraX -= sector<<14;//10+4
            cameraY += (16+(256-32-sector*30))*16;
        }

        //cameraY += 100;
        var voxyProjection = computeProjectionMat(matrices.projection());

        int[] dims = new int[4];
        glGetIntegerv(GL_VIEWPORT, dims);

        int width = dims[2];
        int height = dims[3];

        {//Apply render scaling factor
            var factor = this.pipeline.getRenderScalingFactor();
            if (factor != null) {
                width = (int) (width*factor[0]);
                height = (int) (height*factor[1]);
            }
        }

        viewport
                .setVanillaProjection(matrices.projection())
                .setProjection(voxyProjection)
                .setModelView(new Matrix4f(matrices.modelView()))
                .setCamera(cameraX, cameraY, cameraZ)
                .setScreenSize(width, height)
                .update();

        if (VoxyClient.getOcclusionDebugState()==0) {
            viewport.frameId++;
        }

        return viewport;
    }

    public boolean isSecondary() {
        return this.secondary;
    }

    public void renderOpaque(Viewport<?> viewport) {
        this.renderOpaqueImpl(viewport, ImmPortalsCompat.isRenderingPortal());
    }

    /**
     * Immersive Portals portal pass entry point (portal-LOD prototype). Deliberately NOT named renderOpaque:
     * the "Immersive Portals: Aeronautics" fork (ip_aeronautics) ships its own Voxy mixins that inject at
     * renderOpaque HEAD and cancel any portal pass that did not go through its own setupViewport hook. This
     * Voxy build owns portal-LOD drawing itself (own per-portal viewport, stencil, clip plane), so the portal
     * pass bypasses that hook instead of letting both implementations fight over the same pass.
     */
    public void renderPortalOpaque(Viewport<?> viewport) {
        this.renderOpaqueImpl(viewport, true);
    }

    private void renderOpaqueImpl(Viewport<?> viewport, final boolean portalPass) {
        if (viewport == null) {
            if (RENDER_SKIP_LOGS < 8) {
                RENDER_SKIP_LOGS++;
                Logger.info("Skipping Voxy render because viewport is null; irisShadowActive=" + IrisUtil.irisShadowActive());
            }
            return;
        }
        if (viewport.width <= 0 || viewport.height <= 0) {
            if (RENDER_SKIP_LOGS < 8) {
                RENDER_SKIP_LOGS++;
                Logger.info("Skipping Voxy render because viewport has invalid size " + viewport.width + "x" + viewport.height);
            }
            return;//Only render on valid viewport
        }
        if (RENDER_ENTRY_LOGS < 8) {
            RENDER_ENTRY_LOGS++;
            Logger.info("Entering Voxy render with viewport " + viewport.width + "x" + viewport.height
                    + " camera=(" + viewport.cameraX + ", " + viewport.cameraY + ", " + viewport.cameraZ + ")"
                    + ", geometrySections=" + this.geometryData.getSectionCount()
                    + ", meshTasks=" + this.renderGen.getTaskCount()
                    + ", modelQueue=" + this.modelService.getProcessingCount()
                    + ", nodeWork=" + this.nodeManager.hasWork());
        }

        //Immersive Portals nested pass. For same-dimension portals, per-frame work runs only in the main pass.
        // For cross-dimension secondary renderers (which have no main pass), per-frame work runs once per main frame.
        final boolean tickSecondary = portalPass && this.secondary
                && (this.lastSecondaryTickMainFrame != CrossDimPortalManager.currentMainFrame());
        if (tickSecondary) {
            this.lastSecondaryTickMainFrame = CrossDimPortalManager.currentMainFrame();
            this.viewportSelector.tickPortalViewports(this.lastSecondaryTickMainFrame);
        }
        if (portalPass && PORTAL_PASS_LOGS < 4) {
            PORTAL_PASS_LOGS++;
            Logger.info("Voxy portal pass (secondary=" + this.secondary + "): viewport " + viewport.width + "x" + viewport.height
                    + " camera=(" + viewport.cameraX + ", " + viewport.cameraY + ", " + viewport.cameraZ + "), portal viewports="
                    + this.viewportSelector.getPortalViewportCount());
        }
        if (!portalPass) {
            this.mainFrameCounter++;
            this.viewportSelector.tickPortalViewports(this.mainFrameCounter);
        }

        if (!portalPass) TimingStatistics.resetSamplers();

        TimingStatistics.all.start();
        GPUTiming.INSTANCE.marker();//Start marker
        TimingStatistics.main.start();

        //TODO: optimize
        int[] oldBufferBindings = new int[10];
        for (int i = 0; i < oldBufferBindings.length; i++) {
            oldBufferBindings[i] = glGetIntegeri(GL_SHADER_STORAGE_BUFFER_BINDING, i);
        }


        int oldFB = GL11.glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);
        int boundFB = oldFB;

        //Immersive Portals/Aeronautics (stencil renderer) enables GL_STENCIL_TEST once per frame before the world
        // render and relies on it still being enabled when it draws portal view areas before translucent terrain.
        // Voxy's pipeline disables it with raw GL, which made portals invisible and drew the portal contents
        // (clipped at the destination portal plane) over the whole screen. Save and restore the stencil state.
        final boolean voxy$stencilWasEnabled = glIsEnabled(GL_STENCIL_TEST);
        final int voxy$stencilFunc = glGetInteger(GL_STENCIL_FUNC);
        final int voxy$stencilRef = glGetInteger(GL_STENCIL_REF);
        final int voxy$stencilValueMask = glGetInteger(GL_STENCIL_VALUE_MASK);
        final int voxy$stencilFail = glGetInteger(GL_STENCIL_FAIL);
        final int voxy$stencilZFail = glGetInteger(GL_STENCIL_PASS_DEPTH_FAIL);
        final int voxy$stencilZPass = glGetInteger(GL_STENCIL_PASS_DEPTH_PASS);
        final int voxy$stencilWriteMask = glGetInteger(GL_STENCIL_WRITEMASK);
        //Inside portal passes IP enables GL_CLIP_DISTANCE0 (its Sodium shaders write gl_ClipDistance[0], Voxy's do
        // not, which is undefined behaviour) and possibly depth clamp. Turn both off for Voxy and restore after.
        final boolean voxy$clip0WasEnabled = glIsEnabled(GL_CLIP_DISTANCE0);
        final boolean voxy$depthClampWasEnabled = glIsEnabled(GL_DEPTH_CLAMP);
        if (voxy$clip0WasEnabled) glDisable(GL_CLIP_DISTANCE0);
        if (voxy$depthClampWasEnabled) glDisable(GL_DEPTH_CLAMP);
        if (portalPass) {
            PortalPassState.active = true;
            PortalPassState.tickSecondaryWorld = tickSecondary;
            PortalPassState.stencilEnabled = voxy$stencilWasEnabled;
            PortalPassState.stencilFunc = voxy$stencilFunc;
            PortalPassState.stencilRef = voxy$stencilRef;
            PortalPassState.stencilValueMask = voxy$stencilValueMask;
            PortalPassState.clipEquation = ImmPortalsCompat.activeClipEquation();
        } else {
            PortalPassState.reset();
        }

        int[] dims = new int[4];
        glGetIntegerv(GL_VIEWPORT, dims);

        glViewport(0,0, viewport.width, viewport.height);

        //var target = DefaultTerrainRenderPasses.CUTOUT.getTarget();
        //boundFB = ((net.minecraft.client.texture.GlTexture) target.getColorAttachment()).getOrCreateFramebuffer(((GlBackend) RenderSystem.getDevice()).getFramebufferManager(), target.getDepthAttachment());
        if (boundFB == 0) {
            throw new IllegalStateException("Cannot use the default framebuffer as cannot source from it");
        }

        //this.autoBalanceSubDivSize();

        this.pipeline.preSetup(viewport);

        TimingStatistics.E.start();
        if ((!VoxyClient.disableSodiumChunkRender())&&!IrisUtil.irisShadowActive()) {
            this.chunkBoundRenderer.render(viewport);
        } else {
            viewport.depthBoundingBuffer.clear(0);
        }
        TimingStatistics.E.stop();


        GPUTiming.INSTANCE.marker();
        //The entire rendering pipeline (excluding the chunkbound thing)
        try {
            this.pipeline.runPipeline(viewport, boundFB, dims[2], dims[3]);
        } finally {
            PortalPassState.reset();
        }
        GPUTiming.INSTANCE.marker();


        TimingStatistics.main.stop();
        TimingStatistics.postDynamic.start();

        if (!portalPass) PrintfDebugUtil.tick();

        //As much dynamic runtime stuff here (main pass, or once per main frame for cross-dimension secondary renderers)
        if (!portalPass) {
            //Tick upload stream (this is ok to do here as upload ticking is just memory management)
            UploadStream.INSTANCE.tick();

            // Expand LOD ring: with Frex, drain in one frame; otherwise run a bounded number of steps per frame so
            // large render distances actually fill in without waiting minutes (the old `&& isFrexActive()` short-circuit
            // limited this to a single ~40-node batch per frame when Frex was off).
            int ringSteps = VoxyClient.isFrexActive() ? 100_000 : 2048;
            while (ringSteps-- > 0 && this.renderDistanceTracker.setCenterAndProcess(viewport.cameraX, viewport.cameraZ)) {
            }
            TimingStatistics.H.start();
            //Done here as is allows less gl state resetup
            do { ColourTintSafety.guardedModelTick(this.modelService, 900_000L); } while (VoxyClient.isFrexActive() && !this.modelService.areQueuesEmpty());
            TimingStatistics.H.stop();
        } else if (tickSecondary) {
            UploadStream.INSTANCE.tick();
            int ringSteps = 512;
            while (ringSteps-- > 0 && this.renderDistanceTracker.setCenterAndProcess(viewport.cameraX, viewport.cameraZ)) {
            }
            ColourTintSafety.guardedModelTick(this.modelService, 400_000L);
        }
        GPUTiming.INSTANCE.marker();
        TimingStatistics.postDynamic.stop();

        if (!portalPass) GPUTiming.INSTANCE.tick();

        glBindFramebuffer(GlConst.GL_FRAMEBUFFER, oldFB);
        glViewport(dims[0], dims[1], dims[2], dims[3]);

        {//Reset state manager stuffs
            glUseProgram(0);
            glEnable(GL_DEPTH_TEST);
            glDisable(GL_STENCIL_TEST);

            GlStateManager._glBindVertexArray(0);//Clear binding

            GlStateManager._activeTexture(GlConst.GL_TEXTURE1);
            for (int i = 0; i < 12; i++) {
                GlStateManager._activeTexture(GlConst.GL_TEXTURE0+i);
                GlStateManager._bindTexture(0);
                glBindSampler(i, 0);
            }

            IrisUtil.clearIrisSamplers();//Thanks iris (sigh)

            //TODO: should/needto actually restore all of these, not just clear them
            //Clear all the bindings
            for (int i = 0; i < oldBufferBindings.length; i++) {
                glBindBufferBase(GL_SHADER_STORAGE_BUFFER, i, oldBufferBindings[i]);
            }

            //((SodiumShader) Iris.getPipelineManager().getPipelineNullable().getSodiumPrograms().getProgram(DefaultTerrainRenderPasses.CUTOUT).getInterface()).setupState(DefaultTerrainRenderPasses.CUTOUT, fogParameters);
        }

        //Restore the caller's stencil state (see Immersive Portals note above)
        glStencilFunc(voxy$stencilFunc, voxy$stencilRef, voxy$stencilValueMask);
        glStencilOp(voxy$stencilFail, voxy$stencilZFail, voxy$stencilZPass);
        glStencilMask(voxy$stencilWriteMask);
        if (voxy$stencilWasEnabled) {
            glEnable(GL_STENCIL_TEST);
        } else {
            glDisable(GL_STENCIL_TEST);
        }
        if (voxy$clip0WasEnabled) glEnable(GL_CLIP_DISTANCE0);
        if (voxy$depthClampWasEnabled) glEnable(GL_DEPTH_CLAMP);

        TimingStatistics.all.stop();

        //TimingStatistics.I.start();
        //glFlush();
        //TimingStatistics.I.stop();

        /*
        TimingStatistics.F.start();
        this.postProcessing.setup(viewport.width, viewport.height, boundFB);
        TimingStatistics.F.stop();

        this.renderer.renderFarAwayOpaque(viewport, this.chunkBoundRenderer.getDepthBoundTexture());


        TimingStatistics.F.start();
        //Compute the SSAO of the rendered terrain, TODO: fix it breaking depth or breaking _something_ am not sure what
        this.postProcessing.computeSSAO(viewport.MVP);
        TimingStatistics.F.stop();

        TimingStatistics.G.start();
        //We can render the translucent directly after as it is the furthest translucent objects
        this.renderer.renderFarAwayTranslucent(viewport, this.chunkBoundRenderer.getDepthBoundTexture());
        TimingStatistics.G.stop();


        TimingStatistics.F.start();
        this.postProcessing.renderPost(viewport, matrices.projection(), boundFB);
        TimingStatistics.F.stop();
         */
    }



    private void autoBalanceSubDivSize() {
        //only increase quality while there are very few mesh queues, this stops,
        // e.g. while flying and is rendering alot of low quality chunks
        boolean canDecreaseSize = this.renderGen.getTaskCount() < 300;
        int MIN_FPS = 55;
        int MAX_FPS = 65;
        float INCREASE_PER_SECOND = 60;
        float DECREASE_PER_SECOND = 30;
        //Auto fps targeting
        if (Minecraft.getInstance().getFps() < MIN_FPS) {
            VoxyConfig.CONFIG.subDivisionSize = Math.min(VoxyConfig.CONFIG.subDivisionSize + INCREASE_PER_SECOND / Math.max(1f, Minecraft.getInstance().getFps()), 256);
        }

        if (MAX_FPS < Minecraft.getInstance().getFps() && canDecreaseSize) {
            VoxyConfig.CONFIG.subDivisionSize = Math.max(VoxyConfig.CONFIG.subDivisionSize - DECREASE_PER_SECOND / Math.max(1f, Minecraft.getInstance().getFps()), 28);
        }
    }

    public static float getRenderDistance() {
        return Minecraft.getInstance().options.getEffectiveRenderDistance()*16;
    }

    /*
    private static float getGameFoV() {
        var client = Minecraft.getInstance();
        var gameRenderer = client.gameRenderer;
        return gameRenderer.getMainCamera().getFov();
    }

    private static Matrix4f makeProjectionMatrix(float near, float far) {
        //TODO: use the existing projection matrix use mulLocal by the inverse of the projection and then mulLocal our projection

        var projection = new Matrix4f();
        var client = Minecraft.getInstance();
        projection.setPerspective(getGameFoV() * 0.01745329238474369f,
                (float) client.getWindow().getWidth() / (float)client.getWindow().getHeight(),
                near, far);
        return projection;
    }

    //TODO: Make a reverse z buffer
    private static Matrix4f computeProjectionMat(Matrix4fc base) {
        //THis is a wild and insane problem to have
        // at short render distances the vanilla terrain doesnt end up covering the 16f near plane voxy uses
        // meaning that it explodes (due to near plane clipping).. _badly_ with the rastered culling being wrong in rare cases for the immediate
        // sections rendered after the vanilla render distance
        float nearVoxy = getRenderDistance()<=32.0f?8f:16f;
        nearVoxy = VoxyClient.disableSodiumChunkRender()?0.1f:nearVoxy;

        return base.mulLocal(
                Minecraft.getInstance().gameRenderer.getGameRenderState().levelRenderState.cameraRenderState.projectionMatrix.invert(new Matrix4f()),
                new Matrix4f()
        ).mulLocal(makeProjectionMatrix(nearVoxy, 16*3000));
    }*/

    private static Matrix4f computeProjectionMat(Matrix4fc base) {

        var proj = new Matrix4f(base);

        float near = getRenderDistance()<=32.0f?8f:16f;
        near = VoxyClient.disableSodiumChunkRender()?0.1f:near;

        float far = 16*3000;

        return proj
                .m22((far + near) / (near - far))
                .m32((far+far) * near / (near - far));
    }

    private boolean frexStillHasWork() {
        if (!VoxyClient.isFrexActive()) {
            return false;
        }
        //If frex is running we must tick everything to ensure correctness
        UploadStream.INSTANCE.tick();
        //Done here as is allows less gl state resetup
        ColourTintSafety.guardedModelTick(this.modelService, 100_000_000L);
        GL11.glFinish();
        return this.nodeManager.hasWork() || this.renderGen.getTaskCount()!=0 || !this.modelService.areQueuesEmpty();
    }

    public void setRenderDistance(float renderDistance) {
        if (this.secondary) {
            renderDistance = Math.min(renderDistance, CrossDimPortalManager.SECONDARY_MAX_RENDER_DISTANCE);
        }
        this.renderDistanceTracker.setRenderDistance((int) Math.ceil(renderDistance+1));//the +1 is to cover the outer ring of chunks when rendering a circle
    }

    public Viewport<?> getViewport() {
        if (IrisUtil.irisShadowActive()) {
            return null;
        }
        return this.viewportSelector.getViewport();
    }

    public void addDebugInfo(List<String> debug) {
        debug.add("Buf/Tex [#/Mb]: [" + GlBuffer.getCount() + "/" + (GlBuffer.getTotalSize()/1_000_000) + "],[" + GlTexture.getCount() + "/" + (GlTexture.getEstimatedTotalSize()/1_000_000)+"]");
        {
            this.modelService.addDebugData(debug);
            this.renderGen.addDebugData(debug);
            this.nodeManager.addDebug(debug);
            this.pipeline.addDebug(debug);
        }
        {
            TimingStatistics.update();
            debug.add("Voxy frame runtime (millis): " + TimingStatistics.dynamic.pVal() + ", " + TimingStatistics.main.pVal()+ ", " + TimingStatistics.postDynamic.pVal()+ ", " + TimingStatistics.all.pVal());
            debug.add("Extra time: " + TimingStatistics.A.pVal() + ", " + TimingStatistics.B.pVal() + ", " + TimingStatistics.C.pVal() + ", " + TimingStatistics.D.pVal());
            debug.add("Extra 2 time: " + TimingStatistics.E.pVal() + ", " + TimingStatistics.F.pVal() + ", " + TimingStatistics.G.pVal() + ", " + TimingStatistics.H.pVal() + ", " + TimingStatistics.I.pVal());
        }
        debug.add(GPUTiming.INSTANCE.getDebug());
        PrintfDebugUtil.addToOut(debug);
    }

    public void shutdown() {
        long stageStart = this.logShutdownStageStart("downloadStream.flushWaitClear (pre)");
        DownloadStream.INSTANCE.flushWaitClear();
        this.logShutdownStageDone("downloadStream.flushWaitClear (pre)", stageStart);
        Logger.info("Shutting down rendering");
        try {
            //Cleanup callbacks
            this.worldIn.setDirtyCallback(null);
            this.worldIn.getMapper().setBiomeCallback(null);
            this.worldIn.getMapper().setStateCallback(null);

            stageStart = this.logShutdownStageStart("nodeManager.stop");
            this.nodeManager.stop();
            this.logShutdownStageDone("nodeManager.stop", stageStart);

            stageStart = this.logShutdownStageStart("modelService.shutdown");
            this.modelService.shutdown();
            this.logShutdownStageDone("modelService.shutdown", stageStart);
            stageStart = this.logShutdownStageStart("renderGen.shutdown");
            this.renderGen.shutdown();
            this.logShutdownStageDone("renderGen.shutdown", stageStart);
            stageStart = this.logShutdownStageStart("traversal.free");
            this.traversal.free();
            this.logShutdownStageDone("traversal.free", stageStart);
            stageStart = this.logShutdownStageStart("nodeCleaner.free");
            this.nodeCleaner.free();
            this.logShutdownStageDone("nodeCleaner.free", stageStart);
            stageStart = this.logShutdownStageStart("geometryData.free");
            this.geometryData.free();
            this.logShutdownStageDone("geometryData.free", stageStart);
            if (((BasicSectionGeometryData)this.geometryData).isExternalGeometryBuffer) {
                RenderResourceReuse.giveBackGeometryBuffer(((BasicSectionGeometryData)this.geometryData).getGeometryBuffer());
            }

            stageStart = this.logShutdownStageStart("chunkBoundRenderer.free");
            this.chunkBoundRenderer.free();
            this.logShutdownStageDone("chunkBoundRenderer.free", stageStart);

            stageStart = this.logShutdownStageStart("viewportSelector.free");
            this.viewportSelector.free();
            this.logShutdownStageDone("viewportSelector.free", stageStart);
        } catch (Exception e) {Logger.error("Error shutting down renderer components", e);}
        Logger.info("Shutting down render pipeline");
        try {
            stageStart = this.logShutdownStageStart("pipeline.free");
            this.pipeline.free();
            this.logShutdownStageDone("pipeline.free", stageStart);
        } catch (Exception e){Logger.error("Error releasing render pipeline", e);}



        stageStart = this.logShutdownStageStart("downloadStream.flushWaitClear (post)");
        DownloadStream.INSTANCE.flushWaitClear();
        this.logShutdownStageDone("downloadStream.flushWaitClear (post)", stageStart);

        //Release hold on the world
        stageStart = this.logShutdownStageStart("world.releaseRef");
        if (this.cacheRoleAttached) {
            this.cacheRoleAttached = false;
            this.worldIn.detachRendererCache();
        }
        this.worldIn.releaseRef();
        this.logShutdownStageDone("world.releaseRef", stageStart);
        Logger.info("Render shutdown completed");
    }

    public WorldEngine getEngine() {
        return this.worldIn;
    }

    private long logShutdownStageStart(String stage) {
        Logger.info("Voxy renderer shutdown stage start: " + stage);
        return System.nanoTime();
    }

    private void logShutdownStageDone(String stage, long startedAt) {
        Logger.info("Voxy renderer shutdown stage done: " + stage + " (" + ((System.nanoTime() - startedAt) / 1_000_000L) + " ms)");
    }
}
