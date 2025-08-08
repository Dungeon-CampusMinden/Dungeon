package contrib.item.concreteItem;

import contrib.components.InventoryComponent;
import contrib.configuration.KeyboardConfig;
import contrib.entities.WorldItemBuilder;
import contrib.item.Item;
import contrib.utils.components.skill.BowSkill;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.elements.tile.FloorTile;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * This item is a bow. It can be used to shoot arrows, if any are stored in the inventory.
 *
 * <p>Registers and removes the callback for the second_skill.
 */
public class ItemWoodenBow extends Item {
  /** The default texture for all health potions. */
  public static final IPath DEFAULT_TEXTURE = new SimpleIPath("items/weapon/wooden_bow.png");

  private static final int BOW_COOLDOWN = 500;

  private static Skill BOW_SKILL =
      new Skill(new BowSkill(SkillTools::cursorPositionAsPoint), BOW_COOLDOWN);

  /** Create a {@link Item} that looks like a bow and can be collected to unlock the BOW_SKILL. */
  public ItemWoodenBow() {
    super(
        "Wooden Bow", "It needs arrows as ammunition", Animation.fromSingleImage(DEFAULT_TEXTURE));
  }

  @Override
  public boolean collect(final Entity itemEntity, final Entity collector) {
    PlayerComponent pc =
        collector
            .fetch(PlayerComponent.class)
            .orElseThrow(() -> MissingComponentException.build(collector, PlayerComponent.class));
    // add the skill to the collector callbacks
    pc.registerCallback(
        KeyboardConfig.SECOND_SKILL.value(),
        collectorEntity ->
            collector
                .fetch(InventoryComponent.class)
                .ifPresent(
                    invComp -> {
                      if (BOW_SKILL
                          .canBeUsedAgain()) { // important to prevent spending more than one ammo
                        if (invComp.hasItem(ItemWoodenArrow.class)
                            && invComp.removeOne(
                                invComp.getSmallestStackOfItemClass(ItemWoodenArrow.class))) {
                          BOW_SKILL.execute(collectorEntity);
                        }
                      }
                    }));

    return collector
        .fetch(InventoryComponent.class)
        .map(
            inventoryComponent -> {
              if (inventoryComponent.add(this)) {
                Game.remove(itemEntity);
                return true;
              }
              return false;
            })
        .orElse(false);
  }

  @Override
  public boolean drop(final Point position) {
    Entity hero = Game.hero().orElseThrow();
    PlayerComponent pc =
        hero.fetch(PlayerComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PlayerComponent.class));
    pc.removeCallback(KeyboardConfig.SECOND_SKILL.value());
    if (Game.tileAT(position) instanceof FloorTile) {
      Game.add(WorldItemBuilder.buildWorldItem(this, position));
      return true;
    }
    return false;
  }

  @Override
  public void use(final Entity user) {
    Entity hero = Game.hero().orElseThrow();
    PositionComponent posc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    // clicking on the bow in the inventory drops it at the current position
    drop(posc.position());
    user.fetch(InventoryComponent.class).ifPresent(component -> component.remove(this));
  }
}
