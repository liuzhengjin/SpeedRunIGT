package com.redlimerl.speedrunigt.timer.category;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimerUtils;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.impl.util.version.VersionPredicateParser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class CustomCategoryManager {

    private static Path getCategoryPath() {
        return SpeedRunIGT.getGlobalPath().resolve("categories");
    }

    public static void init() {
        File dir = getCategoryPath().toFile();
        dir.mkdirs();

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (!file.getName().endsWith(".json")) continue;

            try {
                JsonObject jsonObject = new JsonParser().parse(FileUtils.readFileToString(file, StandardCharsets.UTF_8)).getAsJsonObject();

                if (!VersionPredicateParser.parse(jsonObject.get("version").getAsString()).test(SemanticVersion.parse(InGameTimerUtils.getMinecraftVersion()))) {
                    SpeedRunIGT.error(String.format("Failed to add '%s' category file, it doesn't work for this version", file.getName()));
                    continue;
                }

                RunCategory runCategory = new RunCategory(jsonObject.get("id").getAsString(),
                        jsonObject.get("src_category").getAsString(),
                        jsonObject.get("name").getAsString(),
                        file.getName(),
                        jsonObject.get("conditions").getAsJsonArray());

                try {
                    RunCategory.registerCategory(runCategory);
                } catch (IllegalArgumentException e) {
                    InGameTimerUtils.setCategoryWarningScreen(file.getName(), new InvalidCategoryException(InvalidCategoryException.Reason.DUPLICATED_CATEGORY_ID, ""));
                }
            } catch (JsonParseException | IOException e) {
                InGameTimerUtils.setCategoryWarningScreen(file.getName(), new InvalidCategoryException(InvalidCategoryException.Reason.FAILED_JSON_PARSE, ""));
            } catch (Exception e) {
                InGameTimerUtils.setCategoryWarningScreen(file.getName(), new InvalidCategoryException(InvalidCategoryException.Reason.INVALID_JSON_DATA, "it need to check all require arguments are exists."));
            }
        }
    }
}
