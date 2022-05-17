package level.tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Specifies which textures and layouts should be used for the room.
 *
 * @author Andre Matutat
 */
public enum DesignLabel {
    DEFAULT,
    ICE,
    ALL;

    private static final List<DesignLabel> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size() - 1;
    private static final Random RANDOM = new Random();

    public static DesignLabel randomDesign() {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}
