package com.student.smarthomeconfigurator.library;

/**
 * Категории предметов в библиотеке
 */
public enum ItemCategory {
    FURNITURE("Мебель"),
    LAMP("Освещение"),
    SENSOR("Датчики"),
    WINDOW("Окна"),
    DOOR("Двери");

    private final String displayName;

    ItemCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}