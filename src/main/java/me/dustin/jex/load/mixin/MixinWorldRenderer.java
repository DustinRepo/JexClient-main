package me.dustin.jex.load.mixin;

import me.dustin.jex.event.render.EventBlockOutlineColor;
import me.dustin.jex.event.render.EventRenderRain;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.load.impl.IWorldRenderer;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.impl.render.esp.ESP;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer implements IWorldRenderer {

    @Shadow
    @Final
    private BufferBuilderStorage bufferBuilders;

    private Identifier my_outline = new Identifier("jex", "shaders/entity_outline.json");

    @Redirect(method = "loadEntityOutlineShader", at = @At(value = "NEW", target = "net/minecraft/util/Identifier"))
    public Identifier getIDForOutline(String id) {
        if (Module.get(ESP.class).getState() && ((ESP) Module.get(ESP.class)).mode.equalsIgnoreCase("Shader")) {
            return my_outline;
        }
        return new Identifier("shaders/post/entity_outline.json");
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    public void renderWeather(LightmapTextureManager manager, float f, double d, double e, double g, CallbackInfo ci) {
        EventRenderRain eventRenderRain = new EventRenderRain().run();
        if (eventRenderRain.isCancelled())
            ci.cancel();
    }

    @Inject(method = "drawShapeOutline", at = @At("HEAD"), cancellable = true)
    private static void drawShapeOutline1(MatrixStack matrixStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j, CallbackInfo ci) {
        EventBlockOutlineColor eventBlockOutlineColor = new EventBlockOutlineColor().run();
        if (eventBlockOutlineColor.isCancelled()) {
            Color color = Render2DHelper.INSTANCE.hex2Rgb(Integer.toHexString(eventBlockOutlineColor.getColor()));
            Matrix4f matrix4f = matrixStack.peek().getModel();
            voxelShape.forEachEdge((k, l, m, n, o, p) -> {
                vertexConsumer.vertex(matrix4f, (float) (k + d), (float) (l + e), (float) (m + f)).color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, j).next();
                vertexConsumer.vertex(matrix4f, (float) (n + d), (float) (o + e), (float) (p + f)).color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, j).next();
            });
            ci.cancel();
        }
    }

    @Override
    public BufferBuilderStorage getBufferBuilders() {
        return this.bufferBuilders;
    }
}
