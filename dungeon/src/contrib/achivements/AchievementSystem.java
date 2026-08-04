package contrib.achivements;

import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogType;
import contrib.systems.EventScheduler;
import core.Entity;
import core.Game;
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
   * @return achievements with current local player unlock state
   */
  public static List<Achievement> menuAchievements() {
    return instance().store.achievementsForMenu();
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
      unlockForAll(achievement.get(), player);
    } else {
      unlockForPlayer(player, achievement.get());
    }
  }

  private void unlockForAll(Achievement achievement, Entity triggeringPlayer) {
    if (!store.unlockGlobally(achievement.name())) {
      return;
    }
    showPopup(achievement, true);
    unlockPlatinumIfComplete(achievement, triggeringPlayer);
  }

  private void unlockForPlayer(Entity player, Achievement achievement) {
    if (player == null) {
      LOGGER.warn(
          "Achievement '{}' needs a triggering player but was triggered without one.",
          achievement.name());
      return;
    }
    PlayerComponent playerComponent = player.fetch(PlayerComponent.class).orElse(null);
    if (!store.unlockForPlayer(playerComponent, achievement.name())) {
      return;
    }
    showPopup(achievement, false, player.id());
    unlockPlatinumIfComplete(achievement, player);
  }

  /**
   * Records an unlock received through an achievement popup on this local client.
   *
   * @param name achievement id/name
   * @param global whether the unlock applies globally
   */
  public static void markUnlockedFromPopup(String name, boolean global) {
    instance().recordPopupUnlock(name, global);
  }

  private void recordPopupUnlock(String name, boolean global) {
    if (store.definition(name).isEmpty()) {
      return;
    }
    if (global) {
      store.unlockGlobally(name);
      return;
    }
    PlayerComponent playerComponent =
        Game.player().flatMap(player -> player.fetch(PlayerComponent.class)).orElse(null);
    store.unlockForPlayer(playerComponent, name);
  }

  private void unlockPlatinumIfComplete(Achievement unlockedAchievement, Entity triggeringPlayer) {
    if (unlockedAchievement.platinum()) {
      return;
    }
    Optional<Achievement> platinumAchievement = store.platinumAchievement();
    if (platinumAchievement.isEmpty()) {
      return;
    }
    Achievement platinum = platinumAchievement.get();
    if (platinum.unlocksForAll()) {
      PlayerComponent playerComponent = playerComponent(triggeringPlayer).orElse(null);
      if (store.allNonPlatinumAchievementsUnlocked(playerComponent)
          && store.unlockGlobally(platinum.name())) {
        showPopup(platinum, true);
      }
      return;
    }
    if (unlockedAchievement.unlocksForAll()) {
      Game.allPlayers().forEach(player -> unlockPlatinumForPlayer(player, platinum));
    } else {
      unlockPlatinumForPlayer(triggeringPlayer, platinum);
    }
  }

  private void unlockPlatinumForPlayer(Entity player, Achievement platinum) {
    if (player == null) {
      return;
    }
    PlayerComponent playerComponent = playerComponent(player).orElse(null);
    if (store.allNonPlatinumAchievementsUnlocked(playerComponent)
        && store.unlockForPlayer(playerComponent, platinum.name())) {
      showPopup(platinum, false, player.id());
    }
  }

  private Optional<PlayerComponent> playerComponent(Entity player) {
    if (player == null) {
      return Optional.empty();
    }
    return player.fetch(PlayerComponent.class);
  }

  private void showPopup(Achievement achievement, boolean global, int... targetEntityIds) {
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.ACHIEVEMENT_POPUP)
            .center(false)
            .put(AchievementPopup.KEY_IMAGE_PATH, achievement.imagePath())
            .put(AchievementPopup.KEY_NAME, achievement.name())
            .put(AchievementPopup.KEY_DESCRIPTION, achievement.neschreibung())
            .put(AchievementPopup.KEY_NAME_KEY, achievement.nameKey())
            .put(AchievementPopup.KEY_DESCRIPTION_KEY, achievement.descriptionKey())
            .put(AchievementPopup.KEY_GLOBAL, global)
            .build();

    UIComponent ui = contrib.hud.dialogs.DialogFactory.show(context, false, false, targetEntityIds);
    EventScheduler.scheduleAction(() -> UIUtils.closeDialog(ui, true), POPUP_DURATION_MS);
  }

  @Override
  public void execute() {
    // Unlocking is event-driven through the public API.
  }
}
