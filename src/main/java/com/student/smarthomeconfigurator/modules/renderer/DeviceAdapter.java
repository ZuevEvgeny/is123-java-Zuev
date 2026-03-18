package com.student.smarthomeconfigurator.modules.renderer;

import com.student.smarthomeconfigurator.model.Device;
import com.student.smarthomeconfigurator.devices.Lamp;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

public class DeviceAdapter implements Device.DeviceListener {
    private Device device;
    private Sphere sphere;
    private float[] position;

    public DeviceAdapter(Device device, Sphere sphere, float[] position) {
        this.device = device;
        this.sphere = sphere;
        this.position = position;

        device.addListener(this);

        updateVisual();
    }

    @Override
    public void onDeviceChanged(Device device) {
        updateVisual();
    }

    private void updateVisual() {
        PhongMaterial material = new PhongMaterial();

        if (device instanceof Lamp) {
            Lamp lamp = (Lamp) device;
            if (lamp.isStatus()) {
                material.setDiffuseColor(Color.YELLOW);
                material.setSpecularColor(Color.ORANGE);
            } else {
                material.setDiffuseColor(Color.GRAY);
                material.setSpecularColor(Color.DARKGRAY);
            }
        } else if (device.getType().contains("SENSOR")) {
            if (device.isStatus()) {
                material.setDiffuseColor(Color.CYAN);
            } else {
                material.setDiffuseColor(Color.LIGHTBLUE);
            }
        }

        sphere.setMaterial(material);
    }

    public Device getDevice() { return device; }
    public float[] getPosition() { return position; }
}