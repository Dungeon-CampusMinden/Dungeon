package level.tools;

import java.util.List;
import java.util.Random;

/**
 * Specifies which textures and layouts should be used for the room.
 *
 * @author Andre Matutat
 */
public enum DesignLabel {
    DEFAULT;

    private static final List<DesignLabel> VALUES = List.of(values());
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    /**
     * @return A random enum-value
     */
    public static DesignLabel randomDesign() {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}
