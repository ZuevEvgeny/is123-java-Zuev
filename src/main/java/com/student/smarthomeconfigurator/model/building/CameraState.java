package com.student.smarthomeconfigurator.model.building;

/**
 * Состояние камеры для сохранения
 */
public class CameraState {
    private float posX, posY, posZ;
    private float targetX, targetY, targetZ;
    private float yaw;
    private float pitch;
    private boolean orbitMode;

    public CameraState() {
        this.posX = 8.0f;
        this.posY = 4.0f;
        this.posZ = 15.0f;
        this.targetX = 0.0f;
        this.targetY = 2.0f;
        this.targetZ = 0.0f;
        this.yaw = 0.3f;
        this.pitch = -0.2f;
        this.orbitMode = true;
    }

    // Геттеры и сеттеры
    public float getPosX() { return posX; }
    public void setPosX(float posX) { this.posX = posX; }

    public float getPosY() { return posY; }
    public void setPosY(float posY) { this.posY = posY; }

    public float getPosZ() { return posZ; }
    public void setPosZ(float posZ) { this.posZ = posZ; }

    public float getTargetX() { return targetX; }
    public void setTargetX(float targetX) { this.targetX = targetX; }

    public float getTargetY() { return targetY; }
    public void setTargetY(float targetY) { this.targetY = targetY; }

    public float getTargetZ() { return targetZ; }
    public void setTargetZ(float targetZ) { this.targetZ = targetZ; }

    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }

    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }

    public boolean isOrbitMode() { return orbitMode; }
    public void setOrbitMode(boolean orbitMode) { this.orbitMode = orbitMode; }
}