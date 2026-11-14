package feature.interaction.cables;

import engine.Entity;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;

/**
 * Opens the dialog of a cable puzzle and checks the players' wiring on the server.
 *
 * <p>The puzzle belongs to an entity with a {@link CablesComponent}. The dialog is of the type
 * {@link DialogType.DefaultTypes#CABLES}: by default a {@link CablesUI}, a room may register its
 * own look for it. Every dialog plugs with a {@link CablePlugging}: it checks each plug right away,
 * sends {@link #WRONG_PLUG} for a cable in another cable's socket and {@link #CONNECT} once every
 * cable is in. The server checks both again.
 */
public final class Cables {

  /**
   * Callback key of the finished wiring. The payload is a {@link DialogResponseMessage.IntList}
   * with the index of the socket of every cable.
   */
  public static final String CONNECT = "cablesConnect";

  /**
   * Callback key of a cable put into a socket it does not belong in. The payload is a {@link
   * DialogResponseMessage.IntList} with the index of the cable and the index of the socket.
   */
  public static final String WRONG_PLUG = "cablesWrongPlug";

  /** Dialog attribute (a {@link Boolean}) whether the dialog shows all cables plugged in. */
  public static final String CONNECTED = "cablesConnected";

  private Cables() {}

  /**
   * Shows the cable puzzle of an entity to a player.
   *
   * <p>Once the player's wiring connects the puzzle, the component calls its reaction and the
   * dialogs other players have open for the same entity close. The player's own dialog stays open
   * and shows all cables plugged in.
   *
   * @param entity entity with a {@link CablesComponent}
   * @param player the player plugging the cables
   * @param title title of the dialog, may be a translation key; empty for the default title
   * @param help how to plug the cables, may be a translation key; empty for the default help
   * @return the dialog
   */
  public static UIComponent show(Entity entity, Entity player, String title, String help) {
    CablesComponent cables = entity.fetch(CablesComponent.class).orElseThrow();
    DialogContext.Builder context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.CABLES)
            .put(DialogContextKeys.ENTITY, entity.id())
            .put(DialogContextKeys.TITLE, title)
            .put(DialogContextKeys.DESCRIPTION, help)
            .put(CONNECTED, cables.connected());
    cables.puzzle().put(context);
    UIComponent ui = DialogFactory.show(context.build(), player.id());
    ui.registerCallback(
        CONNECT,
        payload -> {
          if (payload instanceof DialogResponseMessage.IntList(int[] wiring)
              && cables.connect(wiring, player)) {
            // The other players' dialogs of the entity still show loose cables.
            UIUtils.closeDialogs(
                DialogType.DefaultTypes.CABLES,
                other ->
                    other != ui.dialogContext()
                        && other
                            .find(DialogContextKeys.ENTITY, Integer.class)
                            .filter(id -> id == entity.id())
                            .isPresent());
          }
        });
    ui.registerCallback(
        WRONG_PLUG,
        payload -> {
          if (payload instanceof DialogResponseMessage.IntList(int[] plug)) {
            cables.plugWrong(plug, player);
          }
        });
    return ui;
  }
}
