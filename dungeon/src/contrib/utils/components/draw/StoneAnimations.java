package contrib.utils.components.draw;

import core.utils.components.draw.CoreAnimationPriorities;
import core.utils.components.path.IPath;

/** Animationen für Steine (intakt, kaputt, zerbrechen). */
public enum StoneAnimations implements IPath {
  /** Der Stein steht normal da. */
  INTACT("intact", CoreAnimationPriorities.IDLE.priority()),
  /** Stein zerbricht. */
  BREAKING("breaking", CoreAnimationPriorities.IDLE.priority() + 200),
  /** Stein ist kaputt und bleibt so liegen. */
  BROKEN("broken", CoreAnimationPriorities.IDLE.priority() + 100);

  private final String value;
  private final int priority;

  StoneAnimations(final String value, int priority) {
    this.value = value;
    this.priority = priority;
  }

  @Override
  public String pathString() {
    return value;
  }

  @Override
  public String toString() {
    return "StoneAnimation[" + this.value + "]";
  }

  @Override
  public int priority() {
    return priority;
  }
}

