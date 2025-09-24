package contrib.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import contrib.components.BombElementComponent;
import core.Entity;
import core.Game;
import core.System;

public final class BombElementSwitchSystem extends System {

  public BombElementSwitchSystem() {
    super();
  }

  @Override
  public void execute() {
    if (!Gdx.input.isKeyJustPressed(Input.Keys.B)) return;

    Game.hero()
        .ifPresent(
            hero -> {
              BombElementComponent comp = getOrCreateBombElementComponent(hero);
              comp.element(comp.element().next());
            });
  }

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
