package contrib.level.generator.perlinNoise;

import core.level.TileLevel;
import core.level.elements.ILevel;
import core.level.generator.IGenerator;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;

import java.util.Random;

public class PerlinNoiseGenerator implements IGenerator {
    private static final Random GLOBAL_RANDOM = new Random();
    private static final int SMALL_MIN_X_SIZE = 30;
    private static final int SMALL_MIN_Y_SIZE = 30;
    private static final int SMALL_MAX_X_SIZE = 40;
    private static final int SMALL_MAX_Y_SIZE = 40;
    private static final int MEDIUM_MIN_X_SIZE = 40;
    private static final int MEDIUM_MIN_Y_SIZE = 40;
    private static final int MEDIUM_MAX_X_SIZE = 80;
    private static final int MEDIUM_MAX_Y_SIZE = 80;
    private static final int BIG_MIN_X_SIZE = 80;
    private static final int BIG_MIN_Y_SIZE = 80;
    private static final int BIG_MAX_X_SIZE = 150;
    private static final int BIG_MAX_Y_SIZE = 150;

    private static LevelElement[][] layout(final LevelSize size, final Random random) {
        final NoiseArea playingArea = generateNoiseArea(size, random);
        return toLevelElementArray(playingArea);
    }

    private static NoiseArea generateNoiseArea(final LevelSize size, final Random randomGenerator) {
        final int width = widthFromLevelSize(size, randomGenerator);
        final int height = heightFromLevelSize(size, randomGenerator);
        int octavesAdd = 0;
        switch (size) {
            case LARGE:
                octavesAdd += 1;
            case MEDIUM:
                octavesAdd += 1;
            case SMALL:
            default:
                octavesAdd += 1;
        }
        final PerlinNoise pNoise =
                new PerlinNoise(
                        width,
                        height,
                        new int[] {1 + octavesAdd, 2 + octavesAdd},
                        false,
                        randomGenerator);
        final double[][] noise = pNoise.noiseAll(1);

        final NoiseArea[] areas = NoiseArea.areas(new NoiseAreaValues(0.4, 0.6, noise, false));
        NoiseArea area = areas[0];
        for (final NoiseArea f : areas) {
            if (area.size() < f.size()) {
                area = f;
            }
        }
        return area;
    }

    private static LevelElement[][] toLevelElementArray(final NoiseArea playingArea) {
        LevelElement[][] res = new LevelElement[playingArea.width()][playingArea.height()];
        for (int i = 0; i < playingArea.width(); i++) {
            for (int j = 0; j < playingArea.height(); j++) {
                if (playingArea.contains(i, j)) {
                    res[i][j] = LevelElement.FLOOR;
                } else {
                    res[i][j] = LevelElement.SKIP;
                }
            }
        }
        return res;
    }

    private static int widthFromLevelSize(final LevelSize size, final Random random) {
        return switch (size) {
            case LARGE -> random.nextInt(BIG_MAX_X_SIZE - BIG_MIN_X_SIZE) + BIG_MIN_X_SIZE;
            case MEDIUM -> random.nextInt(MEDIUM_MAX_X_SIZE - MEDIUM_MIN_X_SIZE)
                    + MEDIUM_MIN_X_SIZE;
            default -> random.nextInt(SMALL_MAX_X_SIZE - SMALL_MIN_X_SIZE) + SMALL_MIN_X_SIZE;
        };
    }

    private static int heightFromLevelSize(final LevelSize size, final Random random) {
        return switch (size) {
            case LARGE -> random.nextInt(BIG_MAX_Y_SIZE - BIG_MIN_Y_SIZE) + BIG_MIN_Y_SIZE;
            case MEDIUM -> random.nextInt(MEDIUM_MAX_Y_SIZE - MEDIUM_MIN_Y_SIZE)
                    + MEDIUM_MIN_Y_SIZE;
            default -> random.nextInt(SMALL_MAX_Y_SIZE - SMALL_MIN_Y_SIZE) + SMALL_MIN_Y_SIZE;
        };
    }

    @Override
    public ILevel level(final DesignLabel designLabel, final LevelSize size) {
        return level(designLabel, size, GLOBAL_RANDOM);
    }

    @Override
    public LevelElement[][] layout(final LevelSize size) {
        return layout(size, new Random());
    }

    /**
     * Generates a new level based on the seed.
     *
     * <p>The same seed should give the same level.
     *
     * @param seed The seed of the level.
     * @return The level.
     */
    public ILevel level(final long seed) {
        final Random random = new Random(seed);
        DesignLabel designLabel = DesignLabel.values()[random.nextInt(DesignLabel.values().length)];
        LevelSize size = LevelSize.values()[random.nextInt(LevelSize.values().length)];
        return level(designLabel, size, random);
    }

    /**
     * Generates new Level.
     *
     * @param designLabel The design of the level.
     * @param size The level size.
     * @param random Random Object used to generate the level.
     * @return The generated level.
     */
    public ILevel level(final DesignLabel designLabel, final LevelSize size, final Random random) {
        // playing field
        LevelElement[][] elements = layout(size, random);
        return new TileLevel(elements, designLabel);
    }
}
