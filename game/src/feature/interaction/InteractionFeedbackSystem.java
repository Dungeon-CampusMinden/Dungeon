package feature.interaction;

import engine.Game;
import engine.System;

/** Updates client-side cursor and outline feedback for interactable world entities. */
public final class InteractionFeedbackSystem extends System {

  /** Creates a new InteractionFeedbackSystem. */
  public InteractionFeedbackSystem() {
    super(AuthoritativeSide.CLIENT);
  }

  @Override
  public void execute() {
    if (Game.isHeadless()) {
      return;
    }

    InteractionFeedback.update();
  }
}
