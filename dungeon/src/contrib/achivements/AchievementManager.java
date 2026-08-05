package contrib.achivements;

import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogType;
import contrib.systems.EventScheduler;
import core.Entity;
import core.utils.logging.DungeonLogger;
import java.util.List;
import java.util.Optional;

/**
 * Central API for unlocking achievements.
 *
 * <p>Use {@link #pop(String)} or {@link #popFor(Entity, String)} from game logic. The achievement
 * name is the id.
 */
public class AchievementManager {

  private static final long POPUP_DURATION_MS = 4500L;
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(AchievementManager.class);
  private static AchievementManager instance;

  private final AchievementStore store;

  /** Creates the achievement manager using the default asset and status paths. */
  public AchievementManager() {
    this(new AchievementStore());
  }

  AchievementManager(AchievementStore store) {
    this.store = store;
    instance = this;
  }

  /**
   * Returns the process-wide achievement manager instance.
   *
   * @return achievement manager
   */
  public static AchievementManager instance() {
    if (instance == null) {
      instance = new AchievementManager();
    }
    return instance;
  }

  /**
   * Registers the internal JSON asset that defines the achievements for this game.
   *
   * <p>The path is resolved with {@code Gdx.files.internal(path)} when achievements are first used.
   *
   * @param definitionPath internal asset path to the achievement definition JSON
   */
  public static void registerAchievements(String definitionPath) {
    instance().store.registerDefinitions(definitionPath);
  }

  /**
   * Checks if an achievement definition file is available.
   *
   * @return true if the registered achievement definition file exists and contains definitions
   */
  public static boolean isAvailable() {
    return instance().store.hasDefinitions();
  }

  /**
   * Returns all achievement definitions for the menu.
   *
   * @return achievement definitions
   */
  public static List<Achievement> menuAchievements() {
    return instance().store.achievementsForMenu();
  }

  /**
   * Checks whether an achievement is unlocked for the current local menu viewer.
   *
   * @param name achievement id/name
   * @return true if unlocked for the local menu viewer
   */
  public static boolean isUnlockedInMenu(String name) {
    return instance().store.isUnlockedForLocalPlayer(name);
  }

  /**
   * Unlocks an achievement according to its configured unlock scope.
   *
   * @param name achievement id/name
   */
  public void pop(String name) {
    popFor(null, name);
  }

  /**
   * Unlocks an achievement according to its configured unlock scope.
   *
   * @param player player entity
   * @param name achievement id/name
   */
  public void popFor(Entity player, String name) {
    Optional<Achievement> achievement = store.definition(name);
    if (achievement.isEmpty()) {
      LOGGER.warn("Achievement '{}' is not defined.", name);
      return;
    }
    if (achievement.get().unlocksForAll()) {
      unlockForAll(achievement.get());
    } else {
      unlockForPlayer(player, achievement.get());
    }
  }

  private void unlockForAll(Achievement achievement) {
    showPopup(achievement);
  }

  private void unlockForPlayer(Entity player, Achievement achievement) {
    if (player == null) {
      LOGGER.warn(
          "Achievement '{}' needs a triggering player but was triggered without one.",
          achievement.name());
      return;
    }
    showPopup(achievement, player.id());
  }

  /**
   * Records an unlock received through an achievement popup on this local client.
   *
   * @param name achievement id/name
   * @return true if this client recorded the achievement as newly unlocked
   */
  public static boolean markUnlockedFromPopup(String name) {
    return instance().recordPopupUnlock(name);
  }

  private boolean recordPopupUnlock(String name) {
    Optional<Achievement> achievement = store.definition(name);
    if (achievement.isEmpty()) {
      return false;
    }
    boolean newlyUnlocked = store.unlock(name);
    if (newlyUnlocked) {
      unlockPlatinumIfComplete(achievement.get());
    }
    return newlyUnlocked;
  }

  private void unlockPlatinumIfComplete(Achievement unlockedAchievement) {
    if (unlockedAchievement.platinum()) {
      return;
    }
    Optional<Achievement> platinumAchievement = store.platinumAchievement();
    if (platinumAchievement.isEmpty()) {
      return;
    }
    Achievement platinum = platinumAchievement.get();
    if (store.allNonPlatinumAchievementsUnlocked() && !store.isUnlocked(platinum.name())) {
      showPopup(platinum);
    }
  }

  private void showPopup(Achievement achievement, int... targetEntityIds) {
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.ACHIEVEMENT_POPUP)
            .center(false)
            .put(AchievementPopup.KEY_IMAGE_PATH, achievement.imagePath())
            .put(AchievementPopup.KEY_NAME, achievement.name())
            .put(AchievementPopup.KEY_DESCRIPTION, achievement.description())
            .put(AchievementPopup.KEY_NAME_KEY, achievement.nameKey())
            .put(AchievementPopup.KEY_DESCRIPTION_KEY, achievement.descriptionKey())
            .build();

    UIComponent ui = contrib.hud.dialogs.DialogFactory.show(context, false, false, targetEntityIds);
    EventScheduler.scheduleAction(() -> UIUtils.closeDialog(ui, true), POPUP_DURATION_MS);
  }
}
