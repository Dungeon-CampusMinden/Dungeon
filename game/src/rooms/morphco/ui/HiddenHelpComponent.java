package rooms.morphco.ui;

import engine.Component;

/**
 * Marks the hero of a player who plays without puzzle explanations; the puzzle dialogs then leave
 * out the help line below their task.
 *
 * <p>The server adds it from the player's choice at the start of the shift, the snapshots carry it
 * to the clients.
 */
public record HiddenHelpComponent() implements Component {}
