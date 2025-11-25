package contrib.modules.interaction;

import contrib.hud.DialogUtils;

public interface IInteractable {
  default Interaction look() {
    return new Interaction(
        (entity, who) -> {
          DialogUtils.showTextPopup(
              "Wow. Ein echtes Wunder der Durchschnittlichkeit.", "Untersuchen");
        });
  }

  default Interaction interact() {
    return new Interaction(
        (entity, who) -> {
          DialogUtils.showTextPopup(
              "Ich drücke, ziehe und tippe… aber es passiert absolut gar nichts.", "Interagieren");
        });
  }

  default Interaction push() {
    return new Interaction(
        (entity, who) -> {
          DialogUtils.showTextPopup(
              "Ich würde es ja schieben… aber mein Rücken hat heute frei.", "Schieben");
        });
  }

  default Interaction talk() {
    return new Interaction(
        (entity, who) -> {
          DialogUtils.showTextPopup(
              "Er antwortet nicht. Vielleicht will er nicht. Vielleicht kann er nicht. Vielleicht hat er mich einfach nicht gern.",
              "Sprechen");
        });
  }

  default Interaction usewithitem() {
    // TOOD check Item if we have something like quipped Item
    return new Interaction(
        (entity, who) -> {
          DialogUtils.showTextPopup(
              "Diese Dinge passen ungefähr so gut zusammen wie Socken und Sandalen.", "Benutzen");
        });
  }

  default Interaction attack() {
    return new Interaction(
        (entity, who) -> {
          DialogUtils.showTextPopup(
              "Ich könnte es angreifen… aber dann hätte ich wieder Papierkram.", "Angreifen");
        });
  }
}
