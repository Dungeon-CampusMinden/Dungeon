package systems;

import components.HeroActionComponent;
import core.Game;
import core.System;

/**
 * Executes the currently attached {@link HeroActionComponent} on the hero once per frame.
 *
 * <p>This moves hero-action ticking out of {@code BlocklyLevel.onTick()} into the system pipeline,
 * so action updates have an explicit place in system ordering.
 */
public class HeroActionTickSystem extends System {

  @Override
  public void execute() {
    Game.player()
        .flatMap(entity -> entity.fetch(HeroActionComponent.class))
        .ifPresent(HeroActionComponent::tick);
  }
}
