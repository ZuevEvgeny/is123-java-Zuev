package com.student.smarthomeconfigurator.database;

import com.student.smarthomeconfigurator.model.*;
import com.student.smarthomeconfigurator.devices.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static DatabaseManager instance;
    private static final String DB_URL = "jdbc:h2:./smarthome_db";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private Connection connection;

    private DatabaseManager() {
        connect();
        createTables();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("База данных подключена");
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к БД: " + e.getMessage());
        }
    }

    private void createTables() {
        String createProjects = """
            CREATE TABLE IF NOT EXISTS projects (
                id VARCHAR(36) PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                created_at TIMESTAMP,
                modified_at TIMESTAMP
            )
            """;

        String createRooms = """
            CREATE TABLE IF NOT EXISTS rooms (
                id VARCHAR(36) PRIMARY KEY,
                project_id VARCHAR(36) NOT NULL,
                name VARCHAR(255) NOT NULL,
                x DOUBLE,
                y DOUBLE,
                width DOUBLE,
                height DOUBLE,
                floor INT,
                FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
            )
            """;

        String createDevices = """
            CREATE TABLE IF NOT EXISTS devices (
                id VARCHAR(36) PRIMARY KEY,
                room_id VARCHAR(36) NOT NULL,
                name VARCHAR(255) NOT NULL,
                type VARCHAR(100) NOT NULL,
                status BOOLEAN DEFAULT TRUE,
                device_value VARCHAR(255),
                FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
            )
            """;

        String createBuildingProjects = """
            CREATE TABLE IF NOT EXISTS building_projects (
                id VARCHAR(36) PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                json_data CLOB,
                created_at TIMESTAMP,
                modified_at TIMESTAMP
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createProjects);
            stmt.execute(createRooms);
            stmt.execute(createDevices);
            stmt.execute(createBuildingProjects);
            System.out.println("Таблицы созданы");
        } catch (SQLException e) {
            System.err.println("Ошибка создания таблиц: " + e.getMessage());
        }
    }

    public void saveProject(Project project) {
        String insertProject = "MERGE INTO projects (id, name, created_at, modified_at) VALUES (?, ?, ?, ?)";
        String insertRoom = "MERGE INTO rooms (id, project_id, name, x, y, width, height, floor) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String insertDevice = "MERGE INTO devices (id, room_id, name, type, status, device_value) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(insertProject)) {
                pstmt.setString(1, project.getId());
                pstmt.setString(2, project.getName());

                LocalDateTime createdAt = project.getCreatedAt();
                LocalDateTime modifiedAt = project.getModifiedAt();

                if (createdAt == null) {
                    createdAt = LocalDateTime.now();
                    project.setCreatedAt(createdAt);
                }
                if (modifiedAt == null) {
                    modifiedAt = LocalDateTime.now();
                    project.setModifiedAt(modifiedAt);
                }

                pstmt.setTimestamp(3, Timestamp.valueOf(createdAt));
                pstmt.setTimestamp(4, Timestamp.valueOf(modifiedAt));
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = connection.prepareStatement(insertRoom)) {
                for (Room room : project.getRooms()) {
                    pstmt.setString(1, room.getId());
                    pstmt.setString(2, project.getId());
                    pstmt.setString(3, room.getName());
                    pstmt.setDouble(4, room.getX());
                    pstmt.setDouble(5, room.getY());
                    pstmt.setDouble(6, room.getWidth());
                    pstmt.setDouble(7, room.getHeight());
                    pstmt.setInt(8, room.getFloor());
                    pstmt.executeUpdate();

                    try (PreparedStatement deviceStmt = connection.prepareStatement(insertDevice)) {
                        for (Device device : room.getDevices()) {
                            deviceStmt.setString(1, device.getId());
                            deviceStmt.setString(2, room.getId());
                            deviceStmt.setString(3, device.getName());
                            deviceStmt.setString(4, device.getType());
                            deviceStmt.setBoolean(5, device.isStatus());
                            deviceStmt.setString(6, device.getValue() != null ? device.getValue().toString() : null);
                            deviceStmt.executeUpdate();
                        }
                    }
                }
            }

            connection.commit();
            System.out.println("Проект сохранён: " + project.getName());

        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            System.err.println("Ошибка сохранения: " + e.getMessage());
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public Project loadProject(String projectId) {
        String selectProject = "SELECT * FROM projects WHERE id = ?";
        String selectRooms = "SELECT * FROM rooms WHERE project_id = ?";
        String selectDevices = "SELECT * FROM devices WHERE room_id = ?";

        try {
            Project project = null;
            try (PreparedStatement pstmt = connection.prepareStatement(selectProject)) {
                pstmt.setString(1, projectId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    project = new Project(rs.getString("name"));
                    project.setId(rs.getString("id"));

                    Timestamp createdAtTs = rs.getTimestamp("created_at");
                    Timestamp modifiedAtTs = rs.getTimestamp("modified_at");

                    if (createdAtTs != null) {
                        project.setCreatedAt(createdAtTs.toLocalDateTime());
                    }
                    if (modifiedAtTs != null) {
                        project.setModifiedAt(modifiedAtTs.toLocalDateTime());
                    }
                }
            }

            if (project == null) return null;

            try (PreparedStatement pstmt = connection.prepareStatement(selectRooms)) {
                pstmt.setString(1, projectId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Room room = new Room(
                            rs.getString("name"),
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("width"),
                            rs.getDouble("height")
                    );

                    room.setId(rs.getString("id"));
                    room.setFloor(rs.getInt("floor"));

                    try (PreparedStatement deviceStmt = connection.prepareStatement(selectDevices)) {
                        deviceStmt.setString(1, rs.getString("id"));
                        ResultSet deviceRs = deviceStmt.executeQuery();
                        while (deviceRs.next()) {
                            Device device = DeviceFactory.createDevice(
                                    deviceRs.getString("type"),
                                    rs.getString("id")
                            );
                            if (device != null) {
                                device.setId(deviceRs.getString("id"));
                                device.setStatus(deviceRs.getBoolean("status"));

                                String valueStr = deviceRs.getString("device_value");
                                if (valueStr != null) {
                                    device.setValue(valueStr);
                                }
                                room.addDevice(device);
                            }
                        }
                    }
                    project.addRoom(room);
                }
            }

            System.out.println("Проект загружен: " + project.getName());
            return project;

        } catch (SQLException e) {
            System.err.println("Ошибка загрузки: " + e.getMessage());
            return null;
        }
    }

    public List<String> getAllProjectIds() {
        List<String> ids = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM projects")) {
            while (rs.next()) {
                ids.add(rs.getString("id") + " (" + rs.getString("name") + ")");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения списка: " + e.getMessage());
        }
        return ids;
    }

    // === НОВЫЕ МЕТОДЫ ДЛЯ BUILDINGPROJECT ===

    /**
     * Сохранить BuildingProject
     */
    public void saveBuildingProject(String id, String name, String jsonData) {
        String sql = "MERGE INTO building_projects (id, name, json_data, created_at, modified_at) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, jsonData);
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            pstmt.setTimestamp(4, now);
            pstmt.setTimestamp(5, now);
            pstmt.executeUpdate();
            System.out.println("BuildingProject сохранён: " + name);
        } catch (SQLException e) {
            System.err.println("Ошибка сохранения BuildingProject: " + e.getMessage());
        }
    }

    /**
     * Загрузить BuildingProject
     */
    public String loadBuildingProject(String id) {
        String sql = "SELECT json_data FROM building_projects WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("json_data");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки BuildingProject: " + e.getMessage());
        }
        return null;
    }

    /**
     * Получить все BuildingProject (id и name)
     */
    public List<Map<String, String>> getAllBuildingProjects() {
        List<Map<String, String>> projects = new ArrayList<>();
        String sql = "SELECT id, name FROM building_projects ORDER BY modified_at DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> project = new HashMap<>();
                project.put("id", rs.getString("id"));
                project.put("name", rs.getString("name"));
                projects.add(project);
            }
        } catch (SQLException e) {
            System.err.println("Ошибка получения списка BuildingProject: " + e.getMessage());
        }
        return projects;
    }

    /**
     * Удалить BuildingProject
     */
    public void deleteBuildingProject(String id) {
        String sql = "DELETE FROM building_projects WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
            System.out.println("BuildingProject удалён: " + id);
        } catch (SQLException e) {
            System.err.println("Ошибка удаления BuildingProject: " + e.getMessage());
        }
    }


    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}