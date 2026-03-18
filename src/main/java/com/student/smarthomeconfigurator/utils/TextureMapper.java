package com.student.smarthomeconfigurator.utils;

import java.util.HashMap;
import java.util.Map;

public class TextureMapper {
    private static final Map<String, String> materialToTexture = new HashMap<>();

    static {
        // ИЗ ФАЙЛА Имена текстур.txt:
        // растения - leaf
        // другие растения - spatifilumList
        // стены - crisp_paper_ruffle_color_33
        // картины горизонтальные - LakeMoraine
        // картины вертикальные - StatueOfLiberty
        // пол - purty_wood_@2X_ddddddd_color_11
        // мебель - elementHautSalon_GrisMat
        // двери - purty_wood_@2X_ddddddd_color_12

        // СТЕНЫ
        materialToTexture.put("wall", "crisp_paper_ruffle_color_33");
        materialToTexture.put("walls", "crisp_paper_ruffle_color_33");
        materialToTexture.put("interior_wall", "crisp_paper_ruffle_color_33");
        materialToTexture.put("exterior_wall", "crisp_paper_ruffle_color_33");
        materialToTexture.put("08_cornice_01_main", "crisp_paper_ruffle_color_33");
        materialToTexture.put("modern_small_brick_main", "crisp_paper_ruffle_color_33");
        materialToTexture.put("defaultmaterial", "crisp_paper_ruffle_color_33");
        materialToTexture.put("15_-_default", "crisp_paper_ruffle_color_33");
        materialToTexture.put("material_67", "crisp_paper_ruffle_color_33");
        materialToTexture.put("material_66", "crisp_paper_ruffle_color_33");
        materialToTexture.put("concrete", "crisp_paper_ruffle_color_33");
        materialToTexture.put("concrete_wall", "crisp_paper_ruffle_color_33");
        materialToTexture.put("garage_wall", "crisp_paper_ruffle_color_33");

        // ПОЛ - добавляем больше вариантов названий
        materialToTexture.put("floor", "purty_wood_@2X_ddddddd_color_11");
        materialToTexture.put("floors", "purty_wood_@2X_ddddddd_color_11");
        materialToTexture.put("level_0_outsideblock-00-00_floor__0", "purty_wood_@2X_ddddddd_color_11");
        materialToTexture.put("plain_sienna_floor_main", "purty_wood_@2X_ddddddd_color_11");
        materialToTexture.put("plain_light_slate_gray_floor_main", "purty_wood_@2X_ddddddd_color_11");
        materialToTexture.put("plain_light_steel_main", "purty_wood_@2X_ddddddd_color_11");
        materialToTexture.put("garage_floor", "purty_wood_@2X_ddddddd_color_11");
        materialToTexture.put("concrete_floor", "purty_wood_@2X_ddddddd_color_11");
        materialToTexture.put("bedroom_floor", "purty_wood_@2X_ddddddd_color_11");
        materialToTexture.put("wood_floor", "purty_wood_@2X_ddddddd_color_11");
        materialToTexture.put("parquet", "purty_wood_@2X_ddddddd_color_11");
        materialToTexture.put("ground", "purty_wood_@2X_ddddddd_color_11");
        materialToTexture.put("ground_floor", "purty_wood_@2X_ddddddd_color_11");
        materialToTexture.put("outsideblock", "purty_wood_@2X_ddddddd_color_11");
        materialToTexture.put("outdoor_floor", "purty_wood_@2X_ddddddd_color_11");
        materialToTexture.put("indoor_floor", "purty_wood_@2X_ddddddd_color_11");

        // ДВЕРИ (используем purty_wood_@2X_ddddddd_color_12)
        materialToTexture.put("door", "purty_wood_@2X_ddddddd_color_12");
        materialToTexture.put("doors", "purty_wood_@2X_ddddddd_color_12");
        materialToTexture.put("screen-doorpaint_gloss_black", "purty_wood_@2X_ddddddd_color_12");
        materialToTexture.put("screen-doorsynth_screen", "purty_wood_@2X_ddddddd_color_12");
        materialToTexture.put("screen-doorpaint_gloss_white", "purty_wood_@2X_ddddddd_color_12");
        materialToTexture.put("garagedoor2glasswindow", "purty_wood_@2X_ddddddd_color_12");
        materialToTexture.put("double_doormetal_handle", "purty_wood_@2X_ddddddd_color_12");
        materialToTexture.put("double_door_with_little_partglass", "purty_wood_@2X_ddddddd_color_12");
        materialToTexture.put("double_doormetal_gonds", "purty_wood_@2X_ddddddd_color_12");
        materialToTexture.put("doorwhite_door", "purty_wood_@2X_ddddddd_color_12");
        materialToTexture.put("doorwhite_frame", "purty_wood_@2X_ddddddd_color_12");
        materialToTexture.put("doorglasspanelsdoor_glasspanels_1", "purty_wood_@2X_ddddddd_color_12");
        materialToTexture.put("doorglasspanelsdoor_glasspanels_6", "purty_wood_@2X_ddddddd_color_12");
        materialToTexture.put("doorglasspanelsdoor_glasspanels_14", "purty_wood_@2X_ddddddd_color_12");
        materialToTexture.put("doormetal_gonds", "purty_wood_@2X_ddddddd_color_12");
        materialToTexture.put("doormetal_handle", "purty_wood_@2X_ddddddd_color_12");

        // МЕБЕЛЬ (elementHautSalon_GrisMat)
        materialToTexture.put("furniture", "elementHautSalon_GrisMat");
        materialToTexture.put("armchair", "elementHautSalon_GrisMat");
        materialToTexture.put("armchair3sofa", "elementHautSalon_GrisMat");
        materialToTexture.put("armchair3metall", "elementHautSalon_GrisMat");
        materialToTexture.put("metal_glass_tableplastic", "elementHautSalon_GrisMat");
        materialToTexture.put("metal_glass_tableglass", "elementHautSalon_GrisMat");
        materialToTexture.put("cafe_set_tonon_tablepaint", "elementHautSalon_GrisMat");
        materialToTexture.put("cafe_set_tonon_tablepaint_0", "elementHautSalon_GrisMat");
        materialToTexture.put("cafe_set_tonon_tablechrome", "elementHautSalon_GrisMat");
        materialToTexture.put("glass-dining-tableblack_paint", "elementHautSalon_GrisMat");
        materialToTexture.put("glass-dining-tabletabletop", "elementHautSalon_GrisMat");
        materialToTexture.put("chair3silla_co_telas_telas_gr_jpg", "elementHautSalon_GrisMat");
        materialToTexture.put("chair3metall", "elementHautSalon_GrisMat");
        materialToTexture.put("bedside", "elementHautSalon_GrisMat");
        materialToTexture.put("bedsideTablePlastic", "elementHautSalon_GrisMat");
        materialToTexture.put("bed", "elementHautSalon_GrisMat");
        materialToTexture.put("mattress", "elementHautSalon_GrisMat");
        materialToTexture.put("fabricleather_smooth_-_black", "elementHautSalon_GrisMat");
        materialToTexture.put("sofa", "elementHautSalon_GrisMat");
        materialToTexture.put("table", "elementHautSalon_GrisMat");
        materialToTexture.put("chair", "elementHautSalon_GrisMat");

        // ОКНА
        materialToTexture.put("window", "crisp_paper_ruffle_color_33");
        materialToTexture.put("window_stain_glasscage_out", "crisp_paper_ruffle_color_33");
        materialToTexture.put("window_stain_glasscage_in", "crisp_paper_ruffle_color_33");
        materialToTexture.put("window_stain_glassglass", "crisp_paper_ruffle_color_33");
        materialToTexture.put("window_stain_glassglass_0", "crisp_paper_ruffle_color_33");
        materialToTexture.put("window_4x5", "crisp_paper_ruffle_color_33");
        materialToTexture.put("window_4x5Cage_in", "crisp_paper_ruffle_color_33");
        materialToTexture.put("window_4x5Cage_out", "crisp_paper_ruffle_color_33");
        materialToTexture.put("window_4x5Frame_in", "crisp_paper_ruffle_color_33");
        materialToTexture.put("window_4x5Frame_out", "crisp_paper_ruffle_color_33");
        materialToTexture.put("window_4x5Glass", "crisp_paper_ruffle_color_33");
        materialToTexture.put("window_4x5Gonds", "crisp_paper_ruffle_color_33");
        materialToTexture.put("window_4x5Lock", "crisp_paper_ruffle_color_33");

        // КРЫША
        materialToTexture.put("roof", "crisp_paper_ruffle_color_33");
        materialToTexture.put("roof_by_pieces_field", "crisp_paper_ruffle_color_33");

        // ГАРАЖ
        materialToTexture.put("garage", "crisp_paper_ruffle_color_33");

        // ПЛИТКА
        materialToTexture.put("tile", "crisp_paper_ruffle_color_33");
        materialToTexture.put("m_tc_interior_tiles_01_main", "crisp_paper_ruffle_color_33");
        materialToTexture.put("m_tc_interior_tiles_01_main_0", "crisp_paper_ruffle_color_33");
        materialToTexture.put("m_tc_interior_tiles_02_main", "crisp_paper_ruffle_color_33");

        // ЛАМПЫ
        materialToTexture.put("lamp", "elementHautSalon_GrisMat");
        materialToTexture.put("lampe", "elementHautSalon_GrisMat");
        materialToTexture.put("lamp_svet", "elementHautSalon_GrisMat");
        materialToTexture.put("st3_r002__spotlight01styl_refl", "elementHautSalon_GrisMat");
        materialToTexture.put("body_plastik_white", "elementHautSalon_GrisMat");
        materialToTexture.put("glass__ceilingfanceilingfan6900_4_4", "elementHautSalon_GrisMat");
        materialToTexture.put("body__lampehalogeneglassblutint", "elementHautSalon_GrisMat");
        materialToTexture.put("lamp_living_roommetal_stand", "elementHautSalon_GrisMat");
        materialToTexture.put("lampeHalogeneglassblutint", "elementHautSalon_GrisMat");
        materialToTexture.put("spotlight01styl_lamp1", "elementHautSalon_GrisMat");

        // ВЕНТИЛЯТОРЫ
        materialToTexture.put("ceilingfan", "elementHautSalon_GrisMat");
        materialToTexture.put("ceilingFanCeilingFan5028_2_2", "elementHautSalon_GrisMat");
        materialToTexture.put("ceilingFanCeilingFan6820_3_3", "elementHautSalon_GrisMat");
        materialToTexture.put("ceilingFanCeilingFan6900_4_4", "elementHautSalon_GrisMat");
        materialToTexture.put("ceilingFanCeilingFan_1_1", "elementHautSalon_GrisMat");

        // ТЕХНИКА
        materialToTexture.put("fridge", "elementHautSalon_GrisMat");
        materialToTexture.put("stove", "elementHautSalon_GrisMat");
        materialToTexture.put("frigocube_1_1", "elementHautSalon_GrisMat");
        materialToTexture.put("frigonoir", "elementHautSalon_GrisMat");
        materialToTexture.put("frigoacier", "elementHautSalon_GrisMat");
        materialToTexture.put("frigoblancjaunasse", "elementHautSalon_GrisMat");
        materialToTexture.put("largestovecooker_iron", "elementHautSalon_GrisMat");
        materialToTexture.put("largestovecooker_black", "elementHautSalon_GrisMat");
        materialToTexture.put("largestovecooker_chrome", "elementHautSalon_GrisMat");
        materialToTexture.put("largestovecooker_fake_glass", "elementHautSalon_GrisMat");
        materialToTexture.put("largestoveburners", "elementHautSalon_GrisMat");
        materialToTexture.put("largestoveburners_top", "elementHautSalon_GrisMat");

        // ВАННА
        materialToTexture.put("shower", "crisp_paper_ruffle_color_33");
        materialToTexture.put("showershower_handle", "elementHautSalon_GrisMat");
        materialToTexture.put("shower_cabinglass", "crisp_paper_ruffle_color_33");

        // *** ИЗ ФАЙЛА текстуры.txt ***

        // РАСТЕНИЯ
        materialToTexture.put("flower", "leaf");
        materialToTexture.put("plant", "spatifilumList");
        materialToTexture.put("plant_01_plant", "spatifilumList");
        materialToTexture.put("mediumIndoorPlantMaterial", "spatifilumList");
        materialToTexture.put("spatifilumceramic", "spatifilumList");
        materialToTexture.put("spatifilumdirt", "spatifilumList");
        materialToTexture.put("leaves", "spatifilumList");
        materialToTexture.put("stems", "spatifilumList");

        // КАРТИНЫ
        materialToTexture.put("picture", "LakeMoraine");
        materialToTexture.put("picture.001", "LakeMoraine");
        materialToTexture.put("picture.002", "LakeMoraine");
        materialToTexture.put("picture.003", "LakeMoraine");
        materialToTexture.put("picture.004", "LakeMoraine");
        materialToTexture.put("picture.005", "LakeMoraine");
        materialToTexture.put("picture.006", "LakeMoraine");
        materialToTexture.put("picture.007", "LakeMoraine");
        materialToTexture.put("picture.008", "StatueOfLiberty");

        // КАРТИНЫ (из модели)
        materialToTexture.put("lakemoraineframematerial_001", "LakeMoraine");
        materialToTexture.put("statueoflibertyframematerial_001", "StatueOfLiberty");

        // ДОПОЛНИТЕЛЬНЫЕ МАТЕРИАЛЫ
        materialToTexture.put("material_68", "elementHautSalon_GrisMat");
        materialToTexture.put("material_69", "elementHautSalon_GrisMat");
        materialToTexture.put("material_70", "crisp_paper_ruffle_color_33");
        materialToTexture.put("material_71", "elementHautSalon_GrisMat");
        materialToTexture.put("material_72", "elementHautSalon_GrisMat");
    }

    public static String getTextureNameForMaterial(String materialName) {
        if (materialName == null) return null;

        String lowerName = materialName.toLowerCase()
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "");

        for (Map.Entry<String, String> entry : materialToTexture.entrySet()) {
            String key = entry.getKey().toLowerCase()
                    .replace(" ", "")
                    .replace("_", "")
                    .replace("-", "");
            if (lowerName.contains(key)) {
                return entry.getValue();
            }
        }

        return null;
    }
}