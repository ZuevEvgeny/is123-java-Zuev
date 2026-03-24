package com.student.smarthomeconfigurator.utils;

import com.student.smarthomeconfigurator.model.building.*;
import com.student.smarthomeconfigurator.library.ItemLibrary;
import com.student.smarthomeconfigurator.library.LibraryItem;
import java.util.List;

public class CollisionChecker {

    private static final float WALL_THICKNESS = 0.2f;
    private static final float COLLISION_MARGIN = 0.05f;

    // Проверка пересечения со стенами
    public static boolean checkWallCollision(float x, float z, float width, float depth,
                                             float rotation, List<WallSegment> walls) {
        float[][] corners = getRotatedCorners(x, z, width / 2, depth / 2, rotation);

        for (WallSegment wall : walls) {
            for (float[] corner : corners) {
                double dist = pointToLineDistance(
                        corner[0], corner[1],
                        wall.getX1(), wall.getZ1(),
                        wall.getX2(), wall.getZ2()
                );

                if (dist < WALL_THICKNESS / 2 + COLLISION_MARGIN) {
                    return true;
                }
            }
        }
        return false;
    }

    // Проверка пересечения с другой мебелью
    public static boolean checkFurnitureCollision(float x, float z, float width, float depth,
                                                  float rotation, List<FurnitureItem> furniture,
                                                  FurnitureItem excludeItem) {
        float[][] newCorners = getRotatedCorners(x, z, width / 2, depth / 2, rotation);

        for (FurnitureItem item : furniture) {
            if (excludeItem != null && item == excludeItem) continue;

            ItemLibrary lib = ItemLibrary.getInstance();
            LibraryItem libItem = lib.getItem(item.getLibraryId());
            if (libItem == null) continue;

            float itemWidth = libItem.getWidth() * item.getScale();
            float itemDepth = libItem.getDepth() * item.getScale();
            float[][] existingCorners = getRotatedCorners(
                    item.getX(), item.getZ(),
                    itemWidth / 2, itemDepth / 2,
                    item.getRotation()
            );

            if (rectanglesOverlap(newCorners, existingCorners)) {
                return true;
            }
        }
        return false;
    }

    // Получение 4 углов повернутого прямоугольника
    private static float[][] getRotatedCorners(float cx, float cz, float halfW, float halfD, float rotation) {
        float cos = (float) Math.cos(rotation);
        float sin = (float) Math.sin(rotation);

        float[][] corners = new float[4][2];
        float[][] local = {
                {-halfW, -halfD}, { halfW, -halfD},
                { halfW,  halfD}, {-halfW,  halfD}
        };

        for (int i = 0; i < 4; i++) {
            corners[i][0] = cx + local[i][0] * cos - local[i][1] * sin;
            corners[i][1] = cz + local[i][0] * sin + local[i][1] * cos;
        }

        return corners;
    }

    // Проверка пересечения двух прямоугольников (SAT)
    private static boolean rectanglesOverlap(float[][] rect1, float[][] rect2) {
        // Проверка по осям X и Y (упрощенная AABB)
        float min1X = Float.MAX_VALUE, max1X = -Float.MAX_VALUE;
        float min1Z = Float.MAX_VALUE, max1Z = -Float.MAX_VALUE;
        float min2X = Float.MAX_VALUE, max2X = -Float.MAX_VALUE;
        float min2Z = Float.MAX_VALUE, max2Z = -Float.MAX_VALUE;

        for (int i = 0; i < 4; i++) {
            min1X = Math.min(min1X, rect1[i][0]);
            max1X = Math.max(max1X, rect1[i][0]);
            min1Z = Math.min(min1Z, rect1[i][1]);
            max1Z = Math.max(max1Z, rect1[i][1]);

            min2X = Math.min(min2X, rect2[i][0]);
            max2X = Math.max(max2X, rect2[i][0]);
            min2Z = Math.min(min2Z, rect2[i][1]);
            max2Z = Math.max(max2Z, rect2[i][1]);
        }

        return !(max1X < min2X - COLLISION_MARGIN ||
                max2X < min1X - COLLISION_MARGIN ||
                max1Z < min2Z - COLLISION_MARGIN ||
                max2Z < min1Z - COLLISION_MARGIN);
    }

    private static double pointToLineDistance(double px, double py,
                                              double x1, double y1, double x2, double y2) {
        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double len2 = C * C + D * D;

        if (len2 == 0) return Math.hypot(px - x1, py - y1);

        double param = dot / len2;
        double xx, yy;

        if (param < 0) { xx = x1; yy = y1; }
        else if (param > 1) { xx = x2; yy = y2; }
        else { xx = x1 + param * C; yy = y1 + param * D; }

        return Math.hypot(px - xx, py - yy);
    }

    // Автоповорот вдоль стены
    public static float snapToWallRotation(float x, float z, List<WallSegment> walls) {
        WallSegment nearest = null;
        double minDist = Double.MAX_VALUE;

        for (WallSegment wall : walls) {
            double dist = pointToLineDistance(x, z,
                    wall.getX1(), wall.getZ1(), wall.getX2(), wall.getZ2());

            if (dist < minDist) {
                minDist = dist;
                nearest = wall;
            }
        }

        if (nearest != null && minDist < 0.5) {
            return (float) Math.atan2(
                    nearest.getZ2() - nearest.getZ1(),
                    nearest.getX2() - nearest.getX1()
            );
        }

        return 0;
    }

    // Получить ограничивающий прямоугольник предмета
    public static float[] getBoundingBox(FurnitureItem item) {
        ItemLibrary lib = ItemLibrary.getInstance();
        LibraryItem libItem = lib.getItem(item.getLibraryId());

        if (libItem != null) {
            return new float[] {
                    libItem.getWidth() * item.getScale(),
                    libItem.getDepth() * item.getScale(),
                    libItem.getHeight() * item.getScale()
            };
        }

        return new float[] {0.5f, 0.5f, 0.5f};
    }
}