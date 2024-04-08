package systems;

import components.MagicShieldComponent;
import core.Entity;
import core.System;
import core.components.DrawComponent;

/**
 * MagicShieldSystem is a class that extends the System class. It is responsible for managing the
 * magic shield of entities.
 *
 * <p>A magic shield is a shield that can absorb a certain amount of damage before it gets depleted.
 * Once the shield is depleted, it needs to recharge before it can be used again. The
 * MagicShieldSystem checks if the shield is depleted and recharges it if it can.
 *
 * @see MagicShieldComponent
 */
public class MagicShieldSystem extends System {

  private static final int DEFAULT_SHIELD_ENTITY_TINT = 0x9999FFFF;

  /**
   * Constructor for the MagicShieldSystem class. It initializes the system with
   * MagicShieldComponent and DrawComponent.
   *
   * @see MagicShieldComponent
   */
  public MagicShieldSystem() {
    super(MagicShieldComponent.class, DrawComponent.class);
  }

  /** Overridden execute method from the System class. It processes each entity in the system. */
  @Override
  public void execute() {
    this.entityStream().forEach(this::processEntity);
  }

  /**
   * Method to process an entity. It fetches the MagicShieldComponent of the entity and checks if
   * it's depleted. If it is, it processes the shield.
   *
   * @param entity The entity to be processed.
   */
  private void processEntity(Entity entity) {
    entity
        .fetch(MagicShieldComponent.class)
        .ifPresent(shield -> this.processShield(entity, shield));
  }

  /**
   * Method to process the shield of an entity. It checks if the shield is depleted and recharges it
   * if it can.
   *
   * @param entity The entity whose shield is being processed.
   * @param shield The MagicShieldComponent of the entity.
   */
  private void processShield(Entity entity, MagicShieldComponent shield) {
    if (!shield.isDepleted()) {
      this.setTintColor(entity, DEFAULT_SHIELD_ENTITY_TINT);
      return;
    }
    if (shield.canBeRecharged()) {
      shield.recharge();
    } else {
      this.setTintColor(entity, -1);
    }
  }

  /**
   * Method to set the tint color of an entity. It fetches the DrawComponent of the entity and sets
   * its tint color.
   *
   * @param entity The entity whose tint color is being set.
   * @param color The color to set the tint to.
   */
  private void setTintColor(Entity entity, int color) {
    entity.fetch(DrawComponent.class).ifPresent(drawComponent -> drawComponent.tintColor(color));
  }
}
