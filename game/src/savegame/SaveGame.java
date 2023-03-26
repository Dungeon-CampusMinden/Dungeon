package savegame;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import controller.AbstractController;
import controller.SystemController;
import ecs.components.Component;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.systems.ECS_System;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import level.LevelAPI;
import level.elements.ILevel;
import level.elements.TileLevel;
import level.elements.tile.Tile;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import starter.Game;

public class SaveGame {

    private static final String FILE_VERSION = "0.0.1";

    public static final String GAME_DIR_NAME = "PMDungeon";
    public static final String PATH_GAME_DIR;
    public static final String PATH_SAVE_DIR;

    private static final JsonValue.PrettyPrintSettings PRETTY_PRINT_SETTINGS =
            new JsonValue.PrettyPrintSettings();

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    static {
        PRETTY_PRINT_SETTINGS.outputType = JsonWriter.OutputType.json;
        PRETTY_PRINT_SETTINGS.wrapNumericArrays = true;
        PRETTY_PRINT_SETTINGS.singleLineColumns = 0;

        // Check if windows
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            PATH_GAME_DIR = System.getenv("APPDATA") + File.separator + GAME_DIR_NAME;
        } else {
            PATH_GAME_DIR = System.getProperty("user.home") + File.separator + GAME_DIR_NAME;
        }
        PATH_SAVE_DIR = PATH_GAME_DIR + File.separator + "saves";
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
     * Gather all level data
     *
     * @return JsonValue containing all level data
     */
    private static JsonValue getLevelData() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        JsonValue levelSizeJson = new JsonValue(JsonValue.ValueType.object);

        if (Game.currentLevel == null) {
            levelSizeJson.addChild("x", new JsonValue(0));
            levelSizeJson.addChild("y", new JsonValue(0));
            json.addChild("size", levelSizeJson);
            return json;
        }

        Tile[][] layout = Game.currentLevel.getLayout();
        levelSizeJson.addChild("x", new JsonValue(layout.length));
        levelSizeJson.addChild("y", new JsonValue(layout[0].length));
        json.addChild("size", levelSizeJson);

        JsonValue tiles = new JsonValue(JsonValue.ValueType.array);
        for (int x = 0; x < layout.length; x++) {
            for (int y = 0; y < layout[x].length; y++) {
                Tile tile = layout[x][y];
                if (tile == null) {
                    continue;
                }
                tiles.addChild(GameSerialization.serialize(tile));
            }
        }
        json.addChild("tiles", tiles);

        return json;
    }

    private static TileLevel loadLevelData(JsonValue data) {
        JsonValue size = data.get("size");
        LevelElement[][] layout = new LevelElement[size.getInt("y")][size.getInt("x")];
        JsonValue tiles = data.get("tiles");
        for (JsonValue tile : tiles) {
            JsonValue location = tile.get("location");
            int x = location.getInt("x"); // x and y are swapped in the json
            int y = location.getInt("y");
            layout[x][y] = LevelElement.valueOf(tile.getString("levelElement"));
        }
        return new TileLevel(layout, DesignLabel.DEFAULT);
    }

    private static HashSet<Entity> loadEntityData(JsonValue data) {
        HashSet<Entity> entities = new HashSet<>();
        for (JsonValue jsonEntity : data) {
            Entity entity = new Entity();
            entity.getComponents().stream()
                    .map(Component::getClass)
                    .forEach(entity::removeComponent);
            for (JsonValue jsonComponent : jsonEntity.get("components")) {
                entity.addComponent(GameSerialization.deserialize(jsonComponent, entity));
            }
            Game.entities.remove(entity); // Remove from Game.entities to avoid duplicates
            entities.add(entity);
        }
        return entities;
    }

    public static void save() {
        save(String.format("savegame_%s", DATE_FORMAT.format(new Date())));
    }

    /** Save the current game state to a file */
    public static void save(String filename) {
        createGameDir();

        JsonValue root = new JsonValue(JsonValue.ValueType.object);
        root.addChild("version", new JsonValue(FILE_VERSION));
        root.addChild("level", getLevelData());
        root.addChild("entities", getEntityData());
        String jsonString = root.prettyPrint(PRETTY_PRINT_SETTINGS);

        try {
            File file = createSaveFile(filename);
            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(jsonString.getBytes(StandardCharsets.UTF_8));
            fos.close();
            System.out.println("Saved game to " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not save game!", e);
        }
    }

    /** Load latest savegame */
    public static void load() {
        File saveDir = new File(PATH_SAVE_DIR);
        File[] files = saveDir.listFiles();
        assert files != null;
        Arrays.sort(files, (o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));
        if (files.length > 0) {
            load(files[0].getName());
        } else {
            throw new RuntimeException("No savegame found!");
        }
    }

    /**
     * Load specific savegame
     *
     * @param filename name of the savegame file
     */
    public static void load(String filename) {
        System.out.println("Loading savegame " + filename);

        JsonValue root;

        try {
            FileInputStream fis = new FileInputStream(PATH_SAVE_DIR + File.separator + filename);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            JsonReader jsonReader = new JsonReader();
            root = jsonReader.parse(isr);
            isr.close();
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not load savegame!", e);
        }

        if (root == null) {
            throw new RuntimeException("Could not load savegame!");
        }
        if (root.has("version") && !root.getString("version").equals(FILE_VERSION)) {
            throw new RuntimeException("Savegame version does not match!");
        }

        new Thread(
                        () -> {
                            Game.systems.forEach(ECS_System::toggleRun);
                            try {
                                Thread.sleep(
                                        10); // Wait a few milliseconds so the game can finish the
                                // current tick
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            Game.entities = new HashSet<>();
                            Game.systems = new SystemController();
                            disableAllSystems();

                            ILevel level = loadLevelData(root.get("level"));

                            // Set current level without triggering onLevelLoad()
                            LevelAPI levelAPI =
                                    Reflections.getFieldValue(Game.instance, "levelAPI");
                            Reflections.setFieldValue(levelAPI, "currentLevel", level);

                            HashSet<Entity> entities = loadEntityData(root.get("entities"));

                            // Find Hero in entities and set it as Game.hero
                            entities.stream()
                                    .filter(e -> e instanceof Hero)
                                    .map(Hero.class::cast)
                                    .findFirst()
                                    .ifPresent(h -> Game.hero = h);

                            // Set entities.
                            Game.entities = entities;
                            Reflections.setFieldValue(
                                    Game.instance,
                                    "heroPositionComponent",
                                    Game.hero.getComponent(PositionComponent.class).get());

                            Reflections.callVoidMethod(Game.instance, "setupSystems");
                            List<AbstractController<?>> controllers =  Reflections.getFieldValue(Game.instance, "controller");
                            controllers.clear();
                            controllers.add(Game.systems);
                            controllers.add(Reflections.getFieldValue(Game.instance, "pauseMenu"));

                            System.out.println("Loaded savegame " + filename);
                        },
                        "SaveGameLoad")
                .start();
    }

    private static File createSaveFile(String filename) {
        File file = new File(PATH_SAVE_DIR + File.separator + filename + ".json");
        int c = 1;
        while (file.exists() && c < 100) {
            file = new File(PATH_SAVE_DIR + File.separator + filename + "_" + c + ".json");
            c++;
        }
        return file;
    }

    private static void createGameDir() {
        File dir = new File(PATH_GAME_DIR);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Could not create game directory!");
            }
        }

        File savegameDir = new File(PATH_SAVE_DIR);
        if (!savegameDir.exists()) {
            if (!savegameDir.mkdirs()) {
                throw new RuntimeException("Could not create savegame directory!");
            }
        }
    }

    private static void disableAllSystems() {
        Game.systems.forEach(system -> {
            if (system != null) {
                if((boolean) Reflections.getFieldValue(system, "run")) {
                    system.toggleRun();
                }
            }
        });
    }

    private static void enableAllSystems() {
        Game.systems.forEach(system -> {
            if (system != null) {
                if (!(boolean) Reflections.getFieldValue(system, "run")) {
                    system.toggleRun();
                }
            }
        });
    }

}
