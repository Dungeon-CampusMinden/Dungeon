package de.fwatermann.dungine.ecs.systems;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.ecs.components.AudioSourceComponent;
import de.fwatermann.dungine.physics.ecs.RigidBodyComponent;

/** System responsible for updating audio sources in the ECS. */
public class AudioSourceSystem extends System<AudioSourceSystem> {

  /** Constructs a new AudioSourceSystem. */
  public AudioSourceSystem() {
    super(0, false, AudioSourceComponent.class);
  }

  /**
   * Updates the audio sources in the ECS.
   *
   * @param ecs the ECS instance
   */
  @Override
  public void update(ECS ecs) {
    ecs.forEachEntity(
        e -> {
          e.components(AudioSourceComponent.class)
              .forEach(
                  c -> {
                    c.source().position(e.position());
                    e.component(RigidBodyComponent.class)
                        .ifPresent(
                            r -> {
                              c.source().velocity(r.velocity());
                            });
                  });
        },
        AudioSourceComponent.class);
  }

  @Override
  public void onEntityRemove(ECS ecs, Entity entity) {
    entity.components(AudioSourceComponent.class).forEach(AudioSourceComponent::dispose);
  }
}
