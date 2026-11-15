package rooms.morphco.level;

import engine.Entity;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import rooms.morphco.ui.MorphCoDialogs;

/** Opens the books of the factory for one player. */
final class MorphCoDocuments {

  private MorphCoDocuments() {}

  /**
   * Opens a document.
   *
   * @param player reading player
   * @param document document ID, see {@link MorphCoDialogs#HANDBOOK}
   */
  static void show(Entity player, String document) {
    DialogFactory.show(
        DialogContext.builder()
            .type(MorphCoDialogs.DOCUMENT)
            .put(MorphCoDialogs.DOCUMENT_ID, document)
            .build(),
        player.id());
  }
}
