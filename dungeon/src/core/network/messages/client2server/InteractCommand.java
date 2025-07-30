package core.network.messages.client2server;

import contrib.components.InteractionComponent;
import core.Entity;
import core.Game;
import core.utils.components.MissingComponentException;

/** Record representing a command to interact with the world. */
public record InteractCommand(Entity interactable) implements ClientMessage {
  @Override
  public void process() {
    Game.hero()
        .ifPresent(
            hero -> {
              InteractionComponent ic =
                  interactable
                      .fetch(InteractionComponent.class)
                      .orElseThrow(
                          () ->
                              MissingComponentException.build(
                                  interactable, InteractionComponent.class));
              ic.triggerInteraction(interactable, hero);
            });
  }
}
