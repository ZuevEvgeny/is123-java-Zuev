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

        int width = 512;
        int height = 512;
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int brickH = 64;
                int brickW = 128;
                int mortar = 6;

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
                    int mortarVar = (int)(Math.sin(x * 0.1) * 10);
                    r = (byte)(210 + mortarVar);
                    g = (byte)(200 + mortarVar/2);
                    b = (byte)(180 + mortarVar/3);
                } else {
                    int variation = (brickRow * 13 + brickCol * 7) % 40 - 20;
                    int brickType = (brickRow + brickCol) % 3;
                    if (brickType == 0) {
                        r = (byte)(170 + variation);
                        g = (byte)(100 + variation/2);
                        b = (byte)(70 + variation/3);
                    } else if (brickType == 1) {
                        r = (byte)(190 + variation);
                        g = (byte)(110 + variation/2);
                        b = (byte)(80 + variation/3);
                    } else {
                        r = (byte)(150 + variation);
                        g = (byte)(90 + variation/2);
                        b = (byte)(60 + variation/3);
                    }
                }

                r = (byte)Math.min(255, Math.max(0, r));
                g = (byte)Math.min(255, Math.max(0, g));
                b = (byte)Math.min(255, Math.max(0, b));

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

        int width = 512;
        int height = 512;
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int plankWidth = 80;
                int plank = x / plankWidth;
                int localX = x % plankWidth;

                int variation = (plank * 37) % 30 - 15;

                int grain = (int)(Math.sin(x * 0.03) * Math.cos(y * 0.02) * 25);

                byte r, g, b;
                if (localX < 3 || localX > plankWidth - 3) {
                    r = (byte)(80 + variation/3);
                    g = (byte)(50 + variation/4);
                    b = (byte)(30 + variation/5);
                } else {
                    r = (byte)(160 + variation + grain);
                    g = (byte)(110 + variation/2 + grain/2);
                    b = (byte)(70 + variation/3 + grain/3);
                }

                r = (byte)Math.min(255, Math.max(0, r));
                g = (byte)Math.min(255, Math.max(0, g));
                b = (byte)Math.min(255, Math.max(0, b));

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

        int width = 256;
        int height = 512;
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int panelHeight = 140;
                int panelWidth = 100;
                int margin = 20;

                boolean isPanel = (x > margin && x < width - margin &&
                        y > margin && y < height - margin &&
                        (y < margin + panelHeight || y > height - margin - panelHeight));

                byte r, g, b;
                if (isPanel) {
                    r = (byte)200;
                    g = (byte)160;
                    b = (byte)120;
                } else {
                    r = (byte)130;
                    g = (byte)90;
                    b = (byte)60;
                }

                int grain = (int)(Math.sin(x * 0.08) * 15 + Math.cos(y * 0.1) * 15);
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