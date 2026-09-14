package rooms.programming;

import engine.Entity;
import engine.Game;
import engine.language.Language;
import feature.achievements.AchievementManager;
import feature.achievements.AchievementPopup;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import java.util.Locale;

/** Room achievements; recipient scope and persistent unlocks use the shared achievement system. */
public enum ProgrammingAchievements {
  FIRST_RUNE,
  ARCHIVIST,
  BOUND,
  WRONG_TYPE,
  OVERWRITE,
  SLEEPY,
  LIGHTS_OUT,
  OBSERVER,
  FIRST_ROUTE,
  SECOND_TRY,
  SPIN,
  CELLAR_CLEAR,
  CLEAN_RUN,
  PLATINUM;

  /** Registers room definitions for the menu, client and server. */
  public static void register() {
    AchievementManager.registerAchievements(
        "achievements/programming.json", "programming-achievement-unlock.json");
    DialogFactory.register(
        DialogType.DefaultTypes.ACHIEVEMENT_POPUP,
        context -> AchievementPopup.build(context, false));
    registerTranslations();
  }

  private static void registerTranslations() {
    Game.localization().registerTranslationFile(Language.DE, "language/programming/de.json");
    Game.localization().registerTranslationFile(Language.EN, "language/programming/en.json");
  }

  /** Unlocks a shared progress achievement from authoritative game logic. */
  public void unlock() {
    if (!Game.isMultiplayerClient()) AchievementManager.instance().pop(id());
  }

  /**
   * Unlocks an achievement for an actual player action; JSON defines its recipient scope.
   *
   * @param player player who performed the action
   */
  public void unlock(Entity player) {
    if (!Game.isMultiplayerClient()) AchievementManager.instance().popFor(player, id());
  }

  /**
   * @return namespaced definition and localization key
   */
  public String id() {
    return "programming_" + name().toLowerCase(Locale.ROOT);
  }
}
