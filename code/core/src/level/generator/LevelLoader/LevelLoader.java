package level.generator.LevelLoader;

import java.io.File;
import java.util.Random;
import level.elements.Level;
import level.generator.IGenerator;
import tools.Constants;

public class LevelLoader implements IGenerator {

    /**
     * Load a level from a json
     *
     * @return loaded level
     */
    @Override
    public Level getLevel() {
        File dir = new File(Constants.getPathToLevel());
        File[] allLevelFiles = dir.listFiles();
        assert (allLevelFiles != null && allLevelFiles.length > 0);
        File levelFile = allLevelFiles[new Random().nextInt(allLevelFiles.length)];
        return Level.getLevel(levelFile);
    }
}
