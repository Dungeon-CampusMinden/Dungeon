package contrib.achivements;

import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogType;
import contrib.systems.EventScheduler;
import core.Entity;
import core.System;
import core.components.PlayerComponent;
import core.utils.logging.DungeonLogger;
import java.util.List;
import java.util.Optional;

/**
 * Central API for unlocking achievements.
 *
 * <p>Use {@link #pop(String)} or {@link #popFor(Entity, String)} from game logic. The achievement
 * name is the id.
 */
public class AchievementSystem extends System {

  private static final long POPUP_DURATION_MS = 4500L;
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(AchievementSystem.class);
  private static AchievementSystem instance;

  private final AchievementStore store;

  /** Creates the achievement system using the default asset and status paths. */
  public AchievementSystem() {
    this(new AchievementStore());
  }

  AchievementSystem(AchievementStore store) {
    super(AuthoritativeSide.SERVER);
    this.store = store;
    instance = this;
  }

  /**
   * Returns the process-wide achievement system instance.
   *
   * @return achievement system
   */
  public static AchievementSystem instance() {
    if (instance == null) {
      instance = new AchievementSystem();
    }
    return instance;
  }

  /**
   * Checks if an achievement definition file is available.
   *
   * @return true if achievement.json exists and contains definitions
   */
  public static boolean isAvailable() {
    return instance().store.hasDefinitions();
  }

  /**
   * Returns the menu view of all achievements.
   *
   * @return achievements with current aggregate unlock state
   */
  public static List<Achievement> menuAchievements() {
    return instance().store.achievementsForMenu();
  }

  /**
   * Unlocks an achievement globally for all current players.
   *
   * @param name achievement id/name
   */
  public void pop(String name) {
    popForAll(name);
  }

  /**
   * Unlocks an achievement globally for all current players.
   *
   * @param name achievement id/name
   */
  public void popForAll(String name) {
    Optional<Achievement> achievement = store.definition(name);
    if (achievement.isEmpty()) {
      LOGGER.warn("Achievement '{}' is not defined.", name);
      return;
    }
    if (!store.unlockGlobally(name)) {
      return;
    }
    showPopup(achievement.get());
  }

  /**
   * Unlocks an achievement only for the player who triggered the action.
   *
   * @param player player entity
   * @param name achievement id/name
   */
  public void popFor(Entity player, String name) {
    if (player == null) {
      popForAll(name);
      return;
    }
    Optional<Achievement> achievement = store.definition(name);
    if (achievement.isEmpty()) {
      LOGGER.warn("Achievement '{}' is not defined.", name);
      return;
    }
    PlayerComponent playerComponent = player.fetch(PlayerComponent.class).orElse(null);
    if (!store.unlockForPlayer(playerComponent, name)) {
      return;
    }
    showPopup(achievement.get(), player.id());
  }

  private void showPopup(Achievement achievement, int... targetEntityIds) {
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.ACHIEVEMENT_POPUP)
            .center(false)
            .put(AchievementPopup.KEY_IMAGE_PATH, achievement.imagePath())
            .put(AchievementPopup.KEY_NAME, achievement.name())
            .put(AchievementPopup.KEY_DESCRIPTION, achievement.neschreibung())
            .build();

    UIComponent ui = contrib.hud.dialogs.DialogFactory.show(context, false, false, targetEntityIds);
    EventScheduler.scheduleAction(() -> UIUtils.closeDialog(ui, true), POPUP_DURATION_MS);
  }

  @Override
  public void execute() {
    // Unlocking is event-driven through the public API.
  }
}
