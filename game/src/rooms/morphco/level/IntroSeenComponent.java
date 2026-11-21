package rooms.morphco.level;

import engine.Component;

/**
 * Marks the hero of a player who has seen the intro or continues a saved game; from then on the
 * player's client plays the background music.
 *
 * <p>The server adds it, the snapshots carry it to the clients.
 */
public record IntroSeenComponent() implements Component {}
