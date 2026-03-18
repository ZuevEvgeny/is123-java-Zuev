package com.student.smarthomeconfigurator.devices;

import com.student.smarthomeconfigurator.model.Device;

public class DeviceFactory {
    public static Device createDevice(String type, String roomId) {
        return switch (type) {
            case "LIGHT_SENSOR" -> new LightSensor(roomId);
            case "TEMPERATURE_SENSOR" -> new TemperatureSensor(roomId);
            case "HUMIDITY_SENSOR" -> new HumiditySensor(roomId);
            case "MOTION_SENSOR" -> new MotionSensor(roomId);
            case "SMOKE_SENSOR" -> new SmokeSensor(roomId);
            case "LAMP" -> new Lamp(roomId);
            case "SMART_LAMP" -> new Lamp(roomId, true);
            default -> null;
        };
    }
}