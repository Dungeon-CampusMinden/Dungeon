package level;

import com.badlogic.gdx.utils.Align;
import components.SignComponent;
import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemPotionWater;
import contrib.item.concreteItem.ItemResourceMushroomRed;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.MissingHeroException;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import entities.EntityUtils;
import entities.MonsterType;
import entities.SignFactory;
import java.util.List;
import level.utils.ITickable;

/** The tutorial level */
public class DevLevel00 extends DevDungeonLevel implements ITickable {

  // Entity spawn points
  private final Coordinate mobSpawn;
  private final Point chestSpawn;
  private final Point cauldronSpawn;
  private Coordinate lastHeroCoords = new Coordinate(0, 0);

  public DevLevel00(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);
    this.mobSpawn = customPoints.get(0);

    // Static Entity should spawn in the center of the tile (so I have to offset by 0.5)
    this.chestSpawn =
        new Point(customPoints.get(1).toPoint().x + 0.5f, customPoints.get(1).toPoint().y + 0.5f);
    this.cauldronSpawn =
        new Point(customPoints.get(2).toPoint().x + 0.5f, customPoints.get(2).toPoint().y + 0.5f);
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      this.handleFirstTick();
      this.doorTiles().forEach(DoorTile::close);
      this.buildBridge();
    }
    if (lastHeroCoords != null && !lastHeroCoords.equals(EntityUtils.getHeroCoords())) {
      // Only handle text popups if the hero has moved
      this.handleTextPopups();
    }
    this.handleDoors();
    this.lastHeroCoords = EntityUtils.getHeroCoords();
  }

  /**
   * Handles the spawning of the tutorial entities e.g. monsters It also fills the chest with the
   * necessary items for the tutorial
   *
   * @throws RuntimeException if any of the entities could not be created
   */
  private void handleFirstTick() {
    Entity mob = EntityUtils.spawnMonster(MonsterType.TUTORIAL, this.mobSpawn);
    if (mob == null) {
      throw new RuntimeException("Failed to create tutorial monster");
    }
    DoorTile mobDoor = (DoorTile) tileAt(this.customPoints().get(5));
    mob.fetch(HealthComponent.class).ifPresent(hc -> hc.onDeath((e) -> mobDoor.open()));
    Entity chest;
    Entity chest2;
    try {
      chest = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
      chest2 = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create tutorial chest");
    }
    this.setupChest(chest, chest2);
    Entity cauldron;
    try {
      cauldron = MiscFactory.newCraftingCauldron();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create tutorial cauldron");
    }
    this.setupCauldron(cauldron);

    showTextPopup("Verwende WASD (oder RMB), um dich zu bewegen.", "Willkommen im DevDungeon!");
  }

  private void showTextPopup(String text, String title) {
    SignFactory.showTextPopup(
        text, title, SignComponent.DEFAULT_WIDTH, SignComponent.DEFAULT_HEIGHT, Align.top);
  }

  private void handleTextPopups() {
    DoorTile frontDoor = (DoorTile) tileAt(this.customPoints().get(4));
    DoorTile mobDoor = (DoorTile) tileAt(this.customPoints().get(5));
    DoorTile CraftingDoor = (DoorTile) tileAt(this.customPoints().get(6));
    if (EntityUtils.getHeroCoords() == null) return;
    Tile heroTile = tileAt(EntityUtils.getHeroCoords());
    if (heroTile == null) return;

    if (frontDoor.coordinate().equals(heroTile.coordinate())) {
      showTextPopup("Mit Q (oder LMB) kannst du angreifen.", "Kampf");
    } else if (mobDoor.coordinate().equals(heroTile.coordinate())) {
      showTextPopup(
          "Kommen wir zum Craften. Du findest im Verlauf des Spiels \nverschiedene Ressourcen,"
              + " die du in \nTränke und andere nützliche Gegenstände\n verwandeln kannst. "
              + "Du kannst die Truhe und \nden Kessel mit E (oder LMB) öffnen. ",
          "Looting & Crafting");
    } else if (CraftingDoor.coordinate().equals(heroTile.coordinate())) {
      showTextPopup(
          "Im Dungeon findest immerwieder Hinternisse, Fallen und Rätsel.\n"
              + "Versuche sie zu umgehen oder zu lösen.",
          "Rätsel");
    }
  }

  /**
   * Handles the opening of the doors. Except for the mob door, this is handle in the onDeath of the
   * mob
   */
  private void handleDoors() {
    DoorTile frontDoor = (DoorTile) tileAt(this.customPoints().get(4));
    DoorTile CraftingDoor = (DoorTile) tileAt(this.customPoints().get(6));
    Point heroPos;
    try {
      heroPos = SkillTools.heroPositionAsPoint();
    } catch (MissingHeroException e) {
      return;
    }
    if (!frontDoor.isOpen() && frontDoor.position().distance(heroPos) < 2) {
      frontDoor.open();
    }

    Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
    InventoryComponent ic =
        hero.fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, InventoryComponent.class));
    if (!CraftingDoor.isOpen() && ic.hasItem(ItemPotionHealth.class)) {
      CraftingDoor.open();
    }
  }

  /**
   * Sets up the chest with the necessary items for the tutorial
   *
   * @param chest The chest to set up
   */
  private void setupChest(Entity chest, Entity b) {
    PositionComponent pc =
        chest
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, PositionComponent.class));
    InventoryComponent ic =
        chest
            .fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, InventoryComponent.class));

    pc.position(this.chestSpawn);
    ic.add(
        new ItemPotionWater() {
          @Override
          public void use(final Entity e) {} // Disable usage of the potion to prevent soft locking
        });
    ic.add(
        new ItemResourceMushroomRed() {
          @Override
          public void use(final Entity e) {} // Disable usage of the potion to prevent soft locking
        });

    Game.add(chest);

    pc =
        b.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(b, PositionComponent.class));
    ic =
        b.fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(b, InventoryComponent.class));
    pc.position(
        new Point(this.customPoints().get(3).x + 0.5f, this.customPoints().get(3).y + 0.5f));
    ic.add(new ItemPotionHealth(HealthPotionType.NORMAL));
    Game.add(b);
  }

  /**
   * Sets up the crafting cauldron
   *
   * @param cauldron The cauldron to set up
   */
  private void setupCauldron(Entity cauldron) {
    PositionComponent pc =
        cauldron
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(cauldron, PositionComponent.class));
    pc.position(this.cauldronSpawn);
    Game.add(cauldron);
  }

  /** Builds a bridge to a special chest */
  private void buildBridge() {
    // The bridge should extend from x = 39 to x = 52 and y = 11
    // TODO: Implement bridge
  }
}
