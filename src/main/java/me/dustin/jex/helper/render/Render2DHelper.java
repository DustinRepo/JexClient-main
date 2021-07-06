package me.dustin.jex.helper.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.shader.ShaderProgram;
import me.dustin.jex.helper.render.shader.ShaderUniform;
import me.dustin.jex.load.impl.IItemRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.nio.FloatBuffer;

public enum Render2DHelper {
    INSTANCE;
    protected Identifier cog = new Identifier("jex", "gui/click/cog.png");
    //private BlurShader blurShader = new BlurShader();

    public void setup2DRender(boolean disableDepth) {
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        if (disableDepth)
            RenderSystem.disableDepthTest();
    }

    public void end2DRender() {
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }

    public double getScaleFactor() {
        return Wrapper.INSTANCE.getWindow().getScaleFactor();
    }

    public int getScaledWidth() {
        return Wrapper.INSTANCE.getWindow().getScaledWidth();
    }

    public int getScaledHeight() {
        return Wrapper.INSTANCE.getWindow().getScaledHeight();
    }

    public void drawTexture(MatrixStack matrices, float x, float y, float u, float v, float width, float height, int textureWidth, int textureHeight) {
        drawTexture(matrices, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
    }

    private void drawTexture(MatrixStack matrices, float x, float y, float width, float height, float u, float v, float regionWidth, float regionHeight, int textureWidth, int textureHeight) {
        drawTexture(matrices, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
    }

    private void drawTexture(MatrixStack matrices, float x0, float y0, float x1, float y1, int z, float regionWidth, float regionHeight, float u, float v, int textureWidth, int textureHeight) {
        drawTexturedQuad(matrices.peek().getModel(), x0, y0, x1, y1, z, (u + 0.0F) / (float)textureWidth, (u + (float)regionWidth) / (float)textureWidth, (v + 0.0F) / (float)textureHeight, (v + (float)regionHeight) / (float)textureHeight);
    }

    public void drawTexturedQuad(Matrix4f matrices, float x0, float x1, float y0, float y1, float z, float u0, float u1, float v0, float v1) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrices, (float)x0, (float)y1, (float)z).texture(u0, v1).next();
        bufferBuilder.vertex(matrices, (float)x1, (float)y1, (float)z).texture(u1, v1).next();
        bufferBuilder.vertex(matrices, (float)x1, (float)y0, (float)z).texture(u1, v0).next();
        bufferBuilder.vertex(matrices, (float)x0, (float)y0, (float)z).texture(u0, v0).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
    }

    public void fill(MatrixStack matrixStack, float x1, float y1, float x2, float y2, int color) {
        Matrix4f matrix = matrixStack.peek().getModel();
        float j;
        if (x1 < x2) {
            j = x1;
            x1 = x2;
            x2 = j;
        }

        if (y1 < y2) {
            j = y1;
            y1 = y2;
            y2 = j;
        }

        float f = (float)(color >> 24 & 255) / 255.0F;
        float g = (float)(color >> 16 & 255) / 255.0F;
        float h = (float)(color >> 8 & 255) / 255.0F;
        float k = (float)(color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float)x1, (float)y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float)x2, (float)y1, 0.0F).color(g, h, k, f).next();
        bufferBuilder.vertex(matrix, (float)x1, (float)y1, 0.0F).color(g, h, k, f).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public Color hexToColor(String value) {
        String digits;
        if (value.startsWith("#")) {
            digits = value.substring(1, Math.min(value.length(), 7));
        } else {
            digits = value;
        }
        String hstr = "0x" + digits;
        Color c;
        try {
            c = Color.decode(hstr);
        } catch (NumberFormatException nfe) {
            c = null;
        }
        return c;
    }

    public void drawFace(MatrixStack matrixStack, float x, float y, int renderScale, Identifier id) {
        try {
            bindTexture(id);
            drawTexture(matrixStack, x, y, 8 * renderScale, 8 * renderScale, 8 * renderScale, 8 * renderScale, 8 * renderScale, 8 * renderScale, 64 * renderScale, 64 * renderScale);
            drawTexture(matrixStack, x, y, 8 * renderScale, 8 * renderScale, 40 * renderScale, 8 * renderScale, 8 * renderScale, 8 * renderScale, 64 * renderScale, 64 * renderScale);
        }catch (Exception e){}
    }

    public void fillAndBorder(MatrixStack matrixStack, float left, float top, float right, float bottom, int bcolor, int icolor, float f) {
        fill(matrixStack, left + f, top + f, right - f, bottom - f, icolor);
        fill(matrixStack, left, top, left + f, bottom, bcolor);
        fill(matrixStack, left + f, top, right, top + f, bcolor);
        fill(matrixStack, left + f, bottom - f, right, bottom, bcolor);
        fill(matrixStack, right - f, top + f, right, bottom - f, bcolor);
    }

    public void drawGradientRect(double x, double y, double x2, double y2, int col1, int col2) {
        float f = (float) (col1 >> 24 & 0xFF) / 255F;
        float f1 = (float) (col1 >> 16 & 0xFF) / 255F;
        float f2 = (float) (col1 >> 8 & 0xFF) / 255F;
        float f3 = (float) (col1 & 0xFF) / 255F;

        float f4 = (float) (col2 >> 24 & 0xFF) / 255F;
        float f5 = (float) (col2 >> 16 & 0xFF) / 255F;
        float f6 = (float) (col2 >> 8 & 0xFF) / 255F;
        float f7 = (float) (col2 & 0xFF) / 255F;

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(x2, y, 0).color(f1, f2, f3, f).next();
        bufferBuilder.vertex(x, y, 0).color(f1, f2, f3, f).next();

        bufferBuilder.vertex(x, y2, 0).color(f5, f6, f7, f4).next();
        bufferBuilder.vertex(x2, y2, 0).color(f5, f6, f7, f4).next();

        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public void drawFullCircle(int cx, int cy, double r, int c, MatrixStack matrixStack) {
        float f = (c >> 24 & 0xFF) / 255.0F;
        float f1 = (c >> 16 & 0xFF) / 255.0F;
        float f2 = (c >> 8 & 0xFF) / 255.0F;
        float f3 = (c & 0xFF) / 255.0F;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 360; i++) {
            double x = Math.sin(i * 3.141592653589793D / 180.0D) * r;
            double y = Math.cos(i * 3.141592653589793D / 180.0D) * r;
            bufferBuilder.vertex(cx + x, cy + y, -64).color(f1, f2, f3, f).next();
        }
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.defaultBlendFunc();
    }

    public void drawArc(float cx, float cy, double r, int c, int startpoint, double arc, int linewidth, MatrixStack matrixStack) {
        float f = (c >> 24 & 0xFF) / 255.0F;
        float f1 = (c >> 16 & 0xFF) / 255.0F;
        float f2 = (c >> 8 & 0xFF) / 255.0F;
        float f3 = (c & 0xFF) / 255.0F;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(linewidth);

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);//TRIANGLE_STRIP is fucked too I guess

        for (int i = (int) startpoint; i <= arc; i += 1) {
            double x = Math.sin(i * 3.141592653589793D / 180.0D) * r;
            double y = Math.cos(i * 3.141592653589793D / 180.0D) * r;
            bufferBuilder.vertex(cx + x, cy + y, 0).color(f1, f2, f3, f).next();
        }
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);

        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        RenderSystem.defaultBlendFunc();
    }

    public void drawHLine(MatrixStack matrixStack, float par1, float par2, float par3, int par4) {
        if (par2 < par1) {
            float var5 = par1;
            par1 = par2;
            par2 = var5;
        }

        fill(matrixStack, par1, par3, par2 + 1, par3 + 1, par4);
    }

    public void drawVLine(MatrixStack matrixStack, float par1, float par2, float par3, int par4) {
        if (par3 < par2) {
            float var5 = par2;
            par2 = par3;
            par3 = var5;
        }

        fill(matrixStack, par1, par2 + 1, par1 + 1, par3, par4);
    }

    public void glColor(int hex) {
        float alpha = (hex >> 24 & 0xFF) / 255.0F;
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public Color hex2Rgb(String colorStr) {
        try {
            return new Color(Integer.valueOf(colorStr.substring(2, 4), 16), Integer.valueOf(colorStr.substring(4, 6), 16), Integer.valueOf(colorStr.substring(6, 8), 16));
        } catch (Exception e) {
            return Color.WHITE;
        }
    }

    public boolean isHovered(float x, float y, float width, float height) {
        return x < MouseHelper.INSTANCE.getMouseX() && x + width > MouseHelper.INSTANCE.getMouseX() && y < MouseHelper.INSTANCE.getMouseY() && y + height > MouseHelper.INSTANCE.getMouseY();
    }

    public boolean hoversCircle(float centerX, float centerY, float radius) {
        Vec2f vec2f = new Vec2f(MouseHelper.INSTANCE.getMouseX(), MouseHelper.INSTANCE.getMouseY());
        float distance = ClientMathHelper.INSTANCE.getDistance2D(vec2f, new Vec2f(centerX, centerY));
        return distance <= radius;
    }

    public boolean isOnScreen(Vec3d pos) {
        if (pos.getZ() > -1 && pos.getZ() < 1) {
                return true;
        }
        return false;
    }

    public void drawItem(ItemStack stack, float xPosition, float yPosition) {
        drawItem(stack, xPosition, yPosition, 1);
    }
    public void drawItem(ItemStack stack, float xPosition, float yPosition, float scale) {
        String amountText = stack.getCount() != 1 ? stack.getCount() + "" : "";
        IItemRenderer iItemRenderer = (IItemRenderer) Wrapper.INSTANCE.getMinecraft().getItemRenderer();
        iItemRenderer.renderItemIntoGUI(stack, xPosition, yPosition);
        renderGuiItemOverlay(Wrapper.INSTANCE.getMinecraft().textRenderer, stack, xPosition - 0.5f, yPosition + 1, scale, amountText);
    }

    public void renderGuiItemOverlay(TextRenderer renderer, ItemStack stack, float x, float y, float scale, @Nullable String countLabel) {
        if (!stack.isEmpty()) {
            MatrixStack matrixStack = new MatrixStack();
            if (stack.getCount() != 1 || countLabel != null) {
                String string = countLabel == null ? String.valueOf(stack.getCount()) : countLabel;
                matrixStack.translate(0.0D, 0.0D, (double)(Wrapper.INSTANCE.getMinecraft().getItemRenderer().zOffset + 200.0F));
                VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                renderer.draw(string, (float)(x + 19 - 2 - renderer.getWidth(string)), (float)(y + 6 + 3), 16777215, true, matrixStack.peek().getModel(), immediate, false, 0, 15728880);
                immediate.draw();
            }

            if (stack.isItemBarVisible()) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableBlend();
                int i = stack.getItemBarStep();
                int j = stack.getItemBarColor();
                this.fill(matrixStack, x + 2, y + 13, x + 2 + 13, y + 13 + 2, 0xff000000);
                this.fill(matrixStack, x + 2, y + 13, x + 2 + i, y + 13 + 1, new Color(j >> 16 & 255, j >> 8 & 255, j & 255, 255).getRGB());
                RenderSystem.enableBlend();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
            float f = clientPlayerEntity == null ? 0.0F : clientPlayerEntity.getItemCooldownManager().getCooldownProgress(stack.getItem(), MinecraftClient.getInstance().getTickDelta());
            if (f > 0.0F) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tessellator tessellator2 = Tessellator.getInstance();
                BufferBuilder bufferBuilder2 = tessellator2.getBuffer();
                this.renderGuiQuad(bufferBuilder2, x, y + MathHelper.floor(16.0F * (1.0F - f)), 16, MathHelper.ceil(16.0F * f), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

        }
    }

    private void renderGuiQuad(BufferBuilder buffer, float x, float y, float width, float height, int red, int green, int blue, int alpha) {
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex((double) (x + 0), (double) (y + 0), 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex((double) (x + 0), (double) (y + height), 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex((double) (x + width), (double) (y + height), 0.0D).color(red, green, blue, alpha).next();
        buffer.vertex((double) (x + width), (double) (y + 0), 0.0D).color(red, green, blue, alpha).next();
        Tessellator.getInstance().draw();
    }

    float offset = 0;
    float a = 0.69f;
    boolean up = false;
    private Timer timer = new Timer();

    public void background(MatrixStack matrixStack, float x, float y, float width, float height) {
        if (timer.hasPassed(20)) {
            if (up) {
                if (a < .69f)//nice
                    a+=0.01f;
                else
                    up = false;
            } else {
                if (a > 0.01f)
                    a-=0.01f;
                else
                    up = true;
            }
            offset += 0.25f;
            if (offset > 270)
                offset -=270;
            timer.reset();
        }
        float topLeftColor = offset;
        float topRightColor = offset + 80;
        float bottomRightColor = offset + (80 * 2);
        float bottomLeftColor = offset + (80 * 3);
        if (topRightColor > 270)
            topRightColor-=270;
        if (bottomRightColor > 270)
            bottomRightColor-=270;
        if (bottomLeftColor > 270)
            bottomLeftColor-=270;
        Matrix4f matrix4f = matrixStack.peek().getModel();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        shaderColor(0xffffffff);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Color topLeft = ColorHelper.INSTANCE.getColorViaHue(topLeftColor);
        Color topRight = ColorHelper.INSTANCE.getColorViaHue(topRightColor);
        Color bottomRight = ColorHelper.INSTANCE.getColorViaHue(bottomRightColor);
        Color bottomLeft = ColorHelper.INSTANCE.getColorViaHue(bottomLeftColor);

        bufferBuilder.vertex(matrix4f, x, y + height, 0.0F).color(bottomLeft.getRed() / 255.f, bottomLeft.getGreen() / 255.f, bottomLeft.getBlue() / 255.f, a + 0.3f).next();
        bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0F).color(bottomRight.getRed() / 255.f, bottomRight.getGreen() / 255.f, bottomRight.getBlue() / 255.f, 1 - a).next();
        bufferBuilder.vertex(matrix4f, x + width, y, 0.0F).color(topRight.getRed() / 255.f, topRight.getGreen() / 255.f, topRight.getBlue() / 255.f, a + 0.3f).next();
        bufferBuilder.vertex(matrix4f, x, y, 0.0F).color(topLeft.getRed() / 255.f, topLeft.getGreen() / 255.f, topLeft.getBlue() / 255.f, 1 - a).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();


        /*VertexObjectList vertexObjectList = new VertexObjectList();
        vertexObjectList.vertex(matrix4f, x + width, y + height, 0).color(0, 0, 1, 0.5f);
        vertexObjectList.vertex(matrix4f, x, y + height, 0).color(1, 1, 0, 0.5f);
        vertexObjectList.vertex(matrix4f, x + width, y, 0).color(0, 1, 1, 0.5f);
        vertexObjectList.vertex(matrix4f, x, y, 0).color(1, 0, 1, 0.5f);
        vertexObjectList.end();
        VertexObjectList.draw(vertexObjectList);*/
    }

    public int getPercentColor(float percent) {
        if (percent <= 15)
            return new Color(255, 0, 0).getRGB();
        else if (percent <= 25)
            return new Color(255, 75, 92).getRGB();
        else if (percent <= 50)
            return new Color(255, 123, 17).getRGB();
        else if (percent <= 75)
            return new Color(255, 234, 0).getRGB();
        return new Color(0, 255, 0).getRGB();
    }

    public Vec3d to2D(Vec3d worldPos) {
        Vec3d bound = Render3DHelper.INSTANCE.getRenderPosition(worldPos);
        Vec3d twoD = to2D(bound.x, bound.y, bound.z);
        return new Vec3d(twoD.x, twoD.y, twoD.z);
    }

    private Vec3d to2D(double x, double y, double z) {
        int displayHeight = Wrapper.INSTANCE.getWindow().getHeight();
        Vector3f screenCoords = new Vector3f();
        int[] viewport = new int[4];
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer modelView = stack.mallocFloat(16);
            FloatBuffer projection = stack.mallocFloat(16);
            RenderSystem.getModelViewMatrix().write(modelView, false);//glGetFloatV doesn't work for getting model or view matrix anymore but thankfully minecraft has handy-dandy methods to grab them
            RenderSystem.getProjectionMatrix().write(projection, false);//and we write them to framebuffer so we can convert to JOML's matrix4f to do the math I don't fucking want to do
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
            new org.joml.Matrix4f(projection).mul(new org.joml.Matrix4f(modelView)).project((float) x, (float) y, (float) z, viewport, screenCoords);
        }
        return new Vec3d(screenCoords.x / Render2DHelper.INSTANCE.getScaleFactor(), (displayHeight - screenCoords.y) / Render2DHelper.INSTANCE.getScaleFactor(), screenCoords.z);
    }

    public Vec3d getHeadPos(Entity entity, float partialTicks) {
        Vec3d bound = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, partialTicks);
        Vec3d twoD = to2D(bound.x, bound.y + entity.getHeight() + 0.2, bound.z);
        return new Vec3d(twoD.x, twoD.y, twoD.z);
    }

    public Vec3d getFootPos(Entity entity, float partialTicks) {
        Vec3d bound = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, partialTicks);
        Vec3d twoD = to2D(bound.x, bound.y, bound.z);
        return new Vec3d(twoD.x, twoD.y, twoD.z);
    }

    public Vec3d getPos(Entity entity, float yOffset, float partialTicks) {
        Vec3d bound = Render3DHelper.INSTANCE.getEntityRenderPosition(entity, partialTicks);
        Vec3d twoD = to2D(bound.x, bound.y + yOffset, bound.z);
        return new Vec3d(twoD.x, twoD.y, twoD.z);
    }

    public void drawArrow(MatrixStack matrixStack, float x, float y, boolean open, int color) {
        bindTexture(cog);
        shaderColor(color);
        DrawableHelper.drawTexture(matrixStack, (int) x - 5, (int) y - 5, 0, 0, 10, 10, 10, 10);
        shaderColor(-1);
    }

    public void bindTexture(Identifier identifier) {
        RenderSystem.setShaderTexture(0, identifier);
    }

    public void shaderColor(int hex) {
        float alpha = (hex >> 24 & 0xFF) / 255.0F;
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    protected class BlurShader extends ShaderProgram {

        private ShaderUniform modelViewMat, projMat;
        public BlurShader() {
            super("blur");
            this.modelViewMat = addUniform("ModelViewMat");
            this.projMat = addUniform("ProjMat");
            this.bindAttribute("Position", 0);
            this.bindAttribute("Color", 1);
        }

        @Override
        public void updateUniforms() {
            modelViewMat.setMatrix(RenderSystem.getModelViewMatrix());
            projMat.setMatrix(RenderSystem.getProjectionMatrix());
        }
    }
}
