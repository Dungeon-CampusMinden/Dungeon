package contrib.item.concreteItem;

import contrib.components.InventoryComponent;
import contrib.components.SkillComponent;
import contrib.entities.WorldItemBuilder;
import contrib.item.Item;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.projectileSkill.BowSkill;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.elements.tile.FloorTile;
import core.utils.Point;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
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
                    .ifPresent(sc -> sc.addSkill(new BowSkill(SkillTools::cursorPositionAsPoint)));

                Game.remove(itemEntity);
                return true;
              }
              return false;
            })
        .orElse(false);
  }

  @Override
  public Optional<Entity> drop(final Point position) {
    Game.hero()
        .flatMap(hero -> hero.fetch(SkillComponent.class))
        .ifPresent(sc -> sc.removeSkill(BowSkill.class));

    return Game.tileAt(position)
        .filter(FloorTile.class::isInstance)
        .map(
            tile -> {
              Entity bow = WorldItemBuilder.buildWorldItem(this, position);
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
