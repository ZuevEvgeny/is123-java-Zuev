package com.student.smarthomeconfigurator.generator;

import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;

public class ProceduralModelGenerator {

    public static class ModelData {
        public List<float[]> verticesList;
        public List<float[]> normalsList;
        public List<float[]> texCoordsList;
        public List<int[]> indicesList;
        public List<Matrix4f> transforms;
        public List<float[]> colors;

        public ModelData() {
            verticesList = new ArrayList<>();
            normalsList = new ArrayList<>();
            texCoordsList = new ArrayList<>();
            indicesList = new ArrayList<>();
            transforms = new ArrayList<>();
            colors = new ArrayList<>();
        }

        public void addMesh(float[] vertices, float[] normals, float[] texCoords, int[] indices,
                            Matrix4f transform, float r, float g, float b) {
            verticesList.add(vertices);
            normalsList.add(normals);
            texCoordsList.add(texCoords);
            indicesList.add(indices);
            transforms.add(transform);
            colors.add(new float[]{r, g, b});
        }
    }

    // ==================== МЕБЕЛЬ ====================

    public static ModelData createCornerSofa(float x, float y, float z, float scale, float rotation) {
        ModelData model = new ModelData();
        Matrix4f baseTransform = new Matrix4f().translate(x, y, z).rotateY(rotation).scale(scale);
        float[] norms = createBoxNormals();
        float[] tex = createBoxTexCoords();
        int[] indices = createBoxIndices();

        // Основная часть (длинная)
        float[] mainVerts = createBox(0.8f, 0.4f, 0.6f);
        Matrix4f mainTransform = new Matrix4f(baseTransform).translate(0, 0.2f, 0, new Matrix4f());
        model.addMesh(mainVerts, norms, tex, indices, mainTransform, 0.5f, 0.3f, 0.2f);

        // Угловая часть
        Matrix4f cornerTransform = new Matrix4f(baseTransform).translate(0.6f, 0.2f, 0.4f, new Matrix4f());
        model.addMesh(mainVerts, norms, tex, indices, cornerTransform, 0.5f, 0.3f, 0.2f);

        // Спинка основной части
        float[] backVerts = createBox(0.8f, 0.3f, 0.1f);
        Matrix4f backMainTransform = new Matrix4f(baseTransform).translate(0, 0.35f, -0.3f, new Matrix4f());
        model.addMesh(backVerts, norms, tex, indices, backMainTransform, 0.6f, 0.4f, 0.3f);

        // Спинка угловой части
        Matrix4f backCornerTransform = new Matrix4f(baseTransform).translate(0.6f, 0.35f, 0.1f, new Matrix4f());
        model.addMesh(backVerts, norms, tex, indices, backCornerTransform, 0.6f, 0.4f, 0.3f);

        // Подушки
        float[] pillowVerts = createBox(0.25f, 0.1f, 0.45f);
        Matrix4f pillow1Transform = new Matrix4f(baseTransform).translate(-0.25f, 0.45f, 0, new Matrix4f());
        Matrix4f pillow2Transform = new Matrix4f(baseTransform).translate(0, 0.45f, 0, new Matrix4f());
        Matrix4f pillow3Transform = new Matrix4f(baseTransform).translate(0.25f, 0.45f, 0, new Matrix4f());
        model.addMesh(pillowVerts, norms, tex, indices, pillow1Transform, 0.7f, 0.5f, 0.3f);
        model.addMesh(pillowVerts, norms, tex, indices, pillow2Transform, 0.7f, 0.5f, 0.3f);
        model.addMesh(pillowVerts, norms, tex, indices, pillow3Transform, 0.7f, 0.5f, 0.3f);

        return model;
    }

    public static ModelData createDoubleBed(float x, float y, float z, float scale, float rotation) {
        ModelData model = new ModelData();
        Matrix4f baseTransform = new Matrix4f().translate(x, y, z).rotateY(rotation).scale(scale);
        float[] norms = createBoxNormals();
        float[] tex = createBoxTexCoords();
        int[] indices = createBoxIndices();

        // Основание
        float[] baseVerts = createBox(0.9f, 0.2f, 1.6f);
        Matrix4f baseTransformMat = new Matrix4f(baseTransform).translate(0, 0.1f, 0, new Matrix4f());
        model.addMesh(baseVerts, norms, tex, indices, baseTransformMat, 0.5f, 0.3f, 0.2f);

        // Матрас
        float[] mattressVerts = createBox(0.85f, 0.15f, 1.55f);
        Matrix4f mattressTransform = new Matrix4f(baseTransform).translate(0, 0.25f, 0, new Matrix4f());
        model.addMesh(mattressVerts, norms, tex, indices, mattressTransform, 0.7f, 0.5f, 0.3f);

        // Подушки
        float[] pillowVerts = createBox(0.4f, 0.1f, 0.6f);
        Matrix4f pillow1Transform = new Matrix4f(baseTransform).translate(-0.25f, 0.4f, 0.5f, new Matrix4f());
        Matrix4f pillow2Transform = new Matrix4f(baseTransform).translate(0.25f, 0.4f, 0.5f, new Matrix4f());
        model.addMesh(pillowVerts, norms, tex, indices, pillow1Transform, 0.8f, 0.7f, 0.5f);
        model.addMesh(pillowVerts, norms, tex, indices, pillow2Transform, 0.8f, 0.7f, 0.5f);

        // Изголовье
        float[] headboardVerts = createBox(0.95f, 0.3f, 0.1f);
        Matrix4f headboardTransform = new Matrix4f(baseTransform).translate(0, 0.35f, -0.85f, new Matrix4f());
        model.addMesh(headboardVerts, norms, tex, indices, headboardTransform, 0.6f, 0.4f, 0.3f);

        return model;
    }

    public static ModelData createWardrobe(float x, float y, float z, float scale, float rotation) {
        ModelData model = new ModelData();
        Matrix4f baseTransform = new Matrix4f().translate(x, y, z).rotateY(rotation).scale(scale);
        float[] norms = createBoxNormals();
        float[] tex = createBoxTexCoords();
        int[] indices = createBoxIndices();

        // Корпус
        float[] bodyVerts = createBox(0.8f, 1.8f, 0.6f);
        Matrix4f bodyTransform = new Matrix4f(baseTransform).translate(0, 0.9f, 0, new Matrix4f());
        model.addMesh(bodyVerts, norms, tex, indices, bodyTransform, 0.7f, 0.5f, 0.3f);

        // Дверцы
        float[] doorVerts = createBox(0.38f, 1.6f, 0.05f);
        Matrix4f doorLeftTransform = new Matrix4f(baseTransform).translate(-0.4f, 0.9f, 0.31f, new Matrix4f());
        Matrix4f doorRightTransform = new Matrix4f(baseTransform).translate(0.4f, 0.9f, 0.31f, new Matrix4f());
        model.addMesh(doorVerts, norms, tex, indices, doorLeftTransform, 0.8f, 0.6f, 0.4f);
        model.addMesh(doorVerts, norms, tex, indices, doorRightTransform, 0.8f, 0.6f, 0.4f);

        // Ручки
        float[] handleVerts = createCylinder(0.03f, 0.1f, 8);
        float[] cylNorms = createCylinderNormals(handleVerts);
        float[] cylTex = createCylinderTexCoords(handleVerts);
        int[] cylIndices = createCylinderIndices(8);
        Matrix4f handleLeftTransform = new Matrix4f(baseTransform).translate(-0.4f, 0.9f, 0.34f, new Matrix4f());
        Matrix4f handleRightTransform = new Matrix4f(baseTransform).translate(0.4f, 0.9f, 0.34f, new Matrix4f());
        model.addMesh(handleVerts, cylNorms, cylTex, cylIndices, handleLeftTransform, 0.9f, 0.8f, 0.6f);
        model.addMesh(handleVerts, cylNorms, cylTex, cylIndices, handleRightTransform, 0.9f, 0.8f, 0.6f);

        return model;
    }

    public static ModelData createShelf(float x, float y, float z, float scale, float rotation) {
        ModelData model = new ModelData();
        Matrix4f baseTransform = new Matrix4f().translate(x, y, z).rotateY(rotation).scale(scale);
        float[] norms = createBoxNormals();
        float[] tex = createBoxTexCoords();
        int[] indices = createBoxIndices();

        // Вертикальные стойки
        float[] poleVerts = createBox(0.08f, 1.2f, 0.08f);
        Matrix4f pole1Transform = new Matrix4f(baseTransform).translate(-0.4f, 0.6f, -0.3f, new Matrix4f());
        Matrix4f pole2Transform = new Matrix4f(baseTransform).translate(0.4f, 0.6f, -0.3f, new Matrix4f());
        Matrix4f pole3Transform = new Matrix4f(baseTransform).translate(-0.4f, 0.6f, 0.3f, new Matrix4f());
        Matrix4f pole4Transform = new Matrix4f(baseTransform).translate(0.4f, 0.6f, 0.3f, new Matrix4f());
        model.addMesh(poleVerts, norms, tex, indices, pole1Transform, 0.6f, 0.4f, 0.3f);
        model.addMesh(poleVerts, norms, tex, indices, pole2Transform, 0.6f, 0.4f, 0.3f);
        model.addMesh(poleVerts, norms, tex, indices, pole3Transform, 0.6f, 0.4f, 0.3f);
        model.addMesh(poleVerts, norms, tex, indices, pole4Transform, 0.6f, 0.4f, 0.3f);

        // Полки
        float[] shelfVerts = createBox(0.9f, 0.05f, 0.7f);
        Matrix4f shelf1Transform = new Matrix4f(baseTransform).translate(0, 0.2f, 0, new Matrix4f());
        Matrix4f shelf2Transform = new Matrix4f(baseTransform).translate(0, 0.6f, 0, new Matrix4f());
        Matrix4f shelf3Transform = new Matrix4f(baseTransform).translate(0, 1.0f, 0, new Matrix4f());
        model.addMesh(shelfVerts, norms, tex, indices, shelf1Transform, 0.7f, 0.5f, 0.3f);
        model.addMesh(shelfVerts, norms, tex, indices, shelf2Transform, 0.7f, 0.5f, 0.3f);
        model.addMesh(shelfVerts, norms, tex, indices, shelf3Transform, 0.7f, 0.5f, 0.3f);

        return model;
    }

    public static ModelData createChair(float x, float y, float z, float scale, float rotation) {
        ModelData model = new ModelData();
        Matrix4f baseTransform = new Matrix4f().translate(x, y, z).rotateY(rotation).scale(scale);
        float[] norms = createBoxNormals();
        float[] tex = createBoxTexCoords();
        int[] indices = createBoxIndices();

        // Сиденье
        float[] seatVerts = createBox(0.4f, 0.05f, 0.4f);
        Matrix4f seatTransform = new Matrix4f(baseTransform).translate(0, 0.2f, 0, new Matrix4f());
        model.addMesh(seatVerts, norms, tex, indices, seatTransform, 0.6f, 0.4f, 0.3f);

        // Спинка
        float[] backVerts = createBox(0.4f, 0.4f, 0.05f);
        Matrix4f backTransform = new Matrix4f(baseTransform).translate(0, 0.4f, -0.2f, new Matrix4f());
        model.addMesh(backVerts, norms, tex, indices, backTransform, 0.6f, 0.4f, 0.3f);

        // Ножки
        float[] legVerts = createCylinder(0.05f, 0.2f, 8);
        float[] cylNorms = createCylinderNormals(legVerts);
        float[] cylTex = createCylinderTexCoords(legVerts);
        int[] cylIndices = createCylinderIndices(8);

        Matrix4f leg1Transform = new Matrix4f(baseTransform).translate(-0.3f, 0.05f, -0.3f, new Matrix4f());
        Matrix4f leg2Transform = new Matrix4f(baseTransform).translate(0.3f, 0.05f, -0.3f, new Matrix4f());
        Matrix4f leg3Transform = new Matrix4f(baseTransform).translate(-0.3f, 0.05f, 0.3f, new Matrix4f());
        Matrix4f leg4Transform = new Matrix4f(baseTransform).translate(0.3f, 0.05f, 0.3f, new Matrix4f());

        model.addMesh(legVerts, cylNorms, cylTex, cylIndices, leg1Transform, 0.4f, 0.3f, 0.2f);
        model.addMesh(legVerts, cylNorms, cylTex, cylIndices, leg2Transform, 0.4f, 0.3f, 0.2f);
        model.addMesh(legVerts, cylNorms, cylTex, cylIndices, leg3Transform, 0.4f, 0.3f, 0.2f);
        model.addMesh(legVerts, cylNorms, cylTex, cylIndices, leg4Transform, 0.4f, 0.3f, 0.2f);

        return model;
    }

    public static ModelData createTable(float x, float y, float z, float scale, float rotation) {
        ModelData model = new ModelData();
        Matrix4f baseTransform = new Matrix4f().translate(x, y, z).rotateY(rotation).scale(scale);
        float[] norms = createBoxNormals();
        float[] tex = createBoxTexCoords();
        int[] indices = createBoxIndices();

        // Столешница
        float[] topVerts = createBox(0.8f, 0.08f, 0.8f);
        Matrix4f topTransform = new Matrix4f(baseTransform).translate(0, 0.4f, 0, new Matrix4f());
        model.addMesh(topVerts, norms, tex, indices, topTransform, 0.7f, 0.5f, 0.3f);

        // Ножки
        float[] legVerts = createCylinder(0.08f, 0.4f, 8);
        float[] cylNorms = createCylinderNormals(legVerts);
        float[] cylTex = createCylinderTexCoords(legVerts);
        int[] cylIndices = createCylinderIndices(8);

        Matrix4f leg1Transform = new Matrix4f(baseTransform).translate(-0.35f, 0.2f, -0.35f, new Matrix4f());
        Matrix4f leg2Transform = new Matrix4f(baseTransform).translate(0.35f, 0.2f, -0.35f, new Matrix4f());
        Matrix4f leg3Transform = new Matrix4f(baseTransform).translate(-0.35f, 0.2f, 0.35f, new Matrix4f());
        Matrix4f leg4Transform = new Matrix4f(baseTransform).translate(0.35f, 0.2f, 0.35f, new Matrix4f());

        model.addMesh(legVerts, cylNorms, cylTex, cylIndices, leg1Transform, 0.5f, 0.3f, 0.2f);
        model.addMesh(legVerts, cylNorms, cylTex, cylIndices, leg2Transform, 0.5f, 0.3f, 0.2f);
        model.addMesh(legVerts, cylNorms, cylTex, cylIndices, leg3Transform, 0.5f, 0.3f, 0.2f);
        model.addMesh(legVerts, cylNorms, cylTex, cylIndices, leg4Transform, 0.5f, 0.3f, 0.2f);

        return model;
    }

    // ==================== ЛАМПЫ ====================

    public static ModelData createChandelier(float x, float y, float z, float scale, float rotation) {
        ModelData model = new ModelData();
        Matrix4f baseTransform = new Matrix4f().translate(x, y, z).rotateY(rotation).scale(scale);

        // Центральная часть
        float[] centerVerts = createSphere(0.15f, 16, 16);
        float[] sphereNorms = createSphereNormals(centerVerts);
        float[] sphereTex = createSphereTexCoords(centerVerts);
        int[] sphereIndices = createSphereIndices(16, 16);
        Matrix4f centerTransform = new Matrix4f(baseTransform);
        model.addMesh(centerVerts, sphereNorms, sphereTex, sphereIndices, centerTransform, 0.9f, 0.8f, 0.6f);

        // Лампочки по кругу
        float[] bulbVerts = createSphere(0.1f, 12, 12);
        for (int i = 0; i < 4; i++) {
            float angle = (float)(Math.PI * 2 * i / 4);
            float dx = (float)Math.cos(angle) * 0.4f;
            float dz = (float)Math.sin(angle) * 0.4f;
            Matrix4f bulbTransform = new Matrix4f(baseTransform).translate(dx, 0.1f, dz, new Matrix4f());
            model.addMesh(bulbVerts, sphereNorms, sphereTex, sphereIndices, bulbTransform, 1.0f, 0.9f, 0.7f);
        }

        // Подвес
        float[] chainVerts = createCylinder(0.05f, 0.2f, 8);
        float[] cylNorms = createCylinderNormals(chainVerts);
        float[] cylTex = createCylinderTexCoords(chainVerts);
        int[] cylIndices = createCylinderIndices(8);
        Matrix4f chainTransform = new Matrix4f(baseTransform).translate(0, 0.2f, 0, new Matrix4f());
        model.addMesh(chainVerts, cylNorms, cylTex, cylIndices, chainTransform, 0.7f, 0.6f, 0.5f);

        return model;
    }

    public static ModelData createPendantLamp(float x, float y, float z, float scale, float rotation) {
        ModelData model = new ModelData();
        Matrix4f baseTransform = new Matrix4f().translate(x, y, z).rotateY(rotation).scale(scale);

        // Плафон
        float[] shadeVerts = createCylinder(0.25f, 0.3f, 24);
        float[] cylNorms = createCylinderNormals(shadeVerts);
        float[] cylTex = createCylinderTexCoords(shadeVerts);
        int[] cylIndices = createCylinderIndices(24);
        Matrix4f shadeTransform = new Matrix4f(baseTransform);
        model.addMesh(shadeVerts, cylNorms, cylTex, cylIndices, shadeTransform, 0.8f, 0.7f, 0.5f);

        // Лампочка внутри
        float[] bulbVerts = createSphere(0.12f, 12, 12);
        float[] sphereNorms = createSphereNormals(bulbVerts);
        float[] sphereTex = createSphereTexCoords(bulbVerts);
        int[] sphereIndices = createSphereIndices(12, 12);
        Matrix4f bulbTransform = new Matrix4f(baseTransform).translate(0, 0.05f, 0, new Matrix4f());
        model.addMesh(bulbVerts, sphereNorms, sphereTex, sphereIndices, bulbTransform, 1.0f, 0.9f, 0.7f);

        // Подвес
        float[] cordVerts = createCylinder(0.04f, 0.2f, 6);
        float[] cordNorms = createCylinderNormals(cordVerts);
        float[] cordTex = createCylinderTexCoords(cordVerts);
        int[] cordIndices = createCylinderIndices(6);
        Matrix4f cordTransform = new Matrix4f(baseTransform).translate(0, 0.2f, 0, new Matrix4f());
        model.addMesh(cordVerts, cordNorms, cordTex, cordIndices, cordTransform, 0.6f, 0.5f, 0.4f);

        return model;
    }

    public static ModelData createDeskLamp(float x, float y, float z, float scale, float rotation) {
        ModelData model = new ModelData();
        Matrix4f baseTransform = new Matrix4f().translate(x, y, z).rotateY(rotation).scale(scale);

        // Основание
        float[] baseVerts = createCylinder(0.3f, 0.1f, 16);
        float[] cylNorms = createCylinderNormals(baseVerts);
        float[] cylTex = createCylinderTexCoords(baseVerts);
        int[] cylIndices = createCylinderIndices(16);
        Matrix4f baseTransformMat = new Matrix4f(baseTransform).translate(0, 0.05f, 0, new Matrix4f());
        model.addMesh(baseVerts, cylNorms, cylTex, cylIndices, baseTransformMat, 0.5f, 0.3f, 0.2f);

        // Стойка
        float[] poleVerts = createCylinder(0.08f, 0.5f, 12);
        Matrix4f poleTransform = new Matrix4f(baseTransform).translate(0, 0.3f, 0, new Matrix4f());
        model.addMesh(poleVerts, cylNorms, cylTex, cylIndices, poleTransform, 0.6f, 0.4f, 0.3f);

        // Абажур
        float[] shadeVerts = createCone(0.25f, 0.2f, 16);
        float[] coneNorms = createConeNormals(shadeVerts);
        float[] coneTex = createConeTexCoords(shadeVerts);
        int[] coneIndices = createConeIndices(16);
        Matrix4f shadeTransform = new Matrix4f(baseTransform).translate(0, 0.6f, 0, new Matrix4f());
        model.addMesh(shadeVerts, coneNorms, coneTex, coneIndices, shadeTransform, 0.9f, 0.7f, 0.5f);

        // Лампочка
        float[] bulbVerts = createSphere(0.12f, 12, 12);
        float[] sphereNorms = createSphereNormals(bulbVerts);
        float[] sphereTex = createSphereTexCoords(bulbVerts);
        int[] sphereIndices = createSphereIndices(12, 12);
        Matrix4f bulbTransform = new Matrix4f(baseTransform).translate(0, 0.75f, 0, new Matrix4f());
        model.addMesh(bulbVerts, sphereNorms, sphereTex, sphereIndices, bulbTransform, 1.0f, 0.9f, 0.6f);

        return model;
    }

    public static ModelData createFloorLamp(float x, float y, float z, float scale, float rotation) {
        ModelData model = new ModelData();
        Matrix4f baseTransform = new Matrix4f().translate(x, y, z).rotateY(rotation).scale(scale);

        // Основание
        float[] baseVerts = createCylinder(0.35f, 0.1f, 16);
        float[] cylNorms = createCylinderNormals(baseVerts);
        float[] cylTex = createCylinderTexCoords(baseVerts);
        int[] cylIndices = createCylinderIndices(16);
        Matrix4f baseTransformMat = new Matrix4f(baseTransform).translate(0, 0.05f, 0, new Matrix4f());
        model.addMesh(baseVerts, cylNorms, cylTex, cylIndices, baseTransformMat, 0.4f, 0.3f, 0.2f);

        // Стойка
        float[] poleVerts = createCylinder(0.1f, 1.2f, 12);
        Matrix4f poleTransform = new Matrix4f(baseTransform).translate(0, 0.65f, 0, new Matrix4f());
        model.addMesh(poleVerts, cylNorms, cylTex, cylIndices, poleTransform, 0.5f, 0.4f, 0.3f);

        // Плафон
        float[] shadeVerts = createCone(0.35f, 0.25f, 24);
        float[] coneNorms = createConeNormals(shadeVerts);
        float[] coneTex = createConeTexCoords(shadeVerts);
        int[] coneIndices = createConeIndices(24);
        Matrix4f shadeTransform = new Matrix4f(baseTransform).translate(0, 1.3f, 0, new Matrix4f());
        model.addMesh(shadeVerts, coneNorms, coneTex, coneIndices, shadeTransform, 0.85f, 0.7f, 0.55f);

        return model;
    }

    public static ModelData createSpotlight(float x, float y, float z, float scale, float rotation) {
        ModelData model = new ModelData();
        Matrix4f baseTransform = new Matrix4f().translate(x, y, z).rotateY(rotation).scale(scale);

        // Корпус
        float[] bodyVerts = createCylinder(0.12f, 0.15f, 12);
        float[] cylNorms = createCylinderNormals(bodyVerts);
        float[] cylTex = createCylinderTexCoords(bodyVerts);
        int[] cylIndices = createCylinderIndices(12);
        Matrix4f bodyTransform = new Matrix4f(baseTransform);
        model.addMesh(bodyVerts, cylNorms, cylTex, cylIndices, bodyTransform, 0.7f, 0.6f, 0.5f);

        // Линза
        float[] lensVerts = createSphere(0.08f, 10, 10);
        float[] sphereNorms = createSphereNormals(lensVerts);
        float[] sphereTex = createSphereTexCoords(lensVerts);
        int[] sphereIndices = createSphereIndices(10, 10);
        Matrix4f lensTransform = new Matrix4f(baseTransform).translate(0, 0.1f, 0, new Matrix4f());
        model.addMesh(lensVerts, sphereNorms, sphereTex, sphereIndices, lensTransform, 0.9f, 0.9f, 0.8f);

        // Крепление
        float[] mountVerts = createBox(0.08f, 0.05f, 0.08f);
        float[] boxNorms = createBoxNormals();
        float[] boxTex = createBoxTexCoords();
        int[] boxIndices = createBoxIndices();
        Matrix4f mountTransform = new Matrix4f(baseTransform).translate(0, -0.1f, 0, new Matrix4f());
        model.addMesh(mountVerts, boxNorms, boxTex, boxIndices, mountTransform, 0.6f, 0.5f, 0.4f);

        return model;
    }

    // ==================== ДАТЧИКИ ====================

    public static ModelData createMotionSensor(float x, float y, float z, float scale, float rotation) {
        ModelData model = new ModelData();
        Matrix4f baseTransform = new Matrix4f().translate(x, y, z).rotateY(rotation).scale(scale);
        float[] boxNorms = createBoxNormals();
        float[] boxTex = createBoxTexCoords();
        int[] boxIndices = createBoxIndices();

        // Корпус
        float[] bodyVerts = createBox(0.2f, 0.2f, 0.1f);
        Matrix4f bodyTransform = new Matrix4f(baseTransform);
        model.addMesh(bodyVerts, boxNorms, boxTex, boxIndices, bodyTransform, 0.9f, 0.9f, 0.9f);

        // Линза (полусфера)
        float[] lensVerts = createHemisphere(0.08f, 12, 12);
        float[] lensNorms = createHemisphereNormals(lensVerts);
        float[] lensTex = createHemisphereTexCoords(lensVerts);
        int[] lensIndices = createHemisphereIndices(12, 12);
        Matrix4f lensTransform = new Matrix4f(baseTransform).translate(0, 0, 0.06f, new Matrix4f());
        model.addMesh(lensVerts, lensNorms, lensTex, lensIndices, lensTransform, 0.2f, 0.5f, 0.8f);

        // LED индикатор
        float[] ledVerts = createSphere(0.03f, 8, 8);
        float[] sphereNorms = createSphereNormals(ledVerts);
        float[] sphereTex = createSphereTexCoords(ledVerts);
        int[] sphereIndices = createSphereIndices(8, 8);
        Matrix4f ledTransform = new Matrix4f(baseTransform).translate(0.1f, 0.08f, 0.07f, new Matrix4f());
        model.addMesh(ledVerts, sphereNorms, sphereTex, sphereIndices, ledTransform, 1.0f, 0.2f, 0.2f);

        return model;
    }

    public static ModelData createTemperatureSensor(float x, float y, float z, float scale, float rotation) {
        ModelData model = new ModelData();
        Matrix4f baseTransform = new Matrix4f().translate(x, y, z).rotateY(rotation).scale(scale);

        // Корпус
        float[] bodyVerts = createCylinder(0.12f, 0.2f, 12);
        float[] cylNorms = createCylinderNormals(bodyVerts);
        float[] cylTex = createCylinderTexCoords(bodyVerts);
        int[] cylIndices = createCylinderIndices(12);
        Matrix4f bodyTransform = new Matrix4f(baseTransform);
        model.addMesh(bodyVerts, cylNorms, cylTex, cylIndices, bodyTransform, 0.9f, 0.9f, 0.9f);

        // Экран
        float[] screenVerts = createBox(0.08f, 0.1f, 0.02f);
        float[] boxNorms = createBoxNormals();
        float[] boxTex = createBoxTexCoords();
        int[] boxIndices = createBoxIndices();
        Matrix4f screenTransform = new Matrix4f(baseTransform).translate(0, 0.05f, 0.07f, new Matrix4f());
        model.addMesh(screenVerts, boxNorms, boxTex, boxIndices, screenTransform, 0.2f, 0.6f, 0.9f);

        return model;
    }

    public static ModelData createSmokeSensor(float x, float y, float z, float scale, float rotation) {
        ModelData model = new ModelData();
        Matrix4f baseTransform = new Matrix4f().translate(x, y, z).rotateY(rotation).scale(scale);

        // Корпус
        float[] bodyVerts = createCylinder(0.15f, 0.1f, 16);
        float[] cylNorms = createCylinderNormals(bodyVerts);
        float[] cylTex = createCylinderTexCoords(bodyVerts);
        int[] cylIndices = createCylinderIndices(16);
        Matrix4f bodyTransform = new Matrix4f(baseTransform);
        model.addMesh(bodyVerts, cylNorms, cylTex, cylIndices, bodyTransform, 0.9f, 0.9f, 0.9f);

        // Верхняя часть с решеткой
        float[] topVerts = createCylinder(0.16f, 0.05f, 16);
        Matrix4f topTransform = new Matrix4f(baseTransform).translate(0, 0.08f, 0, new Matrix4f());
        model.addMesh(topVerts, cylNorms, cylTex, cylIndices, topTransform, 0.8f, 0.8f, 0.8f);

        // Индикатор
        float[] ledVerts = createSphere(0.04f, 8, 8);
        float[] sphereNorms = createSphereNormals(ledVerts);
        float[] sphereTex = createSphereTexCoords(ledVerts);
        int[] sphereIndices = createSphereIndices(8, 8);
        Matrix4f ledTransform = new Matrix4f(baseTransform).translate(0.1f, 0.05f, 0.1f, new Matrix4f());
        model.addMesh(ledVerts, sphereNorms, sphereTex, sphereIndices, ledTransform, 1.0f, 0.0f, 0.0f);

        return model;
    }

    public static ModelData createLightSensor(float x, float y, float z, float scale, float rotation) {
        ModelData model = new ModelData();
        Matrix4f baseTransform = new Matrix4f().translate(x, y, z).rotateY(rotation).scale(scale);
        float[] boxNorms = createBoxNormals();
        float[] boxTex = createBoxTexCoords();
        int[] boxIndices = createBoxIndices();

        // Корпус
        float[] bodyVerts = createBox(0.15f, 0.1f, 0.1f);
        Matrix4f bodyTransform = new Matrix4f(baseTransform);
        model.addMesh(bodyVerts, boxNorms, boxTex, boxIndices, bodyTransform, 0.9f, 0.9f, 0.9f);

        // Сенсорная линза
        float[] lensVerts = createHemisphere(0.06f, 8, 8);
        float[] lensNorms = createHemisphereNormals(lensVerts);
        float[] lensTex = createHemisphereTexCoords(lensVerts);
        int[] lensIndices = createHemisphereIndices(8, 8);
        Matrix4f lensTransform = new Matrix4f(baseTransform).translate(0, 0, 0.06f, new Matrix4f());
        model.addMesh(lensVerts, lensNorms, lensTex, lensIndices, lensTransform, 0.3f, 0.6f, 0.9f);

        return model;
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private static float[] createCylinder(float radius, float height, int segments) {
        List<Float> verts = new ArrayList<>();
        float halfHeight = height / 2;
        for (int i = 0; i <= segments; i++) {
            float angle = (float)(2 * Math.PI * i / segments);
            float x = (float)Math.cos(angle) * radius;
            float z = (float)Math.sin(angle) * radius;
            verts.add(x); verts.add(-halfHeight); verts.add(z);
            verts.add(x); verts.add(halfHeight); verts.add(z);
        }
        float[] result = new float[verts.size()];
        for (int i = 0; i < verts.size(); i++) result[i] = verts.get(i);
        return result;
    }

    private static float[] createCone(float radius, float height, int segments) {
        List<Float> verts = new ArrayList<>();
        float halfHeight = height / 2;
        for (int i = 0; i <= segments; i++) {
            float angle = (float)(2 * Math.PI * i / segments);
            float x = (float)Math.cos(angle) * radius;
            float z = (float)Math.sin(angle) * radius;
            verts.add(x); verts.add(-halfHeight); verts.add(z);
        }
        verts.add(0f); verts.add(halfHeight); verts.add(0f);
        float[] result = new float[verts.size()];
        for (int i = 0; i < verts.size(); i++) result[i] = verts.get(i);
        return result;
    }

    private static float[] createSphere(float radius, int rings, int sectors) {
        List<Float> verts = new ArrayList<>();
        for (int i = 0; i <= rings; i++) {
            float phi = (float)(Math.PI * i / rings);
            float y = (float)Math.cos(phi) * radius;
            float r = (float)Math.sin(phi) * radius;
            for (int j = 0; j <= sectors; j++) {
                float theta = (float)(2 * Math.PI * j / sectors);
                float x = (float)Math.cos(theta) * r;
                float z = (float)Math.sin(theta) * r;
                verts.add(x); verts.add(y); verts.add(z);
            }
        }
        float[] result = new float[verts.size()];
        for (int i = 0; i < verts.size(); i++) result[i] = verts.get(i);
        return result;
    }

    private static float[] createHemisphere(float radius, int rings, int sectors) {
        List<Float> verts = new ArrayList<>();
        for (int i = 0; i <= rings/2; i++) {
            float phi = (float)(Math.PI * i / rings);
            float y = (float)Math.cos(phi) * radius;
            float r = (float)Math.sin(phi) * radius;
            for (int j = 0; j <= sectors; j++) {
                float theta = (float)(2 * Math.PI * j / sectors);
                float x = (float)Math.cos(theta) * r;
                float z = (float)Math.sin(theta) * r;
                verts.add(x); verts.add(y); verts.add(z);
            }
        }
        float[] result = new float[verts.size()];
        for (int i = 0; i < verts.size(); i++) result[i] = verts.get(i);
        return result;
    }

    private static float[] createBox(float width, float height, float depth) {
        float hw = width / 2;
        float hh = height / 2;
        float hd = depth / 2;
        return new float[] {
                -hw, -hh, -hd,  hw, -hh, -hd,  hw,  hh, -hd, -hw,  hh, -hd,
                -hw, -hh,  hd,  hw, -hh,  hd,  hw,  hh,  hd, -hw,  hh,  hd
        };
    }

    private static float[] createCylinderNormals(float[] vertices) {
        float[] normals = new float[vertices.length];
        for (int i = 0; i < vertices.length / 3; i++) {
            float x = vertices[i*3];
            float z = vertices[i*3+2];
            float len = (float)Math.sqrt(x*x + z*z);
            if (len > 0.01f) {
                normals[i*3] = x / len;
                normals[i*3+2] = z / len;
            }
        }
        return normals;
    }

    private static float[] createCylinderTexCoords(float[] vertices) {
        float[] texCoords = new float[vertices.length / 3 * 2];
        for (int i = 0; i < vertices.length / 3; i++) {
            float angle = (float)Math.atan2(vertices[i*3+2], vertices[i*3]);
            texCoords[i*2] = (angle + (float)Math.PI) / (2 * (float)Math.PI);
            texCoords[i*2+1] = (vertices[i*3+1] + 0.5f);
        }
        return texCoords;
    }

    private static int[] createCylinderIndices(int segments) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;
            indices.add(i*2);
            indices.add(next*2);
            indices.add(next*2 + 1);
            indices.add(i*2);
            indices.add(next*2 + 1);
            indices.add(i*2 + 1);
        }
        int[] result = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) result[i] = indices.get(i);
        return result;
    }

    private static float[] createBoxNormals() {
        float[] normals = new float[24];
        for (int i = 0; i < 4; i++) normals[i*3+2] = -1;
        for (int i = 4; i < 8; i++) normals[i*3+2] = 1;
        for (int i = 0; i < 8; i+=4) normals[i*3+1] = -1;
        for (int i = 2; i < 8; i+=4) normals[i*3+1] = 1;
        normals[0*3] = -1; normals[3*3] = -1; normals[4*3] = -1; normals[7*3] = -1;
        normals[1*3] = 1; normals[2*3] = 1; normals[5*3] = 1; normals[6*3] = 1;
        return normals;
    }

    private static float[] createBoxTexCoords() {
        float[] texCoords = new float[16];
        for (int i = 0; i < 4; i++) {
            texCoords[i*2] = 0;
            texCoords[i*2+1] = 0;
        }
        for (int i = 4; i < 8; i++) {
            texCoords[i*2] = 1;
            texCoords[i*2+1] = 1;
        }
        return texCoords;
    }

    private static int[] createBoxIndices() {
        return new int[] {
                0,1,2, 0,2,3,
                4,6,5, 4,7,6,
                0,4,1, 1,4,5,
                3,2,7, 2,6,7,
                0,3,4, 3,7,4,
                1,5,2, 2,5,6
        };
    }

    private static float[] createConeNormals(float[] vertices) {
        return new float[vertices.length];
    }

    private static float[] createConeTexCoords(float[] vertices) {
        return new float[vertices.length / 3 * 2];
    }

    private static int[] createConeIndices(int segments) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;
            indices.add(i);
            indices.add(next);
            indices.add(segments);
        }
        int[] result = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) result[i] = indices.get(i);
        return result;
    }

    private static float[] createSphereNormals(float[] vertices) {
        float[] normals = new float[vertices.length];
        for (int i = 0; i < vertices.length / 3; i++) {
            float x = vertices[i*3];
            float y = vertices[i*3+1];
            float z = vertices[i*3+2];
            float len = (float)Math.sqrt(x*x + y*y + z*z);
            if (len > 0.01f) {
                normals[i*3] = x / len;
                normals[i*3+1] = y / len;
                normals[i*3+2] = z / len;
            }
        }
        return normals;
    }

    private static float[] createSphereTexCoords(float[] vertices) {
        float[] texCoords = new float[vertices.length / 3 * 2];
        for (int i = 0; i < vertices.length / 3; i++) {
            float u = (float)(Math.atan2(vertices[i*3+2], vertices[i*3]) / (2 * Math.PI) + 0.5);
            float v = (float)(Math.asin(vertices[i*3+1]) / Math.PI + 0.5);
            texCoords[i*2] = u;
            texCoords[i*2+1] = v;
        }
        return texCoords;
    }

    private static int[] createSphereIndices(int rings, int sectors) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < rings; i++) {
            for (int j = 0; j < sectors; j++) {
                int p1 = i * (sectors + 1) + j;
                int p2 = p1 + 1;
                int p3 = (i + 1) * (sectors + 1) + j;
                int p4 = p3 + 1;
                indices.add(p1);
                indices.add(p3);
                indices.add(p4);
                indices.add(p1);
                indices.add(p4);
                indices.add(p2);
            }
        }
        int[] result = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) result[i] = indices.get(i);
        return result;
    }

    private static float[] createHemisphereNormals(float[] vertices) {
        return createSphereNormals(vertices);
    }

    private static float[] createHemisphereTexCoords(float[] vertices) {
        return createSphereTexCoords(vertices);
    }

    private static int[] createHemisphereIndices(int rings, int sectors) {
        return createSphereIndices(rings/2, sectors);
    }
}