package level.generator.hamster;

import level.elements.ILevel;
import level.elements.TileLevel;
import level.generator.IGenerator;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import level.tools.LevelSize;

/**
 * Level generator used for the Hamster-Simulator implementation
 *
 * @author Maxim Fruendt
 */
public class HamsterGenerator implements IGenerator {

    private static final int SMALL_X_SIZE = 10;
    private static final int SMALL_Y_SIZE = 5;
    private static final int MEDIUM_X_SIZE = 20;
    private static final int MEDIUM_Y_SIZE = 10;
    private static final int BIG_X_SIZE = 30;
    private static final int BIG_Y_SIZE = 15;

    /**
     * Get a level with a random configuration.
     *
     * @return The level.
     */
    @Override
    public ILevel getLevel(DesignLabel designLabel, LevelSize size) {
        return new TileLevel(getLayout(size), designLabel);
    }

    /**
     * Generates the floor layout to a specified level size
     *
     * @param size size of the level to be generated
     * @return layout of the level
     */
    @Override
    public LevelElement[][] getLayout(LevelSize size) {

        int sizeX, sizeY;

        switch (size) {
            case SMALL -> {
                sizeX = SMALL_X_SIZE;
                sizeY = SMALL_Y_SIZE;
            }
            case LARGE -> {
                sizeX = BIG_X_SIZE;
                sizeY = BIG_Y_SIZE;
            }
            default -> {
                sizeX = MEDIUM_X_SIZE;
                sizeY = MEDIUM_Y_SIZE;
            }
        }

        LevelElement[][] layout = new LevelElement[sizeY][sizeX];

        // Create a rectangular level without anything in it
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                layout[y][x] = LevelElement.FLOOR;
            }
        }

        return layout;
    }
}
