package core.level.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Specifies which textures and layouts should be used for the room. */
public enum DesignLabel {
    DEFAULT(50), // 50% chance
    FIRE(10), // 10% chance
    FOREST(9), // 9% chance
    ICE(10), // 10% chance
    TEMPLE(10), // 10% chance
    DARK(10), // 10% chance
    RAINBOW(1); // 1% chance

    private final int chance;
    private static final Random RANDOM = new Random();
    private static final List<DesignLabel> VALUES = new ArrayList<>();

    static {
        VALUES.addAll(Arrays.asList(values()));
    }

    /**
     * Create a new label.
     *
     * @param chance chance in % that this label will be returned by the {@link #randomDesign()}
     *     function.
     */
    DesignLabel(int chance) {
        this.chance = chance;
    }

    /**
     * @return A random enum-value based on chances
     */
    public static DesignLabel randomDesign() {
        int totalChances = VALUES.stream().mapToInt(label -> label.chance).sum();
        int randomValue = RANDOM.nextInt(totalChances);
        int cumulativeChances = 0;
        for (DesignLabel label : VALUES) {
            cumulativeChances += label.chance;
            if (randomValue < cumulativeChances) {
                return label;
            }
        }
        // error case
        return DEFAULT;
    }
}
