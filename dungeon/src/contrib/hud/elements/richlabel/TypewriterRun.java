package contrib.hud.elements.richlabel;

/**
 * A control run that changes the typewriter speed for subsequent runs. A speed of 0 disables
 * typewriter mode (text appears instantly). A speed greater than 0 enables character-by-character
 * reveal at that many characters per second.
 *
 * @param speed characters per second, or 0 to disable typewriter mode
 */
public record TypewriterRun(float speed) implements Run {}
