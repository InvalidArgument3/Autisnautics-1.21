package dev.wyseman.portalrenderguard;

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.ArrayDeque;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;

/**
 * Saves the fixed-function GL state Immersive Portals' portal pass still
 * needs after Voxy's LOD draw, and puts it back through GlStateManager.
 *
 * <p>Voxy's {@code renderOpaqueImpl} turns the stencil test off inside the
 * pipeline and only turns it back on if it was on at entry. It does not put
 * back blend, the depth function, depth mask, colour mask, cull winding,
 * scissor, polygon offset, polygon mode, provoking vertex, the read
 * framebuffer, or the active texture unit (the cleanup loop leaves
 * GlStateManager on unit 11). The portal framebuffer copy then calls
 * {@code GlStateManager._viewport}, {@code _depthMask}, {@code _colorMask},
 * and {@code _enableDepthTest}. Those setters skip the GL call when their
 * cache already matches, so a raw {@code glViewport} / {@code glDepthMask}
 * restore leaves the cache pointing at the framebuffer size and a depth mask
 * of true. The next solid pass asks for a true depth mask, the cache says it
 * is already true, and depth writes stay off.
 */
public final class GlStateGuard {
    private static final ArrayDeque<Snap> STACK = new ArrayDeque<>();
    private static final int GL_CLIP_DISTANCE0 = 0x3000;
    private static final int GL_DEPTH_CLAMP = 0x864F;
    private static final int GL_NEVER = 0x0200;
    private static final int GL_ALWAYS = 0x0207;

    private GlStateGuard() {}

    public static void push() {
        STACK.push(Snap.capture());
    }

    public static void pop() {
        Snap snap = STACK.poll();
        if (snap != null) {
            snap.restore();
        }
    }

    /** Disable blending for one draw, then the caller's pop puts the old blend back. */
    public static void disableBlend() {
        forceBlend(false);
    }

    private static final class Snap {
        private boolean depthTest;
        private boolean blend;
        private boolean cull;
        private boolean scissor;
        private boolean stencil;
        private boolean clip0;
        private boolean depthClamp;
        private boolean polygonOffset;
        private int depthFunc;
        private boolean depthMask;
        private final double[] depthRange = new double[2];
        private int blendSrcRgb;
        private int blendDstRgb;
        private int blendSrcA;
        private int blendDstA;
        private int blendEqRgb;
        private int blendEqA;
        private final boolean[] colorMask = new boolean[4];
        private int cullFace;
        private int frontFace;
        private final int[] scissorBox = new int[4];
        private float polygonFactor;
        private float polygonUnits;
        private int provoking;
        private int stencilFunc;
        private int stencilRef;
        private int stencilValueMask;
        private int stencilWriteMask;
        private int stencilFail;
        private int stencilZFail;
        private int stencilZPass;
        private int drawFbo;
        private int readFbo;
        private final int[] viewport = new int[4];
        private int activeTexture;
        private int program;

        private static Snap capture() {
            Snap snap = new Snap();
            snap.depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
            snap.blend = GL11.glIsEnabled(GL11.GL_BLEND);
            snap.cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
            snap.scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
            snap.stencil = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
            snap.clip0 = GL11.glIsEnabled(GL_CLIP_DISTANCE0);
            snap.depthClamp = GL11.glIsEnabled(GL_DEPTH_CLAMP);
            snap.polygonOffset = GL11.glIsEnabled(GL11.GL_POLYGON_OFFSET_FILL);
            snap.depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
            snap.depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
            snap.blendSrcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
            snap.blendDstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
            snap.blendSrcA = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
            snap.blendDstA = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
            snap.blendEqRgb = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB);
            snap.blendEqA = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_ALPHA);
            snap.cullFace = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);
            snap.frontFace = GL11.glGetInteger(GL11.GL_FRONT_FACE);
            snap.polygonFactor = GL11.glGetFloat(GL11.GL_POLYGON_OFFSET_FACTOR);
            snap.polygonUnits = GL11.glGetFloat(GL11.GL_POLYGON_OFFSET_UNITS);
            snap.provoking = GL11.glGetInteger(GL32.GL_PROVOKING_VERTEX);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                DoubleBuffer depthRange = stack.mallocDouble(2);
                GL11.glGetDoublev(GL11.GL_DEPTH_RANGE, depthRange);
                snap.depthRange[0] = depthRange.get(0);
                snap.depthRange[1] = depthRange.get(1);
                ByteBuffer colorMask = stack.malloc(4);
                GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK, colorMask);
                for (int i = 0; i < 4; i++) {
                    snap.colorMask[i] = colorMask.get(i) != 0;
                }
                IntBuffer scissor = stack.mallocInt(4);
                GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, scissor);
                for (int i = 0; i < 4; i++) {
                    snap.scissorBox[i] = scissor.get(i);
                }
                IntBuffer viewport = stack.mallocInt(4);
                GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
                for (int i = 0; i < 4; i++) {
                    snap.viewport[i] = viewport.get(i);
                }
            }
            snap.stencilFunc = GL11.glGetInteger(GL11.GL_STENCIL_FUNC);
            snap.stencilRef = GL11.glGetInteger(GL11.GL_STENCIL_REF);
            snap.stencilValueMask = GL11.glGetInteger(GL11.GL_STENCIL_VALUE_MASK);
            snap.stencilWriteMask = GL11.glGetInteger(GL11.GL_STENCIL_WRITEMASK);
            snap.stencilFail = GL11.glGetInteger(GL11.GL_STENCIL_FAIL);
            snap.stencilZFail = GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_FAIL);
            snap.stencilZPass = GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_PASS);
            snap.drawFbo = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
            snap.readFbo = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
            snap.activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
            snap.program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
            return snap;
        }

        private void restore() {
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.drawFbo);
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, this.readFbo);
            GlStateManager._viewport(this.viewport[0], this.viewport[1], this.viewport[2], this.viewport[3]);
            GlStateManager._glUseProgram(this.program);
            forceDepthTest(this.depthTest);
            forceDepthFunc(this.depthFunc);
            forceDepthMask(this.depthMask);
            GL11.glDepthRange(this.depthRange[0], this.depthRange[1]);
            forceBlend(this.blend);
            // GlStateManager has no separate-equation cache. _blendEquation always calls GL.
            GL20.glBlendEquationSeparate(this.blendEqRgb, this.blendEqA);
            forceBlendFunc(this.blendSrcRgb, this.blendDstRgb, this.blendSrcA, this.blendDstA);
            forceColorMask(this.colorMask[0], this.colorMask[1], this.colorMask[2], this.colorMask[3]);
            forceCull(this.cull);
            GL11.glCullFace(this.cullFace);
            GL11.glFrontFace(this.frontFace);
            forceScissor(this.scissor);
            GlStateManager._scissorBox(this.scissorBox[0], this.scissorBox[1], this.scissorBox[2], this.scissorBox[3]);
            forcePolygonOffset(this.polygonOffset);
            forcePolygonFactor(this.polygonFactor, this.polygonUnits);
            GL32.glProvokingVertex(this.provoking);
            setEnabled(GL11.GL_STENCIL_TEST, this.stencil);
            forceStencilFunc(this.stencilFunc, this.stencilRef, this.stencilValueMask);
            forceStencilOp(this.stencilFail, this.stencilZFail, this.stencilZPass);
            forceStencilMask(this.stencilWriteMask);
            setEnabled(GL_CLIP_DISTANCE0, this.clip0);
            setEnabled(GL_DEPTH_CLAMP, this.depthClamp);
            forceActiveTexture(this.activeTexture);
        }
    }

    /**
     * GlStateManager setters no-op when the cache already equals the request.
     * Voxy writes many of these with raw GL, and the portal copy writes the
     * cache, so the cache and the GL value disagree. Flip to the other value
     * first. That misses the cache and issues the GL call, then the real value
     * does too. The cache and the driver end on the saved value.
     */
    private static void forceDepthTest(boolean enabled) {
        if (enabled) {
            GlStateManager._disableDepthTest();
            GlStateManager._enableDepthTest();
        } else {
            GlStateManager._enableDepthTest();
            GlStateManager._disableDepthTest();
        }
    }

    private static void forceDepthFunc(int func) {
        GlStateManager._depthFunc(func == GL_NEVER ? GL_ALWAYS : GL_NEVER);
        GlStateManager._depthFunc(func);
    }

    private static void forceDepthMask(boolean mask) {
        GlStateManager._depthMask(!mask);
        GlStateManager._depthMask(mask);
    }

    private static void forceBlend(boolean enabled) {
        if (enabled) {
            GlStateManager._disableBlend();
            GlStateManager._enableBlend();
        } else {
            GlStateManager._enableBlend();
            GlStateManager._disableBlend();
        }
    }

    private static void forceBlendFunc(int srcRgb, int dstRgb, int srcA, int dstA) {
        if (srcRgb == 0 && dstRgb == 0 && srcA == 0 && dstA == 0) {
            GlStateManager._blendFuncSeparate(1, 0, 1, 0);
        } else {
            GlStateManager._blendFuncSeparate(0, 0, 0, 0);
        }
        GlStateManager._blendFuncSeparate(srcRgb, dstRgb, srcA, dstA);
    }

    private static void forceColorMask(boolean r, boolean g, boolean b, boolean a) {
        GlStateManager._colorMask(!r, !g, !b, !a);
        GlStateManager._colorMask(r, g, b, a);
    }

    private static void forceCull(boolean enabled) {
        if (enabled) {
            GlStateManager._disableCull();
            GlStateManager._enableCull();
        } else {
            GlStateManager._enableCull();
            GlStateManager._disableCull();
        }
    }

    private static void forceScissor(boolean enabled) {
        if (enabled) {
            GlStateManager._disableScissorTest();
            GlStateManager._enableScissorTest();
        } else {
            GlStateManager._enableScissorTest();
            GlStateManager._disableScissorTest();
        }
    }

    private static void forcePolygonOffset(boolean enabled) {
        if (enabled) {
            GlStateManager._disablePolygonOffset();
            GlStateManager._enablePolygonOffset();
        } else {
            GlStateManager._enablePolygonOffset();
            GlStateManager._disablePolygonOffset();
        }
    }

    private static void forcePolygonFactor(float factor, float units) {
        GlStateManager._polygonOffset(factor + 1f, units + 1f);
        GlStateManager._polygonOffset(factor, units);
    }

    private static void forceStencilFunc(int func, int ref, int mask) {
        GlStateManager._stencilFunc(func == GL_NEVER ? GL_ALWAYS : GL_NEVER, ref + 1, mask);
        GlStateManager._stencilFunc(func, ref, mask);
    }

    private static void forceStencilOp(int fail, int zFail, int zPass) {
        GlStateManager._stencilOp(fail == GL11.GL_KEEP ? GL11.GL_ZERO : GL11.GL_KEEP, zFail, zPass);
        GlStateManager._stencilOp(fail, zFail, zPass);
    }

    private static void forceStencilMask(int mask) {
        GlStateManager._stencilMask(mask == 0 ? 0xFF : 0);
        GlStateManager._stencilMask(mask);
    }

    private static void forceActiveTexture(int unitEnum) {
        int other = unitEnum == GL13.GL_TEXTURE0 ? GL13.GL_TEXTURE1 : GL13.GL_TEXTURE0;
        GlStateManager._activeTexture(other);
        GlStateManager._activeTexture(unitEnum);
    }

    private static void setEnabled(int cap, boolean enabled) {
        if (enabled) {
            GL11.glEnable(cap);
        } else {
            GL11.glDisable(cap);
        }
    }
}
