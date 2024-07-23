package de.fwatermann.dungine.ecs.systems;

import de.fwatermann.dungine.ecs.System;

public class RenderableSystem extends System<RenderableSystem> {

  public RenderableSystem() {
    super(1); //Execute every frame.
  }

  @Override
  public void update() {
  }
}
