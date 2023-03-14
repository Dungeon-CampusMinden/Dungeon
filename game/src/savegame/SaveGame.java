package savegame;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import ecs.entities.Entity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import starter.Game;

public class SaveGame {

    private static final JsonValue.PrettyPrintSettings PRETTY_PRINT_SETTINGS =
            new JsonValue.PrettyPrintSettings();

    static {
        PRETTY_PRINT_SETTINGS.outputType = JsonWriter.OutputType.json;
        PRETTY_PRINT_SETTINGS.wrapNumericArrays = true;
        PRETTY_PRINT_SETTINGS.singleLineColumns = 0;
    }

    /**
     * Gather all entities and their components
     *
     * @return JsonValue containing all entities and their components
     */
    private static JsonValue getEntityData() {

        JsonValue entities = new JsonValue(JsonValue.ValueType.array);

        for (Entity entity : Game.entities) {
            JsonValue json = new JsonValue(JsonValue.ValueType.object);
            json.addChild("class", new JsonValue(entity.getClass().getName()));
            JsonValue components = new JsonValue(JsonValue.ValueType.array);
            entity.getComponents().stream()
                    .map(GameSerialization::serialize)
                    .forEach(components::addChild);
            json.addChild("components", components);
            entities.addChild(json);
        }

        return entities;
    }

    /**
     * Save the current game state to a file
     *
     * @throws IOException If the file could not be written
     */
    public static void save() throws IOException {
        System.out.println(getEntityData().toJson(JsonWriter.OutputType.json));

        JsonValue root = new JsonValue(JsonValue.ValueType.object);
        root.addChild("entities", getEntityData());
        String jsonString = root.prettyPrint(PRETTY_PRINT_SETTINGS);

        File file = new File("savegame.json");
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file, false);
        fos.write(jsonString.getBytes(StandardCharsets.UTF_8));
        fos.close();
    }
}
