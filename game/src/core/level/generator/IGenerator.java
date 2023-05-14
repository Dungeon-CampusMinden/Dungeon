package core.level.generator;

import core.level.elements.ILevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;

public interface IGenerator {
    /**
     * Get a level with the given configuration.
     *
     * @param designLabel Design of the level
     * @param size Size of the level
     * @return The level
     */
    ILevel getLevel(DesignLabel designLabel, LevelSize size);

    /**
     * Get a level with a random configuration.
     *
     * @return The level.
     */
    default ILevel getLevel() {
        return getLevel(DesignLabel.randomDesign(), LevelSize.randomSize());
    }

    /**
     * Get a level with the given configuration and a random size.
     *
     * @param designLabel Design of the level
     * @return The level
     */
    default ILevel getLevel(DesignLabel designLabel) {
        return getLevel(designLabel, LevelSize.randomSize());
    }

    /**
     * Get a level with the given configuration and a random design.
     *
     * @param size Size of the level
     * @return The level
     */
    default ILevel getLevel(LevelSize size) {
        return getLevel(DesignLabel.randomDesign(), size);
    }

    /**
     * Get a level layout with the given configuration.
     *
     * @param size Size of the level
     * @return The layout
     */
    LevelElement[][] getLayout(LevelSize size);
}
