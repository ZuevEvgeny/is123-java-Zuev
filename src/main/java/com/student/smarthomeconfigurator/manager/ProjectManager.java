package com.student.smarthomeconfigurator.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.student.smarthomeconfigurator.model.building.BuildingProject;
import com.student.smarthomeconfigurator.database.DatabaseManager;
import javafx.scene.control.TextInputDialog;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Менеджер для сохранения и загрузки проектов зданий
 */
public class ProjectManager {
    private static ProjectManager instance;
    private final ObjectMapper objectMapper;
    private final DatabaseManager dbManager;
    private final String projectsDir;

    private ProjectManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.dbManager = DatabaseManager.getInstance();
        this.projectsDir = System.getProperty("user.dir") + "/projects";

        // Создаем директорию для проектов если её нет
        File dir = new File(projectsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static synchronized ProjectManager getInstance() {
        if (instance == null) {
            instance = new ProjectManager();
        }
        return instance;
    }

    /**
     * Сохранить проект в JSON файл
     */
    public void saveToFile(BuildingProject project, String filename) throws IOException {
        String fullPath = projectsDir + "/" + filename;
        objectMapper.writeValue(new File(fullPath), project);
        System.out.println("💾 Проект сохранен в файл: " + fullPath);
    }

    /**
     * Загрузить проект из JSON файла
     */
    public BuildingProject loadFromFile(String filename) throws IOException {
        String fullPath = projectsDir + "/" + filename;
        return objectMapper.readValue(new File(fullPath), BuildingProject.class);
    }

    public void saveToFileWithDialog(BuildingProject project, javafx.stage.Window owner) {
        TextInputDialog dialog = new TextInputDialog(project.getName());
        dialog.setTitle("Сохранение проекта");
        dialog.setHeaderText("Введите название проекта:");
        dialog.setContentText("Название:");
        dialog.initOwner(owner);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                project.setName(name.trim());
                try {
                    String filename = name.trim().replace(" ", "_") + ".json";
                    saveToFile(project, filename);
                    saveToDatabase(project);
                    System.out.println("💾 Проект сохранен как: " + name);
                } catch (IOException e) {
                    System.err.println("Ошибка сохранения: " + e.getMessage());
                }
            }
        });
    }
    /**
     * Сохранить проект в базу данных
     */
    public void saveToDatabase(BuildingProject project) {
        try {
            String json = objectMapper.writeValueAsString(project);
            dbManager.saveBuildingProject(project.getId(), project.getName(), json);
            System.out.println("💾 Проект сохранен в БД: " + project.getName());
        } catch (Exception e) {
            System.err.println("❌ Ошибка сохранения в БД: " + e.getMessage());
        }
    }

    /**
     * Загрузить проект из базы данных
     */
    public BuildingProject loadFromDatabase(String projectId) {
        try {
            String json = dbManager.loadBuildingProject(projectId);
            if (json != null) {
                return objectMapper.readValue(json, BuildingProject.class);
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки из БД: " + e.getMessage());
        }
        return null;
    }

    /**
     * Получить список всех сохраненных проектов
     */
    public List<ProjectInfo> getProjectList() {
        List<ProjectInfo> projects = new ArrayList<>();

        // Из базы данных
        List<Map<String, String>> dbProjects = dbManager.getAllBuildingProjects();
        for (Map<String, String> p : dbProjects) {
            projects.add(new ProjectInfo(p.get("id"), p.get("name"), "database"));
        }

        // Из файлов
        File dir = new File(projectsDir);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                String name = file.getName().replace(".json", "");
                projects.add(new ProjectInfo(name, name, "file"));
            }
        }

        return projects;
    }

    /**
     * Экспорт проекта в JSON строку
     */
    public String exportToJson(BuildingProject project) throws IOException {
        return objectMapper.writeValueAsString(project);
    }

    /**
     * Импорт проекта из JSON строки
     */
    public BuildingProject importFromJson(String json) throws IOException {
        return objectMapper.readValue(json, BuildingProject.class);
    }

    /**
     * Информация о проекте
     */
    public static class ProjectInfo {
        public String id;
        public String name;
        public String source;

        public ProjectInfo(String id, String name, String source) {
            this.id = id;
            this.name = name;
            this.source = source;
        }

        @Override
        public String toString() {
            return name + " (" + source + ")";
        }
    }
}