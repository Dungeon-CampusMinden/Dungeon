package core.network.messages.c2s;

import core.network.messages.NetworkMessage;
import java.io.Serial;

/**
 * Client→server: Open/close inventory UI.
 *
 * <p>This message is sent from the client to the server to indicate whether the player has opened
 * or closed the inventory user interface (UI). The server can use this information to manage game
 * state, such as pausing certain actions while the inventory is open or updating the player's
 * inventory data.
 *
 * @param open true if the inventory UI is opened, false if closed
 */
public record InventoryUIMessage(boolean open) implements NetworkMessage {
  @Serial private static final long serialVersionUID = 1L;
}
