package core.level.generator;

import core.level.elements.ILevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;

/** Generates levels. */
public interface IGenerator {
  /**
   * Get a level with the given configuration.
   *
   * @param designLabel Design of the level
   * @param size Size of the level
   * @return The level
   */
  ILevel level(DesignLabel designLabel, LevelSize size);

  /**
   * Get a level with a random configuration.
   *
   * @return The level.
   */
  default ILevel level() {
    return level(DesignLabel.randomDesign(), LevelSize.randomSize());
  }

  /**
   * Get a level with the given configuration and a random size.
   *
   * @param designLabel Design of the level
   * @return The level
   */
  default ILevel level(DesignLabel designLabel) {
    return level(designLabel, LevelSize.randomSize());
  }

  /**
   * Get a level with the given configuration and a random design.
   *
   * @param size Size of the level
   * @return The level
   */
  default ILevel level(LevelSize size) {
    return level(DesignLabel.randomDesign(), size);
  }

  /**
   * Get a level layout with the given configuration.
   *
   * @param size Size of the level
   * @return The layout
   */
  LevelElement[][] layout(LevelSize size);
}
