package contrib.achievements;

import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
import contrib.systems.EventScheduler;
import core.Entity;
import core.utils.logging.DungeonLogger;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Central API for unlocking achievements.
 *
 * <p>Use {@link #pop(String)} or {@link #popFor(Entity, String)} from game logic. The achievement
 * id is the stable unlock key. The JSON definition is authoritative for the unlock recipients: game
 * code may pass the acting player with {@code popFor}, but {@code unlockForAll} decides whether the
 * achievement is sent to all players or only to that player.
 */
public class AchievementManager {

  private static final long POPUP_DURATION_MS = 4500L;
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(AchievementManager.class);
  private static AchievementManager instance;

  private AchievementStore store;

  private AchievementManager() {}

  /**
   * Creates the achievement manager using explicit asset and status paths.
   *
   * @param definitionPath internal asset path to the achievement definition JSON
   * @param statusPath runtime JSON file used to persist unlocked achievements
   */
  public AchievementManager(String definitionPath, String statusPath) {
    configure(definitionPath, statusPath);
    instance = this;
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
   * Registers the internal JSON asset that defines the achievements and the runtime status file for
   * this game.
   *
   * <p>The definition path is resolved with {@code Gdx.files.internal(path)} when achievements are
   * first used. The status path is resolved relative to the current working directory when it is a
   * relative path.
   *
   * @param definitionPath internal asset path to the achievement definition JSON
   * @param statusPath runtime JSON file used to persist unlocked achievements
   */
  public static void registerAchievements(String definitionPath, String statusPath) {
    instance().configure(definitionPath, statusPath);
  }

  private void configure(String definitionPath, String statusPath) {
    store =
        new AchievementStore(
            normalizeDefinitionPath(definitionPath), Path.of(normalizeStatusPath(statusPath)));
  }

  private String normalizeDefinitionPath(String definitionPath) {
    if (definitionPath == null || definitionPath.isBlank()) {
      throw new IllegalArgumentException("achievement definition path must not be blank");
    }
    return definitionPath.trim();
  }

  private String normalizeStatusPath(String statusPath) {
    if (statusPath == null || statusPath.isBlank()) {
      throw new IllegalArgumentException("achievement status path must not be blank");
    }
    return statusPath.trim();
  }

  /**
   * Checks if an achievement definition file is available.
   *
   * @return true if the registered achievement definition file exists and contains definitions
   */
  public static boolean isAvailable() {
    AchievementStore store = instance().store;
    return store != null && store.hasDefinitions();
  }

  /**
   * Returns all achievement definitions for the menu.
   *
   * @return achievement definitions
   */
  public static List<Achievement> menuAchievements() {
    AchievementStore store = instance().store;
    if (store == null) {
      return Collections.emptyList();
    }
    return store.achievementsForMenu();
  }

  /**
   * Checks whether an achievement is unlocked for the current local menu viewer.
   *
   * @param id achievement id
   * @return true if unlocked for the local menu viewer
   */
  public static boolean isUnlockedInMenu(String id) {
    AchievementStore store = instance().store;
    return store != null && store.isUnlocked(id);
  }

  /**
   * Unlocks an achievement according to its configured recipient.
   *
   * <p>Use this only for achievements that do not have a meaningful acting player. If the JSON
   * definition is changed to a player-scoped achievement, this call cannot infer the missing player
   * and will log a warning instead of unlocking it.
   *
   * @param id achievement id
   */
  public void pop(String id) {
    popFor(null, id);
  }

  /**
   * Unlocks an achievement according to its configured recipient.
   *
   * <p>The given player is the actor, not a hard-coded recipient. The JSON definition remains the
   * source of truth: globally scoped achievements are still unlocked for all players, and
   * player-scoped achievements are unlocked for this player.
   *
   * @param player player entity
   * @param id achievement id
   */
  public void popFor(Entity player, String id) {
    if (store == null) {
      LOGGER.warn(
          "Achievement '{}' cannot be triggered because achievements are not configured.", id);
      return;
    }
    Optional<Achievement> achievement = store.definition(id);
    if (achievement.isEmpty()) {
      LOGGER.warn("Achievement '{}' is not defined.", id);
      return;
    }
    Achievement definition = achievement.get();
    if (definition.unlocksForAll()) {
      showPopup(definition);
      return;
    }
    if (player == null) {
      LOGGER.warn(
          "Achievement '{}' needs a triggering player but was triggered without one.",
          definition.id());
      return;
    }
    showPopup(definition, player.id());
  }

  /**
   * Records an unlock received through an achievement popup on this local client.
   *
   * @param id achievement id
   * @return true if this client recorded the achievement as newly unlocked
   */
  public static boolean markUnlockedFromPopup(String id) {
    return instance().recordPopupUnlock(id);
  }

  private boolean recordPopupUnlock(String id) {
    if (store == null) {
      return false;
    }
    Optional<Achievement> achievement = store.definition(id);
    if (achievement.isEmpty()) {
      return false;
    }
    boolean newlyUnlocked = store.unlock(id);
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
    if (store.allNonPlatinumAchievementsUnlocked() && !store.isUnlocked(platinum.id())) {
      showPopup(platinum);
    }
  }

  private void showPopup(Achievement achievement, int... targetEntityIds) {
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.ACHIEVEMENT_POPUP)
            .center(false)
            .put(AchievementPopup.KEY_IMAGE_PATH, achievement.imagePath())
            .put(AchievementPopup.KEY_ID, achievement.id())
            .build();

    UIComponent ui = DialogFactory.show(context, false, false, targetEntityIds);
    EventScheduler.scheduleAction(() -> UIUtils.closeDialog(ui, true), POPUP_DURATION_MS);
  }
}
