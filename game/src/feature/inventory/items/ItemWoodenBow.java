package feature.inventory.items;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.input.CursorUtils;
import engine.level.elements.tile.FloorTile;
import engine.utils.Point;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import feature.components.InventoryComponent;
import feature.components.SkillComponent;
import feature.entities.WorldItemBuilder;
import feature.inventory.Item;
import feature.skills.projectile.BowSkill;
import java.util.Optional;

/**
 * This item is a bow. It can be used to shoot arrows, if any are stored in the inventory.
 *
 * <p>Registers and removes the callback for the second_skill.
 */
public class ItemWoodenBow extends Item {
  /** The default texture for all wooden bows. */
  public static final IPath DEFAULT_TEXTURE = new SimpleIPath("items/weapon/wooden_bow.png");

  /** Create a {@link Item} that looks like a bow and can be collected to unlock the BOW_SKILL. */
  public ItemWoodenBow() {
    super("Wooden Bow", "It needs arrows as ammunition", new Animation(DEFAULT_TEXTURE));
  }

  @Override
  public boolean collect(final Entity itemEntity, final Entity collector) {
    return collector
        .fetch(InventoryComponent.class)
        .map(
            inventoryComponent -> {
              if (inventoryComponent.add(this)) {
                collector
                    .fetch(SkillComponent.class)
                    .ifPresent(sc -> sc.addSkill(new BowSkill(CursorUtils::positionInWorld)));

                Game.remove(itemEntity);
                return true;
              }
              return false;
            })
        .orElse(false);
  }

  @Override
  public Optional<Entity> drop(final Point position) {
    Game.player()
        .flatMap(player -> player.fetch(SkillComponent.class))
        .ifPresent(sc -> sc.removeSkill(BowSkill.class));

    return Game.tileAt(position)
        .filter(FloorTile.class::isInstance)
        .map(
            tile -> {
              Entity bow = WorldItemBuilder.buildWorldItemSimpleInteraction(this, position);
              Game.add(bow);
              return Optional.of(bow);
            })
        .orElse(Optional.empty());
  }

  @Override
  public void use(final Entity user) {
    user.fetch(PositionComponent.class)
        .map(PositionComponent::position)
        .ifPresent(
            pos -> {
              drop(pos);
              user.fetch(InventoryComponent.class).ifPresent(inv -> inv.remove(this));
            });
  }
}
