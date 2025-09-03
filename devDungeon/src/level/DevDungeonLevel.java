package level;

import contrib.hud.DialogUtils;
import contrib.utils.components.skill.damageSkill.projectile.TPBallSkill;
import contrib.utils.level.ITickable;
import core.level.DungeonLevel;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.loader.DungeonLoader;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Represents a level in the DevDungeon game. This class extends the */
public abstract class DevDungeonLevel extends DungeonLevel {
  protected static final Random RANDOM = new Random();
  protected String description;
  private final List<Coordinate> tpTargets = new ArrayList<>();

  /**
   * Constructs a new DevDungeonLevel with the given layout, design label, and custom points.
   *
   * @param layout The layout of the level, represented as a 2D array of LevelElements.
   * @param designLabel The design label of the level.
   * @param customPoints A list of custom points to be added to the level.
   * @param levelName The name of the level. (can be empty)
   * @param description The description of the level. (only set if levelName is not empty)
   */
  public DevDungeonLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      List<Coordinate> customPoints,
      String levelName,
      String description) {
    super(layout, designLabel, customPoints, levelName);
    this.description = description;
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      DialogUtils.showTextPopup(
          description,
          "Level " + DungeonLoader.currentLevelIndex() + ": " + levelName,
          () -> {
            // Workaround for tutorial popup
            if (levelName.equalsIgnoreCase("tutorial")) {
              onFirstTick();
            }
          });
      endTiles().forEach(ExitTile::close); // close exit at start (to force defeating the boss)
      doorTiles().forEach(DoorTile::close);
      pitTiles()
          .forEach(
              pit -> {
                pit.timeToOpen(50L * ILevel.RANDOM.nextInt(1, 5));
                pit.close();
              });

      if (!levelName.equalsIgnoreCase("tutorial")) {
        onFirstTick();
      }
    }
    onTick();
  }

  /**
   * Called when the level is first ticked.
   *
   * @see #onTick()
   * @see ITickable
   */
  protected abstract void onFirstTick();

  /**
   * Called when the level is ticked.
   *
   * @see #onFirstTick()
   * @see ITickable
   */
  protected abstract void onTick();

  /**
   * Adds a new teleport target to the list.
   *
   * <p>The teleport target is a point where the {@link TPBallSkill TPBallSkill} will teleport the
   * entity to if it hits an entity.
   *
   * @param points The teleport target to be added. Multiple points can be added at once.
   * @see TPBallSkill TPBallSkill
   */
  public void addTPTarget(Coordinate... points) {
    tpTargets.addAll(List.of(points));
  }

  /**
   * Removes a teleport target from the list.
   *
   * <p>The teleport target is a point where the {@link TPBallSkill TPBallSkill} will teleport the
   * entity to if it hits an entity.
   *
   * @param point The teleport target to be removed. Multiple points can be removed at once.
   * @see TPBallSkill TPBallSkill
   */
  public void removeTPTarget(Coordinate... point) {
    tpTargets.removeAll(List.of(point));
  }

  /**
   * Gets a random teleport target from the list.
   *
   * <p>The teleport target is a point where the {@link TPBallSkill TPBallSkill} will teleport the
   * entity to if it hits an entity.
   *
   * @return A random teleport target from the list. If the list is empty, null is returned.
   */
  public Coordinate randomTPTarget() {
    if (tpTargets.isEmpty()) return null;
    return tpTargets.get(RANDOM.nextInt(tpTargets.size()));
  }
}
