package com.student.smarthomeconfigurator.library;

import java.util.*;

public class ItemLibrary {
    private static ItemLibrary instance;
    private Map<String, LibraryItem> items;

    private ItemLibrary() {
        items = new LinkedHashMap<>();
        registerAllItems();
    }

    public static synchronized ItemLibrary getInstance() {
        if (instance == null) {
            instance = new ItemLibrary();
        }
        return instance;
    }

    private void registerAllItems() {
        registerFurniture();
        registerLamps();
        registerSensors();
        registerKitchenAppliances(); // НОВЫЙ МЕТОД
    }

    private void registerFurniture() {
        items.put("chair_modern", new LibraryItem(
                "chair_modern", "Стул современный", ItemCategory.FURNITURE,
                "models/furniture/chair_modern.obj", 0.8f, 0f, "icons/chair.png",
                0.5f, 0.5f, 0.8f, LibraryItem.MountType.FLOOR
        ));

        items.put("table_dining", new LibraryItem(
                "table_dining", "Стол обеденный", ItemCategory.FURNITURE,
                "models/furniture/table_dining.obj", 1.0f, 0f, "icons/table.png",
                1.2f, 0.8f, 0.75f, LibraryItem.MountType.FLOOR
        ));

        items.put("sofa_corner", new LibraryItem(
                "sofa_corner", "Диван угловой", ItemCategory.FURNITURE,
                "models/furniture/sofa_corner.obj", 1.0f, 0f, "icons/sofa.png",
                2.0f, 2.0f, 0.8f, LibraryItem.MountType.FLOOR
        ));

        items.put("bed_double", new LibraryItem(
                "bed_double", "Кровать двуспальная", ItemCategory.FURNITURE,
                "models/furniture/bed_double.obj", 1.0f, 0f, "icons/bed.png",
                1.6f, 2.0f, 0.8f, LibraryItem.MountType.FLOOR
        ));

        items.put("wardrobe", new LibraryItem(
                "wardrobe", "Шкаф", ItemCategory.FURNITURE,
                "models/furniture/wardrobe.obj", 0.9f, 0f, "icons/wardrobe.png",
                0.9f, 0.6f, 2.0f, LibraryItem.MountType.FLOOR
        ));

        items.put("shelf", new LibraryItem(
                "shelf", "Стеллаж", ItemCategory.FURNITURE,
                "models/furniture/shelf.obj", 0.9f, 0f, "icons/shelf.png",
                0.8f, 0.4f, 1.6f, LibraryItem.MountType.FLOOR
        ));

        // НОВАЯ МЕБЕЛЬ
        items.put("coffee_table", new LibraryItem(
                "coffee_table", "Журнальный столик", ItemCategory.FURNITURE,
                "models/furniture/coffee_table.obj", 0.9f, 0f, "icons/coffee_table.png",
                0.8f, 0.5f, 0.45f, LibraryItem.MountType.FLOOR
        ));

        items.put("nightstand", new LibraryItem(
                "nightstand", "Тумбочка", ItemCategory.FURNITURE,
                "models/furniture/nightstand.obj", 0.9f, 0f, "icons/nightstand.png",
                0.45f, 0.4f, 0.5f, LibraryItem.MountType.FLOOR
        ));

        items.put("tv", new LibraryItem(
                "tv", "Телевизор", ItemCategory.FURNITURE,
                "models/furniture/tv.obj", 1.0f, 0.5f, "icons/tv.png",
                1.0f, 0.05f, 0.6f, LibraryItem.MountType.FLOOR
        ));
    }

    private void registerLamps() {
        items.put("lamp_ceiling", new LibraryItem(
                "lamp_ceiling", "Люстра потолочная", ItemCategory.LAMP,
                "models/lamps/ceiling_modern.obj", 0.6f, 2.7f, "icons/lamp_ceiling.png",
                0.5f, 0.5f, 0.4f, LibraryItem.MountType.CEILING
        ));

        items.put("lamp_floor", new LibraryItem(
                "lamp_floor", "Торшер", ItemCategory.LAMP,
                "models/lamps/floor_lamp.obj", 0.7f, 0f, "icons/lamp_floor.png",
                0.4f, 0.4f, 1.4f, LibraryItem.MountType.FLOOR
        ));

        items.put("lamp_desk", new LibraryItem(
                "lamp_desk", "Настольная лампа", ItemCategory.LAMP,
                "models/lamps/desk_lamp.obj", 0.5f, 0.7f, "icons/lamp_desk.png",
                0.3f, 0.3f, 0.5f, LibraryItem.MountType.FLOOR
        ));

        items.put("lamp_spot", new LibraryItem(
                "lamp_spot", "Спот", ItemCategory.LAMP,
                "models/lamps/spotlight.obj", 0.4f, 2.7f, "icons/lamp_spot.png",
                0.2f, 0.2f, 0.2f, LibraryItem.MountType.CEILING
        ));

        items.put("lamp_pendant", new LibraryItem(
                "lamp_pendant", "Подвесная лампа", ItemCategory.LAMP,
                "models/lamps/pendant.obj", 0.6f, 2.5f, "icons/lamp_pendant.png",
                0.4f, 0.4f, 0.5f, LibraryItem.MountType.CEILING
        ));
    }

    private void registerSensors() {
        items.put("sensor_motion", new LibraryItem(
                "sensor_motion", "Датчик движения", ItemCategory.SENSOR,
                "models/sensors/motion_sensor.obj", 0.3f, 1.2f, "icons/sensor_motion.png",
                0.15f, 0.15f, 0.1f, LibraryItem.MountType.WALL
        ));

        items.put("sensor_temp", new LibraryItem(
                "sensor_temp", "Датчик температуры", ItemCategory.SENSOR,
                "models/sensors/temperature_sensor.obj", 0.3f, 1.2f, "icons/sensor_temp.png",
                0.12f, 0.12f, 0.08f, LibraryItem.MountType.WALL
        ));

        items.put("sensor_smoke", new LibraryItem(
                "sensor_smoke", "Датчик дыма", ItemCategory.SENSOR,
                "models/sensors/smoke_sensor.obj", 0.3f, 2.7f, "icons/sensor_smoke.png",
                0.12f, 0.12f, 0.08f, LibraryItem.MountType.CEILING
        ));

        items.put("sensor_light", new LibraryItem(
                "sensor_light", "Датчик освещенности", ItemCategory.SENSOR,
                "models/sensors/light_sensor.obj", 0.3f, 1.5f, "icons/sensor_light.png",
                0.1f, 0.1f, 0.05f, LibraryItem.MountType.WALL
        ));
    }

    // НОВЫЙ МЕТОД ДЛЯ КУХОННОЙ ТЕХНИКИ
    private void registerKitchenAppliances() {
        items.put("stove", new LibraryItem(
                "stove", "Плита", ItemCategory.FURNITURE,
                "models/kitchen/stove.obj", 1.0f, 0f, "icons/stove.png",
                0.6f, 0.6f, 0.85f, LibraryItem.MountType.FLOOR
        ));

        items.put("fridge", new LibraryItem(
                "fridge", "Холодильник", ItemCategory.FURNITURE,
                "models/kitchen/fridge.obj", 1.0f, 0f, "icons/fridge.png",
                0.7f, 0.7f, 1.8f, LibraryItem.MountType.FLOOR
        ));

        items.put("microwave", new LibraryItem(
                "microwave", "Микроволновая печь", ItemCategory.FURNITURE,
                "models/kitchen/microwave.obj", 0.9f, 0.85f, "icons/microwave.png",
                0.55f, 0.45f, 0.35f, LibraryItem.MountType.FLOOR
        ));

        items.put("dishwasher", new LibraryItem(
                "dishwasher", "Посудомоечная машина", ItemCategory.FURNITURE,
                "models/kitchen/dishwasher.obj", 1.0f, 0f, "icons/dishwasher.png",
                0.6f, 0.6f, 0.85f, LibraryItem.MountType.FLOOR
        ));

        items.put("washing_machine", new LibraryItem(
                "washing_machine", "Стиральная машина", ItemCategory.FURNITURE,
                "models/kitchen/washing_machine.obj", 1.0f, 0f, "icons/washing_machine.png",
                0.6f, 0.6f, 0.85f, LibraryItem.MountType.FLOOR
        ));
    }

    public List<LibraryItem> getItemsByCategory(ItemCategory category) {
        List<LibraryItem> result = new ArrayList<>();
        for (LibraryItem item : items.values()) {
            if (item.getCategory() == category) {
                result.add(item);
            }
        }
        return result;
    }

    public LibraryItem getItem(String id) {
        return items.get(id);
    }

    public Collection<LibraryItem> getAllItems() {
        return items.values();
    }

    public float getDefaultYOffset(String itemId) {
        LibraryItem item = items.get(itemId);
        return item != null ? item.getDefaultYOffset() : 0f;
    }
}