package contrib.modules.interaction;

import contrib.hud.DialogUtils;

/**
 * Interface that provides default implementations for each type of interaction used by the {@link
 * InteractionComponent}.
 *
 * <p>The default implementations display humorous, point-and-click-style popups.
 *
 * <p>Override the specific interaction methods to implement custom behavior.
 *
 * <p>Use {@link ISimpleIInteractable} if your entity only supports a single interaction.
 */
public interface IInteractable {

  /** The label used for the "look" interaction option. */
  String LOOK_LABEL = "Untersuchen";

  /** The label used for the general "interact" interaction option. */
  String INTERACT_LABEL = "Interagieren";

  /** The label used for the "talk" interaction option. */
  String TALK_LABEL = "Reden";

  /** The label used for the "take" or "pick up" interaction option. */
  String TAKE_LABEL = "Nehmen";

  /** The label used for the "use with item" interaction option. */
  String USE_WITH_LABEL = "Verwenden mit Item";

  /** The label used for the "attack" interaction option. */
  String ATTACK_LABEL = "Attack";

  /**
   * Default "look" interaction.
   *
   * <p>Shows a humorous message when the player examines the entity.
   */
  Interaction LOOK =
      new Interaction(
          (entity, who) -> {
            DialogUtils.showTextPopup(
                "Wow. Ein echtes Wunder der Durchschnittlichkeit.", LOOK_LABEL);
          },
          LOOK_LABEL);

  /**
   * Default "interact" action.
   *
   * <p>Represents a general interaction attempt, typically used when no specific interaction
   * behavior is defined.
   */
  Interaction INTERACT =
      new Interaction(
          (entity, who) -> {
            DialogUtils.showTextPopup(
                "Ich drücke, ziehe und tippe… aber es passiert absolut gar nichts.",
                INTERACT_LABEL);
          },
          INTERACT_LABEL);

  /**
   * Default "take" interaction.
   *
   * <p>Shows a humorous message when attempting to pick up the entity.
   */
  Interaction TAKE =
      new Interaction(
          (entity, who) -> {
            DialogUtils.showTextPopup(
                "Ich würde es mitnehmen… aber ich verliere solche Sachen sowieso ständig.",
                TAKE_LABEL);
          },
          TAKE_LABEL);

  /**
   * Default "talk" interaction.
   *
   * <p>Shows a humorous message when attempting to talk to the entity.
   */
  Interaction TALK =
      new Interaction(
          (entity, who) -> {
            DialogUtils.showTextPopup(
                "Er antwortet nicht. Vielleicht will er nicht. Vielleicht kann er nicht. "
                    + "Vielleicht hat er mich einfach nicht gern.",
                TALK_LABEL);
          },
          TALK_LABEL);

  /**
   * Default "use with item" interaction.
   *
   * <p>Triggered when the player tries to use an inventory item on the entity.
   */
  Interaction USE_WITH_ITEM =
      new Interaction(
          (entity, who) -> {
            DialogUtils.showTextPopup(
                "Diese Dinge passen ungefähr so gut zusammen wie Socken und Sandalen.",
                USE_WITH_LABEL);
          },
          USE_WITH_LABEL);

  /**
   * Default "attack" interaction.
   *
   * <p>Shows a humorous message when the player attempts to attack the entity.
   */
  Interaction ATTACK =
      new Interaction(
          (entity, who) -> {
            DialogUtils.showTextPopup(
                "Ich könnte es angreifen… aber dann hätte ich wieder Papierkram.", ATTACK_LABEL);
          },
          ATTACK_LABEL);

  /**
   * Returns the default look interaction.
   *
   * @return the LOOK interaction
   */
  default Interaction look() {
    return LOOK;
  }

  /**
   * Returns the default interaction behavior.
   *
   * @return the INTERACT interaction
   */
  default Interaction interact() {
    return INTERACT;
  }

  /**
   * Returns the default take interaction.
   *
   * @return the TAKE interaction
   */
  default Interaction take() {
    return TAKE;
  }

  /**
   * Returns the default talk interaction.
   *
   * @return the TALK interaction
   */
  default Interaction talk() {
    return TALK;
  }

  /**
   * Returns the default "use with item" interaction.
   *
   * @return the USE_WITH_ITEM interaction
   */
  default Interaction usewithitem() {
    // TODO check item if we have something like equipped items
    return USE_WITH_ITEM;
  }

  /**
   * Returns the default attack interaction.
   *
   * @return the ATTACK interaction
   */
  default Interaction attack() {
    return ATTACK;
  }
}
