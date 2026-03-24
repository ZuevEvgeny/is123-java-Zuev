package com.student.smarthomeconfigurator.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class TextureManager {
    private static TextureManager instance;
    private Map<String, Integer> textureCache = new HashMap<>();

    private TextureManager() {}

    public static synchronized TextureManager getInstance() {
        if (instance == null) {
            instance = new TextureManager();
        }
        return instance;
    }

    public int loadTexture(String path) {
        if (textureCache.containsKey(path)) {
            return textureCache.get(path);
        }

        File file = new File(path);
        if (!file.exists()) {
            return 0;
        }

        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return 0;
            }

            int width = image.getWidth();
            int height = image.getHeight();

            int[] pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);

            ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = pixels[y * width + x];
                    buffer.put((byte) ((pixel >> 16) & 0xFF));
                    buffer.put((byte) ((pixel >> 8) & 0xFF));
                    buffer.put((byte) (pixel & 0xFF));
                    buffer.put((byte) ((pixel >> 24) & 0xFF));
                }
            }
            buffer.flip();

            int textureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0,
                    GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            textureCache.put(path, textureId);
            return textureId;

        } catch (IOException e) {
            return 0;
        }
    }

    public int generateBrickTexture() {
        if (textureCache.containsKey("brick")) {
            return textureCache.get("brick");
        }

        int width = 256;
        int height = 256;
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int brickH = 32;
                int brickW = 64;
                int mortar = 4;

                int brickRow = y / brickH;
                int brickCol = x / brickW;

                if (brickRow % 2 == 1) {
                    brickCol = (x + brickW/2) / brickW;
                }

                int localX = x % brickW;
                int localY = y % brickH;

                boolean isMortar = (localX < mortar) || (localX > brickW - mortar) ||
                        (localY < mortar) || (localY > brickH - mortar);

                byte r, g, b;
                if (isMortar) {
                    r = (byte)200; g = (byte)190; b = (byte)170;
                } else {
                    int variation = (brickRow * 13 + brickCol * 7) % 30 - 15;
                    r = (byte)(180 + variation);
                    g = (byte)(100 + variation/2);
                    b = (byte)(70 + variation/3);
                }

                buffer.put(r);
                buffer.put(g);
                buffer.put(b);
                buffer.put((byte)255);
            }
        }
        buffer.flip();

        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        textureCache.put("brick", textureId);
        return textureId;
    }

    public int generateWoodTexture() {
        if (textureCache.containsKey("wood")) {
            return textureCache.get("wood");
        }

        int width = 256;
        int height = 256;
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int stripeWidth = 16;
                int stripe = x / stripeWidth;
                int localX = x % stripeWidth;

                boolean darkStripe = (stripe % 2 == 0);

                int noise = (int)(Math.sin(x * 0.05) * Math.cos(y * 0.05) * 20);

                byte r, g, b;
                if (darkStripe) {
                    r = (byte)(120 + noise);
                    g = (byte)(80 + noise/2);
                    b = (byte)(50 + noise/3);
                } else {
                    r = (byte)(180 + noise);
                    g = (byte)(140 + noise/2);
                    b = (byte)(100 + noise/3);
                }

                buffer.put(r);
                buffer.put(g);
                buffer.put(b);
                buffer.put((byte)255);
            }
        }
        buffer.flip();

        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        textureCache.put("wood", textureId);
        return textureId;
    }

    public int generateDoorTexture() {
        if (textureCache.containsKey("door")) {
            return textureCache.get("door");
        }

        int width = 128;
        int height = 256;
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int panelHeight = 80;
                int panelWidth = 60;
                int margin = 15;

                boolean isPanel = (x > margin && x < width - margin &&
                        y > margin && y < height - margin &&
                        (y < margin + panelHeight || y > height - margin - panelHeight));

                byte r, g, b;
                if (isPanel) {
                    r = (byte)180;
                    g = (byte)140;
                    b = (byte)100;
                } else {
                    r = (byte)120;
                    g = (byte)80;
                    b = (byte)50;
                }

                int grain = (int)(Math.sin(x * 0.1) * 10 + Math.cos(y * 0.15) * 10);
                r = (byte)Math.min(255, Math.max(0, r + grain));
                g = (byte)Math.min(255, Math.max(0, g + grain/2));
                b = (byte)Math.min(255, Math.max(0, b + grain/3));

                buffer.put(r);
                buffer.put(g);
                buffer.put(b);
                buffer.put((byte)255);
            }
        }
        buffer.flip();

        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        textureCache.put("door", textureId);
        return textureId;
    }

    public void cleanup() {
        for (int textureId : textureCache.values()) {
            GL11.glDeleteTextures(textureId);
        }
        textureCache.clear();
    }
}