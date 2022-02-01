package level.generator.LevelLoader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import level.elements.Level;
import level.generator.IGenerator;
import tools.Constants;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class LevelLoader implements IGenerator {

    @Override
    public Level getLevel() {
        FileHandle handle = Gdx.files.local(Constants.PATH_TO_LEVEL);
        FileHandle[] allLevelFiles = handle.list();
        FileHandle levelFile = allLevelFiles[new Random().nextInt(allLevelFiles.length)];
        Level level = loadLevel(levelFile.path());
        if (level == null) return getLevel();
        else return level;
    }

    private Level loadLevel(String path) {
        Type levelType = new TypeToken<Level>() {}.getType();
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(path, StandardCharsets.UTF_8));
            return new Gson().fromJson(reader, levelType);
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("File may be corrupted ");
            e.printStackTrace();
        }
        return null;
    }
}
