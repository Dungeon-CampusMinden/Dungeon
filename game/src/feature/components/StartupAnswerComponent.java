package feature.components;

import engine.Component;

/**
 * Server-side answer of a player to the game's startup question in the main menu.
 *
 * <p>The server adds it to the hero when spawning it for a client that sent an answer; heroes of
 * clients without an answer have no such component.
 *
 * @param answer identifier of the selected answer
 */
public record StartupAnswerComponent(String answer) implements Component {}
