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
import java.util.Optional;

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

    Optional<InventoryComponent> collectorInvComp = collector.fetch(InventoryComponent.class);

    pc.registerCallback(
        KeyboardConfig.SECOND_SKILL.value(),
        collectorEntity ->
            collectorInvComp
                .flatMap(
                    invComp ->
                        invComp
                            .itemOfClass(ItemWoodenArrow.class)
                            .filter(item -> BOW_SKILL.canBeUsedAgain() && invComp.removeOne(item)))
                .ifPresent(item -> BOW_SKILL.execute(collectorEntity)));

    return collectorInvComp
        .map(
            inv -> {
              if (inv.add(this)) {
                Game.remove(itemEntity);
                return true;
              }
              return false;
            })
        .orElse(false);
  }

  @Override
  public boolean drop(final Point position) {
    Game.hero()
        .flatMap(hero -> hero.fetch(PlayerComponent.class))
        .ifPresent(pc -> pc.removeCallback(KeyboardConfig.SECOND_SKILL.value()));

    if (Game.tileAT(position) instanceof FloorTile) {
      Game.add(WorldItemBuilder.buildWorldItem(this, position));
      return true;
    }
    return false;
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
