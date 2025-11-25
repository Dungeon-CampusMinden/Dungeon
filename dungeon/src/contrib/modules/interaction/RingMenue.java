package contrib.modules.interaction;

import java.util.Optional;

public class RingMenue {

  // TODO
  public static Optional<Interaction> showInteractionMenue(IInteractable i) {
    return Optional.of(i.interact());
  }
}
