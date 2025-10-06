package contrib.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import contrib.components.BombElementComponent;
import core.Entity;
import core.Game;
import core.System;

/**
 * System that rotates the hero's bomb element when the player presses the B key.
 *
 * @see BombElementComponent
 */
public final class BombElementSwitchSystem extends System {

  private static final int SWITCH_ELEMENT_KEY = Input.Keys.B;

  /** Constructs the bomb element switch system. */
  public BombElementSwitchSystem() {
    super();
  }

  /** Polls for the B key and advances the hero's bomb element to the next value if pressed. */
  @Override
  public void execute() {
    if (!Gdx.input.isKeyJustPressed(SWITCH_ELEMENT_KEY)) return;

    Game.hero()
        .ifPresent(
            hero -> {
              BombElementComponent comp = getOrCreateBombElementComponent(hero);
              comp.element(comp.element().next());
            });
  }

  /**
   * Retrieves the hero's {@link BombElementComponent}, or creates and attaches one if missing.
   *
   * @param hero The hero entity.
   * @return A {@link BombElementComponent} attached to the hero.
   */
  private BombElementComponent getOrCreateBombElementComponent(Entity hero) {
    return hero.fetch(BombElementComponent.class)
        .orElseGet(
            () -> {
              BombElementComponent created = new BombElementComponent();
              hero.add(created);
              return created;
            });
  }
}
