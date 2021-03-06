package me.dustin.jex.addon.cape;

import com.google.common.collect.Maps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Cape {

    public static HashMap<String, Identifier> capes = Maps.newHashMap();

    public static void setPersonalCape(File file) {
        if (!file.exists())
            return;
        try {
            BufferedImage in = ImageIO.read(file);
            BufferedImage newImage = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = newImage.createGraphics();
            g.drawImage(in, 0, 0, null);
            g.dispose();
            NativeImage capeImage = readTexture(imageToBase64String(newImage, "png"));
            int imageWidth = 64;
            int imageHeight = 32;

            for (int srcWidth = capeImage.getWidth(), srcHeight = capeImage.getHeight(); imageWidth < srcWidth || imageHeight < srcHeight; ) {
                imageWidth *= 2;
                imageHeight *= 2;
            }

            NativeImage imgNew = new NativeImage(imageWidth, imageHeight, true);
            for (int x = 0; x < capeImage.getWidth(); x++) {
                for (int y = 0; y < capeImage.getHeight(); y++) {
                    imgNew.setPixelColor(x, y, capeImage.getPixelColor(x, y));
                }
            }

            capeImage.close();
            Identifier id = new Identifier("jex", "capes/self");
            applyTexture(id, imgNew);
            if (capes.containsKey("self"))
                capes.replace("self", id);
            else
                capes.put("self", id);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void parseCape(String cape, String uuid) {
        NativeImage capeImage = readTexture(cape);
        int imageWidth = 64;
        int imageHeight = 32;

        for (int srcWidth = capeImage.getWidth(), srcHeight = capeImage.getHeight(); imageWidth < srcWidth || imageHeight < srcHeight; ) {
            imageWidth *= 2;
            imageHeight *= 2;
        }

        NativeImage imgNew = new NativeImage(imageWidth, imageHeight, true);
        for (int x = 0; x < capeImage.getWidth(); x++) {
            for (int y = 0; y < capeImage.getHeight(); y++) {
                imgNew.setPixelColor(x, y, capeImage.getPixelColor(x, y));
            }
        }

        capeImage.close();
        Identifier id = new Identifier("jex", "capes/" + uuid);
        applyTexture(id, imgNew);
        capes.put(uuid, id);
    }

    private static NativeImage readTexture(String textureBase64) {
        try {
            byte[] imgBytes = Base64.decodeBase64(textureBase64);
            ByteArrayInputStream bais = new ByteArrayInputStream(imgBytes);
            return NativeImage.read(bais);
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    private static String imageToBase64String(BufferedImage image, String type) {
        String ret = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, type, bos);
            byte[] bytes = bos.toByteArray();
            Base64 encoder = new Base64();
            ret = encoder.encodeAsString(bytes);
            ret = ret.replace(System.lineSeparator(), "");
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return ret;
    }

    private static void applyTexture(Identifier identifier, NativeImage nativeImage) {
        MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, new NativeImageBackedTexture(nativeImage)));
    }

}
